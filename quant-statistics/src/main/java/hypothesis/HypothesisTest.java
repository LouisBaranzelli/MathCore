package hypothesis;

@FunctionalInterface
public interface HypothesisTest<S> {

    /**
     * Exécute le test d'hypothèse sur l'échantillon fourni.
     *
     * @param sample L'échantillon de données.
     * @param alpha Le seuil de signification (ex: 0.05 pour 5%).
     * @return Le résultat du test d'hypothèse.
     */
    HypothesisTestResult test(S sample, double alpha);
}