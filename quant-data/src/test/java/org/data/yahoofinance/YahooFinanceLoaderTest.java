package org.data.yahoofinance;

import org.data.csv.CsvInstrumentDataBase;
import org.data.csv.DataSaver;
import org.data.definitions.LoadingException;
import org.data.definitions.assets.Stock;
import org.data.definitions.candles.Candle;
import org.data.definitions.history.DataLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.timeserie.TimeFrame;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Intégration YahooFinanceLoader avec Cache CSV")
class YahooFinanceLoaderTest {

    @TempDir
    Path tempDir;

    private DataSaver saverCsv;
    private DataLoader loaderCsv;
    private DataLoader yahooFinanceLoader;

    private final ZoneIdEnum parisZone = ZoneIdEnum.EUROPE_PARIS;
    private static final double EPSILON = 1e-4;

    @BeforeEach
    void setUp() {
        CsvInstrumentDataBase db = new CsvInstrumentDataBase(tempDir);
        this.saverCsv = db;
        this.loaderCsv = db;
        this.yahooFinanceLoader = new YahooFinanceLoader(loaderCsv, saverCsv);
    }

    @Nested
    @DisplayName("Chargement Direct depuis l'API")
    class ApiDirectLoadingTest {

        @Test
        @DisplayName("Devrait charger les bougies Daily jusqu'au 7 Août (5 jours)")
        void shouldLoadDailyCandlesUntil7th() throws LoadingException {
            long start = TimeTools.fromDayStringToLong("2026-08-03", parisZone);
            long end7th = TimeTools.fromDayStringToLong("2026-08-07", parisZone);

            List<Candle> candles = yahooFinanceLoader.load(start, end7th, Stock.TTE, TimeFrame.D);

            assertEquals(5, candles.size());
            assertEquals(74.08999, candles.get(candles.size() - 1).close(), EPSILON);
            assertEquals(74.54000, candles.get(candles.size() - 2).close(), EPSILON);
            assertEquals(73.80000, candles.get(candles.size() - 3).close(), EPSILON);
        }

        @Test
        @DisplayName("Devrait charger les bougies Daily jusqu'au 6 Août (4 jours)")
        void shouldLoadDailyCandlesUntil6th() throws LoadingException {
            long start = TimeTools.fromDayStringToLong("2026-08-03", parisZone);
            long end6th = TimeTools.fromDayStringToLong("2026-08-06", parisZone);

            List<Candle> candles = yahooFinanceLoader.load(start, end6th, Stock.TTE, TimeFrame.D);

            assertEquals(4, candles.size());
            assertEquals(74.54000, candles.get(candles.size() - 1).close(), EPSILON);
            assertEquals(73.80000, candles.get(candles.size() - 2).close(), EPSILON);
        }

        @Test
        @DisplayName("Devrait lever LoadingException si la plage inclut des dates futures sans données")
        void shouldThrowException_whenRequestingFutureMissingData() {
            long start = TimeTools.fromDayStringToLong("2026-08-03", parisZone);
            long endFuture = TimeTools.fromDayStringToLong("2026-08-09", parisZone);

            assertThrows(LoadingException.class, () ->
                    yahooFinanceLoader.load(start, endFuture, Stock.TTE, TimeFrame.D)
            );
        }
    }

    @Nested
    @DisplayName("Invalidation et Lecture depuis le Cache CSV")
    class CachingMechanismTest {

        @Test
        @DisplayName("Devrait réutiliser les données enregistrées dans le cache CSV")
        void shouldPersistAndRetrieveFromCsvCache() throws LoadingException {
            long start = TimeTools.fromDayStringToLong("2026-08-03", parisZone);
            long end7th = TimeTools.fromDayStringToLong("2026-08-07", parisZone);
            long end6th = TimeTools.fromDayStringToLong("2026-08-06", parisZone);

            List<Candle> initialCandles = yahooFinanceLoader.load(start, end7th, Stock.TTE, TimeFrame.D);
            Candle candle7th = initialCandles.get(initialCandles.size() - 1);
            Candle candle6th = initialCandles.get(initialCandles.size() - 2);

            // Rechargement depuis le cache
            List<Candle> cached7th = yahooFinanceLoader.load(start, end7th, Stock.TTE, TimeFrame.D);
            assertEquals(candle7th, cached7th.get(cached7th.size() - 1));

            List<Candle> cached6th = yahooFinanceLoader.load(start, end6th, Stock.TTE, TimeFrame.D);
            assertEquals(candle6th, cached6th.get(cached6th.size() - 1));
        }

        @Test
        @DisplayName("Devrait privilégier les données du cache CSV lors d'une agrégation Weekly")
        void shouldReadModifiedCsvData_whenQueryingWeeklyTimeFrame() throws LoadingException {
            long start = TimeTools.fromDayStringToLong("2026-08-03", parisZone);
            long end7th = TimeTools.fromDayStringToLong("2026-08-07", parisZone);

            List<Candle> originalCandles = yahooFinanceLoader.load(start, end7th, Stock.TTE, TimeFrame.D);

            // Modification sécurisée de la liste pour tester l'impact sur le cache
            List<Candle> modifiedCandles = new ArrayList<>(originalCandles);
            Candle injectedCandle = Candle.randomCandle(end7th, Stock.TTE);
            modifiedCandles.remove(modifiedCandles.size() - 1);
            modifiedCandles.add(injectedCandle);

            // Force l'écriture d'une version altérée dans le CSV
            saverCsv.save(Stock.TTE, TimeFrame.D, modifiedCandles);

            // Vérifie que la lecture Weekly consomme la donnée altérée du CSV
            List<Candle> weeklyCandles = yahooFinanceLoader.load(start, end7th, Stock.TTE, TimeFrame.WK);
            assertEquals(injectedCandle, weeklyCandles.get(weeklyCandles.size() - 1));
        }
    }
}