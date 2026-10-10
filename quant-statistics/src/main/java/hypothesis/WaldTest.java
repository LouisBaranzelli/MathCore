package hypothesis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;

public class WaldTest {
    private static final Logger log = LoggerFactory.getLogger(WaldTest.class);

    private final ContinuousDistribution referenceDistribution;

    public WaldTest(ContinuousDistribution referenceDistribution) {
        if (referenceDistribution == null) {
            throw new IllegalArgumentException("Reference distribution must not be null.");
        }
        this.referenceDistribution = referenceDistribution;
        log.debug("Initialized WaldTest with distribution: {}", referenceDistribution.getClass().getSimpleName());
    }

    /**
     * Calcule la statistique de Wald et la p-value associée.
     *
     * @ Estimation ponctuelle du paramètre (\hat{\theta})
     * @ standardError Erreur standard de l'estimation (SE)
     * @ nullValue Valeur du paramètre sous l'hypothèse nulle (\theta_0)
     */

    public HypothesisTestResult test(double estimate, double standardError, double nullValue) {
        if (standardError <= 0.0) {
            log.error("Invalid standard error provided: {}", standardError);
            throw new IllegalArgumentException("Standard error must be strictly positive.");
        }

        double w = (estimate - nullValue) / standardError;
        double absW = Math.abs(w);

        // p-value pour un test bilatéral
        double pValue = 2.0 * (1.0 - referenceDistribution.cdf(absW));

        log.trace("Wald test computed: stat={}, pValue={}", w, pValue);
        return new HypothesisTestResult(w, pValue);
    }
}