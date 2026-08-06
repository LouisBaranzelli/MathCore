package org.data.csv;

import lombok.Getter;
import org.common.CsvService;
import org.common.TriConsumer;
import org.data.definitions.LoadingException;
import org.data.definitions.TickEnum;
import org.data.definitions.TickService;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.data.definitions.history.DataLoader;

import org.series.timeserie.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class CsvInstrumentDataBase implements DataLoader, DataSaver {

    @Getter
    private final Path rootPath;

    @Getter
    private final TriConsumer<Instrument, TimeFrame, List<Candle>> triConsumerOnSuccessLoading;


    private final CandleMapper candleMapper = new CandleMapper();
    private final CsvService<Candle> csvService = new CsvService<>(candleMapper);

    public CsvInstrumentDataBase(Path path) {
        this(path, null);
    }

    public CsvInstrumentDataBase(Path path, TriConsumer<Instrument, TimeFrame, List<Candle>> triConsumerOnSuccessLoading) {
        Objects.requireNonNull(path, "csv loader path can not be null");
        this.rootPath = path;
        this.triConsumerOnSuccessLoading = triConsumerOnSuccessLoading;
    }

    public List<Candle> loadAll(Instrument instrument, TickEnum tickEnum) throws LoadingException {
        List<Candle> candles = new ArrayList<>();
        try {
            Path csvPath = getCsvPath(instrument, tickEnum);
            candles.addAll(csvService.readFromFile(csvPath));
            return candles;
        } catch (IOException e) {
            throw new LoadingException("Failed to open the csv file: " + instrument.getLabel() +" in " + rootPath.getParent().toAbsolutePath());
        }
    }

    @Override
    public List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        List<Candle> candles = loadAll(instrument, TickService.getTick(timeFrame));
        onSuccessLoading(instrument, timeFrame, candles);
        return candles.stream().filter(c -> c.timestamp() >= start && c.timestamp() <= end).toList();
    }

    @Override
    public String getLabel() {
        return "CSV database";
    }

    @Override
    public void save(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) throws LoadingException {
        TickEnum tickEnum = TickService.getTick(timeFrame);
        List<Candle> existingSavedCandles = loadAll(instrument, tickEnum);
        List<Candle> allCandles = Stream.concat(existingSavedCandles.stream(), candles.stream())
                .distinct()
                .sorted()
                .toList();
        try {
            csvService.writeToFile(getCsvPath(instrument, tickEnum), allCandles);
        } catch (IOException e) {
            throw new LoadingException(e.toString());
        }
    }

    private String getCsvFileName(Instrument instrument, String complement){
        return instrument.getLabel() + complement + ".csv";
    }

    private Path getCsvPath(Instrument instrument, TickEnum tickEnum) throws IOException {
        Files.createDirectories(rootPath);
        String endStr = tickEnum == TickEnum.DAY ? "_tick_day" : "_tick_second";
        File csvFile = new File(rootPath.toFile(), getCsvFileName(instrument, endStr));
        csvFile.createNewFile();
        return csvFile.toPath();
    }
}
