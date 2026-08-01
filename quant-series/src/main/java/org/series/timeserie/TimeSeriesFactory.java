package org.series.timeserie;

import org.series.InvalidTimeSerieException;
import org.series.TimeTools;
import org.series.imputation.ImputationStrategy;
import org.series.timegrid.TimeGrid;
import org.series.timegrid.TimeGridFactory;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

public class TimeSeriesFactory {
    /**
     * Crée une Time Serie en prenant la dernière valeur temporelle comme référence, avec une fréquence de données: timeFrame
     * modulé par le prédicate.
     * @param observations
     * @param imputationStrategy
     * @param timeFrame
     * @param timeGridValidDatePredicate
     * @return
     * @throws InvalidTimeSerieException
     */
    public static DoubleTimeSerie create(Observation[] observations, ImputationStrategy imputationStrategy, TimeFrame timeFrame, Predicate<Long> timeGridValidDatePredicate) throws InvalidTimeSerieException {

        Long lastZoneDateTime = getLastZoneDateTime(observations);
        Long firstZoneDateTime = getFirstZoneDateTime(observations);
        TimeGrid timeGrid = TimeGridFactory.create(firstZoneDateTime, lastZoneDateTime, timeGridValidDatePredicate, timeFrame);
        TimeSeriesAligner timeSeriesAligner = new TimeSeriesAligner(imputationStrategy);
        return timeSeriesAligner.align(observations, timeGrid);
    }

    public static DoubleTimeSerie create(Observation[] observations, ImputationStrategy imputationStrategy, TimeFrame timeFrame, int size, Predicate<Long> validDate) throws InvalidTimeSerieException {

        Long lastZoneDateTime = getLastZoneDateTime(observations);
        TimeGrid timeGrid = TimeGridFactory.create(lastZoneDateTime, size, validDate, timeFrame);
        TimeSeriesAligner timeSeriesAligner = new TimeSeriesAligner(imputationStrategy);
        return timeSeriesAligner.align(observations, timeGrid);
    }

    private static Long getLastZoneDateTime(Observation[] observations) throws InvalidTimeSerieException {
        if (observations == null) {
            throw new IllegalArgumentException("Observations cannot be null");
        }
        if (observations.length == 0){
            throw new InvalidTimeSerieException("Observations array cannot be empty");
        }

        return Arrays.stream(observations)
                .map(Observation::getDateTime
                )
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new InvalidTimeSerieException("..."));
    }

    private static Long getFirstZoneDateTime(Observation[] observations) throws InvalidTimeSerieException {
        if (observations == null) {
            throw new IllegalArgumentException("Observations cannot be null");
        }
        if (observations.length == 0){
            throw new InvalidTimeSerieException("Observations array cannot be empty");
        }

        return Arrays.stream(observations)
                .map(Observation::getDateTime)
                .min(Comparator.naturalOrder())
                .orElseThrow(() -> new InvalidTimeSerieException("..."));
    }
}
