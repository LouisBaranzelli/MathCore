package org.series.imputation;

import org.series.timegrid.TimeGrid;

public class StubImputationStrategy implements ImputationStrategy {
    @Override
    public double[] alignAndImpute(long[] sortedDates, double[] rawValues, TimeGrid targetGrid) {
        // Renvoie simplement un tableau de la taille de la grille cible
        // pré-rempli avec la première valeur brute reçue pour simuler un alignement
        double[] alignmentMock = new double[targetGrid.size()];
        if (rawValues.length > 0) {
            java.util.Arrays.fill(alignmentMock, rawValues[0]);
        }
        return alignmentMock;
    }
}
