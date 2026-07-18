package org.series.timeserie;

import org.series.InvalidTimeSerieException;
import org.series.TimeTools;
import org.series.imputation.ImputationStrategy;
import org.series.timegrid.TimeGrid;

import java.util.Arrays;
import java.util.Comparator;

public class TimeSeriesAligner {

    private final ImputationStrategy imputationStrategy;

    public TimeSeriesAligner(ImputationStrategy imputationStrategy) {
        if (imputationStrategy == null) {
            throw new IllegalArgumentException("ImputationStrategy cannot be null");
        }
        this.imputationStrategy = imputationStrategy;
    }

    public DoubleTimeSerie align(Observation[] values, TimeGrid targetGrid) throws InvalidTimeSerieException {
        if (values == null || targetGrid == null) {
            throw new IllegalArgumentException("Inputs cannot be null");
        }
        if (values.length == 0) {
            throw new InvalidTimeSerieException("Time series empty");
        }

        Observation[] observations = values.clone();
        Arrays.sort(observations, Comparator.comparing(Observation::getZonedDateTime));

        int m = observations.length;
        long[] sortedRawDates = new long[m];
        double[] sortedValues = new double[m];

        for (int i = 0; i < m; i++) {
            Observation obs = observations[i];
            sortedRawDates[i] = TimeTools.fromZonedDateTimeToIndex(obs.getZonedDateTime());
            sortedValues[i] = obs.getDoubleValue();
        }

        double[] alignedValues = imputationStrategy.alignAndImpute(sortedRawDates, sortedValues, targetGrid);

        return new ImmutableDoubleTimeSerie(targetGrid, alignedValues);
    }
}