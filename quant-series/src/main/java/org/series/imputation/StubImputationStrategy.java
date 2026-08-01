package org.series.imputation;

import org.series.timegrid.TimeGrid;

public class StubImputationStrategy implements ImputationStrategy {
    @Override
    public double[] alignAndImpute(long[] sortedDates, double[] rawValues, TimeGrid targetGrid) {
        double[] alignmentMock = new double[targetGrid.size()];
        if (rawValues.length > 0) {
            java.util.Arrays.fill(alignmentMock, rawValues[0]);
        }
        return alignmentMock;
    }
}
