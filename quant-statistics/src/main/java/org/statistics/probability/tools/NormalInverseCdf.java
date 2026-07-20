package org.statistics.probability.tools;

/**
 * Calcul direct de la fonction Probit (Inverse CDF de N(0,1))
 * Algorithme de Peter J. Acklam.
 */
public final class NormalInverseCdf {

    private NormalInverseCdf() {}

    private static final double[] A = {
            -3.969683028665376e+01,  2.209460984245205e+02,
            -2.759285104469687e+02,  1.383577518672690e+02,
            -3.066479806614716e+01,  2.506628277459239e+00
    };
    private static final double[] B = {
            -5.447609879822406e+01,  1.615858368580409e+02,
            -1.556989798598866e+02,  6.680131188771972e+01,
            -1.328068155288572e+01
    };
    private static final double[] C = {
            -7.784894002430293e-03, -3.223964580411365e-01,
            -2.400758277161838e+00, -2.549732539343734e+00,
            4.374664141464968e+00,  2.938163982698783e+00
    };
    private static final double[] D = {
            7.784695709041462e-03,  3.224671290700398e-01,
            2.445134137142996e+00,  3.754408661907416e+00
    };

    private static final double P_LOW  = 0.02425;
    private static final double P_HIGH = 1.0 - P_LOW;

    public static double compute(double p) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("p doit être strictement dans ]0, 1[.");
        }

        if (p < P_LOW) {
            double q = Math.sqrt(-2.0 * Math.log(p));
            return (((((C[0]*q + C[1])*q + C[2])*q + C[3])*q + C[4])*q + C[5]) /
                    ((((D[0]*q + D[1])*q + D[2])*q + D[3])*q + 1.0);
        }

        if (p <= P_HIGH) {
            double q = p - 0.5;
            double r = q * q;
            double num = (((((A[0]*r + A[1])*r + A[2])*r + A[3])*r + A[4])*r + A[5])*q;
            double den = strokeB(r);
            return num / den;
        }

        double q = Math.sqrt(-2.0 * Math.log(1.0 - p));
        return -(((((C[0]*q + C[1])*q + C[2])*q + C[3])*q + C[4])*q + C[5]) /
                ((((D[0]*q + D[1])*q + D[2])*q + D[3])*q + 1.0);
    }

    private static double strokeB(double r) {
        return ((((B[0]*r + B[1])*r + B[2])*r + B[3])*r + B[4])*r + 1.0;
    }
}