package org.statistics.descriptive;

import org.math.vector.Vector;

public record QQPlotData(
        Vector theoreticalQuantiles,
        Vector empiricalQuantiles,
        String title,
        String xLabel,
        String yLabel
) {
    public int size() {
        return theoreticalQuantiles.size();
    }
}