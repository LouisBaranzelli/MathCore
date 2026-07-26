package org.statistics.probability.distributions.multivariate;

import lombok.Getter;
import org.math.matrix.CholeskyDecomposition;
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.probability.tools.Gamma;

import java.util.Objects;
import java.util.Random;

/**
 * Représente une loi t de Student Multivariée t_ν(μ, Σ) sur R^k.
 * Idéale pour modéliser les rendements financiers avec des queues épaisses (Fat Tails).
 *
 * Attention: impossibilité d'avoir des degrés de liberté différents par actif. -> utiliser copule*
 *
 * * 2. LOI STUDENT-T (MultivariateStudentTDistribution) :
 *  *    - Utilisation : Gestion des risques (Value-at-Risk, Expected Shortfall),
 *  *      simulation Monte-Carlo de crises, clustering robuste au bruit.
 *  *    - Quand ? Dès que les données ont des "queues épaisses" (fat-tails / krachs
 *  *      simultanés fréquents). Paramètre nu (degrés de liberté) :
 *  *        * nu ~ 3 à 5  : Fort risque d'extrêmes (rendements boursiers/crypto).
 *  *        * nu > 30     : Équivalent pratique à une loi Normale.
 *
 *
 */
public final class MultivariateStudentTDistribution implements MultivariateDistribution {

    @Getter
    private final double degreesOfFreedom;     // ν (nu), ex: 4.0 pour des queues très épaisses
    private final Vector mean;                  // Vecteur de localisation μ (taille k)

    @Getter
    private final Matrix scaleMatrix;           // Matrice d'échelle Σ (k x k)
    private final Matrix choleskyL;              // Matrice triangulaire inférieure L telle que Σ = L * L^T
    private final CholeskyDecomposition cholesky;
    private final double logDeterminant;
    private final Random random;

    public MultivariateStudentTDistribution(double degreesOfFreedom, Vector mean, Matrix scaleMatrix) {
        this(degreesOfFreedom, mean, scaleMatrix, new Random());
    }

    public MultivariateStudentTDistribution(double degreesOfFreedom, Vector mean, Matrix scaleMatrix, Random random) {
        if (degreesOfFreedom <= 0) {
            throw new IllegalArgumentException("Les degrés de liberté (nu) doivent être strictly positifs.");
        }
        Objects.requireNonNull(mean, "Le vecteur de moyenne ne peut être null.");
        Objects.requireNonNull(scaleMatrix, "La matrice d'échelle ne peut être null.");

        int k = mean.size();
        if (scaleMatrix.rowCount() != k || scaleMatrix.columnCount() != k) {
            throw new IllegalArgumentException("Incohérence de dimension entre moyenne et matrice d'échelle.");
        }

        checkSymmetry(scaleMatrix, k);

        this.degreesOfFreedom = degreesOfFreedom;
        this.mean = mean;
        this.scaleMatrix = scaleMatrix;
        this.random = random;

        this.cholesky = new CholeskyDecomposition(scaleMatrix);
        this.choleskyL = cholesky.getL();
        this.logDeterminant = cholesky.getLogDeterminant();
    }

    @Override
    public int getDimension() {
        return mean.size();
    }

    @Override
    public Vector getMean() {
        if (degreesOfFreedom <= 1) {
            throw new IllegalStateException("La moyenne est indéfinie pour nu <= 1.");
        }
        return mean;
    }

    /**
     * Matrice de Covariance : Cov(X) = [nu / (nu - 2)] * Σ (pour nu > 2)
     */
    public Matrix getCovariance() {
        if (degreesOfFreedom <= 2) {
            throw new IllegalStateException("La variance est infinie/indéfinie pour nu <= 2.");
        }
        double factor = degreesOfFreedom / (degreesOfFreedom - 2.0);
        return scaleMatrix.multiply(factor);
    }

    /**
     * Génère un échantillon x ~ t_ν(μ, Σ)
     */
    @Override
    public Vector sample() {
        int k = getDimension();

        // 1. Tirage de Z ~ N(0, I)
        double[] zData = new double[k];
        for (int i = 0; i < k; i++) {
            zData[i] = random.nextGaussian();
        }
        Vector z = new ArrayVector(zData);

        // 2. L * z -> Injecte la structure de covariance/échelle Σ
        Vector correlatedGaussian = choleskyL.multiply(z);

        // 3. Tirage de W ~ Chi2(ν)
        double w = sampleChiSquare(degreesOfFreedom);

        // 4. Facteur d'échelle sqrt(ν / W) pour les queues épaisses
        double scaleFactor = Math.sqrt(degreesOfFreedom / w);

        // 5. Formule : X = μ + sqrt(ν / W) * (L * z)
        return mean.add(correlatedGaussian.multiply(scaleFactor));
    }

    /**
     * Calcule le log de la densité ln f(x) à partir d'un Vector
     */
    public double logDensity(Vector x) {
        Objects.requireNonNull(x, "Le vecteur x ne peut être null.");
        return logDensity(x.toArray());
    }

    /**
     * Calcule le log de la densité ln f(x)
     */
    public double logDensity(double[] x) {
        checkDimension(x);
        int k = getDimension();
        double v = degreesOfFreedom;

        // Soustraction de la moyenne
        double[] diff = new double[k];
        for (int i = 0; i < k; i++) {
            diff[i] = x[i] - mean.getValue(i);
        }

        // Distance de Mahalanobis delta^2 = (x - μ)^T * Σ^-1 * (x - μ)
        double mahalanobisSq = cholesky.solveLowerTriangularSquaredNorm(diff);

        // Termes Gamma : ln Γ((ν + k) / 2) - ln Γ(ν / 2)
        double logGammaTerm = Gamma.logGamma((v + k) / 2.0) - Gamma.logGamma(v / 2.0);

        // Formule complète de la densité de Student multivariée
        return logGammaTerm
                - 0.5 * k * Math.log(v * Math.PI)
                - 0.5 * logDeterminant
                - 0.5 * (v + k) * Math.log(1.0 + mahalanobisSq / v);
    }

    public double density(Vector x) {
        return Math.exp(logDensity(x));
    }

    @Override
    public double density(double[] x) {
        return Math.exp(logDensity(x));
    }

    private double sampleChiSquare(double nu) {
        double alpha = nu / 2.0;
        return sampleGamma(alpha, 2.0);
    }

    private double sampleGamma(double alpha, double beta) {
        if (alpha >= 1.0) {
            double d = alpha - 1.0 / 3.0;
            double c = 1.0 / Math.sqrt(9.0 * d);
            while (true) {
                double z = random.nextGaussian();
                double u = random.nextDouble();
                double v = 1.0 + c * z;
                if (v <= 0) continue;
                v = v * v * v;
                if (u < 1.0 - 0.0331 * z * z * z * z) return d * v * beta;
                if (Math.log(u) < 0.5 * z * z + d * (1.0 - v + Math.log(v))) return d * v * beta;
            }
        } else {
            return sampleGamma(alpha + 1.0, beta) * Math.pow(random.nextDouble(), 1.0 / alpha);
        }
    }

    private void checkDimension(double[] x) {
        if (x.length != getDimension()) {
            throw new IllegalArgumentException("Dimension du vecteur x incorrecte.");
        }
    }

    private void checkSymmetry(Matrix matrix, int size) {
        double epsilon = 1e-10;
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                if (Math.abs(matrix.get(i, j) - matrix.get(j, i)) > epsilon) {
                    throw new IllegalArgumentException(
                            String.format("La matrice d'échelle n'est pas symétrique aux indices (%d, %d).", i, j)
                    );
                }
            }
        }
    }
}