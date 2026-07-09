package org.statistics.probability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SampleProbabilityTest {

    @Test
    void shouldComputeProbabilityOfEvenNumbers() {
        SampleProbability<Integer> probability =
                new SampleProbability<>(1, 2, 3, 4, 5, 6);

        Event<Integer> even = new Event<>(x -> x % 2 == 0);

        assertEquals(0.5, probability.probability(even));
    }

    @Test
    void shouldReturnOneWhenEventAlwaysOccurs() {
        SampleProbability<Integer> probability =
                new SampleProbability<>(1, 2, 3, 4);

        Event<Integer> always = new Event<>(x -> true);

        assertEquals(1.0, probability.probability(always));
    }

    @Test
    void shouldReturnZeroWhenEventNeverOccurs() {
        SampleProbability<Integer> probability =
                new SampleProbability<>(1, 2, 3, 4);

        Event<Integer> never = new Event<>(x -> false);

        assertEquals(0.0, probability.probability(never));
    }

    @Test
    void shouldComputeProbabilityWithDuplicates() {
        SampleProbability<Integer> probability =
                new SampleProbability<>(1, 1, 2, 2, 2);

        Event<Integer> even = new Event<>(x -> x % 2 == 0);

        assertEquals(0.6, probability.probability(even));
    }

    @Test
    void shouldComputeProbabilityForObjects() {
        record Person(String name, int age) {}

        SampleProbability<Person> probability =
                new SampleProbability<>(
                        new Person("Alice", 18),
                        new Person("Bob", 35),
                        new Person("Charlie", 42)
                );

        Event<Person> adult = new Event<>(p -> p.age() >= 30);

        assertEquals(2.0 / 3.0, probability.probability(adult));
    }
}