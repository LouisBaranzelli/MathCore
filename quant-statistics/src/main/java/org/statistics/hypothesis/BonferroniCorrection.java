package org.statistics.hypothesis;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BonferroniCorrection {

    /*
    Si tu implémentes un moteur de backtesting ou d'analyse statistique dans ta bibliothèque Java :

    Le piège : Si tu scannes l'univers des actions (disons 500 actions du S&P 500) pour y chercher des anomalies statistiques ou des relations de co-intégration, tu vas réaliser des centaines de tests simultanés.

    La conséquence : Sans correction, tes résultats de backtest seront truffés de faux signaux dus au hasard.

    Le réflexe Bonferroni (ou équivalent) : Si tu automatises la découverte de signaux, tu dois intégrer une correction des p-valeurs (comme Bonferroni, ou des méthodes plus modernes comme FDR / Benjamini-Hochberg si les tests sont corrélés) pour t'assurer que les patterns détectés ne sont pas des mirages statistiques.
     */

    private static final Logger log = LoggerFactory.getLogger(BonferroniCorrection.class);

    public static class Result {
        private final Vector adjustedPValues;
        private final boolean[] rejections;

        public Result(Vector adjustedPValues, boolean[] rejections) {
            this.adjustedPValues = adjustedPValues;
            this.rejections = rejections;
        }

        public Vector getAdjustedPValues() {
            return adjustedPValues;
        }

        public boolean[] getRejections() {
            return rejections;
        }
    }

    /**
     * Applies Bonferroni correction on a Vector of raw p-values.
     *
     * @param pValues Vector containing m p-values from independent tests
     * @param alpha   The global significance level desired (e.g., 0.05)
     * @return A Result object containing the adjusted p-values as a Vector and rejection decisions
     */
    public static Result evaluate(Vector pValues, double alpha) {
        if (pValues == null) {
            log.error("Attempted to apply Bonferroni correction with a null p-values vector.");
            throw new IllegalArgumentException("The p-values vector cannot be null.");
        }
        if (alpha <= 0.0 || alpha >= 1.0) {
            log.error("Attempted to apply Bonferroni correction with invalid alpha: {}", alpha);
            throw new IllegalArgumentException("Significance level alpha must be strictly between 0 and 1.");
        }

        int m = pValues.size();
        double[] adjustedArray = new double[m];
        boolean[] rejections = new boolean[m];

        log.debug("Applying Bonferroni correction for m = {} tests at global alpha = {}", m, alpha);

        for (int i = 0; i < m; i++) {
            double p = pValues.getValue(i);

            if (p < 0.0 || p > 1.0) {
                log.error("Invalid p-value detected at index {}: {}", i, p);
                throw new IllegalArgumentException("P-value must be between 0.0 and 1.0, got: " + p);
            }

            double adjusted = p * m;
            if (adjusted > 1.0) {
                adjusted = 1.0;
            }
            adjustedArray[i] = adjusted;

            rejections[i] = (adjusted < alpha);
        }

        log.debug("Bonferroni correction successfully computed for {} tests.", m);

        return new Result(new ArrayVector(adjustedArray), rejections);
    }
}