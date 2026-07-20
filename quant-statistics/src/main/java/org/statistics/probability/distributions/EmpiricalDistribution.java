package org.statistics.probability.distributions;

import org.math.vector.Vector;
import org.statistics.probability.definitions.Sample;

import java.util.Arrays;

public class EmpiricalDistribution implements Distribution{

    private final Sample sample;

    public EmpiricalDistribution(Sample sample){

        if (sample == null) {
            throw new IllegalArgumentException("Sample cannot be null");
        }
        if (sample.size() < 1) {
            throw new IllegalArgumentException("Sample size must be at least 2");
        }
        this.sample = sample;
    }

    @Override
    public double cdf(double x) {
        int n = sample.size();

        if (x < sample.getSorted(0)) {
            return 0.0;
        }
        if (x >= sample.getSorted(n - 1)) {
            return 1.0;
        }

        int index = sample.binarySearch(x);

        if (index >= 0) {
            // x a été trouvé. On s'assure d'aller au dernier doublon égal à x
            while (index + 1 < n && sample.getSorted(index + 1) == x) {
                index++;
            }
            return (double) (index + 1) / n;
        } else {
            int insertionPoint = -index - 1;
            return (double) insertionPoint / n;
        }
    }

    @Override
    public double inverseCdf(double p) {
            if (p < 0.0 || p > 1.0) {
                throw new IllegalArgumentException("Probability must be in [0, 1], got " + p);
            }
            int n = sample.size();
            if (p == 0.0) {
                return sample.getSorted(0);
            }
            if (p == 1.0) {
                return sample.getSorted(n - 1);
            }
            int index = (int) Math.ceil(p * n) - 1;
            // Guard-rail pour éviter -1 si p est un très petit double positif proche de 0
            int safeIndex = Math.max(0, index);
            return sample.getSorted(safeIndex);
    }

}
