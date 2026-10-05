package org.fin.definitions.indicators;

import org.math.function.MultivariateFunction;
import org.math.vector.Vector;
import org.statistics.estimator.parametric.model.ParametricModel;

/*
    mesure l'excès de rentabilité d'un actif ou d'un portefeuille par unité de risque pris.
 */
public class SharpeRatioFunction implements MultivariateFunction {

    private final ParametricModel<?> model;
    private final double riskFreeRate;

    public SharpeRatioFunction(ParametricModel<?> model, double riskFreeRate) {
        this.model = model;
        this.riskFreeRate = riskFreeRate;
    }


    /*
    Grille d'interprétation du Ratio de SharpeValeur du Sharpe RatioInterprétation f
    inancière < 0.0
    Inintéressant : La rentabilité est inférieure au taux sans risque.

    0.0 à 0.5
    Faible : Le rendement excédentaire ne compense pas suffisamment la volatilité subie.

    0.5 à 1.0
    Bon : Le portefeuille offre une rémunération adéquate du risque.

    > 1.0
    Excellent : La stratégie génère un rendement très élevé par unité de risque.
     */

    @Override
    public double evaluate(Vector point) {
        double expectedReturn = model.mean(point);
        double stdDev = Math.sqrt(model.variance(point));

        return (expectedReturn - riskFreeRate) / stdDev;
    }
}