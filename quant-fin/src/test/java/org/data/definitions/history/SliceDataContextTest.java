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

import static org.junit.jupiter.api.Assertions.*;

class SliceDataContextTest {

    @Nested
    @DisplayName("Tests de creation et de decoupage de SliceDataContext")
    class SliceDataContextCreationTests {

        @Test
        @DisplayName("Devrait charger correctement le contexte parent et extraire la sous-tranche")
        void shouldCreateFullAndSliceDataContextCorrectly() {
            // Given
            DataLoader dataloader = new DummyDataLoader();
            long startFull = TimeTools.fromDayStringToLong("2020-01-01", ZoneIdEnum.EUROPE_PARIS);
            long endFull = TimeTools.fromDayStringToLong("2020-01-30", ZoneIdEnum.EUROPE_PARIS);

            FullDataContext dataContextInDays = new FullDataContext(
                    startFull,
                    endFull,
                    new StubImputationStrategy(),
                    List.of(dataloader),
                    List.of(Stock.TTE, Stock.AI),
                    List.of(TimeFrame.D, TimeFrame.HR)
            );

            // Then : Vérification du contexte parent
            int expectedDailyFullSize = 29 + 1;       // 30 points
            int expectedHourlyFullSize = 29 * 24 + 1; // 697 points

            assertEquals(expectedDailyFullSize, dataContextInDays.getCandleTimeSerie(Stock.TTE, TimeFrame.D).size());
            assertEquals(expectedHourlyFullSize, dataContextInDays.getCandleTimeSerie(Stock.TTE, TimeFrame.HR).size());

            // When : Découpage du 10 au 20 janvier
            long startSlice = TimeTools.fromDayStringToLong("2020-01-10", ZoneIdEnum.EUROPE_PARIS);
            long endSlice = TimeTools.fromDayStringToLong("2020-01-20", ZoneIdEnum.EUROPE_PARIS);

            SliceDataContext sliceDataContext = new SliceDataContext(startSlice, endSlice, dataContextInDays);

            // Then : Vérification de la tranche
            int expectedDailySliceSize = 10 + 1;       // 11 points
            int expectedHourlySliceSize = 10 * 24 + 1; // 241 points

            assertEquals(expectedDailySliceSize, sliceDataContext.getCandleTimeSerie(Stock.TTE, TimeFrame.D).size());
            assertEquals(expectedHourlySliceSize, sliceDataContext.getCandleTimeSerie(Stock.TTE, TimeFrame.HR).size());
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