package org.data;

import org.data.csv.CsvInstrumentDataBase;
import org.data.csv.DataSaver;
import org.data.definitions.assets.Country;
import org.data.definitions.assets.Instrument;
import org.data.definitions.assets.InstrumentFactory;
import org.data.definitions.assets.InstrumentService;
import org.data.definitions.history.DataLoader;
import org.data.definitions.history.FullDataContext;
import org.data.yahoofinance.YahooFinanceLoader;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.ImputationStrategy;
import org.series.imputation.StubImputationStrategy;
import org.series.timeserie.TimeFrame;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

public class MainLoader {

    public static void main(String[] args) {
        Country country = Country.FR;
        String dirCsv = "C:\\Users\\baran\\Documents\\csv_files";
        List<TimeFrame> timeFrames = List.of(TimeFrame.WK);
        List<Instrument> instruments = InstrumentService.findByCountry(Country.FR);


        long start = TimeTools.fromDayStringToLong("2020-01-02", ZoneIdEnum.EUROPE_PARIS);
        long end = TimeTools.fromDayStringToLong("2026-07-10", ZoneIdEnum.EUROPE_PARIS);
        DataLoader csvLoader = new CsvInstrumentDataBase(new File(dirCsv).toPath());
//        DataLoader yahooFinanceLoader = new YahooFinanceLoader(csvLoader, (DataSaver) csvLoader);
        ImputationStrategy imputationStrategy = new StubImputationStrategy();
        FullDataContext context = new FullDataContext(start, end, imputationStrategy, List.of(csvLoader), instruments, timeFrames);
    }
}
