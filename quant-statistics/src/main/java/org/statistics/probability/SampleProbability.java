package org.statistics.probability;

import java.util.Arrays;

public class SampleProbability<T> implements ProbabilityMeasure<T>{

    private final T[] samples;

    public SampleProbability(T... samples){
        if (samples == null || samples.length == 0) {
            throw new IllegalArgumentException("Samples must not be null or empty.");
        }
        this.samples = samples.clone();
    }

    @Override
    public double probability(Event<T> event) {
       return Arrays.stream(samples).filter(event::occurs).count() / (double) samples.length;
    }
}
