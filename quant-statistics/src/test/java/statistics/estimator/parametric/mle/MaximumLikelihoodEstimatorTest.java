package statistics.estimator.parametric.mle;

import org.statistics.data.Sample;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;
import org.statistics.estimator.parametric.mle.MaximumLikelihoodEstimator;
import org.statistics.estimator.parametric.mle.MleResult;
import org.statistics.estimator.parametric.model.NormalModel;
import org.statistics.estimator.parametric.model.ParametricModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.matrix.Matrix;
import org.math.optimizer.ConvergenceException;
import org.math.optimizer.MultivariateOptimizer;
import org.math.optimizer.NelderMeadOptimizer;
import org.math.random.RandomVectorFactory;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.*;

class MaximumLikelihoodEstimatorTest {

    private static final double EPSILON = 1e-2;

    @Test
    @DisplayName("Devrait estimer la moyenne (mu) et l'écart-type (sigma) d'une distribution normale")
    void shouldEstimateNormalDistributionParametersFromDataset() {
        // Jeu de données synthétique avec mu = 10.0 et sigma = 2.0
        // (x̄ = 10.0, s^2 = 4.0 -> s = 2.0)
        Sample dataset = Sample.of(6.0, 7.0, 8.0, 9.0, 10.0, 10.0, 11.0, 12.0, 13.0, 14.0);

        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-4, 200);
        MaximumLikelihoodEstimator estimator = new MaximumLikelihoodEstimator(optimizer);
        ParametricModel<NormalDistribution> parametricModel = new NormalModel();
        MleResult result = estimator.estimate(parametricModel, dataset, ArrayVector.of(1, 1));

        assertNotNull(result, "Le résultat de l'estimation ne doit pas être nul");

        // En MLE pour une loi normale :
        // mu_MLE = moyenne empirique = 10.0
        // sigma_MLE = sqrt(variance biaisée) = sqrt(20.0 / 10) = sqrt(2.0) ≈ 2.4494
        NormalDistribution normalDistribution = parametricModel.createDistribution(result.parameters());
        assertEquals(10.0, normalDistribution.getMu(), EPSILON, "La moyenne estimée (mu) doit être proche de 10.0");
        assertEquals(2.4494,normalDistribution.getSigma(), EPSILON, "L'écart-type estimé (sigma) doit être correct");
    }

    @Test
    @DisplayName("Devrait lever une exception si le jeu de données est vide ou nul")
    void shouldThrowExceptionForInvalidDataset() {
        MultivariateOptimizer optimizer = new NelderMeadOptimizer(1e-4, 200);
        MaximumLikelihoodEstimator estimator = new MaximumLikelihoodEstimator(optimizer);
        ParametricModel<NormalDistribution> parametricModel = new NormalModel();
        assertThrows(IllegalArgumentException.class, () -> {
            MleResult result = estimator.estimate(parametricModel, Sample.of(), ArrayVector.of(1, 1));
        });
        assertThrows(ConvergenceException.class, () -> {
            MleResult result = estimator.estimate(parametricModel, Sample.of(1, 2, 3, 4), ArrayVector.of(0, 0, 0));
        });
    }

    @Test
    @DisplayName("Devrait calculer la matrice de covariance pour une loi Normale (diagonale et hors-diagonale)")
    void testGaussianMleCovarianceMatrix() throws Exception {
        // 1. Génération d'un échantillon synthétique N(mu=10.0, sigma=2.0)
        double trueMu = 10.0;
        double trueSigma = 2.0;
        int n = 10_000; // Grand n pour une bonne convergence asymptotique

        Vector data = RandomVectorFactory.generateGaussianData(n, trueMu, trueSigma);
        Sample sample = new Sample(data);

        // 2. Initialisation du modèle et du résolveur MLE
        // Supopse l'existence d'un GaussianModel où le paramètre [0] = mu et [1] = sigma
        ParametricModel<NormalDistribution> gaussianModel = new NormalModel();
        MaximumLikelihoodEstimator mle = new MaximumLikelihoodEstimator();

        Vector initialGuess = new ArrayVector(new double[]{8.0, 1.0});

        MleResult result = mle.estimate(gaussianModel, sample, initialGuess);
        Vector thetaHat = result.parameters();
        Matrix covMatrix = result.estimate().covarianceMatrix();

        double hatMu = thetaHat.getValue(0);
        double hatSigma = thetaHat.getValue(1);

        // 4. Valeurs théoriques attendues de la matrice de covariance (I^-1)
        double expectedVarMu = (hatSigma * hatSigma) / n;          // sigma^2 / n
        double expectedVarSigma = (hatSigma * hatSigma) / (2 * n);  // sigma^2 / (2n)
        double expectedCovariance = 0.0;                           // Indépendance orthogonale

        double delta = 1e-3; // Tolérance liée aux différences finies et à l'optimiseur

        // 5. Assertions

        // --- Termes diagonaux (Variances des paramètres) ---
        assertEquals(expectedVarMu, covMatrix.get(0, 0), delta,
                "La variance de mu doit être égale à sigma^2 / n");

        assertEquals(expectedVarSigma, covMatrix.get(1, 1), delta,
                "La variance de sigma doit être égale à sigma^2 / (2n)");

        // --- Termes hors-diagonale (Covariances croisées) ---
        assertEquals(expectedCovariance, covMatrix.get(0, 1), delta,
                "La covariance entre mu et sigma doit être nulle");

        assertEquals(expectedCovariance, covMatrix.get(1, 0), delta,
                "La matrice de covariance doit être symétrique avec Cov(sigma, mu) = 0");
    }


}