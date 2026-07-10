package org.statistics.probability;


public interface ProbabilityMeasure<T> {

    double probability(Event<T> event);

    default double conditionalProbability(
            Event<T> event,
            Event<T> given) {

        double pGiven = probability(given);

        if (pGiven == 0.0) {
            throw new IllegalArgumentException(
                    "Conditional probability is undefined because P(given) = 0."
            );        }

        return probability(event.and(given))
                / pGiven;
    }
}
