package org.statistics.hypothesis;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ChiSquaredDistribution;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;
import org.statistics.estimator.parametric.mle.ChiSquareTestResult;

import java.util.Arrays;
import java.util.Objects;

/**
 * Chi-Square goodness-of-fit test for parametric models.
 */
public class ChiSquareGoodnessOfFitTest {

    private static final Logger logger = LoggerFactory.getLogger(ChiSquareGoodnessOfFitTest.class);

    private final ContinuousDistribution distribution;
    // utiliser l'implémentation: GoodnessOfFitAnalyzer
    public ChiSquareGoodnessOfFitTest(ContinuousDistribution distribution) {
        this.distribution = Objects.requireNonNull(distribution, "Distribution cannot be null");
    }

    /**
     * Computes the Chi-Square goodness-of-fit test statistic and p-value.
     */
    public ChiSquareTestResult computeStatistic(Vector binBounds, int[] observedCounts, int estimatedParametersCount) {
        logger.trace("Entering computeStatistic with binBounds={}, observedCounts={}, estimatedParametersCount={}",
                binBounds, Arrays.toString(observedCounts), estimatedParametersCount);

        int k = observedCounts.length;
        int degreesOfFreedom = k - 1 - estimatedParametersCount;

        validateInputs(binBounds, observedCounts, degreesOfFreedom);

        double totalN = 0;
        for (int count : observedCounts) {
            if (count < 0) {
                throw new IllegalArgumentException("Observed count cannot be negative: " + count);
            }
            totalN += count;
        }
        if (totalN == 0) {
            throw new IllegalArgumentException("Total sample size must be strictly positive");
        }

        double chiSquareStat = 0.0;
        for (int j = 0; j < k; j++) {
            double pJ = distribution.cdf(binBounds.getValue(j + 1)) - distribution.cdf(binBounds.getValue(j));

            if (pJ <= 0.0) {
                logger.warn("Bin {} has non-positive probability ({}), check bin bounds.", j, pJ);
                throw new IllegalArgumentException("Bin probability must be strictly positive");
            }

            double expected = totalN * pJ;

            if (expected < 5.0) {
                logger.warn("Expected frequency in bin {} is low ({}), asymptotic chi-square approximation may be poor.", j, expected);
            }

            double diff = observedCounts[j] - expected;
            chiSquareStat += (diff * diff) / expected;
        }

        ChiSquaredDistribution chiSqDist = new ChiSquaredDistribution(degreesOfFreedom);
        double pValue = 1 - chiSqDist.cdf(chiSquareStat);

        ChiSquareTestResult result = new ChiSquareTestResult(chiSquareStat, degreesOfFreedom, pValue);
        logger.debug("Exiting computeStatistic with result: statistic={}, df={}, pValue={}",
                chiSquareStat, degreesOfFreedom, pValue);
        return result;
    }

    private void validateInputs(Vector bounds, int[] counts, int degreesOfFreedom) {

        if (bounds == null || counts == null) {
            throw new IllegalArgumentException("Inputs cannot be null");
        }

        if (bounds.size() != counts.length + 1) {
            throw new IllegalArgumentException("Bin bounds length must be counts.length + 1");
        }

        if (degreesOfFreedom <= 0) {
            logger.error("Degrees of freedom must be strictly positive (df={})", degreesOfFreedom);
            throw new IllegalArgumentException("Insufficient bins for the number of estimated parameters.");
        }

    }
}