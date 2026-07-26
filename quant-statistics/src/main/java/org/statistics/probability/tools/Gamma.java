package org.statistics.probability.tools;

/**
 * Classe utilitaire fournissant les fonctions spéciales Gamma : Γ(x) et ln(Γ(x)).
 * <p>
 * Implémentation basée sur l'approximation de Lanczos (précision ~15 chiffres significatifs).
 */
public final class Gamma {

    // Coefficients de Lanczos pour g = 7, n = 9
    private static final double[] LANCZOS_P = {
            0.99999999999980993227682381958641,
            676.5203681218851,
            -1259.1392167224028,
            771.32342877765313,
            -176.61502916214059,
            12.507343278686905,
            -0.13857109526572012,
            9.9843695780195716e-6,
            1.5056327351493116e-7
    };

    private static final double HALF_LOG_TWO_PI = 0.5 * Math.log(2.0 * Math.PI);

    // Constructeur privé pour empêcher l'instanciation
    private Gamma() {
        throw new UnsupportedOperationException("Classe utilitaire non instanciable.");
    }

    /**
     * Calcule le logarithme naturel de la fonction Gamma : ln(Γ(x))
     * <p>
     * Indispensable pour éviter le dépassement de capacité (overflow) lors de calculs
     * statistiques sur de grands paramètres.
     *
     * @param x La valeur d'entrée (x > 0)
     * @return ln(Γ(x)), ou Double.NaN si x <= 0
     */
    public static double logGamma(double x) {
        if (Double.isNaN(x) || x <= 0.0) {
            return Double.NaN;
        }

        // Formule de réflexion d'Euler pour x < 0.5 :
        // Γ(x) * Γ(1-x) = π / sin(π*x) ==> ln(Γ(x)) = ln(π) - ln(sin(π*x)) - ln(Γ(1-x))
        if (x < 0.5) {
            return Math.log(Math.PI) - Math.log(Math.sin(Math.PI * x)) - logGamma(1.0 - x);
        }

        double z = x - 1.0;
        double base = z + 7.5; // g + 0.5 = 7 + 0.5
        double sum = LANCZOS_P[0];

        for (int i = 1; i < LANCZOS_P.length; i++) {
            sum += LANCZOS_P[i] / (z + i);
        }

        return HALF_LOG_TWO_PI + (z + 0.5) * Math.log(base) - base + Math.log(sum);
    }

    /**
     * Calcule la fonction Gamma : Γ(x)
     * <p>
     * Pour tout entier positif n : Γ(n) = (n - 1)!
     *
     * @param x La valeur d'entrée
     * @return Γ(x)
     */
    public static double gamma(double x) {
        if (Double.isNaN(x)) {
            return Double.NaN;
        }

        // Pôles aux entiers négatifs ou nul
        if (x <= 0.0 && Math.floor(x) == x) {
            return Double.NaN;
        }

        // Évite la tentative de calcul si la valeur dépasse la capacité d'un double (x > 171.61)
        if (x > 171.61) {
            return Double.POSITIVE_INFINITY;
        }

        // Utilisation de la réflexion directe pour x < 0.5
        if (x < 0.5) {
            return Math.PI / (Math.sin(Math.PI * x) * gamma(1.0 - x));
        }

        return Math.exp(logGamma(x));
    }
}