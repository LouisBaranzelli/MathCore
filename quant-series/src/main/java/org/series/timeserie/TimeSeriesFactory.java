package org.series.timeserie;

import org.series.InvalidTimeSerieException;
import org.series.imputation.ImputationStrategy;
import org.series.timegrid.TimeGrid;
import org.series.timegrid.TimeGridFactory;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Predicate;

public class TimeSeriesFactory {

    public DoubleTimeSerie create(Observation[] observations, ImputationStrategy imputationStrategy, TimeFrame timeFrame, int size, Predicate<ZonedDateTime> validDate) throws InvalidTimeSerieException {
        if (observations == null) {
            throw new IllegalArgumentException("Observations cannot be null");
        }
        if (observations.length == 0){
            throw new InvalidTimeSerieException("Observations array cannot be empty");
        }

        ZonedDateTime lastZoneDateTime = Arrays.stream(observations)
                .map(Observation::getZonedDateTime)
                .max(Comparator.naturalOrder())
                .orElseThrow(() -> new InvalidTimeSerieException("..."));

        TimeGrid timeGrid = TimeGridFactory.create(lastZoneDateTime, size, validDate, timeFrame);
        TimeSeriesAligner timeSeriesAligner = new TimeSeriesAligner(imputationStrategy);
        return timeSeriesAligner.align(observations, timeGrid);
    }
}
