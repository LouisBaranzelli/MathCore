package org.example.common;

import java.lang.reflect.Array;
import java.util.Arrays;

public class Shape {

    private final int[] dimensions;

    public Shape(int... dimensions){
        if (dimensions == null || dimensions.length == 0){
            throw new IllegalArgumentException("Shape must have at least one dimension");
        }
        for (int dimension : dimensions) {
            if (dimension <= 0){
                throw new IllegalArgumentException("Dimension must be positive");
            }
        }
        this.dimensions = dimensions.clone();
    }

    public int getDimension(int index){
        return dimensions[index];
    }

    public int[] getDimensions() {
        return dimensions.clone();
    }

    public int countDimensions(){
        return dimensions.length;
    }

    @Override
    public boolean equals(Object that){
        if (this == that){
            return true;
        }
        if (!(that instanceof Shape otherShape)){
            return false;
        }
        return Arrays.equals(dimensions, otherShape.getDimensions());
    }

    @Override
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0 ; i < dimensions.length ; i++){
            if (i > 0){
                stringBuilder.append("x");
            }
            stringBuilder.append(dimensions[i]);
        }
        return "(" + stringBuilder +")";
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(dimensions);
    }
}
