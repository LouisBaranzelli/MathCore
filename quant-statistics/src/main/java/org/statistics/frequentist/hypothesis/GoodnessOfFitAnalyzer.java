package org.statistics.frequentist.hypothesis;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;
import org.statistics.frequentist.estimator.parametric.mle.ChiSquareTestResult;
import org.statistics.tools.BinsFactory;

import java.util.Objects;

/**
 * High-level runner for parametric goodness-of-fit tests on raw samples.
 */
public class GoodnessOfFitAnalyzer {

    private static final Logger logger = LoggerFactory.getLogger(GoodnessOfFitAnalyzer.class);
    private static final int MIN_SAMPLE_SIZE = 30;

    /**
     * Exécute un test du Chi-2 à partir d'un échantillon brut en déterminant
     * automatiquement le nombre de bacs (k = n / 10).
     *
     * @param sample                   Échantillon de données brutes
     * @param distribution             Modèle de distribution théorique
     * @param estimatedParametersCount Nombre de paramètres du modèle estimés à partir de l'échantillon (s).
     *                                 - 0 si les paramètres sont fixés a priori (ex: test d'une loi N(0,1) théorique).
     *                                 - > 0 si les paramètres ont été estimés sur les données (ex: moyenne/écart-type estimés).
     *                                   Note mathématique (Chernoff-Lehmann, 1954) : si les paramètres sont estimés sur les
     *                                   données brutes et non sur les effectifs groupés, la statistique Q ne suit pas rigoureusement
     *                                   une loi du Chi-2 exacte, mais l'utilisation de k - 1 - s reste une approximation standard conservative.
     */
    public static ChiSquareTestResult testNormalityOrModel(Vector sample, ContinuousDistribution distribution, int estimatedParametersCount) {
        logger.trace("Entering GoodnessOfFitAnalyzer with sampleSize={}, estimatedParametersCount={} for : {}",
                sample != null ? sample.size() : null, estimatedParametersCount, distribution.getLabel());

        Objects.requireNonNull(sample, "Sample cannot be null");
        Objects.requireNonNull(distribution, "Distribution cannot be null");

        int n = sample.size();
        if (n < MIN_SAMPLE_SIZE) {
            throw new IllegalArgumentException("Sample size (" + n + ") is too small for asymptotic chi-square test. Minimum required: " + MIN_SAMPLE_SIZE);
        }

        int k = Math.max(5, n / 10);

        Vector binBounds = BinsFactory.createEqualProbabilityBins(distribution, k);
        int[] observedCounts = computeObservedCounts(sample, binBounds);

        ChiSquareGoodnessOfFitTest testEngine = new ChiSquareGoodnessOfFitTest(distribution);
        ChiSquareTestResult result = testEngine.computeStatistic(binBounds, observedCounts, estimatedParametersCount);

        logger.debug("Exiting testNormalityOrModel with statistic={}, pValue={}", result.getStatistic(), result.getPValue());
        return result;
    }

    private static int[] computeObservedCounts(Vector sample, Vector binBounds) {
        int k = binBounds.size() - 1;
        int[] counts = new int[k];

        for (int i = 0; i < sample.size(); i++) {
            double value = sample.getValue(i);
            int binIndex = findBinIndex(value, binBounds);
            counts[binIndex]++;
        }
        return counts;
    }

    private static int findBinIndex(double value, Vector bounds) {
        int k = bounds.size() - 1;
        for (int j = 0; j < k; j++) {
            double lower = bounds.getValue(j);
            double upper = bounds.getValue(j + 1);
            if (value >= lower && value < upper) {
                return j;
            }
        }
        return k - 1;
    }
}