package org.statistics.probability;

import org.junit.jupiter.api.DisplayName;
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
            @Override public int getSize() { return 0; }
            @Override public double getValue(int index) { throw new IndexOutOfBoundsException(); }
            @Override public org.math.common.Shape getShape() { return new org.math.common.Shape(0, 1); }
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
}