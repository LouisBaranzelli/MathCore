package org.statistics.estimator.nonparametric.test;

import org.junit.jupiter.api.Test;
import org.statistics.data.CategoricalMultinomialData;
import org.statistics.data.CategoricalProbabilityDistribution;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PearsonChiSquareGoodnessOfFitTestTest {

    @Test
    void shouldEvaluateChiSquareSuccessfullyWithFixedParameters() {
        // Given
        Map<String, Integer> counts = Map.of(
                "CategoryA", 50,
                "CategoryB", 30,
                "CategoryC", 20
        );
        CategoricalMultinomialData observedData = new CategoricalMultinomialData(counts);

        Map<String, Double> probabilities = Map.of(
                "CategoryA", 0.5,
                "CategoryB", 0.3,
                "CategoryC", 0.2
        );
        CategoricalProbabilityDistribution theoreticalDist = new CategoricalProbabilityDistribution(probabilities);

        PearsonChiSquareGoodnessOfFitTest tester = new PearsonChiSquareGoodnessOfFitTest();

        // When
        GoodnessOfFitResult result = tester.evaluate(observedData, theoreticalDist);

        // Then
        assertTrue(result.totalObservations() == 100);
        assertTrue(result.degreesOfFreedom() == 2);
        assertTrue(Math.abs(result.chiSquareStatistic() - 0.0) < 1e-6); // Correspondance parfaite -> T = 0
        assertTrue(Math.abs(result.pValue() - 1.0) < 1e-6);
    }

    @Test
    void shouldEvaluateChiSquareSuccessfullyWithOthetFixedParameters() {
        // Given
        Map<String, Integer> counts = Map.of(
                "CategoryA", 15,
                "CategoryB", 10,
                "CategoryC", 15
        );
        CategoricalMultinomialData observedData = new CategoricalMultinomialData(counts);

        Map<String, Double> probabilities = Map.of(
                "CategoryA", 0.5,
                "CategoryB", 0.3,
                "CategoryC", 0.2
        );
        CategoricalProbabilityDistribution theoreticalDist = new CategoricalProbabilityDistribution(probabilities);

        PearsonChiSquareGoodnessOfFitTest tester = new PearsonChiSquareGoodnessOfFitTest();

        GoodnessOfFitResult result = tester.evaluate(observedData, theoreticalDist);

        assertTrue(result.pValue() < 0.05);
    }

    @Test
    void shouldEvaluateChiSquareWithEstimatedParameters() {
        // Given
        Map<String, Integer> counts = Map.of(
                "Cat1", 40,
                "Cat2", 35,
                "Cat3", 25
        );
        CategoricalMultinomialData observedData = new CategoricalMultinomialData(counts);

        Map<String, Double> probabilities = Map.of(
                "Cat1", 0.33,
                "Cat2", 0.33,
                "Cat3", 0.34
        );
        CategoricalProbabilityDistribution theoreticalDist = new CategoricalProbabilityDistribution(probabilities);

        PearsonChiSquareGoodnessOfFitTest tester = new PearsonChiSquareGoodnessOfFitTest();

        // When (m = 1 paramètre estimé)
        GoodnessOfFitResult result = tester.evaluate(observedData, theoreticalDist, 1);

        // Then: df = k - 1 - m = 3 - 1 - 1 = 1
        assertTrue(result.degreesOfFreedom() == 1);
        assertTrue(result.chiSquareStatistic() > 0.0);
        assertTrue(result.pValue() >= 0.0 && result.pValue() <= 1.0);
    }

    @Test
    void shouldThrowExceptionWhenDegreesOfFreedomAreNonPositive() {
        // Given: K = 1 catégorie, donc df = 1 - 1 - 0 = 0 -> invalide
        Map<String, Integer> counts = Map.of("OnlyCat", 10);
        CategoricalMultinomialData observedData = new CategoricalMultinomialData(counts);

        Map<String, Double> probabilities = Map.of("OnlyCat", 1.0);
        CategoricalProbabilityDistribution theoreticalDist = new CategoricalProbabilityDistribution(probabilities);

        PearsonChiSquareGoodnessOfFitTest tester = new PearsonChiSquareGoodnessOfFitTest();

        // When & Then
        assertThrows(IllegalStateException.class, () -> {
            tester.evaluate(observedData, theoreticalDist, 0);
        });
    }

    @Test
    void shouldThrowExceptionWhenNullInputsProvided() {
        PearsonChiSquareGoodnessOfFitTest tester = new PearsonChiSquareGoodnessOfFitTest();

        assertThrows(IllegalArgumentException.class, () -> {
            tester.evaluate(null, null);
        });
    }
}