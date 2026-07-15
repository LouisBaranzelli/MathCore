package org.series;

import org.junit.jupiter.api.Test;
import org.math.vector.Vector;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ImmutableDoubleTimeSerieTest {

    @Test
    void shouldThrowExceptionWhenTimestampsArrayIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(null, new double[]{10.0}));
        assertTrue(ex.getMessage().contains("Arrays cannot be null"));
    }

    @Test
    void shouldThrowExceptionWhenValuesArrayIsNull() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(new long[]{1000L}, null));
        assertTrue(ex.getMessage().contains("Arrays cannot be null"));
    }

    @Test
    void shouldThrowExceptionWhenLengthsMismatch() {
        long[] timestamps = {1000L, 2000L};
        double[] values = {10.5};

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(timestamps, values));
        assertTrue(ex.getMessage().contains("Mismatched lengths"));
    }

    @Test
    void shouldThrowExceptionWhenArraysAreEmpty() {
        long[] timestamps = {};
        double[] values = {};

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(timestamps, values));
        assertTrue(ex.getMessage().contains("Time serie cannot be empty"));
    }

    @Test
    void shouldThrowExceptionWhenValueIsNaN() {
        long[] timestamps = {1000L, 2000L};
        double[] withNaN = {10.5, Double.NaN};

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(timestamps, withNaN));
        assertTrue(ex.getMessage().contains("Invalid numeric value (NaN/Infinite)"));
    }

    @Test
    void shouldThrowExceptionWhenValueIsInfinite() {
        long[] timestamps = {1000L, 2000L};
        double[] withInfinity = {Double.POSITIVE_INFINITY, 12.0};

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(timestamps, withInfinity));
        assertTrue(ex.getMessage().contains("Invalid numeric value (NaN/Infinite)"));
    }

    @Test
    void shouldThrowExceptionWhenTimestampsAreDecreasing() {
        double[] values = {10.0, 11.0, 12.0};
        long[] decreasing = {3000L, 2000L, 4000L};

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(decreasing, values));
        assertTrue(ex.getMessage().contains("Timestamps must be strictly increasing"));
    }

    @Test
    void shouldThrowExceptionWhenTimestampsHaveDuplicates() {
        double[] values = {10.0, 11.0, 12.0};
        long[] duplicate = {1000L, 1000L, 2000L};

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new ImmutableDoubleTimeSerie(duplicate, values));
        assertTrue(ex.getMessage().contains("Timestamps must be strictly increasing"));
    }

    @Test
    void shouldStoreAndRetrieveDataCorrectly() {
        long[] timestamps = {1000L, 2000L, 3000L};
        double[] values = {10.5, 11.2, 12.8};

        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(timestamps, values);

        assertEquals(3, serie.size());
        assertEquals(10.5, serie.getValue(0));
        assertEquals(3000L, serie.getTimestamp(2));
    }

    @Test
    void shouldBeProtectedAgainstExternalSourceMutation() {
        long[] timestamps = {1000L, 2000L};
        double[] values = {10.0, 20.0};

        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(timestamps, values);

        timestamps[0] = 999999L;
        values[0] = -99.9;

        assertEquals(1000L, serie.getTimestamp(0));
        assertEquals(10.0, serie.getValue(0));
    }


    @Test
    void shouldReturnCorrectStartInstant() {
        long[] timestamps = {1672531200000L, 1672534800000L}; // 2023-01-01 00:00:00 UTC et +1h
        double[] values = {10.0, 15.0};

        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(timestamps, values);

        Instant expectedStart = Instant.ofEpochMilli(1672531200000L);
        assertEquals(expectedStart, serie.getStart());
    }

    @Test
    void shouldReturnCorrectEndInstant() {
        long[] timestamps = {1672531200000L, 1672534800000L};
        double[] values = {10.0, 15.0};

        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(timestamps, values);

        Instant expectedEnd = Instant.ofEpochMilli(1672534800000L);
        assertEquals(expectedEnd, serie.getEnd());
    }

    @Test
    void shouldReturnCorrectZonedDateTimeForGivenZoneId() {
        // Epoch milli pour le 2023-01-01 à 00:00:00 UTC
        long[] timestamps = {1672531200000L};
        double[] values = {10.0};

        DoubleTimeSerie serie = new ImmutableDoubleTimeSerie(timestamps, values);

        // En UTC, l'heure doit être 2023-01-01T00:00:00Z
        ZoneId zoneUTC = ZoneId.of("UTC");
        ZonedDateTime expectedUTC = ZonedDateTime.of(2023, 1, 1, 0, 0, 0, 0, zoneUTC);
        ZonedDateTime actualUTC = serie.getZoneDateTime(0, zoneUTC);

        // On compare les instants et les fuseaux
        assertTrue(expectedUTC.isEqual(actualUTC));
        assertEquals(zoneUTC, actualUTC.getZone());

        // En Europe/Paris (UTC+1 en hiver), l'heure locale doit être 01:00:00
        ZoneId zoneParis = ZoneId.of("Europe/Paris");
        ZonedDateTime expectedParis = ZonedDateTime.of(2023, 1, 1, 1, 0, 0, 0, zoneParis);
        ZonedDateTime actualParis = serie.getZoneDateTime(0, zoneParis);

        assertTrue(expectedParis.isEqual(actualParis));
        assertEquals(zoneParis, actualParis.getZone());
    }

    @Test
    void shouldConvertToVectorSuccessfully() {
        // Given
        long[] timestamps = {1000L, 2000L, 3000L};
        double[] values = {10.5, -2.3, 42.0};
        ImmutableDoubleTimeSerie serie = new ImmutableDoubleTimeSerie(timestamps, values);

        // When
        Vector vector = serie.toVector();

        // Then
        assertNotNull(vector, "Le vecteur généré ne doit pas être null");
        assertEquals(serie.size(), vector.getSize(), "Le vecteur doit avoir la même taille que la série");

        for (int i = 0; i < values.length; i++) {
            assertEquals(serie.getValue(i), vector.getValue(i), 1e-9,
                    "La valeur du vecteur à l'index " + i + " doit être identique à celle de la série");
        }
    }
}