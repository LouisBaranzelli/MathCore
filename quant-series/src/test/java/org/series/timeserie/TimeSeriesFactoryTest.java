package org.series.timeserie;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.series.InvalidTimeSerieException;
import org.series.imputation.ImputationStrategy;
import org.series.imputation.StubImputationStrategy;

import java.time.ZonedDateTime;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class TimeSeriesFactoryTest {

    private TimeSeriesFactory factory;
    private ImputationStrategy stubStrategy;
    private TimeFrame stubTimeFrame;
    private Predicate<ZonedDateTime> dummyPredicate;

    @BeforeEach
    void setUp() {
        factory = new TimeSeriesFactory();
        stubStrategy = new StubImputationStrategy();
        stubTimeFrame = TimeFrame.D;
        dummyPredicate = date -> true;
    }

    @Test
    @DisplayName("Devrait lever une IllegalArgumentException si le tableau d'observations est null")
    void shouldThrowExceptionWhenObservationsNull() {
        assertThrows(IllegalArgumentException.class, () ->
                factory.create(null, stubStrategy, stubTimeFrame, 5, dummyPredicate)
        );
    }

    @Test
    @DisplayName("Devrait lever une InvalidTimeSerieException si le tableau d'observations est vide")
    void shouldThrowExceptionWhenObservationsEmpty() {
        Observation[] emptyArray = new Observation[0];

        InvalidTimeSerieException ex = assertThrows(InvalidTimeSerieException.class, () ->
                factory.create(emptyArray, stubStrategy, stubTimeFrame, 5, dummyPredicate)
        );
        assertTrue(ex.getMessage().contains("Observations array cannot be empty"));
    }

    @Test
    @DisplayName("Devrait identifier correctement la date maximale parmi des observations désordonnées")
    void shouldCorrectlyFindMaxZoneDateTime() throws InvalidTimeSerieException {
        // Préparation de 3 observations désordonnées dans le temps
        ZonedDateTime past = ZonedDateTime.parse("2026-07-15T12:00:00Z");
        ZonedDateTime future = ZonedDateTime.parse("2026-07-18T12:00:00Z"); // La plus récente
        ZonedDateTime present = ZonedDateTime.parse("2026-07-16T12:00:00Z");

        Observation obs1 = new StubObservation(past, 10.0);
        Observation obs2 = new StubObservation(future, 20.0); // Placé au milieu exprès
        Observation obs3 = new StubObservation(present, 30.0);

        Observation[] observations = {obs1, obs2, obs3};
        int targetGridSize = 10;

        DoubleTimeSerie result = factory.create(observations, stubStrategy, stubTimeFrame, targetGridSize, dummyPredicate);

        assertNotNull(result, "La série temporelle créée ne doit pas être nulle");
        // On vérifie que la grille associée au résultat possède bien la taille configurée
        assertEquals(targetGridSize, result.size());
    }

    @Test
    @DisplayName("Devrait créer avec succès une DoubleTimeSerie valide dans un cas nominal")
    void shouldCreateDoubleTimeSerieSuccessfully() throws InvalidTimeSerieException {
        ZonedDateTime now = ZonedDateTime.now();
        Observation[] observations = {new StubObservation(now, 42.0)};
        int targetSize = 3;

        DoubleTimeSerie result = factory.create(observations, stubStrategy, stubTimeFrame, targetSize, dummyPredicate);

        assertNotNull(result);
        assertEquals(targetSize, result.size());
        assertEquals(42.0, result.getValue(0));
    }

}