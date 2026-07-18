package org.series.imputation;

import org.series.InvalidTimeSerieException;
import org.series.timegrid.TimeGrid;

public class LastObservationCarriedForwardStrategy implements ImputationStrategy {

    // La tolérance exprime désormais la durée maximale (ex: en secondes) 
    // pendant laquelle on accepte de propager un vieux prix.
    private final long maxTimeTolerance;
    private final double maxPercentMissingValue;

    public LastObservationCarriedForwardStrategy(long maxTimeTolerance, double maxPercentMissingValue) {
        if (maxTimeTolerance <= 0) {
            throw new IllegalArgumentException("Tolerance must be strictly positive");
        }
        if (maxPercentMissingValue > 1 || maxPercentMissingValue < 0){
            throw new IllegalArgumentException("Percent missing values must be included between [0; 1]");

        }
        this.maxPercentMissingValue = maxPercentMissingValue;
        this.maxTimeTolerance = maxTimeTolerance;
    }

    @Override
    public double[] alignAndImpute(long[] sortedDates, double[] rawValues, TimeGrid targetGrid) throws InvalidTimeSerieException {
        int counterMissingValues = 0;
        if (sortedDates == null || rawValues == null || targetGrid == null) {
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        if (sortedDates.length != rawValues.length) {
            throw new IllegalArgumentException("Dates and values arrays must have the same length");
        }

        int targetSize = targetGrid.size();
        double[] output = new double[targetSize];

        int rawIdx = 0;
        int rawLength = sortedDates.length;

        double lastKnownValue = Double.NaN;
        long lastKnownDate = Long.MIN_VALUE;

        for (int i = 0; i < targetSize; i++) {
            long targetTime = targetGrid.getTimeStamp(i);

            if (i > 0 && targetTime <= targetGrid.getTimeStamp(i - 1)) {
                throw new IllegalStateException("Target grid must be strictly increasing");
            }

            while (rawIdx < rawLength && sortedDates[rawIdx] <= targetTime) {
                if (rawIdx > 0 && sortedDates[rawIdx] < sortedDates[rawIdx - 1]) {
                    throw new IllegalArgumentException("rawDates must be sorted");
                }
                lastKnownValue = rawValues[rawIdx];
                lastKnownDate = sortedDates[rawIdx];
                rawIdx++;
            }

            if (Long.valueOf(lastKnownDate).equals(Long.MIN_VALUE)) {
                output[i] = Double.NaN;
                counterMissingValues++;
                checkMissingValue(counterMissingValues, targetGrid.size());
            } else if ((targetTime - lastKnownDate) > maxTimeTolerance) {
                output[i] = Double.NaN;
                counterMissingValues++;
                checkMissingValue(counterMissingValues, targetGrid.size());
            } else {
                output[i] = lastKnownValue;
            }
        }

        return output;
    }

    private void checkMissingValue(int counter, int totalSize) throws InvalidTimeSerieException {
        if (maxPercentMissingValue == 1){
            return;
        }
        if (counter / (double) totalSize > maxPercentMissingValue){
            throw new InvalidTimeSerieException(
                    String.format(
                            "Imputation halted by circuit-breaker: missing values ratio exceeded max threshold. " +
                                    "[Missing count: %d/%d | Current ratio: %.2f%% | Max allowed: %.2f%%]",
                            counter,
                            totalSize,
                            ((double) counter / totalSize) * 100,
                            maxPercentMissingValue * 100
                    )
            );
        }
    }
}