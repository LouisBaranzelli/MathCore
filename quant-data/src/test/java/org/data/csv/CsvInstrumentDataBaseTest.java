package org.data.csv;

import org.data.definitions.LoadingException;
import org.data.definitions.assets.Stock;
import org.data.definitions.candles.Candle;
import org.data.definitions.history.AlignerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.ImputationStrategy;
import org.series.imputation.StubImputationStrategy;
import org.series.timeserie.TimeFrame;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Gestionnaire de base de données CSV pour Instruments")
class CsvInstrumentDataBaseTest {

    @TempDir
    Path tempDir;

    private CsvInstrumentDataBase csvDataBase;
    private final ZoneIdEnum zoneId = ZoneIdEnum.EUROPE_PARIS;

    @BeforeEach
    void setUp() {
        csvDataBase = new CsvInstrumentDataBase(tempDir);
    }

    @Nested
    @DisplayName("Tests de Chargement et Sauvegarde de Base")
    class BasicOperationsTest {

        @Test
        @DisplayName("Devrait lever LoadingException si le fichier/données n'existent pas")
        void shouldThrowException_whenLoadingNonExistentData() {
            assertThrows(LoadingException.class, () ->
                    csvDataBase.load(0, 4000, Stock.SU, TimeFrame.D, date -> true)
            );
        }

        @Test
        @DisplayName("Devrait sauvegarder et recharger les bougies nominalement")
        void shouldSaveAndLoadCandlesCorrectly() throws LoadingException {
            long start = TimeTools.fromDateTimeStringToLong("2020-01-01T00:00:00", zoneId);
            long lastDate = TimeTools.fromDateTimeStringToLong("2020-01-06T00:00:00", zoneId);

            Candle firstCandle = Candle.randomCandle(start);
            List<Candle> candlesToSave = List.of(
                    firstCandle,
                    Candle.randomCandle(TimeTools.fromDateTimeStringToLong("2020-01-02T00:00:00", zoneId)),
                    Candle.randomCandle(TimeTools.fromDateTimeStringToLong("2020-01-03T00:00:00", zoneId)),
                    Candle.randomCandle(TimeTools.fromDateTimeStringToLong("2020-01-04T00:00:00", zoneId)),
                    Candle.randomCandle(TimeTools.fromDateTimeStringToLong("2020-01-05T00:00:00", zoneId)),
                    Candle.randomCandle(lastDate)
            );

            csvDataBase.save(Stock.SU, TimeFrame.D, candlesToSave);

            List<Candle> loadedCandles = csvDataBase.load(start, lastDate, Stock.SU, TimeFrame.D, date -> true);

            assertEquals(6, loadedCandles.size());
            assertEquals(firstCandle, loadedCandles.get(0));
        }

        @Test
        @DisplayName("Devrait ignorer les bougies doublons lors de la resauvegarde")
        void shouldDeduplicateCandles_whenSavingDuplicateTimeFrames() throws LoadingException {
            long start = TimeTools.fromDateTimeStringToLong("2020-01-01T00:00:00", zoneId);
            long date1 = TimeTools.fromDateTimeStringToLong("2020-01-02T00:00:00", zoneId);

            Candle candle1 = Candle.randomCandle(start);
            Candle candle2 = Candle.randomCandle(date1);

            csvDataBase.save(Stock.SU, TimeFrame.D, List.of(candle1, candle2));
            // Tentative de sauvegarde avec un doublon de candle2
            csvDataBase.save(Stock.SU, TimeFrame.D, List.of(candle1, candle2, candle2));

            List<Candle> loadedCandles = csvDataBase.load(start, date1, Stock.SU, TimeFrame.D, date -> true);

            assertEquals(2, loadedCandles.size());
        }
    }

    @Nested
    @DisplayName("Tests d'Isolation des Données et Validation")
    class IsolationAndValidationTest {

        @Test
        @DisplayName("Devrait isoler les données par TimeFrame sur le même stock")
        void shouldIsolateDataByTimeFrame() throws LoadingException {
            long start = TimeTools.fromDateTimeStringToLong("2020-01-01T00:00:00", zoneId);
            long endMi5 = TimeTools.fromDateTimeStringToLong("2020-01-01T00:05:00", zoneId);

            Candle c1 = Candle.randomCandle(start);
            Candle c2 = Candle.randomCandle(endMi5);

            csvDataBase.save(Stock.SU, TimeFrame.MI5, List.of(c1, c2));

            List<Candle> loadedMi5 = csvDataBase.load(start, endMi5, Stock.SU, TimeFrame.MI5, date -> true);
            assertEquals(2, loadedMi5.size());

            // Vérifie que le TimeFrame Daily sur le même stock n'est pas pollué
            assertThrows(LoadingException.class, () ->
                    csvDataBase.load(start, endMi5, Stock.SU, TimeFrame.D, date -> true)
            );
        }

        @Test
        @DisplayName("Devrait isoler les données par Instrument")
        void shouldIsolateDataByInstrument() throws LoadingException {
            long start = TimeTools.fromDateTimeStringToLong("2020-01-01T00:00:00", zoneId);
            long end = TimeTools.fromDateTimeStringToLong("2020-01-02T00:00:00", zoneId);

            csvDataBase.save(Stock.SU, TimeFrame.D, List.of(Candle.randomCandle(start)));

            assertThrows(LoadingException.class, () ->
                    csvDataBase.load(start, end, Stock.TTE, TimeFrame.D, date -> true)
            );
        }

        @Test
        @DisplayName("Devrait échouer lors du chargement si le trou de données dépasse le seuil autorisé")
        void shouldThrowException_whenDataCompletenessUnderThreshold() throws LoadingException {
            long start = TimeTools.fromDateTimeStringToLong("2020-01-01T00:00:00", zoneId);
            long endWithGap = TimeTools.fromDateTimeStringToLong("2020-01-09T00:00:00", zoneId);

            // Données éparses créant un trou important entre le 01 et le 09 janvier
            List<Candle> gappedCandles = List.of(
                    Candle.randomCandle(start),
                    Candle.randomCandle(endWithGap)
            );

            csvDataBase.save(Stock.SU, TimeFrame.D, gappedCandles);

            assertThrows(LoadingException.class, () ->
                    csvDataBase.load(start, endWithGap, Stock.SU, TimeFrame.D, date -> true)
            );
        }
    }

    @Test
    @DisplayName("Devrait convertir la résolution lors du passage de Daily à Weekly")
    void shouldSwitchFromDaysToWeeks() throws LoadingException {
        ZoneIdEnum parisZone = ZoneIdEnum.EUROPE_PARIS;
        long day1 = TimeTools.fromDayStringToLong("2026-08-07", parisZone);

        Candle candle1 = Candle.randomCandle(day1);
        csvDataBase.save(Stock.TTE, TimeFrame.D, List.of(candle1));

        List<Candle> loadedWeekly = csvDataBase.load(day1, day1, Stock.TTE, TimeFrame.WK, AlignerService.getValidDatePredicateBasedOnAligner(Stock.TTE, TimeFrame.WK));
        assertEquals(1, loadedWeekly.size());

        List<Candle> fullWeekCandles = List.of(
                candle1,
                Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-10", parisZone)),
                Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-11", parisZone)),
                Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-12", parisZone)),
                Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-13", parisZone)),
                Candle.randomCandle(TimeTools.fromDayStringToLong("2026-08-14", parisZone))
        );

        csvDataBase.save(Stock.TTE, TimeFrame.D, fullWeekCandles);

        long endWeek = TimeTools.fromDayStringToLong("2026-08-14", parisZone);
        List<Candle> reloadedWeekly = csvDataBase.load(day1, endWeek, Stock.TTE, TimeFrame.WK, AlignerService.getValidDatePredicateBasedOnAligner(Stock.TTE, TimeFrame.WK));

        assertEquals(2, reloadedWeekly.size());
    }


}