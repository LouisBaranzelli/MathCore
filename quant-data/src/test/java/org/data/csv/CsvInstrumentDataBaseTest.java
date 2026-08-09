package org.data.csv;

import org.common.DirectoryUtils;
import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Stock;
import org.data.definitions.candles.Candle;
import org.data.definitions.candles.CompositeCandleTimeSerie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.series.TimeTools;
import org.series.ZoneIdEnum;

import org.series.timeserie.TimeFrame;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvInstrumentDataBaseTest {

    private CompositeCandleTimeSerie sourceSerie;
    private final long[] timestamps = {1000L, 2000L, 3000L, 4000L, 5000L};
    Path pathSource1 = new File("source1_test").toPath();
    Path pathSource2 = new File("source2_test").toPath();


    @Test
    @DisplayName("Sauvegarde une serie en csv")
    void shouldSaveData() throws LoadingException, IOException {
        if (pathSource1.toFile().exists()){
          DirectoryUtils.deleteDirectory(pathSource1);
        }
        CsvInstrumentDataBase csvInstrumentDataBase = new CsvInstrumentDataBase(pathSource1);
        List<Candle> candles = new ArrayList<>();
        assertThrows(LoadingException.class,() -> csvInstrumentDataBase.load(0, 4000, Stock.SU, TimeFrame.D));
        Candle candle1 = Candle.randomCandle(1000L);
        Candle candle2 = Candle.randomCandle(2000L);
        candles.add(candle1);
        candles.add(candle2);
        candles.add(Candle.randomCandle(3000L));
        candles.add(Candle.randomCandle(4000L));
        candles.add(Candle.randomCandle(5000L));
        csvInstrumentDataBase.save(Stock.SU, TimeFrame.D, candles);
        candles.clear();
        candles = new ArrayList<>(csvInstrumentDataBase.load(0, 4000L, Stock.SU, TimeFrame.D));
        assertThrows(LoadingException.class,() -> csvInstrumentDataBase.load(0, 4001L, Stock.SU, TimeFrame.D));
        assertEquals(4, candles.size());
        assertEquals(candle1, candles.get(0));
        candles.add(Candle.randomCandle(6000L));
        csvInstrumentDataBase.save(Stock.SU, TimeFrame.D, candles);
        candles.clear();
        candles.addAll(csvInstrumentDataBase.load(0, 6000, Stock.SU, TimeFrame.D));
        assertEquals(6, candles.size());

        // sur un tick minute: sauvegarde sur un autre fichier
        assertThrows(LoadingException.class,() -> csvInstrumentDataBase.load(0,4000L , Stock.SU, TimeFrame.MI5));
        candles.clear();
        candles.add(Candle.randomCandle(4000L));
        candles.add(candle2);
        csvInstrumentDataBase.save(Stock.SU, TimeFrame.MI5, candles);
        candles.clear();
        candles.addAll(csvInstrumentDataBase.load(0,4000L , Stock.SU, TimeFrame.MI5));
        assertEquals(candles.get(0), candle2);

        // Wrong company
        assertThrows(LoadingException.class,() -> csvInstrumentDataBase.load(0, 4000L, Stock.TTE, TimeFrame.D));

        if (pathSource1.toFile().exists()){
            DirectoryUtils.deleteDirectory(pathSource1);
        }
    }

    @DisplayName("Sauvegarde une serie en csv")
    @Test
    void shouldSwitcthFromDaysToWeeks() throws LoadingException, IOException {

        if (pathSource1.toFile().exists()){
            DirectoryUtils.deleteDirectory(pathSource1);
        }
        CsvInstrumentDataBase csvInstrumentDataBase = new CsvInstrumentDataBase(pathSource1);


        ZoneIdEnum zoneId= ZoneIdEnum.EUROPE_ZURICH;
        Candle candle1 = Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-07", zoneId));
        List<Candle> candles = new ArrayList<>();
        candles.add(candle1);
        csvInstrumentDataBase.save(Stock.TTE, TimeFrame.D, candles);
        candles.clear();
        candles.addAll(csvInstrumentDataBase.load(0,TimeTools.fromDayStringToLong("2026-08-07", zoneId) , Stock.TTE, TimeFrame.WK));
        assertEquals(1, candles.size());

        candles.add(Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-10", zoneId)));
        candles.add(Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-11", zoneId)));
        candles.add(Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-12", zoneId)));
        candles.add(Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-13", zoneId)));
        candles.add(Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-14", zoneId)));
        assertEquals(6, candles.size());
        csvInstrumentDataBase.save(Stock.TTE, TimeFrame.D, candles);

        candles.clear();
        candles.addAll(csvInstrumentDataBase.load(0,TimeTools.fromDayStringToLong("2026-08-14", zoneId) , Stock.TTE, TimeFrame.WK));
        assertEquals(6, candles.size()); // le filtrage en semaine est géré par le timegrid -> à tester

    }

}