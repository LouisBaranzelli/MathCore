package org.statistics.bayesien;

import org.math.vector.Vector;
import org.statistics.distributions.monovariate.Distribution;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;

/**
 * Interface commune pour tous les moteurs d'inférence bayésienne (analytiques ou numériques).
 * Formalise la mise à jour des croyances : (Prior, Data) -> Posterior.
*/
public interface BayesianInferenceEngine {

    /**
     * Calcule la distribution a posteriori à partir d'un a priori et de données observées.
     *
     * @param prior la distribution a priori f(theta)
     * @param data les données observées x^n
     * @return la distribution a posteriori f(theta | x^n) sous forme de ContinuousDistribution
     */
    ContinuousDistribution computePosterior(BayesianPrior prior, Vector data);
}