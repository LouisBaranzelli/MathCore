package org.statistics.probability;

import org.math.vector.Vector;

public final class DescriptiveStatistics {

    private DescriptiveStatistics() {}

    public static double sum(Vector vector) {
        double sum = 0;
        int n = vector.getSize();
        for (int i = 0; i < n; i++) {
            sum += vector.getValue(i);
        }
        return sum;
    }

    public static double mean(Vector vector) {
        if (vector.getSize() == 0) return Double.NaN;
        return sum(vector) / vector.getSize();
    }

    public static double variance(Vector vector) {
        int n = vector.getSize();
        if (n <= 1) return 0.0;

        double mu = mean(vector);
        double temp = 0;
        for (int i = 0; i < n; i++) {
            double diff = vector.getValue(i) - mu;
            temp += diff * diff;
        }
        // Variance échantillonale (n - 1) - standard en finance quantitave
        return temp / (n - 1);
    }

    public static double standardDeviation(Vector vector) {
        return Math.sqrt(variance(vector));
    }

    public static double min(Vector vector) {
        if (vector == null || vector.getSize() == 0) {
            throw new IllegalArgumentException("Le vecteur ne peut pas être nul ou vide.");
        }

        double min = vector.getValue(0); // ou vector.getFirst()

        // On commence à 1 car le premier élément (indice 0) sert déjà de référence
        for (int i = 1; i < vector.getSize(); i++) {
            double current = vector.getValue(i);
            if (current < min) {
                min = current;
            }
        }
        return min;
    }

    public static double max(Vector vector) {
        if (vector == null || vector.getSize() == 0) {
            throw new IllegalArgumentException("Le vecteur ne peut pas être nul ou vide.");
        }

        double max = vector.getValue(0); // ou vector.getFirst()

        // On commence également à i = 1
        for (int i = 1; i < vector.getSize(); i++) {
            double current = vector.getValue(i);
            if (current > max) {
                max = current;
            }
        }
        return max;
    }

    }