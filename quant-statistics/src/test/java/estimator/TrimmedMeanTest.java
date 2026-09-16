package estimator;

import org.estimator.nonparametric.bootstrap.TrimmedMean;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests de l'estimateur TrimmedMean")
class TrimmedMeanTest {

    private static final double EPSILON = 1e-9;

    @Nested
    @DisplayName("Validation du constructeur")
    class ConstructorValidation {

        @Test
        @DisplayName("Doit instancier correctement quand alpha est valide (ex: 0.1)")
        void shouldInstantiateWithValidAlpha() {
            TrimmedMean estimator = new TrimmedMean(0.1);
            assertNotNull(estimator);
        }

        @Test
        @DisplayName("Doit accepter la borne inférieure alpha = 0.0")
        void shouldAcceptZeroAlpha() {
            TrimmedMean estimator = new TrimmedMean(0.0);
            assertNotNull(estimator);
        }

        @ParameterizedTest(name = "Alpha invalide : {0}")
        @ValueSource(doubles = {-0.01, -0.1, 0.5, 0.6, 1.0})
        @DisplayName("Doit lever IllegalArgumentException si alpha n'est pas dans [0.0, 0.5[")
        void shouldThrowExceptionForInvalidAlpha(double invalidAlpha) {
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new TrimmedMean(invalidAlpha)
            );
            assertTrue(exception.getMessage().contains("La proportion de tronquage alpha doit être dans [0.0, 0.5["));
        }
    }

    @Nested
    @DisplayName("Comportement d'estimation (Nominal)")
    class EstimationBehavior {

        @Test
        @DisplayName("Alpha = 0.0 doit équivaloir à la moyenne arithmétique simple")
        void shouldReturnStandardMeanWhenAlphaIsZero() {
            Vector sample = new ArrayVector(new double[]{1.0, 2.0, 3.0, 4.0, 5.0});
            TrimmedMean estimator = new TrimmedMean(0.0);

            Double result = estimator.estimate(sample);

            assertTrue(Math.abs(3.0 - result) < EPSILON, "Le résultat attendu était 3.0 mais était : " + result);
        }

        @Test
        @DisplayName("Doit correctement tronquer k éléments à chaque extrémité et calculer la moyenne")
        void shouldTrimCorrectlyOnSymmetricData() {
            // n = 10, alpha = 0.2 -> k = floor(10 * 0.2) = 2
            // Échantillon trié : [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]
            // Après tronquage (retrait de 1, 2 et 9, 10) : [3, 4, 5, 6, 7, 8]
            // Somme = 33, Taille = 6 -> Moyenne = 5.5
            Vector sample = new ArrayVector(new double[]{10.0, 1.0, 9.0, 2.0, 8.0, 3.0, 7.0, 4.0, 6.0, 5.0});
            TrimmedMean estimator = new TrimmedMean(0.2);

            Double result = estimator.estimate(sample);

            assertTrue(Math.abs(5.5 - result) < EPSILON, "Le résultat attendu était 5.5 mais était : " + result);
        }

        @Test
        @DisplayName("Doit être insensible aux valeurs aberrantes (outliers) extrêmes")
        void shouldBeRobustAgainstOutliers() {
            // Données bruitées avec deux d'énormes outliers aux extrémités
            // n = 10, alpha = 0.1 -> k = floor(10 * 0.1) = 1
            // Retrait de -9999.0 et +9999.0
            // Reste [10, 10, 10, 10, 10, 10, 10, 10] -> Moyenne = 10.0
            Vector sample = new ArrayVector(new double[]{-9999.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 9999.0});
            TrimmedMean estimator = new TrimmedMean(0.1);

            Double result = estimator.estimate(sample);

            assertTrue(Math.abs(10.0 - result) < EPSILON, "Le résultat attendu était 10.0 mais était : " + result);
        }

        @Test
        @DisplayName("Doit tronquer 0 élément si floor(n * alpha) == 0 (ex: n=4, alpha=0.1 -> k=0)")
        void shouldNotTrimIfCalculatedKIsZero() {
            // n = 4, alpha = 0.1 -> k = floor(0.4) = 0
            Vector sample = new ArrayVector(new double[]{2.0, 4.0, 6.0, 8.0});
            TrimmedMean estimator = new TrimmedMean(0.1);

            Double result = estimator.estimate(sample);

            assertTrue(Math.abs(5.0 - result) < EPSILON, "Le résultat attendu était 5.0 mais était : " + result);
        }
    }

    @Nested
    @DisplayName("Gestion des cas limites et exceptions")
    class EdgeCasesAndExceptions {

        @Test
        @DisplayName("Doit lever NullPointerException si l'échantillon est null")
        void shouldThrowExceptionWhenSampleIsNull() {
            TrimmedMean estimator = new TrimmedMean(0.1);

            NullPointerException exception = assertThrows(
                    NullPointerException.class,
                    () -> estimator.estimate(null)
            );
            assertTrue(exception.getMessage().equals("L'échantillon ne peut pas être nul."));
        }



        @Test
        @DisplayName("Doit supporter un échantillon à élément unique sans tronquage")
        void shouldHandleSingleElementSample() {
            Vector singleElementSample = new ArrayVector(new double[]{42.0});
            TrimmedMean estimator = new TrimmedMean(0.4);

            Double result = estimator.estimate(singleElementSample);

            assertTrue(Math.abs(42.0 - result) < EPSILON, "Le résultat attendu était 42.0 mais était : " + result);
        }
    }
}