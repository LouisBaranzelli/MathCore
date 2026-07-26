package org.statistics.probability.distributions.multivariate;


import org.math.matrix.Matrix;
import org.math.vector.Vector;

/**
 * Contrat fondamental pour toute distribution de probabilité continue multivariée sur R^k.
 * Appartient au noyau pur math-core.
 */
public interface MultivariateDistribution {

    /**
     * @return La dimension k du vecteur aléatoire (ex: nombre d'actifs dans le portefeuille).
     */
    int getDimension();

    /**
     * Calcule la densité de probabilité f(x) au point x.
     *
     * @param x Vecteur de dimension k
     * @return Valeur de la densité f(x) >= 0
     * @throws IllegalArgumentException si la taille de x != getDimension()
     */
    double density(double[] x);

    /**
     * Calcule le logarithme naturel de la densité de probabilité : ln(f(x)).
     * <p>
     * Recommandé par rapport à {@link #density(double[])} pour éviter le sous-dépassement
     * numérique (underflow) en grande dimension.
     * </p>
     *
     * @param x Vecteur de dimension k
     * @return ln(f(x))
     * @throws IllegalArgumentException si la taille de x != getDimension()
     */
    double logDensity(double[] x);

    /**
     * Génère un vecteur aléatoire selon la distribution conjointement corrélée.
     * (Essentiel pour les moteurs de simulation Monte-Carlo).
     *
     * @return Un tableau de double de taille k
     */
    Vector sample();

    /**
     * Génère N échantillons indépendants de la distribution.
     *
     * @param sampleSize Nombre d'échantillons (scénarios) à générer
     * @return Matrice de taille (sampleSize x k) où chaque ligne est un tirage
     */
    default Vector[] sample(int sampleSize) {
        if (sampleSize <= 0) {
            throw new IllegalArgumentException("La taille de l'échantillon doit être > 0");
        }
        Vector[] samples = new Vector[sampleSize];
        for (int i = 0; i < sampleSize; i++) {
            samples[i] = sample();
        }
        return samples;
    }


    Vector getMean();


    Matrix getCovariance();
}