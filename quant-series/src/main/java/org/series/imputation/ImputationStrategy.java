package org.series.imputation;

import org.series.InvalidTimeSerieException;
import org.series.timegrid.TimeGrid;

public interface ImputationStrategy {

    double[] alignAndImpute(long[] rawDates, double[] rawValues, TimeGrid targetGrid) throws InvalidTimeSerieException;
}