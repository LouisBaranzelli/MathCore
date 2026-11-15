package org.statistics.bayesien.multiparameters.mcmc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.bayesien.multiparameters.MultivariateBayesianPrior;
import org.statistics.bayesien.multiparameters.ParameterSampleMatrix;
import org.statistics.bayesien.multiparameters.prior.MultivariateUniformPrior;
import org.statistics.frequentist.estimator.parametric.model.NormalModel;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import static org.junit.jupiter.api.Assertions.assertTrue;

class NormalModelBayesianComparisonTest {

    private ParametricModel<?> normalModel;
    private MultivariateBayesianPrior flatPrior;
    private Vector observedData;

    @BeforeEach
    void setUp() {
        normalModel = new NormalModel();

        // Données synthétiques issues d'une loi normale (vrai mu = 2.0, vrai sigma = 1.5)
        observedData = new ArrayVector(new double[]{1.8, 2.2, 0.9, 3.1, 1.5, 2.0, 2.5, 1.2, 1.9, 2.1});
        Vector thetaMin = new ArrayVector(new double[]{-10.0, 0.001});
        Vector thetaMax = new ArrayVector(new double[]{10.0, 10.0});
        flatPrior = new MultivariateUniformPrior(thetaMin, thetaMax);
    }

    @Test
    void testMcmcConvergenceOnNormalModel2D() {
        // Configuration de la proposition MCMC (marche aléatoire gaussienne)
        GaussianRandomWalkProposal proposal = new GaussianRandomWalkProposal(0.05);

        // 20 000 itérations, 5 000 de burn-in
        MetropolisHastingsMcmcEngine mcmcEngine = new MetropolisHastingsMcmcEngine(
                normalModel, proposal, 20000, 5000
        );

        Vector initialTheta = new ArrayVector(new double[]{0.0, 1.0}); // Point de départ arbitraire
        ParameterSampleMatrix samples = mcmcEngine.sample(flatPrior, observedData, initialTheta);

        // Assertions structurelles
        assertTrue(samples.getNumSamples() == 15000, "Should retain exactly 15000 samples after burn-in");
        assertTrue(samples.getNumParams() == 2, "Parameter dimension must be 2 (mu and sigma)");

        // Calcul des moyennes a posteriori estimées par la chaîne MCMC
        Vector estimatedMean = computeMean(samples);
        double estimatedMu = estimatedMean.getValue(0);
        double estimatedSigma = estimatedMean.getValue(1);

        // Validation que l'estimation converge autour des moyennes empiriques des données observées
        // (La moyenne empirique de observedData est autour de 1.92)
        assertTrue(estimatedMu > 1.5 && estimatedMu < 2.5, "Estimated mu should be close to data mean");
        assertTrue(estimatedSigma > 0.5 && estimatedSigma < 2.5, "Estimated sigma should be within plausible bounds");
    }

    private Vector computeMean(ParameterSampleMatrix matrix) {
        int rows = matrix.getNumSamples();
        int cols = matrix.getNumParams();
        double[] means = new double[cols];

        for (int j = 0; j < cols; j++) {
            double sum = 0;
            for (int i = 0; i < rows; i++) {
                sum += matrix.getSampleAt(i).getValue(j);
            }
            means[j] = sum / rows;
        }
        return new ArrayVector(means);
    }
}