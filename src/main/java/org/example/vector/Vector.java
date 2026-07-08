package org.example.vector;

import org.example.common.Shapable;
import org.example.common.Shape;
import org.example.common.exception.ShapeException;

import java.util.Arrays;

public class Vector implements Shapable {

    private final double[] values;
    private final Shape shape;
    private double sum;
    private boolean sumCalculated = false;

    private double average;
    private boolean averageCalculated = false;

    public Vector(double... values){
        if (values == null || values.length == 0){
            throw new IllegalArgumentException("Vector  must have at least one dimension");
        }
        this.values = values.clone();
        this.shape = new Shape(values.length, 1);
    }

    public Vector add(Vector vector){
        if (!vector.getShape().equals(getShape())){
            throw new ShapeException(getShape(), vector.getShape());
        }
        double[] otherValues = vector.internalValues();
        double[] result  = new double[getSize()];
        for (int i=0; i<getSize(); i++){
            result[i] =  values[i] + otherValues[i];
        }
        return new Vector(result);
    }

    public Vector minus(Vector vector){
        if (!vector.getShape().equals(getShape())){
            throw new ShapeException(getShape(), vector.getShape());
        }
        double[] otherValues = vector.internalValues();
        double[] result = new double[getSize()];
        for (int i=0; i<getSize(); i++){
            result[i] =  values[i] - otherValues[i];
        }
        return new Vector(result);
    }

    public double dot(Vector vector){
        if (!vector.getShape().equals(getShape())){
            throw new ShapeException(getShape(), vector.getShape());
        }
        double[] otherValues = vector.internalValues();
        double result = 0;
        for (int i=0; i<getSize(); i++){
            result +=  values[i] * otherValues[i];
        }
        return result;
    }

    public double sum() {
        if (!sumCalculated) {
            for (double value : values) {
                sum += value;
            }
            sumCalculated = true;
        }
        return sum;
    }

    public double mean(){
        if (averageCalculated){
            return average;
        }
        averageCalculated = true;
        return average = sum() / getSize();
    }

    public double norm() {
        return Math.sqrt(dot(this));
    }

    @Override
    public Shape getShape() {
        return this.shape;
    }

    @Override
    public boolean equals(Object that){
        if (this == that){
            return true;
        }
        if (!(that instanceof Vector otherVector)){
            return false;
        }
        return Arrays.equals(values, otherVector.internalValues());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(values);
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0 ; i < values.length ; i++){
            if (i > 0){
                stringBuilder.append(", ");
            }
            stringBuilder.append(values[i]);
        }
        return "[" + stringBuilder +"]";
    }

    public double[] getValues() {
        return values.clone();
    }

    double[] internalValues() {
        return values;
    }

    public int getSize(){
        return this.values.length;
    }

}
