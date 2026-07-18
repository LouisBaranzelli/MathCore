package org.series.timeserie;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;


public final class SliceDoubleTimeSerie implements DoubleTimeSerie {
    private final DoubleTimeSerie source;
    private final int startIndex;
    private final int length;

        /**
         * Crée une tranche de la série temporelle source entre start (inclus) et end (exclu).
         *
         * @param source     La série temporelle d'origine
         * @param start      L'index de départ (inclus)
         * @param end        L'index de fin (exclu)
         */
        public SliceDoubleTimeSerie(DoubleTimeSerie source, int start, int end) {
            if (source == null) {
                throw new IllegalArgumentException("Source time serie cannot be null");
            }

            this.length = end - start;

            if (start < 0 || this.length <= 0 || end > source.size()) {
                throw new IndexOutOfBoundsException(
                        String.format("Invalid slice boundaries: start=%d, end=%d, sourceSize=%d",
                                start, end, source.size())
                );
            }

            this.source = source;
            this.startIndex = start;
        }


        @Override
    public double getValue(int index) {
        if (index < 0 || index >= length) throw new IndexOutOfBoundsException();
        return source.getValue(startIndex + index);
    }

    @Override
    public long getTimestamp(int index) {
        if (index < 0 || index >= length) throw new IndexOutOfBoundsException();
        return source.getTimestamp(startIndex + index);
    }

    @Override
    public Vector toVector() {
        if (length == 0) {
            throw new IllegalStateException("Cannot convert an empty slice to a Vector (Vector requires at least 1 dimension)");
        }
        double[] copyNonRecurssive = new double[length];
        for (int i=startIndex; i < startIndex + length; i++){
            copyNonRecurssive[i - startIndex] = source.getValue(i );
        }
        return new ArrayVector(copyNonRecurssive);
    }

    @Override
    public int size() {
        return length;
    }
}
