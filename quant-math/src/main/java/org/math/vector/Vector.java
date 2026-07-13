package org.math.vector;

import org.math.common.Shapable;

public interface Vector extends Shapable {
    int getSize();
    double getValue(int index);

    // Les opérations retournent un Vector générique
    Vector add(Vector other);
    Vector minus(Vector other);
    double dot(Vector other);
    double norm();
}