package org.statistics.frequentist.estimator.parametric.model;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

/**
 * Encapsule les résultats complets d'une régression linéaire simple :
 * paramètres estimés, erreurs standard sous hypothèse d'homoscédasticité,
 * résidus et qualité d'ajustement.
 */
public final class SimpleLinearRegressionResult {

    private final Vector parameters; // [beta_0, beta_1, sigma]
    private final double beta0StandardError;
    private final double beta1StandardError;
    private final Vector residuals;
    private final double rSquared;

    public SimpleLinearRegressionResult(
            double beta0,
            double beta1,
            double sigma,
            double beta0StandardError,
            double beta1StandardError,
            Vector residuals,
            double rSquared) {
        this.parameters = new ArrayVector(new double[]{beta0, beta1, sigma});
        this.beta0StandardError = beta0StandardError;
        this.beta1StandardError = beta1StandardError;
        this.residuals = residuals;
        this.rSquared = rSquared;
    }

    /**
     * Retourne le vecteur de paramètres [beta_0, beta_1, sigma] prêt à être utilisé
     * par SimpleLinearRegressionModel.
     */
    public Vector getParameters() {
        return parameters;
    }

    public double getBeta0() {
        return parameters.getValue(0);
    }

    public double getBeta1() {
        return parameters.getValue(1);
    }

    public double getSigma() {
        return parameters.getValue(2);
    }

    public double getBeta0StandardError() {
        return beta0StandardError;
    }

    public double getBeta1StandardError() {
        return beta1StandardError;
    }

    /**
     * Retourne le vecteur des résidus bruts e_i = y_i - (beta_0 + beta_1 * x_i)
     * permettant de diagnostiquer la normalité et l'homoscédasticité.
     */
    public Vector getResiduals() {
        return residuals;
    }

    public double getRSquared() {
        return rSquared;
    }

    /**
     * Calcule la statistique t de Student pour tester l'hypothèse nulle H0 : beta_1 = 0.
     */
    public double getBeta1TStatistic() {
        if (beta1StandardError == 0.0) {
            return Double.POSITIVE_INFINITY * Math.signum(getBeta1());
        }
        return getBeta1() / beta1StandardError;
    }
}