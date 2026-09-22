package org.statistics.probability.tools;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GammaTest {

    private static final double EPSILON = 1e-12;

    @Test
    @DisplayName("Propriété factorielle : Γ(n) = (n - 1)!")
    void testFactorialProperty() {
        assertEquals(1.0, org.math.function.special.Gamma.gamma(1.0), EPSILON); // 0! = 1
        assertEquals(1.0, org.math.function.special.Gamma.gamma(2.0), EPSILON); // 1! = 1
        assertEquals(2.0, org.math.function.special.Gamma.gamma(3.0), EPSILON); // 2! = 2
        assertEquals(6.0, org.math.function.special.Gamma.gamma(4.0), EPSILON); // 3! = 6
        assertEquals(24.0, org.math.function.special.Gamma.gamma(5.0), EPSILON); // 4! = 24
        assertEquals(120.0, org.math.function.special.Gamma.gamma(6.0), EPSILON); // 5! = 120
    }

    @Test
    @DisplayName("Valeur remarquable : Γ(0.5) = √π")
    void testHalfValue() {
        double expected = Math.sqrt(Math.PI);
        assertEquals(expected, org.math.function.special.Gamma.gamma(0.5), EPSILON);
    }

    @Test
    @DisplayName("Stabilité du logGamma sur de grands nombres sans Overflow")
    void testLogGammaLargeValues() {
        // x = 300 dépasse la capacité d'un double (1.79e308), mais logGamma reste parfaitement stable
        double logGammaVal = org.math.function.special.Gamma.logGamma(300.0);
        assertFalse(Double.isInfinite(logGammaVal));
        assertTrue(logGammaVal > 0);

        // gamma(300.0) doit renvoyer POSITIVE_INFINITY proprement
        assertEquals(Double.POSITIVE_INFINITY, org.math.function.special.Gamma.gamma(300.0));
    }

    @Test
    @DisplayName("Gestion des cas limites (NaN et valeurs négatives invalides)")
    void testEdgeCases() {
        assertTrue(Double.isNaN(org.math.function.special.Gamma.gamma(0.0)));
        assertTrue(Double.isNaN(org.math.function.special.Gamma.gamma(-1.0)));
        assertTrue(Double.isNaN(org.math.function.special.Gamma.logGamma(-5.0)));
    }
}