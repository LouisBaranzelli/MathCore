package org.math.vector;


public interface Vector {


    int size();

    double getValue(int index);

    Vector add(Vector other);

    Vector minus(Vector other);

    default Vector multiply(double scalar){
        double[] result = new double[size()];
        for (int i = 0; i < size(); i++) {
            result[i] = getValue(i) * scalar;
        }
        return new ArrayVector(result);
    }

    double dot(Vector other);

    double norm();

    default void checkDimensionCompatibility(Vector other) {
        if (other.size() != this.size()) {
            throw new IllegalArgumentException(
                    String.format("Vector dimensions mismatch: expected %d, got %d", this.size(), other.size())
            );
        }
    }
}