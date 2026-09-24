package org.math.optimizer;

import org.math.function.MultivariateFunction;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Optimiseur multivarié basé sur l'algorithme de Nelder-Mead (Downhill Simplex).
 *
 * Avantages : Très robuste, ne nécessite pas de calculer le gradient/dérivée de la fonction,
 * fonctionne bien sur des fonctions non dérivables ou bruitées.
 *
 * Inconvénients : Pas de garantie de convergence vers un minimum global (peut se bloquer dans des minima locaux),
 * relativement lent en très haute dimension ($N > 10$ ou $20$).
 */
public class NelderMeadOptimizer implements MultivariateOptimizer {

    private static final Logger log = LoggerFactory.getLogger(NelderMeadOptimizer.class);

    private final double alpha; // Coefficient de réflexion (> 0)
    private final double gamma; // Coefficient d'expansion (> 1)
    private final double rho;   // Coefficient de contraction (0 < rho <= 0.5)
    private final double sigma; // Coefficient de réduction (0 < sigma < 1)

    private final double tolerance;
    private final int maxIterations;
    private final double stepSize;

    public NelderMeadOptimizer(double tolerance, int maxIterations) {
        this(1.0, 2.0, 0.5, 0.5, tolerance, maxIterations, 0.05);
    }

    public NelderMeadOptimizer(double alpha, double gamma, double rho, double sigma,
                               double tolerance, int maxIterations, double stepSize) {
        this.alpha = alpha;
        this.gamma = gamma;
        this.rho = rho;
        this.sigma = sigma;
        this.tolerance = tolerance;
        this.maxIterations = maxIterations;
        this.stepSize = stepSize;
    }

    @Override
    public OptimizationResult optimize(MultivariateFunction function, Vector initialGuess) {
        int n = initialGuess.size();

        PointValuePair[] simplex = buildInitialSimplex(function, initialGuess, stepSize);

        for (int iter = 0; iter < maxIterations; iter++) {
            Arrays.sort(simplex);

            PointValuePair best = simplex[0];
            PointValuePair worst = simplex[n];
            PointValuePair secondWorst = simplex[n - 1];

            if (isConverged(simplex, tolerance)) {
                log.trace("Nelder-Mead converged in {} iterations", iter);
                return new OptimizationResult(
                        best.point,
                        best.value,
                        iter,
                        TerminationCriterion.CONVERGED_VALUE_TOL
                );
            }

            // Calculate centroid of all points except the worst
            Vector centroid = computeCentroid(simplex, n);

            // 1. Reflection
            Vector reflectedPoint = centroid.add(centroid.minus(worst.point).multiply(alpha));
            double reflectedValue = function.evaluate(reflectedPoint);

            if (reflectedValue < secondWorst.value && reflectedValue >= best.value) {
                simplex[n] = new PointValuePair(reflectedPoint, reflectedValue);
                continue;
            }

            // 2. Expansion
            if (reflectedValue < best.value) {
                Vector expandedPoint = centroid.add(reflectedPoint.minus(centroid).multiply(gamma));
                double expandedValue = function.evaluate(expandedPoint);

                if (expandedValue < reflectedValue) {
                    simplex[n] = new PointValuePair(expandedPoint, expandedValue);
                } else {
                    simplex[n] = new PointValuePair(reflectedPoint, reflectedValue);
                }
                continue;
            }

            // 3. Contraction
            if (reflectedValue < worst.value) {
                // Outside contraction
                Vector contractedPoint = centroid.add(reflectedPoint.minus(centroid).multiply(rho));
                double contractedValue = function.evaluate(contractedPoint);

                if (contractedValue <= reflectedValue) {
                    simplex[n] = new PointValuePair(contractedPoint, contractedValue);
                    continue;
                }
            } else {
                // Inside contraction
                Vector contractedPoint = centroid.add(worst.point.minus(centroid).multiply(rho));
                double contractedValue = function.evaluate(contractedPoint);

                if (contractedValue < worst.value) {
                    simplex[n] = new PointValuePair(contractedPoint, contractedValue);
                    continue;
                }
            }

            // 4. Shrink (Reduction)
            for (int i = 1; i <= n; i++) {
                Vector shrunkPoint = best.point.add(simplex[i].point.minus(best.point).multiply(sigma));
                simplex[i] = new PointValuePair(shrunkPoint, function.evaluate(shrunkPoint));
            }
        }

        log.warn("Nelder-Mead optimization exceeded maximum iterations: {}", maxIterations);
        Arrays.sort(simplex);
        return new OptimizationResult(
                simplex[0].point,
                simplex[0].value,
                maxIterations,
                TerminationCriterion.MAX_ITERATIONS_REACHED
        );
    }

    private PointValuePair[] buildInitialSimplex(MultivariateFunction f, Vector start, double delta) {
        int n = start.size();
        PointValuePair[] simplex = new PointValuePair[n + 1];
        simplex[0] = new PointValuePair(start, f.evaluate(start));

        double[] base = start.toArray();
        for (int i = 0; i < n; i++) {
            double[] point = base.clone();
            point[i] = (point[i] == 0.0) ? 0.00025 : point[i] * (1.0 + delta);
            Vector v = new ArrayVector(point);
            simplex[i + 1] = new PointValuePair(v, f.evaluate(v));
        }

        return simplex;
    }

    private Vector computeCentroid(PointValuePair[] simplex, int n) {
        double[] sum = new double[simplex[0].point.size()];
        for (int i = 0; i < n; i++) {
            double[] coords = simplex[i].point.toArray();
            for (int j = 0; j < sum.length; j++) {
                sum[j] += coords[j];
            }
        }
        for (int j = 0; j < sum.length; j++) {
            sum[j] /= n;
        }
        return new ArrayVector(sum);
    }

    private boolean isConverged(PointValuePair[] simplex, double tol) {
        PointValuePair best = simplex[0];
        PointValuePair worst = simplex[simplex.length - 1];
        double delta = Math.abs(worst.value - best.value);
        return delta < tol;
    }

    private record PointValuePair(Vector point, double value) implements Comparable<PointValuePair> {
        @Override
        public int compareTo(PointValuePair other) {
            return Double.compare(this.value, other.value);
        }
    }
}