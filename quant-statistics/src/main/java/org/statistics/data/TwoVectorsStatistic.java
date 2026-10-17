package org.statistics.data;

import org.math.vector.Vector;

@FunctionalInterface
public interface TwoVectorsStatistic {
    double compute(Vector vectorA, Vector vectorB);
}
