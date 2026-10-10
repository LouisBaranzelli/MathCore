package hypothesis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public record HypothesisTestResult(
        double testStatistic,
        double pValue
) {
    private static final Logger log = LoggerFactory.getLogger(HypothesisTestResult.class);

    public HypothesisTestResult {
        if (Double.isNaN(testStatistic) || Double.isNaN(pValue)) {
            log.error("Invalid hypothesis test result: testStatistic={}, pValue={}", testStatistic, pValue);
            throw new IllegalArgumentException("Test results cannot be NaN.");
        }
        if (pValue < 0.0 || pValue > 1.0) {
            log.error("P-value out of bounds: pValue={}", pValue);
            throw new IllegalArgumentException("P-value must be between 0 and 1.");
        }
        log.trace("HypothesisTestResult created successfully with statistic={} and pValue={}", testStatistic, pValue);
    }

    public boolean isRejected(double alpha) {
        if (alpha <= 0.0 || alpha >= 1.0) {
            log.warn("Invalid significance level alpha provided: alpha={}", alpha);
            throw new IllegalArgumentException("Significance level alpha must be between 0 and 1.");
        }
        boolean rejected = pValue <= alpha;
        log.debug("Evaluated test rejection at alpha={}: pValue={}, rejected={}", alpha, pValue, rejected);
        return rejected;
    }
}