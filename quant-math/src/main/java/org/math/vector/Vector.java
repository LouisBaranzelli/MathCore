package org.math.vector;

import org.math.common.Shapable;

public interface Vector extends Shapable {
    int size();
    double getValue(int index);

    // Les opérations retournent un Vector générique
    Vector add(Vector other);
    Vector minus(Vector other);
    double dot(Vector other);
    double norm();

    default double getFirst(){
        return getValue(0);
    }

    default double getLast(){
        return getValue(size() - 1);
    }
}