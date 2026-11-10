package org.statistics.bayesien;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.Integrator.NumericalIntegrator;
import org.math.Integrator.SimpsonIntegrator;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;
import org.statistics.distributions.monovariate.continuous.GridDiscretizedDistribution;
import org.statistics.distributions.monovariate.continuous.UniformDistribution;
import org.statistics.frequentist.estimator.parametric.model.BernoulliModel;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import static org.junit.jupiter.api.Assertions.*;

class GridBayesianInferenceEngineTest {

    private NumericalIntegrator integrator;

    @BeforeEach
    void setUp() {
        integrator = new SimpsonIntegrator(1000);
    }

    @Test
    @DisplayName("Should correctly infer Bernoulli success probability p using Beta prior")
    void testBernoulliBetaPosteriorInference() {
        // Le paramètre theta unique représente p ∈ [0, 1]
        ParametricModel<?> bernoulliModel = new BernoulliModel();

        // 2. Données observées : 7 succès (1.0), 3 échecs (0.0)
        Vector data = ArrayVector.of(1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0.0, 0.0, 0.0);

        // 3. Prior : Uniforme sur [0, 1], ce qui équivaut à un Beta(1, 1)
        ContinuousDistribution distribution = new UniformDistribution(0, 1);

        BayesianPrior uniformPrior = new BayesianPrior(distribution);

        GridBayesianInferenceEngine engine = new GridBayesianInferenceEngine(
                bernoulliModel, integrator, 0.0, 1.0, 500
        );

        ContinuousDistribution posterior = engine.computePosterior(uniformPrior, data);

        assertNotNull(posterior, "Posterior distribution should not be null");

        // 6. Validation analytique :
        // Théorie : Prior Beta(1,1) + 7 succès + 3 échecs => A posteriori exact = Beta(8, 4)
        // Pour une loi Beta(alpha, beta), l'espérance mathématique (moyenne) est alpha / (alpha + beta)
        // Ici : 8 / (8 + 4) = 8 / 12 = 2/3 ≈ 0.666667

        double expectedMean = 8.0 / 12.0;

        // On vérifie que la densité a son pic ou que l'espérance/médiane numérique est très proche de la théorie
        double computedMedian = posterior.inverseCdf(0.5); // Médiane de la distribution a posteriori

        // Tolérance serrée grâce à la finesse de la grille (500 points) et Simpson
        assertEquals(expectedMean, computedMedian, 0.01, "The median of the posterior should match the theoretical Beta(8,4) expectation");
    }
}