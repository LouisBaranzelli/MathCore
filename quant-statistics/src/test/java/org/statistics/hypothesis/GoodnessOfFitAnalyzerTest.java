package org.statistics.hypothesis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.statistics.distributions.monovariate.continuous.NormalDistribution;
import org.statistics.frequentist.estimator.parametric.mle.ChiSquareTestResult;
import org.statistics.frequentist.hypothesis.GoodnessOfFitAnalyzer;

import static org.junit.jupiter.api.Assertions.*;

class GoodnessOfFitAnalyzerTest {

    @Test
    @DisplayName("Test d'adéquation avec une loi normale et des paramètres fixés a priori (s = 0)")
    void testGoodnessOfFitNormalFixedParameters() {
        // Modèle théorique N(0, 1)
        NormalDistribution distribution = new NormalDistribution(0.0, 1.0);

        // Génération d'un échantillon synthétique de taille n = 50 basé sur les quantiles théoriques
        // pour garantir une adéquation parfaite sous H0.
        int n = 50;
        double[] data = new double[n];
        for (int i = 0; i < n; i++) {
            double p = (i + 0.5) / n;
            data[i] = distribution.inverseCdf(p);
        }
        Vector sample = new ArrayVector(data);

        ChiSquareTestResult result = GoodnessOfFitAnalyzer.testNormalityOrModel(sample, distribution, 0);

        assertNotNull(result, "Le résultat du test ne doit pas être null.");
        assertTrue(result.getStatistic() >= 0.0, "La statistique du Chi-2 doit être positive.");
        // k = 5, s = 0 -> df = 5 - 1 - 0 = 4
        assertEquals(4, result.getDegreesOfFreedom(), "Les degrés de liberté doivent être k - 1 - s.");
        assertTrue(result.getPValue() >= 0.0 && result.getPValue() <= 1.0, "La p-value doit être entre 0 et 1.");
        assertTrue(!result.rejectNull(0.5), "Le modèle ne doit pas être rejeté pour des données conformes.");
    }

    @Test
    @DisplayName("Test d'adéquation avec des paramètres estimés (ex: s = 2 pour moyenne et variance)")
    void testGoodnessOfFitNormalEstimatedParameters() {
        NormalDistribution distribution = new NormalDistribution(0.0, 1.0);

        int n = 60; // k = 60 / 10 = 6 bacs
        double[] data = new double[n];
        for (int i = 0; i < n; i++) {
            data[i] = distribution.inverseCdf((i + 0.5) / n);
        }
        Vector sample = new ArrayVector(data);

        // s = 2 (estimation de μ et σ sur l'échantillon)
        int estimatedParametersCount = 2;
        ChiSquareTestResult result = GoodnessOfFitAnalyzer.testNormalityOrModel(sample, distribution, estimatedParametersCount);

        assertNotNull(result);
        // k = 6, s = 2 -> df = 6 - 1 - 2 = 3
        assertEquals(3, result.getDegreesOfFreedom(), "Degrés de liberté incorrects avec paramètres estimés.");
        assertTrue(result.getStatistic() >= 0.0);
    }

    @Test
    @DisplayName("Vérification de l'exception si la taille de l'échantillon est trop petite (< 30)")
    void testSampleSizeTooSmallThrowsException() {
        NormalDistribution distribution = new NormalDistribution(0.0, 1.0);

        // Échantillon de taille 20 (inférieur à MIN_SAMPLE_SIZE = 30)
        double[] smallData = new double[20];
        Vector smallSample = new ArrayVector(smallData);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            GoodnessOfFitAnalyzer.testNormalityOrModel(smallSample, distribution, 0);
        }, "Un échantillon trop petit doit lever une IllegalArgumentException.");

        assertTrue(exception.getMessage().contains("Sample size"), "Le message d'erreur doit mentionner la taille de l'échantillon.");
    }

    @Test
    @DisplayName("Test d'adéquation avec une p-value faible (données non conformes au modèle)")
    void testGoodnessOfFitLowPValue() {
        // Modèle théorique de référence attendu : N(0, 1)
        NormalDistribution theoreticalDistribution = new NormalDistribution(0.0, 1.0);

        // Échantillon réel généré à partir d'une distribution complètement décalée : N(3.0, 1.0)
        NormalDistribution actualDataDistribution = new NormalDistribution(3.0, 1.0);
        int n = 50;
        double[] data = new double[n];
        for (int i = 0; i < n; i++) {
            data[i] = actualDataDistribution.inverseCdf((i + 0.5) / n);
        }
        Vector sample = new ArrayVector(data);

        // Exécution du test d'adéquation avec le modèle N(0,1)
        ChiSquareTestResult result = GoodnessOfFitAnalyzer.testNormalityOrModel(sample, theoreticalDistribution, 0);

        assertNotNull(result, "Le résultat ne doit pas être null.");
        assertTrue(result.getStatistic() > 0.0, "La statistique du Chi-2 doit être strictement positive.");

        // La p-value doit être très proche de 0 (inférieure au seuil alpha de 5%)
        assertTrue(result.getPValue() < 0.05,
                "La p-value devrait être faible pour des données non conformes, valeur actuelle : " + result.getPValue());

        // Le test doit rejeter l'hypothèse nulle
        assertTrue(result.rejectNull(0.05), "Le modèle théorique doit être rejeté pour cet échantillon.");
    }
}