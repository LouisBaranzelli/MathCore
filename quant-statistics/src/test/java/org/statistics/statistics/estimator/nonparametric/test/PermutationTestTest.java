package org.statistics.statistics.estimator.nonparametric.test;

import org.statistics.estimator.nonparametric.test.PermutationTest;
import org.junit.jupiter.api.BeforeEach;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.statistics.data.TwoVectorsStatistic;
import org.statistics.descriptive.DescriptiveStatistics;

import static org.junit.jupiter.api.Assertions.*;
@DisplayName("Permutation Test Suite")
class PermutationTestTest {

    private  TwoVectorsStatistic diffOfMeans;

    @BeforeEach
    public void setUp(){
        diffOfMeans = (a, b) -> {
            double meanA = DescriptiveStatistics.mean(a);
            double meanB = DescriptiveStatistics.mean(b);
            return meanA - meanB;
        };
    }

    @Test
    @DisplayName("Should return a high p-value when samples are drawn from identical distributions")
    void testEvaluateWithIdenticalDistributions() {
        // Given: Two identical samples
        Vector vectorA = new ArrayVector(1.0, 1.0, 3.0, 6.0);
        Vector vectorB = new ArrayVector(1.0, 2.0, 4.0, 4.0);
        PermutationTest test = new PermutationTest(1000, 42L);
        double pValue = test.evaluate(vectorA, vectorB, diffOfMeans);
        assertTrue(pValue > 0.5, "P-value should be high for identical distributions, got: " + pValue);
    }

    @Test
    @DisplayName("Should return a low p-value when samples are drawn from distinct distributions")
    void testEvaluateWithDistinctDistributions() {
        Vector vectorA = new ArrayVector(20, 10, 7, 10);
        Vector vectorB = new ArrayVector(1.0, 2.0, 3.0, 2);

        PermutationTest test = new PermutationTest(1000, 42L);
        double pValue = test.evaluate(vectorA, vectorB, diffOfMeans);
        assertTrue(pValue < 0.05, "P-value should be low for distinct distributions, got: " + pValue);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when number of permutations is zero or negative")
    void testInvalidNumberOfPermutations() {
        // Given / When / Then
        assertThrows(IllegalArgumentException.class, () -> new PermutationTest(0));
        assertThrows(IllegalArgumentException.class, () -> new PermutationTest(-50));
    }

}