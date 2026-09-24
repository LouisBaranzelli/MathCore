package org.math.vector;

import java.util.Arrays;

public final class ArrayVector implements Vector {

    private final double[] values;

    public ArrayVector(double... values) {
        if (values == null || values.length == 0) {
            throw new IllegalArgumentException("Vector must have at least one dimension");
        }
        this.values = values.clone();
    }

    public static ArrayVector of(double... values){
        return new ArrayVector(values);
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public double getValue(int index) {
        return values[index];
    }

    @Override
    public Vector add(Vector other) {
        checkDimensionCompatibility(other);
        double[] result = new double[size()];
        for (int i = 0; i < size(); i++) {
            result[i] = this.values[i] + other.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public Vector minus(Vector other) {
        checkDimensionCompatibility(other);
        double[] result = new double[size()];
        for (int i = 0; i < size(); i++) {
            result[i] = this.values[i] - other.getValue(i);
        }
        return new ArrayVector(result);
    }

    @Override
    public Vector multiply(double scalar) {
        double[] result = new double[this.values.length];
        for (int i = 0; i < this.values.length; i++) {
            result[i] = this.values[i] * scalar;
        }
        return new ArrayVector(result);
    }

    @Override
    public double dot(Vector other) {
        checkDimensionCompatibility(other);
        double result = 0;
        for (int i = 0; i < size(); i++) {
            result += this.values[i] * other.getValue(i);
        }
        return result;
    }

    @Override
    public double norm() {
        return Math.sqrt(dot(this));
    }


    @Override
    public boolean equals(Object that) {
        if (this == that) return true;
        if (!(that instanceof Vector other)) return false;
        if (this.size() != other.size()) return false;

        if (other instanceof ArrayVector otherArray) {
            return Arrays.equals(this.values, otherArray.values);
        }

        for (int i = 0; i < size(); i++) {
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