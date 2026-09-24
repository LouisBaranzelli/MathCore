package org.estimator.parametric.mle;

import org.data.Sample;
import org.distributions.monovariate.continuous.NormalDistribution;
import org.estimator.parametric.model.NormalModel;
import org.estimator.parametric.model.ParametricModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.optimizer.ConvergenceException;
import org.math.optimizer.MultivariateOptimizer;
import org.math.optimizer.NelderMeadOptimizer;
import org.math.vector.ArrayVector;

import static org.junit.jupiter.api.Assertions.*;

class MaximumLikelihoodEstimatorTest {

    private static final double EPSILON = 1e-2;

    @Test
    @DisplayName("Devrait estimer la moyenne (mu) et l'écart-type (sigma) d'une distribution normale")
    void shouldEstimateNormalDistributionParametersFromDataset() {
        // Jeu de données synthétique avec mu = 10.0 et sigma = 2.0
        // (x̄ = 10.0, s^2 = 4.0 -> s = 2.0)
        Sample dataset = Sample.of(6.0, 7.0, 8.0, 9.0, 10.0, 10.0, 11.0, 12.0, 13.0, 14.0);

        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-4, 200);
        MaximumLikelihoodEstimator estimator = new MaximumLikelihoodEstimator(optimizer);
        ParametricModel<NormalDistribution> parametricModel = new NormalModel();
        LikelihoodResult result = estimator.estimate(parametricModel, dataset, ArrayVector.of(1, 1));

        assertNotNull(result, "Le résultat de l'estimation ne doit pas être nul");
        assertTrue(result.optimizationResult().converged(), "L'optimisation du MLE aurait dû converger");

        // En MLE pour une loi normale :
        // mu_MLE = moyenne empirique = 10.0
        // sigma_MLE = sqrt(variance biaisée) = sqrt(20.0 / 10) = sqrt(2.0) ≈ 2.4494
        NormalDistribution normalDistribution = parametricModel.createDistribution(result.theta());
        assertEquals(10.0, normalDistribution.getMu(), EPSILON, "La moyenne estimée (mu) doit être proche de 10.0");
        assertEquals(2.4494,normalDistribution.getSigma(), EPSILON, "L'écart-type estimé (sigma) doit être correct");
    }

    @Test
    @DisplayName("Devrait lever une exception si le jeu de données est vide ou nul")
    void shouldThrowExceptionForInvalidDataset() {
        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-4, 200);
        MaximumLikelihoodEstimator estimator = new MaximumLikelihoodEstimator(optimizer);
        ParametricModel<NormalDistribution> parametricModel = new NormalModel();
        assertThrows(IllegalArgumentException.class, () -> {
            LikelihoodResult result = estimator.estimate(parametricModel, Sample.of(), ArrayVector.of(1, 1));
        });
        assertThrows(ConvergenceException.class, () -> {
            LikelihoodResult result = estimator.estimate(parametricModel, Sample.of(1, 2, 3, 4), ArrayVector.of(0, 0, 0));
        });
    }
}