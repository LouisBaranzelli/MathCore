package org.math.vector;

import org.math.common.Shape;
import org.math.common.exception.ShapeException;

public final class SliceVector implements Vector {
    private final Vector source;
    private final int offset;
    private final int size;
    private final Shape shape;

    /**
     * Crée une vue (tranche) sur un vecteur existant sans copie de données.
     *
     * @param source Le vecteur d'origine
     * @param start  L'index de départ (inclus)
     * @param end    L'index de fin (exclu)
     */
    public SliceVector(Vector source, int start, int end) {
        if (source == null) {
            throw new IllegalArgumentException("Source vector cannot be null");
        }

        this.size = end - start;
        this.shape = new Shape(this.size, 1);

        // Validation rigoureuse des bornes géométriques du sous-espace
        if (start < 0 || this.size <= 0 || end > source.size()) {
            throw new IndexOutOfBoundsException(
                    String.format("Invalid slice boundaries: start=%d, end=%d, sourceSize=%d",
                            start, end, source.size())
            );
        }

        // Aplatissement (Flattening) pour maintenir un accès en O(1)
        if (source instanceof SliceVector parentSlice) {
            this.source = parentSlice.source;
            this.offset = parentSlice.offset + start;
        } else {
            this.source = source;
            this.offset = start;
        }
    }

    @Override
    public double getValue(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index " + index + " out of bounds for slice of size " + size);
        }
        return source.getValue(offset + index);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public Vector add(Vector other) {
        checkShapeCompatibility(other);
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.getValue(i) + other.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public Vector minus(Vector other) {
        checkShapeCompatibility(other);
        double[] result = new double[size];
        for (int i = 0; i < size; i++) {
            result[i] = this.getValue(i) - other.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public double dot(Vector other) {
        checkShapeCompatibility(other);
        double result = 0;
        for (int i = 0; i < size; i++) {
            result += this.getValue(i) * other.getValue(i);
        }
        return result;
    }

    @Override
    public double norm() {
        return Math.sqrt(dot(this));
    }

    private void checkShapeCompatibility(Vector other) {
        if (other.size() != this.size) {
            throw new ShapeException(this.getShape(), other.getShape());
        }
    }


    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof Vector other)) return false;
        if (this.size() != other.size()) return false;

        for (int i = 0; i < size; i++) {
            if (Double.compare(this.getValue(i), other.getValue(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < size; i++) {
            long bits = Double.doubleToLongBits(getValue(i));
            result = 31 * result + (int) (bits ^ (bits >>> 32));
        }
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) sb.append(", ");
            sb.append(getValue(i));
        }
        return sb.append("]").toString();
    }
}