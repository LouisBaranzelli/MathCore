package org.statistics.probability;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.*;

class DescriptiveStatisticsTest {

    @Test
    @DisplayName("Devrait calculer correctement la somme d'un vecteur")
    void testSum() {
        Vector v = new ArrayVector(1.0, 2.5, 3.5, 4.0);
        assertEquals(11.0, DescriptiveStatistics.sum(v));
    }

    @Test
    @DisplayName("Devrait calculer correctement la moyenne")
    void testMean() {
        Vector v = new ArrayVector(1.0, 2.0, 3.0, 4.0);
        assertEquals(2.5, DescriptiveStatistics.mean(v));
    }

    @Test
    @DisplayName("La moyenne d'un vecteur vide devrait renvoyer NaN")
    void testMeanEmpty() {
        // Simulation d'un vecteur vide (si ton architecture le permet via un mock ou une sous-classe)
        Vector emptyVector = new Vector() {
            @Override public int size() { return 0; }
            @Override public double getValue(int index) { throw new IndexOutOfBoundsException(); }
            @Override public Vector add(Vector other) { return null; }
            @Override public Vector minus(Vector other) { return null; }
            @Override public double dot(Vector other) { return 0; }
            @Override public double norm() { return 0; }
        };
        assertTrue(Double.isNaN(DescriptiveStatistics.mean(emptyVector)));
    }

    @Test
    @DisplayName("Devrait calculer la variance échantillonnale brute (divisé par n-1)")
    void testVarianceSample() {
        // Échantillon : [10.0, 12.0, 16.0, 20.0] -> Moyenne = 14.5
        // Écarts à la moyenne : [-4.5, -2.5, 1.5, 5.5]
        // Carrés des écarts : [20.25, 6.25, 2.25, 30.25] -> Somme = 59.0
        // Variance échantillonnale (n-1 = 3) : 59.0 / 3 = 19.666666666666668
        Vector v = new ArrayVector(10.0, 12.0, 16.0, 20.0);

        double expectedVariance = 59.0 / 3.0;
        assertEquals(expectedVariance, DescriptiveStatistics.variance(v), 1e-12);
    }

    @Test
    @DisplayName("La variance d'un vecteur de taille <= 1 devrait renvoyer NaN")
    void testVarianceEdgeCases() {
        Vector single = new ArrayVector(42.0);
        assertTrue(Double.isNaN(DescriptiveStatistics.variance(single)));
    }

    @Test
    @DisplayName("Devrait calculer correctement l'écart-type (Standard Deviation)")
    void testStandardDeviation() {
        // Échantillon [3.0, 5.0] -> Moyenne = 4.0
        // Écarts : [-1.0, 1.0] -> Carrés : [1.0, 1.0] -> Somme = 2.0
        // Variance échantillonnale (2 - 1 = 1) : 2.0 / 1 = 2.0
        // Écart-type : sqrt(2.0) = 1.4142135623730951
        Vector v = new ArrayVector(3.0, 5.0);
        assertEquals(Math.sqrt(2.0), DescriptiveStatistics.standardDeviation(v), 1e-12);
    }

    @Test
    @DisplayName("min() doit lever une IllegalArgumentException si le vecteur est nul")
    void minShouldThrowExceptionOnNullVector() {
        assertThrows(IllegalArgumentException.class, () -> DescriptiveStatistics.min(null));
    }

    @Test
    @DisplayName("max() doit lever une IllegalArgumentException si le vecteur est nul")
    void maxShouldThrowExceptionOnNullVector() {
        assertThrows(IllegalArgumentException.class, () -> DescriptiveStatistics.max(null));
    }

    @Test
    @DisplayName("min() doit lever une IllegalArgumentException si le vecteur est vide")
    void minShouldThrowExceptionOnEmptyVector() {
        Vector emptyVector = new ArrayVector(new double[]{});
        assertThrows(IllegalArgumentException.class, () -> DescriptiveStatistics.min(emptyVector));
    }

    @Test
    @DisplayName("max() doit lever une IllegalArgumentException si le vecteur est vide")
    void maxShouldThrowExceptionOnEmptyVector() {
        Vector emptyVector = new ArrayVector(new double[]{});
        assertThrows(IllegalArgumentException.class, () -> DescriptiveStatistics.max(emptyVector));
    }

    @Test
    @DisplayName("min() et max() doivent renvoyer le même élément unique si le vecteur n'a qu'une seule valeur")
    void singleElementVectorShouldReturnSameValueForMinAndMax() {
        Vector singleVector = new ArrayVector(new double[]{42.5});

        assertEquals(42.5, DescriptiveStatistics.min(singleVector), 1e-9);
        assertEquals(42.5, DescriptiveStatistics.max(singleVector), 1e-9);
    }
}

@Nested
@DisplayName("Tests de calcul des valeurs min et max sur des séries réelles")
class CalculationTests {

    @Test
    @DisplayName("Doit trouver correctement le minimum et le maximum parmi des valeurs positives et négatives")
    void shouldFindCorrectMinAndMaxWithMixedNumbers() {
        // min = -15.2, max = 100.0
        Vector vector = new ArrayVector(new double[]{0.5, -2.3, 100.0, -15.2, 42.0, 0.0});

        assertEquals(-15.2, DescriptiveStatistics.min(vector), 1e-9, "Le minimum calculé est incorrect.");
        assertEquals(100.0, DescriptiveStatistics.max(vector), 1e-9, "Le maximum calculé est incorrect.");
    }

    @Test
    @DisplayName("Doit fonctionner correctement quand le min ou max est le premier ou le dernier élément")
    void shouldFindMinAndMaxAtExtremities() {
        // Premier = min, Dernier = max
        Vector vectorFirstLast = new ArrayVector(new double[]{-50.0, 10.0, 20.0, 50.0});

        assertEquals(-50.0, DescriptiveStatistics.min(vectorFirstLast), 1e-9);
        assertEquals(50.0, DescriptiveStatistics.max(vectorFirstLast), 1e-9);
    }

    @Test
    @DisplayName("Doit fonctionner correctement lorsque toutes les valeurs sont négatives")
    void shouldHandleAllNegativeNumbers() {
        Vector vectorAllNegative = new ArrayVector(new double[]{-10.0, -2.5, -100.0, -0.1});

        assertEquals(-100.0, DescriptiveStatistics.min(vectorAllNegative), 1e-9);
        assertEquals(-0.1, DescriptiveStatistics.max(vectorAllNegative), 1e-9);
    }

    @Test
    @DisplayName("Doit gérer correctement un vecteur avec des valeurs constantes identiques")
    void shouldHandleConstantValues() {
        Vector constantVector = new ArrayVector(new double[]{3.14, 3.14, 3.14});

        assertEquals(3.14, DescriptiveStatistics.min(constantVector), 1e-9);
        assertEquals(3.14, DescriptiveStatistics.max(constantVector), 1e-9);
    }

}

@Nested
@DisplayName("Cas limites et validation des entrées")
class EdgeCases {

    private static final double EPSILON = 1e-6;

    @Test
    @DisplayName("Retourne 0.0 quand n < 3 (échantillon insuffisant)")
    void shouldReturnZeroForInsufficientData() {
        Vector oneElement = new ArrayVector(5.0);
        Vector twoElements = new ArrayVector(1.0, 2.0);

        assertEquals(0.0, DescriptiveStatistics.skewness(oneElement), EPSILON);
        assertEquals(0.0, DescriptiveStatistics.skewness(twoElements), EPSILON);
    }

    @Test
    @DisplayName("Retourne 0.0 pour un vecteur constant (écart-type nul)")
    void shouldReturnZeroForConstantVector() {
        Vector constant = new ArrayVector(4.2, 4.2, 4.2, 4.2, 4.2);

        assertEquals(0.0, DescriptiveStatistics.skewness(constant), EPSILON);
    }
}

@Nested
@DisplayName("Propriétés statistiques et symétrie")
class StatisticalProperties {
    private static final double EPSILON = 1e-6;

    @Test
    @DisplayName("Retourne 0.0 pour une distribution parfaitement symétrique")
    void shouldReturnZeroForSymmetricDistribution() {
        Vector symmetric = new ArrayVector(1.0, 2.0, 3.0, 4.0, 5.0);

        assertEquals(0.0, DescriptiveStatistics.skewness(symmetric), EPSILON);
    }

    @Test
    @DisplayName("Calcule une skewness positive (queue étirée à droite)")
    void shouldCalculatePositiveSkewness() {
        Vector rightSkewed = new ArrayVector(1.0, 2.0, 2.5, 3.0, 100.0);

        double result = DescriptiveStatistics.skewness(rightSkewed);
        assertTrue(result > 0.0, "La skewness devrait être strictement positive mais était: " + result);
    }

    @Test
    @DisplayName("Calcule une skewness négative (queue étirée à gauche)")
    void shouldCalculateNegativeSkewness() {
        Vector leftSkewed = new ArrayVector(-100.0, 1.0, 2.0, 2.5, 3.0);

        double result = DescriptiveStatistics.skewness(leftSkewed);
        assertTrue(result < 0.0, "La skewness devrait être strictly négative mais était: " + result);
    }
}

@Nested
@DisplayName("Conformité des calculs et invariants")
class CalculationValidation {
    private static final double EPSILON = 1e-6;

    @Test
    @DisplayName("Conforme aux valeurs de référence (Excel SKEW / R e1071)")
    void shouldMatchKnownSampleSkewnessValue() {
        Vector sample = new ArrayVector(2.0, 8.0, 0.0, 4.0, 1.0, 9.0);
        double expectedSkewness = 0.515432;

        assertEquals(expectedSkewness, DescriptiveStatistics.skewness(sample), 1e-4);
    }

    @Test
    @DisplayName("Invariant par translation (shift) et changement d'échelle positif (scale)")
    void shouldBeInvariantUnderShiftAndPositiveScale() {
        Vector base = new ArrayVector(1.5, 2.0, 2.1, 4.5, 12.0);
        double originalSkewness = DescriptiveStatistics.skewness(base);

        // Y = 10 * X + 5 (a > 0 conserve la direction de la skewness)
        Vector transformed =new ArrayVector(
                10 * 1.5 + 5.0,
                10 * 2.0 + 5.0,
                10 * 2.1 + 5.0,
                10 * 4.5 + 5.0,
                10 * 12.0 + 5.0
        );

        assertEquals(originalSkewness, DescriptiveStatistics.skewness(transformed), EPSILON);
    }
}



