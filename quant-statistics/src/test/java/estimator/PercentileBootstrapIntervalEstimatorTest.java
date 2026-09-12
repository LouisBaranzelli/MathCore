package estimator;

import org.estimator.ConfidenceInterval;
import org.estimator.MeanEstimator;
import org.estimator.PercentileBootstrapIntervalEstimator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de l'estimateur d'intervalle par Bootstrap Percentile")
class PercentileBootstrapIntervalEstimatorTest {

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
                    new PercentileBootstrapIntervalEstimator(sampleMeanEstimator, 99, rng)
            );
        }

        @Test
        @DisplayName("Levée d'exception en cas de null pour les dépendances requises")
        void shouldThrowExceptionOnNullDependencies() {
            assertThrows(NullPointerException.class, () ->
                    new PercentileBootstrapIntervalEstimator(null, 500, rng)
            );
            assertThrows(NullPointerException.class, () ->
                    new PercentileBootstrapIntervalEstimator(sampleMeanEstimator, 500, null)
            );
        }

        @Test
        @DisplayName("Levée d'exception si l'échantillon contient moins de 5 éléments")
        void shouldThrowExceptionWhenSampleSizeIsLessThanFive() {
            var estimator = new PercentileBootstrapIntervalEstimator(sampleMeanEstimator, 500, rng);
            Vector smallSample = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0});

            assertThrows(IllegalArgumentException.class, () ->
                    estimator.estimateInterval(smallSample, 0.95)
            );
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
            var bootstrapEstimator = new PercentileBootstrapIntervalEstimator(sampleMeanEstimator, 1000, rng);
            ConfidenceInterval<Double> interval = bootstrapEstimator.estimateInterval(sample, confidenceLevel);

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

            var estimator1 = new PercentileBootstrapIntervalEstimator(sampleMeanEstimator, 500, rng1);
            var estimator2 = new PercentileBootstrapIntervalEstimator(sampleMeanEstimator, 500, rng2);

            ConfidenceInterval<Double> ci1 = estimator1.estimateInterval(sample, confidenceLevel);
            ConfidenceInterval<Double> ci2 = estimator2.estimateInterval(sample, confidenceLevel);

            assertEquals(ci1.lowerBound(), ci2.lowerBound(), 1e-12, "Les bornes inférieures doivent être strictement identiques.");
            assertEquals(ci1.upperBound(), ci2.upperBound(), 1e-12, "Les bornes supérieures doivent être strictly identiques.");
        }

        @Test
        void shouldCalculateTheRightValue(){
            Vector data = new ArrayVector(new double[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10});
            MeanEstimator meanEstimator = new MeanEstimator();
            PercentileBootstrapIntervalEstimator percentileBootstrapIntervalEstimator = new PercentileBootstrapIntervalEstimator(meanEstimator, 2000, rng);
            ConfidenceInterval<Double> confidenceInterval = percentileBootstrapIntervalEstimator.estimateInterval(data, 0.9);
            assertEquals(4, confidenceInterval.lowerBound());
            assertEquals(7, confidenceInterval.upperBound());
        }
    }
}