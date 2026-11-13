package org.statistics.bayesien.multiparameters.prior;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.multivariate.Covariance;
import org.statistics.multivariate.CovarianceMatrix;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MultivariateNormalPriorTest {

    private Vector meanVector;
    private CovarianceMatrix covarianceMatrix;
    private MultivariateNormalPrior normalPrior;

    @BeforeEach
    void setUp() {
        meanVector = new ArrayVector(new double[]{1.0, 2.0});

        Vector param1Samples = new ArrayVector(new double[]{1.2, 0.8, 1.5, 0.9, 1.1});
        Vector param2Samples = new ArrayVector(new double[]{2.1, 1.8, 2.3, 2.0, 2.2});

        covarianceMatrix = Covariance.of(new Vector[]{param1Samples, param2Samples});

        normalPrior = new MultivariateNormalPrior(meanVector, covarianceMatrix);
    }

    @Test
    void testGetDimension() {
        assertEquals(2, normalPrior.getDimension(), "La dimension du prior doit être égale à 2.");
    }

    @Test
    void testConstructorNullArguments() {
        assertThrows(NullPointerException.class, () ->
                        new MultivariateNormalPrior(null, covarianceMatrix),
                "Doit lever une exception si le vecteur moyenne est null."
        );

        assertThrows(NullPointerException.class, () ->
                        new MultivariateNormalPrior(meanVector, null),
                "Doit lever une exception si la matrice de covariance est null."
        );
    }

    @Test
    void testConstructorDimensionMismatch() {
        // Vecteur moyenne de dimension 3 incompatible avec la matrice de covariance 2x2
        Vector invalidMean = new ArrayVector(new double[]{1.0, 2.0, 3.0});

        assertThrows(IllegalArgumentException.class, () ->
                        new MultivariateNormalPrior(invalidMean, covarianceMatrix),
                "Doit lever une exception si les dimensions de la moyenne et de la covariance divergent."
        );
    }

    @Test
    void testDensityEvaluationAtMean() {
        // Au point moyen, diff = [0, 0], la forme quadratique est nulle.
        double expectedDensity = Math.exp(0.0) / 1.5; // Basé sur le facteur de normalisation du stub
        double actualDensity = normalPrior.density(meanVector);

        assertEquals(expectedDensity, actualDensity, 1e-9, "La densité au point moyen est incorrecte.");
    }

    @Test
    void testDensityEvaluationAtDifferentPoint() {
        // Point theta proche de la moyenne [1.0, 2.0]
        Vector thetaClose = new ArrayVector(new double[]{1.2, 2.2});

        // Point theta très éloigné de la moyenne
        Vector thetaFar = new ArrayVector(new double[]{4.0, 5.0});

        double densityAtMean = normalPrior.density(meanVector);
        double densityClose = normalPrior.density(thetaClose);
        double densityFar = normalPrior.density(thetaFar);

        assertTrue(densityClose > 0.0, "La densité au point proche doit être strictement positive.");
        assertTrue(densityFar > 0.0, "La densité au point lointain doit être strictement positive.");

        assertTrue(densityClose < densityAtMean, "La densité au pic (moyenne) doit être strictement supérieure à celle d'un point proche.");

        assertTrue(densityFar < densityClose, "La densité doit décroître à mesure que l'on s'éloigne du vecteur moyenne.");
    }

    @Test
    void testDensityNullTheta() {
        assertThrows(NullPointerException.class, () ->
                        normalPrior.density(null),
                "Doit lever une exception si le vecteur theta évalué est null."
        );
    }

    @Test
    void testDensityThetaDimensionMismatch() {
        // Theta de dimension 3 alors que le prior attend une dimension 2
        Vector invalidTheta = new ArrayVector(new double[]{1.0, 2.0, 3.0});

        assertThrows(IllegalArgumentException.class, () ->
                        normalPrior.density(invalidTheta),
                "Doit lever une exception si la dimension de theta ne correspond pas au prior."
        );
    }
}