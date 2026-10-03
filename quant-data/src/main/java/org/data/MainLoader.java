package org.data;

import org.data.csv.CsvInstrumentDataBase;
import org.data.csv.DataSaver;
import org.data.yahoofinance.YahooFinanceLoader;
import org.fin.definitions.assets.Country;
import org.fin.definitions.assets.Instrument;
import org.fin.definitions.assets.InstrumentService;
import org.fin.definitions.history.DataLoader;
import org.fin.definitions.history.FullDataContext;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.ImputationStrategy;
import org.series.imputation.StubImputationStrategy;
import org.series.timeserie.TimeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

public class MainLoader {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainLoader.class);

    public static void main(String[] args) {


        LOGGER.trace("========== TRACE TEST ==========");
        LOGGER.debug("========== DEBUG TEST ==========");
        LOGGER.info("========== INFO TEST ==========");

        Country country = Country.DE;
        String dirCsv = "C:\\Users\\baran\\Documents\\csv_files";
        List<TimeFrame> timeFrames = List.of(TimeFrame.D);
        List<Instrument> instruments = InstrumentService.findByCountry(country);


        long start = TimeTools.fromDayStringToLong("2020-01-02", ZoneIdEnum.EUROPE_PARIS);
        long end = TimeTools.fromDayStringToLong("2026-07-10", ZoneIdEnum.EUROPE_PARIS);
        DataLoader csvLoader = new CsvInstrumentDataBase(new File(dirCsv).toPath());
        DataLoader yahooFinanceLoader = new YahooFinanceLoader(csvLoader, (DataSaver) csvLoader);
        ImputationStrategy imputationStrategy = new StubImputationStrategy();
        FullDataContext context = new FullDataContext(start, end, imputationStrategy, List.of(csvLoader, yahooFinanceLoader), instruments, timeFrames);
    }
}
