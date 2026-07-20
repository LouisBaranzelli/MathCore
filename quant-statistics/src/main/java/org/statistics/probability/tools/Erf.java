package org.statistics.probability.tools;

public class Erf {

    /**
     * Calcule la fonction d'erreur (erf) avec une précision double.
     */
    public static double erf(double x) {
        if (Double.isNaN(x)) return Double.NaN;
        if (Double.isInfinite(x)) return x > 0 ? 1.0 : -1.0;

        double absX = Math.abs(x);

        // Pour les petites valeurs, la série de Taylor évite les discontinuités des approximations rationnelles
        if (absX < 0.5) {
            double sum = x;
            double term = x;
            double x2 = x * x;
            for (int i = 1; i < 30; i++) {
                term *= -x2 / i;
                double nextTerm = term / (2 * i + 1);
                sum += nextTerm;
                if (Math.abs(nextTerm) < 1e-16) break;
            }
            return sum * (2.0 / Math.sqrt(Math.PI));
        } else {
            // Pour les grandes valeurs, on passe par la fonction d'erreur complémentaire (erfc)
            double erfcVal = erfc(absX);
            return x < 0 ? erfcVal - 1.0 : 1.0 - erfcVal;
        }
    }

    /**
     * Calcule la fonction d'erreur complémentaire (erfc) avec une haute précision.
     */
    public static double erfc(double x) {
        if (Double.isNaN(x)) return Double.NaN;
        if (x < 0) return 2.0 - erfc(-x);
        if (x > 20.0) return 0.0; // Empêche l'underflow sur l'exponentielle

        // Approximation rationnelle de Chebyshev haute performance (Abramowitz & Stegun 7.1.26)
        double t = 1.0 / (1.0 + 0.5 * x);

        double ans = t * Math.exp(-x * x - 1.26551223 + t * (1.00002368 + t * (0.37409196 +
                t * (0.09678418 + t * (-0.18628806 + t * (0.27886807 +
                        t * (-1.13520398 + t * (1.48851587 + t * (-0.82215223 +
                                t * 0.17087277)))))))));
        return ans;
    }
}