package org.data.csv;

import lombok.Getter;
import lombok.Setter;
import org.common.CsvService;
import org.common.TriConsumer;
import org.data.SavingException;
import org.data.definitions.LoadingException;
import org.data.definitions.TickEnum;
import org.data.definitions.TickService;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.data.definitions.history.AlignerService;
import org.data.definitions.history.DataLoader;

import org.math.common.MathUtil;
import org.series.TimeTools;
import org.series.timeserie.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CsvInstrumentDataBase implements DataLoader, DataSaver {

    Logger logger = LoggerFactory.getLogger(CsvInstrumentDataBase.class);
    @Getter
    private final Path rootPath;

    @Setter
    private double thresholdLoadingError = 0.95;

    private final CandleMapper candleMapper = new CandleMapper();
    private final CsvService<Candle> csvService = new CsvService<>(candleMapper);


    public CsvInstrumentDataBase(Path path) {
        Objects.requireNonNull(path, "csv loader path can not be null");
        this.rootPath = path;
        logger.debug("{} set to path {}", getLabel(), rootPath.toFile().getAbsoluteFile());
    }

    public List<Candle> loadAll(Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        List<Candle> candles = new ArrayList<>();
        try {
            Path csvPath = getCsvPath(instrument, timeFrame);
            candles.addAll(csvService.readFromFile(csvPath));
            Collections.sort(candles);
            return candles;
        } catch (IOException e) {
            throw new LoadingException("Failed to open the csv file: " + instrument.getLabel() +" in " + rootPath.getParent().toAbsolutePath());
        }
    }
    @Override
    public List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        return this.load(start, end, instrument, timeFrame, null); // le prédicat != null = testes
    }

    public List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame, Predicate<Long> validDates) throws LoadingException {
        logger.debug(logLoading(start, end, instrument, timeFrame));
        List<Candle> candles = loadAll(instrument, timeFrame);
        // pour les timeframes mois/week end comme on charge les jours on aura necessairement plus de données que nécessaire
        if (timeFrame.equals(TimeFrame.WK) || timeFrame.equals(TimeFrame.MO)){
            Predicate<Long> validDatesPredicate = AlignerService.getValidDatePredicateBasedOnAligner(instrument, timeFrame);
            candles = candles.stream().filter(candle -> validDatesPredicate.test(candle.timestamp())).toList();
        }
        if (candles.isEmpty()){
            throw new LoadingException(String.format("No data available for %s, %s.",
                    instrument.getLabel(),
                    timeFrame.getLabel()));
        }

        int targetSize;
        Predicate<Long> predicateValidDate = validDates == null ? AlignerService.getValidDatePredicateBasedOnAligner(instrument, timeFrame) : validDates;
        if (TickService.getTick(timeFrame).equals(TickEnum.DAY)){
            targetSize = TimeTools.getNumberValuesStartingFromEndBetween(start, end, TimeFrame.D, predicateValidDate,instrument.getZoneIdEnum().getZoneId());
        } else {
            targetSize = TimeTools.getNumberValuesStartingFromEndBetween(start, end, timeFrame, predicateValidDate,instrument.getZoneIdEnum().getZoneId());
        }
        double availableDataRatio = MathUtil.round((double) candles.size() / targetSize, 1);
        if (availableDataRatio < thresholdLoadingError || availableDataRatio > 1){
            throw new LoadingException(
                    String.format(
                            "Only %s%% of data available for %s.",
                            availableDataRatio * 100,
                            instrument.getLabel()
                    )
            );
        }

        candles = candles.stream().filter(c -> c.timestamp() >= start && c.timestamp() <= end).toList();
        long lastCandleTimeStamp = candles.get(candles.size()-1).timestamp();
        if (end != lastCandleTimeStamp){
            ZoneId zoneId = instrument.getZoneIdEnum().getZoneId();
            throw new LoadingException(String.format("Date requested: %s, date received: %s",
                    TimeTools.fromLongToZonedDateTime(end, zoneId),
                    TimeTools.fromLongToZonedDateTime(lastCandleTimeStamp, zoneId)));
        }
        onSuccessLoading(instrument, timeFrame, candles);
        return candles;
    }

    @Override
    public String getLabel() {
        return "CSV database";
    }

    @Override
    public void onSuccessLoading(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) {
        logger.trace("{} ({}): {} [LOADING SUCCEED]", instrument.getLabel(), timeFrame.getLabel(), getLabel());
    }

    @Override
    public void save(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) throws SavingException {
        try {
        List<Candle> existingSavedCandles = loadAll(instrument, timeFrame);
        List<Candle> allCandles = Stream.concat(existingSavedCandles.stream(), candles.stream())
                .filter(distinctBy(Candle::timestamp))
                .sorted()
                .toList();

            csvService.writeToFile(getCsvPath(instrument, timeFrame), allCandles);
        } catch (IOException | LoadingException e) {
            throw new SavingException(e.toString());
        }
    }

    private static <T> Predicate<T> distinctBy(Function<T, ?> keyExtractor){
        Set<Object> seen = ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }

    private String getCsvFileName(Instrument instrument, String complement){
        return instrument.getLabel() + complement + ".csv";
    }

    private Path getCsvPath(Instrument instrument, TimeFrame timeFrame) throws IOException {
        Files.createDirectories(rootPath);

        String endStr = TickService.getTick(timeFrame) == TickEnum.DAY ? TimeFrame.D.getLabel() : timeFrame.getLabel();
        File csvFile = new File(rootPath.toFile(), getCsvFileName(instrument, endStr));
        csvFile.createNewFile();
        return csvFile.toPath();
    }
}
