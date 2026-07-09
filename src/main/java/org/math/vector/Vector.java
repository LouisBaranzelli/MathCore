package org.math.vector;

import org.math.common.Shapable;
import org.math.common.Shape;
import org.math.common.exception.ShapeException;

import java.util.Arrays;

public class Vector implements Shapable {

    private final double[] values;
    private final Shape shape;
    private double sum;
    private boolean sumCalculated = false;

    private double average;
    private boolean averageCalculated = false;

    private double minimum;
    private boolean minimumCalculated = false;

    private double maximum;
    private boolean maximumCalculated = false;


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

    public double getMinimum() {
        if (minimumCalculated){
            return minimum;
        }
        minimum = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] < minimum){
                minimum = values[i];
            }
        }
        minimumCalculated = true;
        return minimum;
    }

    public double getMaximum() {
        if (maximumCalculated){
            return maximum;
        }
        maximum = values[0];
        for (int i = 1; i < values.length; i++) {
            if (values[i] > maximum){
                maximum = values[i];
            }
        }
        maximumCalculated = true;
        return maximum;
    }

    double getValue(int index){
        return values[index];
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
