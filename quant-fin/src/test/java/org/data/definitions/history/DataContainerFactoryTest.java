package org.data.definitions.history;

import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Stock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.ImputationStrategy;
import org.series.imputation.StubImputationStrategy;
import org.series.timeserie.TimeFrame;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DataContainerFactoryTest {

    @Test
    @DisplayName("Creation d'un  data container horaire horraire: dois retourner la bonne taille")
    public void testCeationHourTick() throws LoadingException {

        DataLoader dummyLoader = new DummyDataLoader();
        ImputationStrategy imputationStrategy = new StubImputationStrategy();
        // Mercredi 1er Janvier 2020
        DataContainerFactory dataContainerFactory = new DataContainerFactory(imputationStrategy, dummyLoader);
        long start = TimeTools.fromDayStringToLong("2020-01-01", ZoneIdEnum.EUROPE_PARIS);
        long end = TimeTools.fromDayStringToLong("2020-01-30", ZoneIdEnum.EUROPE_PARIS);
        DataContainer tteDataContainer = dataContainerFactory.create(start, end, Stock.TTE,  TimeFrame.HR);
        ZonedDateTime firstDateTime = TimeTools.fromLongToZonedDateTime(tteDataContainer.getCandleTimeSerie(TimeFrame.HR).getFirst().timestamp(), ZoneIdEnum.EUROPE_PARIS.getZoneId());
        assertEquals(2, firstDateTime.getDayOfMonth());
        assertEquals(0, firstDateTime.getHour());
        assertEquals(0, firstDateTime.getMinute());

        tteDataContainer = dataContainerFactory.create(start, end, Stock.TTE,  TimeFrame.MI5);
        firstDateTime = TimeTools.fromLongToZonedDateTime(tteDataContainer.getCandleTimeSerie(TimeFrame.MI5).getFirst().timestamp(), ZoneIdEnum.EUROPE_PARIS.getZoneId());
        assertEquals(2, firstDateTime.getDayOfMonth());
        assertEquals(0, firstDateTime.getHour());
        assertEquals(0, firstDateTime.getMinute());

        start = TimeTools.fromDayStringToLong("2019-12-31", ZoneIdEnum.EUROPE_PARIS);
        tteDataContainer = dataContainerFactory.create(start, end, Stock.TTE,  TimeFrame.MI5);
        firstDateTime = TimeTools.fromLongToZonedDateTime(tteDataContainer.getCandleTimeSerie(TimeFrame.MI5).getFirst().timestamp(), ZoneIdEnum.EUROPE_PARIS.getZoneId());
        assertEquals(31, firstDateTime.getDayOfMonth());
        assertEquals(0, firstDateTime.getHour());
        assertEquals(0, firstDateTime.getMinute());

        start = TimeTools.fromDateTimeStringToLong("2020-01-02T00:05:00", ZoneIdEnum.EUROPE_PARIS);
        tteDataContainer = dataContainerFactory.create(start, end, Stock.TTE,  TimeFrame.MI5);
        firstDateTime = TimeTools.fromLongToZonedDateTime(tteDataContainer.getCandleTimeSerie(TimeFrame.MI5).getFirst().timestamp(), ZoneIdEnum.EUROPE_PARIS.getZoneId());
        assertEquals(2, firstDateTime.getDayOfMonth());
        assertEquals(0, firstDateTime.getHour());
        assertEquals(5, firstDateTime.getMinute());

    }
}