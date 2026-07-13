package org.math.vector;

import org.math.common.Shape;
import org.math.common.exception.ShapeException;
import java.util.Arrays;

public final class ArrayVector implements Vector {

    private final double[] values;
    private final Shape shape;

    public ArrayVector(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Vector must have at least one dimension");
        }
        // Copie défensive pour garantir l'immutabilité
        this.values = values.clone();
        this.shape = new Shape(values.length, 1);
    }

    @Override
    public int getSize() {
        return values.length;
    }

    @Override
    public double getValue(int index) {
        return values[index];
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public Vector add(Vector other) {
        checkShapeCompatibility(other);
        double[] result = new double[getSize()];
        for (int i = 0; i < getSize(); i++) {
            result[i] = this.values[i] + other.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public Vector minus(Vector other) {
        checkShapeCompatibility(other);
        double[] result = new double[getSize()];
        for (int i = 0; i < getSize(); i++) {
            result[i] = this.values[i] - other.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public double dot(Vector other) {
        checkShapeCompatibility(other);
        double result = 0;
        for (int i = 0; i < getSize(); i++) {
            result += this.values[i] * other.getValue(i);
        }
        return result;
    }

    @Override
    public double norm() {
        return Math.sqrt(dot(this));
    }

    private void checkShapeCompatibility(Vector other) {
        if (other.getSize() != this.getSize()) {
            throw new ShapeException(getShape(), other.getShape());
        }
    }

    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof Vector other)) return false;
        if (this.getSize() != other.getSize()) return false;

        // Optimisation si les deux sont des ArrayVector
        if (other instanceof ArrayVector otherArray) {
            return Arrays.equals(this.values, otherArray.values);
        }

        // Comparaison générique sinon
        for (int i = 0; i < getSize(); i++) {
            if (Double.compare(this.getValue(i), other.getValue(i)) != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString() {
        return Arrays.toString(values);
    }
}