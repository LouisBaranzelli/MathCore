package estimator;

import org.estimator.nonparametric.bootstrap.DKWConfidenceBandEstimator;
import org.estimator.nonparametric.bootstrap.EmpiricalDistributionConfidenceBand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;
import java.util.function.DoubleUnaryOperator;

import static org.junit.jupiter.api.Assertions.*;
import static org.statistics.probability.tools.RandomVectorFactory.generateUniformSample;

@DisplayName("Tests de l'estimateur de bande de confiance DKW")
class DKWConfidenceBandEstimatorTest {

    private DKWConfidenceBandEstimator estimator;

    @BeforeEach
    void setUp() {
        this.estimator = new DKWConfidenceBandEstimator();
    }

    @Nested
    @DisplayName("Validation des préconditions et contraintes d'arguments")
    class PreconditionsTests {

        @Test
        @DisplayName("Levée d'exception si le niveau de confiance est hors limites ]0, 1[")
        void shouldThrowExceptionWhenConfidenceLevelIsInvalid() {
            Vector sample = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});

            assertThrows(IllegalArgumentException.class, () ->
                    estimator.estimateBand(sample, 0.0)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    estimator.estimateBand(sample, 1.0)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    estimator.estimateBand(sample, 1.05)
            );
            assertThrows(IllegalArgumentException.class, () ->
                    estimator.estimateBand(sample, -0.05)
            );
        }
    }

    @Nested
    @DisplayName("Validation des propriétés mathématiques et théoriques")
    class MathematicalPropertiesTests {

        @Test
        @DisplayName("Calcul exact du paramètre epsilon pour n et alpha donnés")
        void shouldCalculateCorrectEpsilonValue() {
            // Pour n = 100 et confidenceLevel = 0.95 -> alpha = 0.05
            // epsilon_théorique = sqrt( ln(2 / 0.05) / (2 * 100) ) = sqrt( ln(40) / 200 )
            int n = 100;
            double confidenceLevel = 0.95;
            double alpha = 1.0 - confidenceLevel;
            double expectedEpsilon = Math.sqrt(Math.log(2.0 / alpha) / (2.0 * n));

            double[] data = new double[n];
            for (int i = 0; i < n; i++) {
                data[i] = i * 1.0;
            }
            Vector sample = new ArrayVector(data);

            EmpiricalDistributionConfidenceBand band = estimator.estimateBand(sample, confidenceLevel);

            assertNotNull(band);
            assertEquals(confidenceLevel, band.confidenceLevel(), 1e-9);
            assertEquals(expectedEpsilon, band.epsilon(), 1e-12, "Epsilon calculé doit correspondre exactement à la formule DKW.");
        }

        @Test
        @DisplayName("Tronquage systématique des bornes dans l'intervalle [0.0, 1.0]")
        void shouldTruncateBoundsBetweenZeroAndOne() {
            Vector sample = new ArrayVector(new double[]{10.0, 20.0, 30.0, 40.0, 50.0});
            EmpiricalDistributionConfidenceBand band = estimator.estimateBand(sample, 0.95);

            double epsilon = band.epsilon(); // ~0.607

            // 1. Pour x très petit (x = -100) :
            // - L(x) = max(0.0, 0.0 - epsilon) = 0.0  -> TRONQUÉ
            // - U(x) = min(1.0, 0.0 + epsilon) = epsilon
            assertEquals(0.0, band.lowerBand().applyAsDouble(-100.0), 1e-12,
                    "L(x) doit être tronqué à 0.0 lorsque F_n(x) - eps < 0");
            assertEquals(epsilon, band.upperBand().applyAsDouble(-100.0), 1e-12,
                    "U(x) doit valoir epsilon lorsque F_n(x) = 0");

            // 2. Pour x très grand (x = +100) :
            // - U(x) = min(1.0, 1.0 + epsilon) = 1.0  -> TRONQUÉ
            // - L(x) = max(0.0, 1.0 - epsilon) = 1.0 - epsilon
            assertEquals(1.0, band.upperBand().applyAsDouble(100.0), 1e-12,
                    "U(x) doit être tronqué à 1.0 lorsque F_n(x) + eps > 1");
            assertEquals(1.0 - epsilon, band.lowerBand().applyAsDouble(100.0), 1e-12,
                    "L(x) doit valoir 1 - epsilon lorsque F_n(x) = 1");
        }

        @Test
        @DisplayName("La vraie fonction de répartition d'une Uniforme(0, 1) doit être entièrement contenue dans la bande à 95%")
        void shouldContainTrueUniformDistributionFunction() {
            int n = 300;
            double confidenceLevel = 0.95;
            double[] data = new double[n];

            // Génération déterministe d'échantillons i.i.d. de U(0, 1)
            RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(42L);
            for (int i = 0; i < n; i++) {
                data[i] = rng.nextDouble();
            }

            Vector sample = new ArrayVector(data);
            EmpiricalDistributionConfidenceBand band = estimator.estimateBand(sample, confidenceLevel);

            // CDF théorique F(x) de la loi Uniforme sur [0, 1]
            DoubleUnaryOperator trueUniformCdf = x -> {
                if (x < 0.0) return 0.0;
                if (x > 1.0) return 1.0;
                return x;
            };

            // Points de test réguliers sur l'intervalle [-0.5, 1.5]
            double[] testPoints = new double[200];
            for (int i = 0; i < testPoints.length; i++) {
                testPoints[i] = -0.5 + (i * 2.0 / testPoints.length);
            }

            // Vérification globale via la méthode de validation du band
            boolean isContained = band.containsDistributionFunction(trueUniformCdf, testPoints);

            assertTrue(isContained, "La vraie CDF théorique de la loi Uniforme doit résider dans la bande DKW.");
        }
    }


    @Nested
    @DisplayName("Tests de la bande de confiance DKW sur distribution connue avec variation de alpha")
    class DKWConfidenceBandAlphaTest {

        private DKWConfidenceBandEstimator estimator;
        private DoubleUnaryOperator trueUniformCdf;
        private double[] testGrid;

        @BeforeEach
        void setUp() {
            this.estimator = new DKWConfidenceBandEstimator();

            // CDF théorique F(x) de la loi Uniforme sur [0, 1]
            this.trueUniformCdf = x -> {
                if (x < 0.0) return 0.0;
                if (x > 1.0) return 1.0;
                return x;
            };

            // Grille de test fine sur l'intervalle [0, 1]
            this.testGrid = new double[100];
            for (int i = 0; i < testGrid.length; i++) {
                this.testGrid[i] = i / 99.0;
            }
        }

        @Nested
        @DisplayName("Tests comportementaux sur la bande supérieure (Upper Band) en faisant varier Alpha")
        class UpperBandAlphaTests {

            @Test
            @DisplayName("1. Alpha standard (alpha = 0.05, 95% CI) : Upper Band juste au-dessus de la vraie CDF")
            void shouldBeJustAboveTrueCdfForStandardAlpha() {
                // Echantillon i.i.d. de taille n = 200 tiré de U(0, 1)
                int n = 200;
                Vector sample = generateUniformSample(n, 42L);

                double alpha = 0.05; // 95% de confiance
                double confidenceLevel = 1.0 - alpha;

                EmpiricalDistributionConfidenceBand band = estimator.estimateBand(sample, confidenceLevel);

                // 1. La vraie CDF doit être contenue dans la bande globale
                assertTrue(band.containsDistributionFunction(trueUniformCdf, testGrid),
                        "La vraie CDF Uniforme doit être contenue sous la bande supérieure à 95%.");

                // 2. Vérification point par point : U(x) doit être strictement au-dessus de F(x) sur ]0, 1[
                for (double x : testGrid) {
                    if (x > 0.0 && x < 1.0) {
                        double trueFx = trueUniformCdf.applyAsDouble(x);
                        double upperValue = band.upperBand().applyAsDouble(x);

                        assertTrue(upperValue >= trueFx,
                                String.format("Au point x=%f, Upper Band (%f) devrait être >= Vraie CDF (%f)", x, upperValue, trueFx));
                    }
                }
            }

            @Test
            @DisplayName("2. Upper Band intentionnellement abaissée : Rejet dès qu'elle passe sous la vraie CDF")
            void shouldFailWhenUpperBandIsArtificiallyShiftedBelowTrueCdf() {
                int n = 200;
                Vector sample = generateUniformSample(n, 42L);

                double alpha = 0.05;
                EmpiricalDistributionConfidenceBand originalBand = estimator.estimateBand(sample, 1.0 - alpha);

                // On crée artificiellement une Upper Band corrompue/abaissée de 0.20
                // U_corrompue(x) = max(0, U(x) - 0.20)
                DoubleUnaryOperator artificiallyLoweredUpperBand = x ->
                        Math.max(0.0, originalBand.upperBand().applyAsDouble(x) - 0.20);

                // On construit une bande invalide
                EmpiricalDistributionConfidenceBand corruptedBand = new EmpiricalDistributionConfidenceBand(
                        originalBand.lowerBand(),
                        artificiallyLoweredUpperBand,
                        originalBand.epsilon(),
                        originalBand.confidenceLevel()
                );

                // Doit renvoyer false car la vraie CDF dépasse la borne supérieure abaissée
                boolean isContained = corruptedBand.containsDistributionFunction(trueUniformCdf, testGrid);

                assertFalse(isContained,
                        "containsDistributionFunction doit renvoyer FALSE car la Upper Band est passée sous la vraie CDF.");
            }

            @Test
            @DisplayName("3. Alpha très petit (alpha = 0.0001, 99.99% CI) : Upper Band très large, bien au-dessus de la vraie CDF")
            void shouldBeFarAboveTrueCdfForVerySmallAlpha() {
                int n = 200;
                Vector sample = generateUniformSample(n, 42L);

                double alphaStandard = 0.05;   // 95%
                double alphaTiny = 0.0001;     // 99.99%

                EmpiricalDistributionConfidenceBand bandStandard = estimator.estimateBand(sample, 1.0 - alphaStandard);
                EmpiricalDistributionConfidenceBand bandWide = estimator.estimateBand(sample, 1.0 - alphaTiny);

                // Epsilon_tiny doit être nettement supérieur à Epsilon_standard
                assertTrue(bandWide.epsilon() > bandStandard.epsilon(),
                        "Un alpha plus petit (confiance plus élevée) doit produire un epsilon plus grand.");

                // La Upper Band à 99.99% doit être bien au-dessus de la Upper Band à 95%
                double testPoint = 0.50;
                double upperStandardVal = bandStandard.upperBand().applyAsDouble(testPoint);
                double upperWideVal = bandWide.upperBand().applyAsDouble(testPoint);
                double trueFx = trueUniformCdf.applyAsDouble(testPoint); // 0.50

                assertTrue(upperWideVal > upperStandardVal,
                        "La borne supérieure à 99.99% doit être plus haute que celle à 95%.");
                assertTrue(upperWideVal - trueFx > upperStandardVal - trueFx,
                        "La marge de sécurité par rapport à la vraie CDF doit être plus grande pour un alpha minuscule.");

                assertTrue(bandWide.containsDistributionFunction(trueUniformCdf, testGrid));
            }
        }

    }
}