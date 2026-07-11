package org.statistics.probability;

public class BayesTheoreme {
    public static <T> double posterior(
            ProbabilityMeasure<T> measure,
            Event<T> hypothesis,
            Event<T> evidence) {
        // Bayes:
        //           P(B|A) × P(A)
        // P(A|B) = ----------------
        //                P(B)
        // with:
        // P(A): Prior, probability of the hypothesis
        // P(B): probability of the evidence
        // P(B|A): likelihood  of observing the evidence assuming the hypothesis is true.
        // P(A|B): Posterior (target, become the prior...), probability after observing the evidence.
        double pEvidence = measure.probability(evidence);

        if (pEvidence == 0.0) {
            throw new IllegalArgumentException(
                    "Posterior probability is undefined because P(evidence_B) = 0."
            );
        }

        return measure.conditionalProbability(evidence, hypothesis) * measure.probability(hypothesis)
                / pEvidence;
    }
}
