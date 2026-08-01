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
import org.series.InvalidTimeSerieException;
import org.series.imputation.StubImputationStrategy;
import org.series.timegrid.TimeGrid;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.ImmutableDoubleTimeSerie;
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
        List<Candle> candles = new ArrayList<>(csvInstrumentDataBase.load(0, 4000, Stock.SU, TimeFrame.D));
        assertEquals(0, candles.size());
        Candle candle1 = Candle.randomCandle(1000L);
        Candle candle2 = Candle.randomCandle(2000L);
        candles.add(candle1);
        candles.add(candle2);
        candles.add(Candle.randomCandle(3000L));
        candles.add(Candle.randomCandle(4000L));
        candles.add(Candle.randomCandle(5000L));
        csvInstrumentDataBase.save(Stock.SU, TimeFrame.D, candles);
        candles.clear();
        candles = new ArrayList<>(csvInstrumentDataBase.load(0, 4000, Stock.SU, TimeFrame.D));
        assertEquals(4, candles.size());
        assertEquals(candle1, candles.get(0));
        candles.add(Candle.randomCandle(6000L));
        csvInstrumentDataBase.save(Stock.SU, TimeFrame.D, candles);
        candles.clear();
        candles = csvInstrumentDataBase.load(0, 6000, Stock.SU, TimeFrame.D);
        assertEquals(6, candles.size());
    }

}