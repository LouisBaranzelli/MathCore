package org.data.definitions.history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.data.definitions.LoadingException;
import org.data.definitions.assets.Stock;
import org.data.definitions.candles.CandleTimeSerie;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.StubImputationStrategy;
import org.series.timeserie.TimeFrame;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests d'initialisation du data context")
class FullDataContextTest {
    @Test
    @DisplayName("Devrait charger les données depuis le premier Dataloader s'il réussit")
    void shouldLoadFromPrimaryDataloaderWhenSuccessful() throws LoadingException {

        long start = TimeTools.fromDateTimeStringToLong("2023-02-23T00:00:00", ZoneIdEnum.EUROPE_PARIS);
        long end = TimeTools.fromDateTimeStringToLong("2023-02-24T00:00:00", ZoneIdEnum.EUROPE_PARIS);
        DataLoader dataloader = new DummyDataLoader();
        FullDataContext dataContext = new FullDataContext(start, end,new StubImputationStrategy(), List.of(dataloader), List.of(Stock.TTE, Stock.AI), List.of(TimeFrame.D, TimeFrame.HR));
        // Then
        assertEquals(2, dataContext.getInstruments().size());
        assertTrue(dataContext.getInstruments().contains(Stock.AI));
        assertDoesNotThrow(() -> {
            CandleTimeSerie candleTimeSerie = dataContext.getCandleTimeSerie(Stock.AI, TimeFrame.D);
            assertNotNull(candleTimeSerie, "La série temporelle ne devrait pas être null");
        });
        assertThrows(IllegalArgumentException.class, () -> dataContext.getCandleTimeSerie(Stock.AI, TimeFrame.MI30));
        assertThrows(IllegalArgumentException.class, () -> dataContext.getCandleTimeSerie(Stock.SAF, TimeFrame.D));


        FullDataContext dataContextInDays = new FullDataContext(TimeTools.fromDayStringToLong("2020-01-01", ZoneIdEnum.EUROPE_PARIS),
                TimeTools.fromDayStringToLong("2020-01-30", ZoneIdEnum.EUROPE_PARIS),new StubImputationStrategy(), List.of(dataloader),
                List.of(Stock.TTE, Stock.AI), List.of(TimeFrame.D, TimeFrame.HR));
        assertEquals(100., dataContextInDays.getPercentLoaded());
        assertEquals("Data context: 2 companies loaded (100,0 %), timeframes: 1 Day, 1 Hour, start date: 2020-01-01T00:00+01:00[Europe/Paris], end date: 2020-01-30T00:00+01:00[Europe/Paris]", dataContextInDays.getDescription());

    }
}