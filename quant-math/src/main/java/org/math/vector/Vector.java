package org.math.vector;



import java.util.Iterator;

public interface Vector extends Iterable<Double> {



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

    default double[] toArray(){
        double[] output = new double[size()];
        for (int i=0 ; i<size();i++){
            output[i] = getValue(i);
        }
        return output;
    }

        @Override
        default Iterator<Double> iterator() {
            return new Iterator<Double>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return index < size();
                }

                @Override
                public Double next() {
                    return getValue(index++);
                }
            };
        }
}