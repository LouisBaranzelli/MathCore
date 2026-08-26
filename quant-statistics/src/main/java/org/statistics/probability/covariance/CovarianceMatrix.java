package org.statistics.probability.covariance;

import org.math.matrix.SymmetricMatrix;
import org.math.vector.Vector;

public interface CovarianceMatrix extends SymmetricMatrix {


    Vector getVariances();


    Vector getStandardDeviations();


    CorrelationMatrix toCorrelationMatrix();

    /**
     * Calcule la décomposition de Cholesky (L tel que Sigma = L * L^T).
     * Requis pour les simulations de Monte-Carlo.
     */
//    CholeskyDecomposition choleskyDecomposition();

    /**
     * Calcule la variance d'une combinaison linéaire (ex: risque de portefeuille).
     * @param weights le vecteur des poids w
     * @return w^T * Sigma * w
     */
//    double quadraticForm(Vector weights);
}