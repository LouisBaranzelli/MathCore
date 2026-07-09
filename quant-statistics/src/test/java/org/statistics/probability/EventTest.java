package org.statistics.probability;

import org.junit.jupiter.api.Test;
import org.statistics.probability.Event;

import static org.junit.jupiter.api.Assertions.*;

class EventTest {

    @Test
    void shouldDetectEventOccurrence() {
        Event<Integer> even = new Event<>(x -> x % 2 == 0);

        assertTrue(even.occurs(4));
        assertFalse(even.occurs(3));
    }

    @Test
    void shouldCombineEventsWithAnd() {
        Event<Integer> even = new Event<>(x -> x % 2 == 0);
        Event<Integer> greaterThanTen = new Event<>(x -> x > 10);

        Event<Integer> evenAndGreaterThanTen =
                even.and(greaterThanTen);

        assertTrue(evenAndGreaterThanTen.occurs(12));
        assertFalse(evenAndGreaterThanTen.occurs(8));
        assertFalse(evenAndGreaterThanTen.occurs(13));
    }

    @Test
    void shouldCombineMultipleEventsWithAnd() {
        Event<Integer> even = new Event<>(x -> x % 2 == 0);
        Event<Integer> positive = new Event<>(x -> x > 0);
        Event<Integer> lessThanHundred = new Event<>(x -> x < 100);

        Event<Integer> event =
                even.and(positive, lessThanHundred);

        assertTrue(event.occurs(42));
        assertFalse(event.occurs(-42));
        assertFalse(event.occurs(102));
    }

    @Test
    void shouldCombineEventsWithOr() {
        Event<Integer> negative = new Event<>(x -> x < 0);
        Event<Integer> zero = new Event<>(x -> x == 0);

        Event<Integer> negativeOrZero =
                negative.or(zero);

        assertTrue(negativeOrZero.occurs(-5));
        assertTrue(negativeOrZero.occurs(0));
        assertFalse(negativeOrZero.occurs(10));
    }

    @Test
    void shouldCombineMultipleEventsWithOr() {
        Event<Integer> zero = new Event<>(x -> x == 0);
        Event<Integer> ten = new Event<>(x -> x == 10);
        Event<Integer> hundred = new Event<>(x -> x == 100);

        Event<Integer> event =
                zero.or(ten, hundred);

        assertTrue(event.occurs(0));
        assertTrue(event.occurs(10));
        assertTrue(event.occurs(100));
        assertFalse(event.occurs(50));
    }

    @Test
    void shouldNegateEvent() {
        Event<Integer> even = new Event<>(x -> x % 2 == 0);

        Event<Integer> odd = even.not();

        assertTrue(odd.occurs(3));
        assertFalse(odd.occurs(4));
    }

    @Test
    void shouldHandleEmptyAnd() {
        Event<Integer> positive =
                new Event<>(x -> x > 0);

        Event<Integer> result = positive.and();

        assertTrue(result.occurs(5));
        assertFalse(result.occurs(-5));
    }

    @Test
    void shouldHandleEmptyOr() {
        Event<Integer> positive =
                new Event<>(x -> x > 0);

        Event<Integer> result = positive.or();

        assertTrue(result.occurs(5));
        assertFalse(result.occurs(-5));
    }
}