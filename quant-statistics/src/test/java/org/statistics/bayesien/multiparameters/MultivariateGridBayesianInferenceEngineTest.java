package org.statistics.bayesien.multiparameters;

import org.math.Integrator.multivariate.RecursiveProductRuleIntegrator;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.statistics.bayesien.multiparameters.prior.MultivariateUniformPrior;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;


import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.*;

class MultivariateBayesianInferenceTest {

    private Vector syntheticData;
    private ParametricModel<?> normalModel;

    @BeforeEach
    void setUp() {
        // Données synthétiques (vraie moyenne = 1.0, vrai écart-type = 0.2)
        syntheticData = new ArrayVector(new double[]{0.95, 1.05, 1.0, 0.98, 1.02});

        normalModel = new ParametricModel<>() {
            @Override
            public Object createDistribution(Vector params) { return null; }

            @Override
            public boolean isValidParameterSet(Vector params) {
                if (params.size() != 2) return false;
                double sigma = params.getValue(1);
                return sigma > 0.0 && sigma < 10.0;
            }

            @Override
            public double logLikelihood(double x, Vector params) {
                double mu = params.getValue(0);
                double sigma = params.getValue(1);
                double variance = sigma * sigma;
                double diff = x - mu;
                return -0.5 * Math.log(2.0 * Math.PI * variance) - (diff * diff) / (2.0 * variance);
            }

            @Override
            public double mean(Vector theta) { return theta.getValue(0); }

            @Override
            public double variance(Vector theta) { return Math.pow(theta.getValue(1), 2); }
        };
    }

    @Test
    void testMultivariateGridBayesianInference2D() {


        // Utilisation du prior uniforme multivarié tel que demandé
        MultivariateBayesianPrior uniformLikePrior = new MultivariateUniformPrior(
                ArrayVector.of(new double[]{-3.0, 0.1}),
                ArrayVector.of(new double[]{5.0, 3.0})
        );

        Vector thetaMin = new ArrayVector(new double[]{-2.0, 0.1});
        Vector thetaMax = new ArrayVector(new double[]{4.0, 2.0});

        RecursiveProductRuleIntegrator integrator = new RecursiveProductRuleIntegrator(15);
        MultivariateGridBayesianInferenceEngine engine = new MultivariateGridBayesianInferenceEngine(
                normalModel, integrator, thetaMin, thetaMax
        );

        ToDoubleFunction<Vector> posteriorDensity = engine.computePosteriorDensity(uniformLikePrior, syntheticData);
        assertNotNull(posteriorDensity, "La densité a posteriori ne doit pas être nulle.");

        // 1. Définition des points de test
        Vector optimumTheta = new ArrayVector(new double[]{1.0, 0.2});      // Proche de la vraie moyenne et écart-type
        Vector subOptimalTheta = new ArrayVector(new double[]{1.5, 0.5});   // Moins probable mais dans l'espace
        Vector absurdTheta = new ArrayVector(new double[]{3.0, 1.5});       // Aberrant par rapport aux données
        Vector outOfBoundsTheta = new ArrayVector(new double[]{-5.0, 0.05}); // Hors des bornes d'intégration thetaMin/thetaMax

        // 2. Évaluation des densités
        double densityAtOptimum = posteriorDensity.applyAsDouble(optimumTheta);
        double densityAtSubOptimal = posteriorDensity.applyAsDouble(subOptimalTheta);
        double densityAtAbsurd = posteriorDensity.applyAsDouble(absurdTheta);
        double densityOutOfBounds = posteriorDensity.applyAsDouble(outOfBoundsTheta);

        // 3. Assertions multiples et croisées
        // A. La densité doit être strictement positive sur le point optimal
        assertTrue(densityAtOptimum > 0.0,
                "La densité a posteriori doit être strictement positive au point optimal.");

        // B. Hiérarchie des vraisemblances : Optimum > Sous-optimal > Aberrant
        assertTrue(densityAtOptimum > densityAtSubOptimal,
                "La densité au point optimal [1.0, 0.2] doit être supérieure à un point sous-optimal [1.5, 0.5].");

        assertTrue(densityAtSubOptimal > densityAtAbsurd,
                "La densité d'un point sous-optimal doit être supérieure à un point aberrant [3.0, 1.5].");

        assertTrue(densityAtOptimum > densityAtAbsurd,
                "La densité a posteriori doit être maximale près de la vraie valeur des données.");

        // C. Gestion des contraintes de domaine (Hors-bornes -> Densité nulle)
        assertEquals(0.0, densityOutOfBounds, 1e-9,
                "La densité pour un paramètre hors des bornes d'intégration (thetaMin/thetaMax) doit être exactement nulle.");
    }

}