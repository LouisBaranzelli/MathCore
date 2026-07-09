package org.statistics.probability;

import java.util.Arrays;
import java.util.function.Predicate;

public class Event<T> {

    private final Predicate<T> predicate;

    public Event(Predicate<T> predicate) {
        this.predicate = predicate;
    }

    boolean occurs(T state){
        return predicate.test(state);
    }

    public Event<T> and(Event<T>... others){
        return new Event<>(state -> predicate.test(state) && Arrays.stream(others).allMatch(event -> event.occurs(state)));
    }

    public Event<T> or(Event<T>... others){
        return new Event<>(state -> predicate.test(state) || Arrays.stream(others).anyMatch(event -> event.occurs(state)));
    }

    public Event<T> not(){
        return new Event<>((state) -> !predicate.test(state));
    }
}
