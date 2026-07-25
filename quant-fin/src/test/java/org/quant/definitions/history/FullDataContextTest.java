package org.quant.definitions.history;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quant.definitions.LoadingException;
import org.quant.definitions.assets.Stock;
import org.quant.definitions.candles.CandleTimeSerie;
import org.series.timeserie.TimeFrame;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests d'initialisation du data context")
class FullDataContextTest {
    @Test
    @DisplayName("Devrait charger les données depuis le premier Dataloader s'il réussit")
    void shouldLoadFromPrimaryDataloaderWhenSuccessful() throws LoadingException {

        Dataloader dataloader = new DummyDataloader();
        FullDataContext dataContext = new FullDataContext(0, 1000, List.of(dataloader), List.of(Stock.TTE, Stock.AI), List.of(TimeFrame.D, TimeFrame.HR));
        // Then
        assertEquals(2, dataContext.getInstruments().size());
        assertTrue(dataContext.getInstruments().contains(Stock.AI));
        assertDoesNotThrow(() -> {
            CandleTimeSerie candleTimeSerie = dataContext.getCandleTimeSerie(Stock.AI, TimeFrame.D);
            assertNotNull(candleTimeSerie, "La série temporelle ne devrait pas être null");
        });
        assertThrows(IllegalArgumentException.class, () -> dataContext.getCandleTimeSerie(Stock.AI, TimeFrame.MI30));
        assertThrows(IllegalArgumentException.class, () -> dataContext.getCandleTimeSerie(Stock.SAF, TimeFrame.D));

    }
}