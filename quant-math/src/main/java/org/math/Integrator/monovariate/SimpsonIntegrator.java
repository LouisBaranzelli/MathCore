package org.math.Integrator.monovariate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Implémentation de la méthode d'intégration numérique de Simpson composite.
 * Approche l'intégrale d'une fonction sur un intervalle [a, b] en utilisant
 * une interpolation parabolique par morceaux.
 */
public class SimpsonIntegrator implements NumericalIntegrator {

    private static final Logger log = LoggerFactory.getLogger(SimpsonIntegrator.class);
    private static final int DEFAULT_INTERVALS = 1000;

    private final int intervals;

    public SimpsonIntegrator() {
        this(DEFAULT_INTERVALS);
    }

    public SimpsonIntegrator(int intervals) {
        if (intervals <= 0) {
            throw new IllegalArgumentException("Number of intervals must be strictly positive: " + intervals);
        }
        // Simpson's rule strictly requires an even number of intervals
        if (intervals % 2 != 0) {
            log.warn("Number of intervals ({}) must be even for Simpson's rule. Adjusting to {}.", intervals, intervals + 1);
            this.intervals = intervals + 1;
        } else {
            this.intervals = intervals;
        }
    }

    @Override
    public double integrate(ToDoubleFunction<Double> f, double a, double b) {
        Objects.requireNonNull(f, "Function f cannot be null");
        validateIntegrationBounds(a, b);

        if (a == b) {
            log.debug("Integration bounds are identical (a = b = {}). Returning 0.0.", a);
            return 0.0;
        }

        double h = (b - a) / intervals;
        double sum = f.applyAsDouble(a) + f.applyAsDouble(b);

        for (int i = 1; i < intervals; i++) {
            double x = a + i * h;
            double fx = f.applyAsDouble(x);
            if (i % 2 == 0) {
                sum += 2.0 * fx;
            } else {
                sum += 4.0 * fx;
            }
        }

        double result = (h / 3.0) * sum;
        log.debug("Simpson integration computed over [{}, {}] with {} intervals. Result = {}", a, b, intervals, result);

        return result;
    }
}