package org.statistics.probability.distributions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class NormalDistributionTest {

    private static final double EPSILON = 1e-6;

    @Test
    @DisplayName("Devrait lever une exception si sigma est invalide")
    void testInvalidConstructor() {
        assertThrows(IllegalArgumentException.class, () -> new NormalDistribution(0, 0));
        assertThrows(IllegalArgumentException.class, () -> new NormalDistribution(0, -1.5));
    }

    @ParameterizedTest
    @CsvSource({
            "0.0, 1.0",   // N(0,1) Standard
            "10.0, 2.5",  // N(10, 2.5) Financière (ex: rendements décalés)
            "-5.0, 0.1"   // N(-5, 0.1) Petite variance
    })
    @DisplayName("La médiane (p=0.5) doit correspondre exactement à mu")
    void testMedianAndSymmetry(double mu, double sigma) {
        NormalDistribution dist = new NormalDistribution(mu, sigma);

        assertEquals(0.5, dist.cdf(mu), EPSILON);
        assertEquals(mu, dist.inverseCdf(0.5), EPSILON);
    }

    @Test
    @DisplayName("Validation de la PDF (densité) par rapport à des valeurs de référence")
    void testDensityValues() {
        NormalDistribution standard = new NormalDistribution(0, 1);

        // Densité max au sommet de la cloche : 1 / sqrt(2*pi) ≈ 0.3989422804014327
        assertEquals(0.3989422804014327, standard.density(0.0), EPSILON);
        // Densité à 1 écart-type
        assertEquals(0.24197072451914337, standard.density(1.0), EPSILON);
        assertEquals(0.24197072451914337, standard.density(-1.0), EPSILON); // Symétrie
    }

    @ParameterizedTest
    @CsvSource({
            "-1.959963984540054,  0.025",  // Borne basse d'un intervalle de confiance à 95%
            "-0.6744897501960817, 0.25",   // Premier Quartile (Q1)
            "0.0,                 0.5",    // Médiane
            "0.6744897501960817,  0.75",   // Troisième Quartile (Q3)
            "1.959963984540054,   0.975",  // Borne haute d'un intervalle de confiance à 95%
            "2.3263478740408408,  0.99"    // Seuil de VaR classique à 99%
    })
    @DisplayName("Validation de la CDF et InverseCDF sur une N(0,1)")
    void testStandardNormalReferencePoints(double x, double p) {
        NormalDistribution standard = new NormalDistribution(0, 1);

        assertEquals(p, standard.cdf(x), EPSILON, "Échec CDF pour x = " + x);
        assertEquals(x, standard.inverseCdf(p), EPSILON, "Échec InverseCDF pour p = " + p);
    }

    @Test
    @DisplayName("Validation de la cohérence interne : F^(-1)(F(x)) == x")
    void testIdentityProperty() {
        NormalDistribution dist = new NormalDistribution(100.0, 15.0); // Modélisation type cours d'action
        double[] testPoints = {55.0, 85.0, 100.0, 115.0, 145.0};

        for (double x : testPoints) {
            double p = dist.cdf(x);
            double reconstructedX = dist.inverseCdf(p);
            assertEquals(x, reconstructedX, EPSILON, "Perte de précision sur l'inversion de x = " + x);
        }
    }

    @Test
    @DisplayName("L'Inverse CDF doit rejeter les probabilités hors de ]0,1[")
    void testInverseCdfBounds() {
        NormalDistribution dist = new NormalDistribution(0, 1);

        assertThrows(IllegalArgumentException.class, () -> dist.inverseCdf(0.0));
        assertThrows(IllegalArgumentException.class, () -> dist.inverseCdf(1.0));
        assertThrows(IllegalArgumentException.class, () -> dist.inverseCdf(-0.1));
        assertThrows(IllegalArgumentException.class, () -> dist.inverseCdf(1.1));
    }
}