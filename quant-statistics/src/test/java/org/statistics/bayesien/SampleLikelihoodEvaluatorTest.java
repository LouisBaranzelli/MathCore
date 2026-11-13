package org.statistics.bayesien;

import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.bayesien.monoparameter.SampleLikelihoodEvaluator;
import org.statistics.frequentist.estimator.parametric.model.NormalModel;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SampleLikelihoodEvaluatorTest {

    @Test
    void shouldEvaluateNormalLogLikelihoodCorrectlyWithVector() {
        // Données d'exemple stockées dans un Vector
        Vector data = ArrayVector.of(1.0, 2.0, 3.0);

        // Paramètres : mu = 2.0, sigma = 1.0
        Vector params = ArrayVector.of(2.0, 1.0);

        NormalModel model = new NormalModel();

        double logLikelihood = SampleLikelihoodEvaluator.evaluateLogLikelihood(data, model, params);

        // Calcul analytique attendu pour x = {1, 2, 3} avec N(mu=2, sigma=1) :
        // logPdf(1) = -ln(1) - 0.5*ln(2pi) - 0.5*(1-2)^2 / 1 = -0.5*ln(2pi) - 0.5
        // logPdf(2) = -ln(1) - 0.5*ln(2pi) - 0.5*(2-2)^2 / 1 = -0.5*ln(2pi)
        // logPdf(3) = -ln(1) - 0.5*ln(2pi) - 0.5*(3-2)^2 / 1 = -0.5*ln(2pi) - 0.5
        // Somme = -1.5*ln(2pi) - 1.0
        double expected = 3.0 * (-0.5 * Math.log(2.0 * Math.PI)) - 1.0;

        assertTrue(Math.abs(logLikelihood - expected) < 1e-9);
    }
}