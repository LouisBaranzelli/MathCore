package org.fin.definitions.indicators;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.estimator.parametric.model.NormalModel;

import static org.junit.jupiter.api.Assertions.*;

class SharpeRatioFunctionTest {

    private static final double EPSILON = 1e-9;
    private NormalModel normalModel;

    @BeforeEach
    void setUp() {
        normalModel = new NormalModel();
    }

    @Test
    @DisplayName("Devrait calculer correctement le ratio de Sharpe pour une loi Normale")
    void shouldCalculateCorrectSharpeRatioForNormalModel() {
        // Given: mu = 0.10 (10%), sigma = 0.15 (15%), rf = 0.02 (2%)
        double riskFreeRate = 0.02;
        SharpeRatioFunction sharpeFunction = new SharpeRatioFunction(normalModel, riskFreeRate);
        Vector theta = new ArrayVector(0.10, 0.15); // [mu, sigma]

        // Expected: (0.10 - 0.02) / 0.15 = 0.08 / 0.15 = 0.533333333...
        double expectedSharpe = (0.10 - 0.02) / 0.15;

        // When
        double actualSharpe = sharpeFunction.evaluate(theta);

        // Then
        assertEquals(expectedSharpe, actualSharpe, EPSILON);
    }

    @Test
    @DisplayName("Devrait retourner un Sharpe négatif lorsque le rendement est inférieur au taux sans risque")
    void shouldReturnNegativeSharpeWhenReturnIsBelowRiskFreeRate() {
        // Given: mu = 0.01, sigma = 0.10, rf = 0.03
        SharpeRatioFunction sharpeFunction = new SharpeRatioFunction(normalModel, 0.03);
        Vector theta = new ArrayVector(0.010, 0.10); // [mu, sigma]

        // When
        double actualSharpe = sharpeFunction.evaluate(theta);

        // Then
        // Expected: (0.01 - 0.03) / 0.10 = -0.20
        assertEquals(-0.20, actualSharpe, EPSILON);
    }

    @Test
    @DisplayName("Devrait retourner zéro lorsque le rendement est égal au taux sans risque")
    void shouldReturnZeroWhenReturnEqualsRiskFreeRate() {
        // Given: mu = 0.02, sigma = 0.20, rf = 0.02
        SharpeRatioFunction sharpeFunction = new SharpeRatioFunction(normalModel, 0.02);
        Vector theta = new ArrayVector(0.02, 0.2); // [mu, sigma]


        // When
        double actualSharpe = sharpeFunction.evaluate(theta);

        // Then
        assertEquals(0.0, actualSharpe, EPSILON);
    }

    @ParameterizedTest(name = "mu={0}, sigma={1}, rf={2} -> Expected Sharpe={3}")
    @CsvSource({
            "0.12, 0.20, 0.02, 0.50",   // (0.12 - 0.02) / 0.20 = 0.50
            "0.15, 0.10, 0.05, 1.00",   // (0.15 - 0.05) / 0.10 = 1.00
            "0.25, 0.15, 0.01, 1.60"    // (0.25 - 0.01) / 0.15 = 1.60
    })
    @DisplayName("Tests paramétrés pour le calcul du Sharpe Ratio")
    void shouldCalculateSharpeRatioParameterized(double mu, double sigma, double rf, double expected) {
        SharpeRatioFunction sharpeFunction = new SharpeRatioFunction(normalModel, rf);
        Vector theta = new ArrayVector(mu, sigma); // [mu, sigma]

        assertEquals(expected, sharpeFunction.evaluate(theta), EPSILON);
    }

    @Test
    @DisplayName("Validation du NormalModel : la variance doit valoir sigma^2")
    void shouldValidateNormalModelMeanAndVariance() {
        Vector params = new ArrayVector(new double[]{0.08, 0.20}); // mu = 0.08, sigma = 0.20

        assertEquals(0.08, normalModel.mean(params), EPSILON);
        // Var(X) = sigma^2 = 0.20 * 0.20 = 0.04
        assertEquals(0.04, normalModel.variance(params), EPSILON);
    }
}