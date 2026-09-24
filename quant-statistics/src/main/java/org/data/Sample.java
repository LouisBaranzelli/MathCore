package org.data;

import java.util.Arrays;
import java.util.Iterator;

/**
 * Représente un échantillon d'observations statistiques.
 * Appartient au module STATS, pas VECTOR.
 */
public final class Sample implements Iterable<Double> {

    private final double[] sortedValues;

    public static Sample of(double... values){
        return new Sample(values);
    }

    public Sample(double[] values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("L'échantillon ne peut pas être vide.");
        }
        this.sortedValues = values.clone();
        Arrays.sort(this.sortedValues);
    }

    public int size() {
        return sortedValues.length;
    }

    public double getSorted(int index) {
        return sortedValues[index];
    }

    public double min() {
        return sortedValues[0];
    }

    public double max() {
        return sortedValues[sortedValues.length - 1];
    }

    public double median() {
        int n = sortedValues.length;
        if (n % 2 == 1) {
            return sortedValues[n / 2];
        } else {
            return (sortedValues[(n / 2) - 1] + sortedValues[n / 2]) / 2.0;
        }
    }

    public int binarySearch(double x) {
        return Arrays.binarySearch(sortedValues, x);
    }

    @Override
    public Iterator<Double> iterator() {
        return new Iterator<Double>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < size();
            }

            @Override
            public Double next() {
                return getSorted(index++);
            }
        };
    }
}
