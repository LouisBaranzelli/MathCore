package org.series.timeserie;


import org.math.vector.Vector;
import org.series.InvalidTimeSerieException;
import org.series.timegrid.TimeGrid;

import java.util.Objects;
import java.util.Random;

/**
 * Générateur de série temporelle aléatoire basée sur une TimeGrid.
 * Implémente DoubleTimeSerie et encapsule un ImmutableDoubleTimeSerie sous le capot.
 */
public final class RandomDoubleTimeSerie implements DoubleTimeSerie {

    private final DoubleTimeSerie delegate;

    /**
     * Génère une série aléatoire (Marche Aléatoire / Random Walk) avec une valeur initiale.
     * Idéal pour simuler des cours de bourse ou des actifs financiers.
     */
    public RandomDoubleTimeSerie(TimeGrid timeGrid, double initialValue, double volatility, Random random) throws InvalidTimeSerieException {
        Objects.requireNonNull(timeGrid, "timeGrid cannot be null");
        Objects.requireNonNull(random, "random cannot be null");
        if (timeGrid.size() == 0) {
            throw new IllegalArgumentException("timeGrid cannot be empty");
        }

        double[] generatedValues = generateRandomWalk(timeGrid.size(), initialValue, volatility, random);
        this.delegate = new ImmutableDoubleTimeSerie(timeGrid, generatedValues);
    }


    public RandomDoubleTimeSerie(TimeGrid timeGrid, double initialValue, double volatility) throws InvalidTimeSerieException {
        this(timeGrid, initialValue, volatility, new Random());
    }


    private static double[] generateRandomWalk(int size, double initialValue, double volatility, Random random) {
        double[] values = new double[size];
        values[0] = initialValue;

        for (int i = 1; i < size; i++) {
            double returnRate = random.nextGaussian() * volatility;
            double nextValue = values[i - 1] * (1.0 + returnRate);

            values[i] = Math.max(0.0001, nextValue);
        }
        return values;
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public double getValue(int index) {
        return delegate.getValue(index);
    }

    @Override
    public long getTimestamp(int index) {
        return delegate.getTimestamp(index);
    }

    @Override
    public Vector toVector() {
        return delegate.toVector();
    }
}