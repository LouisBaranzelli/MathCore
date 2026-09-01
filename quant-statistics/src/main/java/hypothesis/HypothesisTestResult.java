package hypothesis;

/**
 * Encapsule le résultat d'un test d'hypothèse statistique.
 *
 * @param testStatistic La valeur de la statistique de test (ex: score Z, score t).
 * @param pValue La p-value associée.
 * @param alpha Le seuil de signification (ex: 0.05).
 * @param rejectNull Vrai si l'hypothèse nulle H0 est rejetée au seuil alpha.
 */
public record HypothesisTestResult(
        double testStatistic,
        double pValue,
        double alpha,
        boolean rejectNull
) {
    public HypothesisTestResult {
        if (alpha <= 0.0 || alpha >= 1.0) {
            throw new IllegalArgumentException("threshold alpha must be between 0 and 1.");
        }
    }
}