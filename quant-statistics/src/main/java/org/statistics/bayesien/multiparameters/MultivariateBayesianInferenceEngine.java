package org.statistics.bayesien.multiparameters;

import org.math.vector.Vector;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

/**
 * Interface pour les moteurs d'inférence bayésienne multi-paramétriques (p >= 1).
 * Repose généralement sur des méthodes stochastiques (MCMC) pour résoudre le calcul
 * de l'intégral de normalisation en multi-dimmensionalité.
 */
public interface MultivariateBayesianInferenceEngine {

    /**
     * Calcule ou approxime la distribution a posteriori conjointe sous forme d'un échantillon MCMC.
     *
     * @Modèle paramétrique (supportant p paramètres)
     * @Prior multidimensionnel
     * @Données observées
     * @return Une matrice de taille B x p contenant l'historique des tirages
     */
    ParameterSampleMatrix computePosterior(
            ParametricModel<?> model,
            MultivariateBayesianPrior prior,
            Vector data
    );
}