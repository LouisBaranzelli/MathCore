package org.math.Integrator.multivariate;

import org.math.Integrator.monovariate.SimpsonIntegrator;
import org.math.common.PerformanceMonitor;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.ToDoubleFunction;

/**
 * Implémentation d'un intégrateur numérique multivarié par la règle du produit cartésien
 * (intégration récurrente par la méthode des trapèzes sur chaque dimension) -> uniquement en faible dimension.
 */
public class RecursiveProductRuleIntegrator implements MultivariateNumericalIntegrator {

    private final int stepsPerDimension;
    private static final Logger log = LoggerFactory.getLogger(RecursiveProductRuleIntegrator.class);

    /**
     * @nombre de pas (subdivisions) par dimension pour la grille d'intégration.
     */
    public RecursiveProductRuleIntegrator(int stepsPerDimension) {
        if (stepsPerDimension < 1) {
            throw new IllegalArgumentException("Steps per dimension must be at least 1");
        }
        this.stepsPerDimension = stepsPerDimension;
    }

    @Override
    public double integrate(ToDoubleFunction<Vector> f, Vector a, Vector b) {
       return PerformanceMonitor.measure(log, "[MultivariateNumericalIntegrator]", () ->  {
            validateIntegrationBounds(a, b);
            int dim = a.size();
            double[] currentCoords = new double[dim];
            return integrateRecursive(f, a, b, 0, currentCoords);
        });
    }

    private double integrateRecursive(ToDoubleFunction<Vector> f, Vector a, Vector b, int currentDim, double[] currentCoords) {
        int dim = a.size();
        double lower = a.getValue(currentDim);
        double upper = b.getValue(currentDim);
        int n = stepsPerDimension;
        double h = (upper - lower) / n;

        if (currentDim == dim - 1) {
            double sum = 0.5 * (evaluate(f, currentCoords, currentDim, lower) + evaluate(f, currentCoords, currentDim, upper));
            for (int i = 1; i < n; i++) {
                double x = lower + i * h;
                sum += evaluate(f, currentCoords, currentDim, x);
            }
            return sum * h;
        }

        double sum = 0.5 * (integrateAt(f, a, b, currentDim, lower, currentCoords) +
                integrateAt(f, a, b, currentDim, upper, currentCoords));
        for (int i = 1; i < n; i++) {
            double x = lower + i * h;
            sum += integrateAt(f, a, b, currentDim, x, currentCoords);
        }
        return sum * h;
    }

    private double integrateAt(ToDoubleFunction<Vector> f, Vector a, Vector b, int currentDim, double coordVal, double[] currentCoords) {
        currentCoords[currentDim] = coordVal;
        return integrateRecursive(f, a, b, currentDim + 1, currentCoords);
    }

    private double evaluate(ToDoubleFunction<Vector> f, double[] currentCoords, int currentDim, double coordVal) {
        currentCoords[currentDim] = coordVal;
        // On encapsule le tableau brut dans un Vector immuable/adapté pour la fonction cible
        Vector theta = new ArrayVector(currentCoords.clone());
        return f.applyAsDouble(theta);
    }
}