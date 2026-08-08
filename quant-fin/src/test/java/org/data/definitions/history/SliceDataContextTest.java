package org.data.definitions.history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.data.definitions.assets.Stock;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.StubImputationStrategy;
import org.series.timeserie.TimeFrame;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class SliceDataContextTest {

    @Nested
    @DisplayName("Tests de creation et de decoupage de SliceDataContext")
    class SliceDataContextCreationTests {

        @Test
        @DisplayName("Devrait charger correctement le contexte parent et extraire la sous-tranche")
        void shouldCreateFullAndSliceDataContextCorrectly() {
            // Given 01 Janvier 2020: Mercredi
            DataLoader dataloader = new DummyDataLoader();
            long startFull = TimeTools.fromDayStringToLong("2020-01-01", ZoneIdEnum.EUROPE_PARIS);
            long endFull = TimeTools.fromDayStringToLong("2020-01-30", ZoneIdEnum.EUROPE_PARIS);

            FullDataContext dataContextInDays = new FullDataContext(
                    startFull,
                    endFull,
                    new StubImputationStrategy(),
                    List.of(dataloader),
                    List.of(Stock.TTE, Stock.AI),
                    List.of(TimeFrame.HR, TimeFrame.D, TimeFrame.MI5)
            );

            // Then : Vérification du contexte parent
            int expectedDailyFullSize =21;       // hors samedi et dimanche et le 1er esr ferié, 30 inclus car présent
            int expectedHourlyFullSize = 20 * (8 + 1); // 9h-17h les 2 bornes incluse
            int expected5minlyFullSize = 20 * (8 * 12 + 1);

            int hourSize = dataContextInDays.getCandleTimeSerie(Stock.TTE, TimeFrame.HR).size();


            assertEquals(expectedDailyFullSize, dataContextInDays.getCandleTimeSerie(Stock.TTE, TimeFrame.D).size());
            assertEquals(expected5minlyFullSize, dataContextInDays.getCandleTimeSerie(Stock.TTE, TimeFrame.MI5).size());
            assertEquals(expectedHourlyFullSize, hourSize);

            // When : Découpage du 10 au 20 janvier
            long startSlice = TimeTools.fromDayStringToLong("2020-01-10", ZoneIdEnum.EUROPE_PARIS);
            long endSlice = TimeTools.fromDayStringToLong("2020-01-20", ZoneIdEnum.EUROPE_PARIS);

            SliceDataContext sliceDataContext = new SliceDataContext(startSlice, endSlice, dataContextInDays);

            // Then : Vérification de la tranche
            int expectedDailySliceSize = 7;
            int expectedHourlySliceSize = 6 * (8 + 1) + 1; // + 1 car débute à minuit donc integre 17h de la journée précédente

            assertEquals(expectedDailySliceSize, sliceDataContext.getCandleTimeSerie(Stock.TTE, TimeFrame.D).size());
            assertEquals(expectedHourlySliceSize, sliceDataContext.getCandleTimeSerie(Stock.TTE, TimeFrame.HR).size());

            startSlice = TimeTools.fromDateTimeStringToLong("2020-01-10T10:06:00", ZoneIdEnum.EUROPE_PARIS);
            endSlice = TimeTools.fromDateTimeStringToLong("2020-01-20T16:00:00", ZoneIdEnum.EUROPE_PARIS);

            sliceDataContext = new SliceDataContext(startSlice, endSlice, dataContextInDays);

            // Then : Vérification de la tranche
            expectedDailySliceSize = 7;
            expectedHourlySliceSize = 7 * (8 + 1) - 2;

            assertEquals(expectedDailySliceSize, sliceDataContext.getCandleTimeSerie(Stock.TTE, TimeFrame.D).size());
            assertEquals(expectedHourlySliceSize, sliceDataContext.getCandleTimeSerie(Stock.TTE, TimeFrame.HR).size());

            long targetStart =  TimeTools.fromDateTimeStringToLong("2020-01-10T10:00:00", ZoneIdEnum.EUROPE_PARIS);
            assertEquals(targetStart,  sliceDataContext.getCandleTimeSerie(Stock.TTE, TimeFrame.HR).getFirst().timestamp());
        }

        @Test
        @DisplayName("Devrait lever une IllegalArgumentException si la date de debut du slice est hors bornes")
        void shouldThrowExceptionWhenSliceStartIsBeforeParentStart() {
            DataLoader dataloader = new DummyDataLoader();
            FullDataContext dataContextInDays = new FullDataContext(
                    TimeTools.fromDayStringToLong("2020-01-01", ZoneIdEnum.EUROPE_PARIS),
                    TimeTools.fromDayStringToLong("2020-01-30", ZoneIdEnum.EUROPE_PARIS),
                    new StubImputationStrategy(),
                    List.of(dataloader),
                    List.of(Stock.TTE),
                    List.of(TimeFrame.D)
            );

            long invalidStart = TimeTools.fromDayStringToLong("2019-01-10", ZoneIdEnum.EUROPE_PARIS);
            long validEnd = TimeTools.fromDayStringToLong("2020-01-20", ZoneIdEnum.EUROPE_PARIS);

            assertThrows(IllegalArgumentException.class,
                    () -> new SliceDataContext(invalidStart, validEnd, dataContextInDays));
        }

        @Test
        @DisplayName("Devrait lever une IllegalArgumentException si la date de fin du slice est hors bornes")
        void shouldThrowExceptionWhenSliceEndIsAfterParentEnd() {
            DataLoader dataloader = new DummyDataLoader();
            FullDataContext dataContextInDays = new FullDataContext(
                    TimeTools.fromDayStringToLong("2020-01-01", ZoneIdEnum.EUROPE_PARIS),
                    TimeTools.fromDayStringToLong("2020-01-30", ZoneIdEnum.EUROPE_PARIS),
                    new StubImputationStrategy(),
                    List.of(dataloader),
                    List.of(Stock.TTE),
                    List.of(TimeFrame.D)
            );

            long validStart = TimeTools.fromDayStringToLong("2020-01-10", ZoneIdEnum.EUROPE_PARIS);
            long invalidEnd = TimeTools.fromDayStringToLong("2021-01-20", ZoneIdEnum.EUROPE_PARIS);

            assertThrows(IllegalArgumentException.class,
                    () -> new SliceDataContext(validStart, invalidEnd, dataContextInDays));
        }
    }
}