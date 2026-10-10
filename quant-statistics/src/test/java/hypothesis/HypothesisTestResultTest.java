package hypothesis;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class HypothesisTestResultTest {

    @Test
    void shouldCreateValidResult() {
        HypothesisTestResult result = new HypothesisTestResult(1.96, 0.05);

        assertTrue(result.testStatistic() == 1.96);
        assertTrue(result.pValue() == 0.05);
    }

    @Test
    void shouldThrowExceptionWhenMetricIsNan() {
        assertThrows(IllegalArgumentException.class, () -> new HypothesisTestResult(Double.NaN, 0.05));
        assertThrows(IllegalArgumentException.class, () -> new HypothesisTestResult(1.96, Double.NaN));
    }

    @Test
    void shouldThrowExceptionWhenPValueOutOfBounds() {
        assertThrows(IllegalArgumentException.class, () -> new HypothesisTestResult(1.96, -0.01));
        assertThrows(IllegalArgumentException.class, () -> new HypothesisTestResult(1.96, 1.01));
    }

    @Test
    void shouldCorrectlyEvaluateRejection() {
        HypothesisTestResult result = new HypothesisTestResult(2.58, 0.01);

        // pValue (0.01) <= alpha (0.05) -> Rejet de H0
        assertTrue(result.isRejected(0.05));

        // pValue (0.01) > alpha (0.001) -> Pas de rejet de H0
        assertFalse(result.isRejected(0.001));
    }

    @Test
    void shouldThrowExceptionWhenAlphaIsInvalid() {
        HypothesisTestResult result = new HypothesisTestResult(1.96, 0.05);

        assertThrows(IllegalArgumentException.class, () -> result.isRejected(0.0));
        assertThrows(IllegalArgumentException.class, () -> result.isRejected(1.0));
        assertThrows(IllegalArgumentException.class, () -> result.isRejected(-0.05));
        assertThrows(IllegalArgumentException.class, () -> result.isRejected(1.5));
    }
}