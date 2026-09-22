package org.estimator.nonparametric.bootstrap;

import org.estimator.interval.ConfidenceInterval;
import org.estimator.interval.bootstrap.BootstrapIntervalMethod;
import org.estimator.interval.bootstrap.BootstrapResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.data.Sample;
import org.math.random.RandomVectorFactory;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de l'estimateur d'intervalle par Bootstrap Percentile")
class NonParametricBootstrapTest {

    private RandomGenerator rng;
    private final MeanEstimator sampleMeanEstimator = new MeanEstimator();

    @BeforeEach
    void setUp() {
        // Source aléatoire déterministe avec seed fixe pour la reproductibilité des tests
        this.rng = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
    }

    @Nested
    @DisplayName("Validation des préconditions d'architecture")
    class PreconditionsTests {

        @Test
        @DisplayName("Levée d'exception si le nombre de ré-échantillonnages est trop faible (< 100)")
        void shouldThrowExceptionWhenResampleCountIsTooLow() {
            assertThrows(IllegalArgumentException.class, () ->
                    new NonParametricBootstrap(99, rng)
            );
        }

        @Test
        @DisplayName("Levée d'exception en cas de null pour les dépendances requises")
        void shouldThrowExceptionOnNullDependencies() {
            assertThrows(IllegalArgumentException.class, () ->{
                        NonParametricBootstrap nonParametricBootstrap = new NonParametricBootstrap(99, rng);
                        nonParametricBootstrap.run(null, RandomVectorFactory.generateUniformSample(10, 42));
                    }

            );
            assertThrows(IllegalArgumentException.class, () ->
                    new NonParametricBootstrap(99, null)
            );
        }

        @Test
        @DisplayName("Levée d'exception si l'échantillon contient moins de 5 éléments")
        void shouldThrowExceptionWhenSampleSizeIsLessThanFive() {
            Vector smallSample = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0});

            assertThrows(IllegalArgumentException.class, () ->{
                NonParametricBootstrap nonParametricBootstrap = new NonParametricBootstrap(100, rng);
                nonParametricBootstrap.run(sampleMeanEstimator, smallSample);
            });

        }
    }

    @Nested
    @DisplayName("Validation de la théorie statistique")
    class StatisticalTheoryTests {

        @Test
        @DisplayName("L'intervalle à 95% doit contenir la vraie moyenne pour un tirage Normal(10, 2)")
        void shouldContainTrueMeanForNormalDistribution() {
            // 1. Simulation d'un échantillon i.i.d. de taille n = 500 issu d'une loi N(10, 4) (écart-type = 2)
            int n = 500;
            double trueMean = 10.0;
            double trueStdDev = 2.0;
            double[] data = new double[n];

            // Utilisation d'un RNG dédié à la génération du dataset (seed fixe)
            RandomGenerator dataRng = RandomGeneratorFactory.of("L64X128MixRandom").create(100L);
            for (int i = 0; i < n; i++) {
                data[i] = trueMean + trueStdDev * dataRng.nextGaussian();
            }

            Vector sample = new ArrayVector(data);
            double confidenceLevel = 0.95;

            // 2. Calcul de l'intervalle bootstrap
            NonParametricBootstrap nonParametricBootstrap = new NonParametricBootstrap(1000, rng);
            BootstrapResult bootstrapResult = nonParametricBootstrap.run(sampleMeanEstimator, sample);
            ConfidenceInterval<Double> interval = BootstrapIntervalMethod.PERCENTILE.calculate(bootstrapResult, confidenceLevel);


            // 3. Vérifications mathématiques
            assertNotNull(interval);
            assertEquals(confidenceLevel, interval.confidenceLevel(), 1e-9);
            assertTrue(interval.lowerBound() < interval.upperBound(), "La borne inférieure doit être stricte à la borne supérieure.");

            // La vraie valeur mu = 10.0 doit être contenue dans l'intervalle de confiance calculé
            assertTrue(interval.lowerBound() <= trueMean && trueMean <= interval.upperBound(),
                    String.format("La vraie moyenne (%f) doit être dans l'intervalle [%f, %f]",
                            trueMean, interval.lowerBound(), interval.upperBound()));
        }

        @Test
        @DisplayName("Le déterminisme doit être garanti grâce à l'injection de la seed RNG")
        void shouldProduceIdenticalResultsWithSameRngSeed() {
            double[] data = {1.2, 2.3, 1.8, 3.1, 2.9, 4.0, 2.2, 1.9, 3.5, 2.7};
            Vector sample = new ArrayVector(data);
            double confidenceLevel = 0.90;

            RandomGenerator rng1 = RandomGeneratorFactory.of("L64X128MixRandom").create(12345L);
            RandomGenerator rng2 = RandomGeneratorFactory.of("L64X128MixRandom").create(12345L);

            NonParametricBootstrap nonParametricBootstrap1 = new NonParametricBootstrap(500, rng1);
            NonParametricBootstrap nonParametricBootstrap2 = new NonParametricBootstrap(500, rng2);
            BootstrapResult bootstrapResult1 = nonParametricBootstrap1.run(sampleMeanEstimator, sample);
            BootstrapResult bootstrapResult2 = nonParametricBootstrap2.run(sampleMeanEstimator, sample);
            ConfidenceInterval<Double> ci1 = BootstrapIntervalMethod.PERCENTILE.calculate(bootstrapResult1, confidenceLevel);
            ConfidenceInterval<Double> ci2 = BootstrapIntervalMethod.PERCENTILE.calculate(bootstrapResult2, confidenceLevel);


            assertEquals(ci1.lowerBound(), ci2.lowerBound(), 1e-12, "Les bornes inférieures doivent être strictement identiques.");
            assertEquals(ci1.upperBound(), ci2.upperBound(), 1e-12, "Les bornes supérieures doivent être strictly identiques.");
        }

        @Test
        void shouldCalculateTheRightValue(){
            Vector data = new ArrayVector(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});

            NonParametricBootstrap nonParametricBootstrap = new NonParametricBootstrap(2000, rng);
            BootstrapResult bootstrapResult = nonParametricBootstrap.run(sampleMeanEstimator, data);
            ConfidenceInterval<Double> interval = BootstrapIntervalMethod.PERCENTILE.calculate(bootstrapResult, 0.9);
            assertEquals(4, interval.lowerBound());
            assertEquals(7, interval.upperBound());
        }
    }


}
class PivotalBootstrapIntervalTest {

    private final BootstrapIntervalMethod pivotalMethod = BootstrapIntervalMethod.PIVOTAL;

    @Test
    @DisplayName("Devrait calculer un intervalle pivotal exact sur une distribution bootstrap symétrique")
    void testCalculatePivotalSymmetric() {
        // Given: thetaHat = 10.0 et ré-échantillons distribués uniformément entre 8.0 et 12.0
        // Quantile 2.5% = 8.1, Quantile 97.5% = 11.9
        double pointEstimate = 10.0;
        double[] bootstrapEstimates = new double[101];
        for (int i = 0; i <= 100; i++) {
            bootstrapEstimates[i] = 8.0 + (i * 0.04); // De 8.0 à 12.0
        }

        BootstrapResult result = new BootstrapResult(pointEstimate, new Sample(bootstrapEstimates));

        // When: niveau de confiance = 95% (alpha = 0.05)
        ConfidenceInterval<Double> interval = pivotalMethod.calculate(result, 0.95);

        // Then:
        // q_lower (2.5%) = 8.10
        // q_upper (97.5%) = 11.90
        // Borne inf = 2 * 10.0 - 11.90 = 8.10
        // Borne sup = 2 * 10.0 - 8.10 = 11.90
        assertEquals(8.10, interval.lowerBound(), 1e-6);
        assertEquals(11.90, interval.upperBound(), 1e-6);
        assertEquals(0.95, interval.confidenceLevel(), 1e-6);
    }

    @Test
    @DisplayName("Devrait corriger le biais de centrage via la formule pivotale 2*theta - q")
    void testCalculatePivotalAsymmetricBiasCorrection() {
        // Given: thetaHat = 5.0, mais la distribution bootstrap est décalée vers la droite (biais positif)
        // Ré-échantillons étalés entre 6.0 et 10.0
        double pointEstimate = 5.0;
        double[] bootstrapEstimates = new double[101];
        for (int i = 0; i <= 100; i++) {
            bootstrapEstimates[i] = 6.0 + (i * 0.04); // De 6.0 à 10.0
        }

        BootstrapResult result = new BootstrapResult(pointEstimate, new Sample(bootstrapEstimates));

        // When: confidenceLevel = 0.90 (alpha = 0.10, alpha/2 = 0.05)
        ConfidenceInterval<Double> interval = pivotalMethod.calculate(result, 0.90);

        // Then:
        // q(5%) = 6.20
        // q(95%) = 9.80
        // Borne inf = 2 * 5.0 - 9.80 = 0.20
        // Borne sup = 2 * 5.0 - 6.20 = 3.80
        // L'intervalle est translaté vers la gauche pour corriger le biais positif
        assertEquals(0.20, interval.lowerBound(), 1e-6);
        assertEquals(3.80, interval.upperBound(), 1e-6);
        assertTrue(interval.lowerBound() < interval.upperBound());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, 1.0, -0.05, 1.05})
    @DisplayName("Devrait lever IllegalArgumentException si le niveau de confiance est invalide")
    void testInvalidConfidenceLevel(double invalidLevel) {
        BootstrapResult result = new BootstrapResult(10.0, new Sample(new double[]{8.0, 9.0, 10.0, 11.0, 12.0}));

        assertThrows(IllegalArgumentException.class, () -> pivotalMethod.calculate(result, invalidLevel));
    }

    @Test
    @DisplayName("Devrait lever NullPointerException si BootstrapResult est null")
    void testNullResult() {
        assertThrows(NullPointerException.class, () -> pivotalMethod.calculate(null, 0.95));
    }

    @Test
    @DisplayName("Devrait gérer correctement un échantillon bootstrap constant (variance nulle)")
    void testConstantBootstrapEstimates() {
        // Given: Tous les ré-échantillons sont égaux à la valeur estimée
        double pointEstimate = 42.0;
        double[] bootstrapEstimates = new double[]{42.0, 42.0, 42.0, 42.0, 42.0};

        BootstrapResult result = new BootstrapResult(pointEstimate, new Sample(bootstrapEstimates));

        // When
        ConfidenceInterval<Double> interval = pivotalMethod.calculate(result, 0.95);

        // Then
        assertEquals(42.0, interval.lowerBound(), 1e-6);
        assertEquals(42.0, interval.upperBound(), 1e-6);
    }
}
