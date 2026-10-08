package org.statistics.distributions.monovariate.continuous;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.data.Sample;
import org.statistics.descriptive.QQPlotData;
import org.statistics.distributions.monovariate.Distribution;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public interface ContinuousDistribution extends Distribution {

    default double survivalFunction(double x) {
        return 1.0 - cdf(x);
    }

    /**
     * Calcule la densite de probabilite f(x).
     */
    double density(double x);

    /**
     * Calcule la fonction quantile F^-1(p).
     */
    double inverseCdf(double p);

    default QQPlotData createQQPlotData(Vector empiricalData) {
        int n = empiricalData.size();

        double[] theoreticalQuantiles = new double[n];
        for (int i = 0; i < n; i++) {
            double p = (i + 0.5) / n; // Plotting position (Hazen)
            theoreticalQuantiles[i] = inverseCdf(p);
        }

        return new QQPlotData(
                new ArrayVector(theoreticalQuantiles),
                empiricalData,
                "QQ-Plot",
                "Quantiles Théoriques",
                "Quantiles Empiriques"
        );
    }

    default double getSample(RandomGenerator random){
        double probability = random.nextDouble();
        return inverseCdf(probability);
    }
}
