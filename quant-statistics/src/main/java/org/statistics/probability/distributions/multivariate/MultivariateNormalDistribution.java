package org.statistics.probability.distributions.multivariate;

import lombok.Getter;
import org.math.matrix.CholeskyDecomposition;
import org.math.matrix.Matrix;
import org.math.matrix.MatrixTools;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.Objects;
import java.util.Random;


/**
 * Représente une loi Normale Multivariée N(μ, Σ) sur R^k.
 * Appartient strictly au noyau math-core.
 */

/**
 * MultivariateNormalDistribution permet de :
 * 1. Générer des échantillons de variables aléatoires corrélées (via la méthode sample).
 * 2. Évaluer la vraisemblance (densité de probabilité) d'un vecteur de données corrélées.
 *    Cela permet de mesurer si une observation est courante ou rare/anormale,
 *    en tenant compte à la fois des moyennes individuelles et des corrélations
 *    définies dans la matrice de covariance.
 *
 *    * 1. LOI NORMALE (MultivariateNormalDistribution) :
 *         *    - Utilisation : Modèles classiques, régression linéaire, filtres de Kalman,
 *         *      optimisation de portefeuille type Markowitz (moyenne-variance).
 *         *    - Quand ? Si les données ne présentent pas de krachs extrêmes ou de valeurs
 *  *      aberrantes (outliers), et pour privilégier la rapidité de calcul.
 *
 *
 */



public final class MultivariateNormalDistribution implements MultivariateDistribution {

    private final Vector mean;                // Vecteur des moyennes μ (taille k)

    @Getter
    private final Matrix covariance;          // Matrice de covariance Σ (k x k)
    private final Matrix choleskyL;           // Matrice triangulaire inférieure L telle que Σ = L * L^T
    private final double logDeterminant;      // ln(|Σ|) calculé à la construction pour la stabilité
    private final Random random;
    private final CholeskyDecomposition cholesky;

    public MultivariateNormalDistribution(Vector mean, Matrix covariance) {
        this(mean, covariance, new Random());
    }

    public MultivariateNormalDistribution(Vector mean, Matrix covariance, Random random) {
        Objects.requireNonNull(mean, "Le vecteur de moyenne ne peut être null");
        Objects.requireNonNull(covariance, "La matrice de covariance ne peut être null");

        int k = mean.size();
        if (covariance.rowCount() != k || covariance.columnCount() != k) {
            throw new IllegalArgumentException("Incohérence de dimension entre moyenne et covariance.");
        }
        if (!MatrixTools.isSymetric(covariance, k)){
            throw new IllegalArgumentException("La matrice de covariance n'est pas symétrique aux indices (%d, %d).");
        }

        this.mean = mean;
        this.covariance = covariance;
        this.random = random;

        this.cholesky = new CholeskyDecomposition(covariance);
        this.choleskyL = cholesky.getL();
        this.logDeterminant = cholesky.getLogDeterminant();
    }

    @Override
    public int getDimension() {
        return mean.size();
    }

    @Override
    public Vector getMean() {
        return mean;
    }

    public double logDensity(Vector x) {
        Objects.requireNonNull(x, "Le vecteur x ne peut être null");
        return logDensity(x.toArray());
    }

    public double logDensity(double[] x) {
        checkDimension(x);
        int k = getDimension();

        // 1. Soustraire la moyenne : diff = (x - μ)
        double[] diff = new double[k];
        for (int i = 0; i < k; i++) {
            diff[i] = x[i] - mean.getValue(i);
        }

        // 2. Distance de Mahalanobis
        double mahalanobisSq = cholesky.solveLowerTriangularSquaredNorm(diff);

        // 3. Formule : -0.5 * (k * ln(2π) + ln(|Σ|) + mahalanobisSq)
        return -0.5 * (k * Math.log(2 * Math.PI) + logDeterminant + mahalanobisSq);
    }

    public double density(Vector x) {
        return Math.exp(logDensity(x));
    }

    @Override
    public double density(double[] x) {
        return Math.exp(logDensity(x));
    }

    /**
     * Génère un échantillon corrélé x ~ N(μ, Σ)
     * Formule : x = μ + L * z, avec z ~ N(0, I)
     */
    @Override
    public Vector sample() {
        int k = getDimension();

        double[] zData = new double[k];
        for (int i = 0; i < k; i++) {
            zData[i] = random.nextGaussian();
        }
        Vector z = new ArrayVector(zData);

        // x = μ + L * z
        return mean.add(choleskyL.multiply(z));
    }

    /**
     * Lemme de Wasserman : Y = A*X + c  ==>  Y ~ N(A*μ + c, A*Σ*A^T)
     */
    public MultivariateNormalDistribution linearTransformation(Matrix A, Vector c) {
        if (A.columnCount() != getDimension()) {
            throw new IllegalArgumentException("La matrice A doit avoir autant de colonnes que la dimension de X.");
        }
        if (c.size() != A.rowCount()) {
            throw new IllegalArgumentException("Le vecteur c doit avoir la même dimension que les lignes de A.");
        }

        Vector newMean = A.multiply(this.mean).add(c);
        Matrix newCovariance = A.multiply(this.covariance).multiply(A.transpose());

        return new MultivariateNormalDistribution(newMean, newCovariance, this.random);
    }

    private void checkDimension(double[] x) {
        if (x.length != getDimension()) {
            throw new IllegalArgumentException("Dimension du vecteur x incorrecte.");
        }
    }
}