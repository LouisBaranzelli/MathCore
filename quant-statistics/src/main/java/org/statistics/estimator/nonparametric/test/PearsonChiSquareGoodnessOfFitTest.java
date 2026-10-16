package org.statistics.estimator.nonparametric.test;

import org.math.probability.distribution.ChiSquaredDistribution;
import org.statistics.data.CategoricalMultinomialData;
import org.statistics.data.CategoricalProbabilityDistribution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

public class PearsonChiSquareGoodnessOfFitTest {

    private static final Logger log = LoggerFactory.getLogger(PearsonChiSquareGoodnessOfFitTest.class);

    /**
     * Exécute le test du Chi-deux de Pearson.
     *
     * @param observedData              Données multinomiales observées
     * @param theoreticalDist           Distribution de probabilité théorique
     * @param estimatedParametersCount  Nombre de paramètres estimés à partir de l'échantillon (m)
     */
    public GoodnessOfFitResult evaluate(CategoricalMultinomialData observedData,
                                        CategoricalProbabilityDistribution theoreticalDist,
                                        int estimatedParametersCount) {

       ChiSquaredDistribution distributionFactory;


        if (observedData == null || theoreticalDist == null) {
            log.error("Observed data or theoretical distribution cannot be null.");
            throw new IllegalArgumentException("Observed data and theoretical distribution cannot be null.");
        }

        if (estimatedParametersCount < 0) {
            log.error("Estimated parameters count cannot be negative: {}", estimatedParametersCount);
            throw new IllegalArgumentException("Estimated parameters count must be non-negative.");
        }

        int k = observedData.getCategoriesCount();
        int degreesOfFreedom = k - 1 - estimatedParametersCount;

        if (degreesOfFreedom <= 0) {
            log.error("Invalid degrees of freedom (K - 1 - m <= 0). K = {}, m = {}", k, estimatedParametersCount);
            throw new IllegalStateException("Degrees of freedom must be strictly positive (K - 1 - m > 0).");
        }

        int n = observedData.getTotalCount();
        double chiSquareStat = 0.0;
        Map<String, Double> contributions = new LinkedHashMap<>();

        log.debug("Running Pearson Chi-Square test with K = {}, n = {}, estimated parameters m = {}.", k, n, estimatedParametersCount);

        for (Map.Entry<String, Integer> entry : observedData.getCountsByCategory().entrySet()) {
            String category = entry.getKey();
            int observedCount = entry.getValue();
            double p0 = theoreticalDist.getProbability(category);

            double expectedCount = n * p0;

            if (expectedCount < 5.0) {
                log.warn("Expected frequency for category '{}' is low (E = {} < 5). Asymptotic approximation may be unreliable.", category, expectedCount);
            }

            if (expectedCount == 0.0) {
                log.error("Expected count is zero for category '{}'.", category);
                throw new ArithmeticException("Expected count cannot be zero for category: " + category);
            }

            double diff = observedCount - expectedCount;
            double contribution = (diff * diff) / expectedCount;

            contributions.put(category, contribution);
            chiSquareStat += contribution;
        }

        ChiSquaredDistribution chiSqDist = new ChiSquaredDistribution(degreesOfFreedom);

        double pValue = 1.0 - chiSqDist.cdf(chiSquareStat);

        log.info("Pearson Chi-Square test completed: T = {}, df = {}, p-value = {}", chiSquareStat, degreesOfFreedom, pValue);

        return new GoodnessOfFitResult(n, chiSquareStat, degreesOfFreedom, pValue, contributions);
    }

    /**
     * Surcharge pour le cas standard où les paramètres (theoreticalDist) sont  fixés a priori (m = 0),
     * c a d: créer des categorie sur la base des observedData pour definir une distribution et apres
     * définir des categories
     */
    public GoodnessOfFitResult evaluate(CategoricalMultinomialData observedData,
                                        CategoricalProbabilityDistribution theoreticalDist) {
        return evaluate(observedData, theoreticalDist, 0);
    }
}