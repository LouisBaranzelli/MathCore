package org.statistics.statistics.estimator.parametric.model;

import org.statistics.estimator.parametric.model.ChiSquaredModel;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ChiSquaredModelTest {

    private static final double TOLERANCE = 1e-6;

    @Test
    void testValidParameterSet() {
        ChiSquaredModel model = new ChiSquaredModel();

        Vector validParams = new ArrayVector(new double[]{3.0});
        Vector invalidDecimal = new ArrayVector(new double[]{2.5});
        Vector invalidNegative = new ArrayVector(new double[]{-1.0});
        Vector invalidSize = new ArrayVector(new double[]{2.0, 3.0});

        assertTrue(model.isValidParameterSet(validParams), "Un paramètre k entier positif doit être valide.");
        assertTrue(!model.isValidParameterSet(invalidDecimal), "Un paramètre non entier (k=2.5) doit être invalide.");
        assertTrue(!model.isValidParameterSet(invalidNegative), "Un paramètre négatif doit être invalide.");
        assertTrue(!model.isValidParameterSet(invalidSize), "Un vecteur de taille différente de 1 doit être invalide.");
    }

    @Test
    void testMeanAndVariance() {
        ChiSquaredModel model = new ChiSquaredModel();
        Vector params = new ArrayVector(new double[]{4.0}); // k = 4

        double expectedMean = 4.0;
        double expectedVariance = 8.0; // 2 * k

        assertTrue(Math.abs(expectedMean - model.mean(params)) < TOLERANCE,
                "L'espérance d'un Chi-deux à k=4 doit être égale à 4.");
        assertTrue(Math.abs(expectedVariance - model.variance(params)) < TOLERANCE,
                "La variance d'un Chi-deux à k=4 doit être égale à 8.");
    }

    @Test
    void testLogLikelihood() {
        ChiSquaredModel model = new ChiSquaredModel();
        Vector params = new ArrayVector(new double[]{2.0}); // k = 2 (équivalent exponentiel)

        // Pour k = 2, log-densité à x = 2.0 -> log(0.5 * exp(-1)) = log(0.5) - 1 ≈ -0.693147 - 1 = -1.693147
        double x = 2.0;
        double expectedLogLikelihood = Math.log(0.5 * Math.exp(-1.0));
        double actualLogLikelihood = model.logLikelihood(x, params);

        assertTrue(Math.abs(expectedLogLikelihood - actualLogLikelihood) < TOLERANCE,
                () -> String.format("Log-vraisemblance incorrecte : attendu %.6f, obtenu %.6f", expectedLogLikelihood, actualLogLikelihood));
    }

    @Test
    void testCreateDistribution() {
        ChiSquaredModel model = new ChiSquaredModel();
        Vector params = new ArrayVector(new double[]{5.0});

        boolean distributionCreated = false;
        try {
            var dist = model.createDistribution(params);
            if (dist != null && dist.getDegreesOfFreedom() == 5) {
                distributionCreated = true;
            }
        } catch (Exception e) {
            distributionCreated = false;
        }

        assertTrue(distributionCreated, "La création de la ChiSquaredDistribution via le modèle doit réussir pour k=5.");
    }
}