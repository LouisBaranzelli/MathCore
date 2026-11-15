package org.statistics.bayesien.multiparameters.mcmc;

import org.math.vector.Vector;

/**
 * Interface pour la loi de proposition q(theta* | theta) en MCMC.
 */
public interface ProposalDistribution {
    /**
     * Propose un nouveau vecteur de paramètres theta* à partir de theta actuel.
     */
    Vector propose(Vector currentTheta);

    /**
     * Calcule le log du ratio de transition log(q(theta | theta*) / q(theta* | theta)).
     * Nul si la proposition est symétrique (ex: marche aléatoire gaussienne).
     */
    default double logProposalRatio(Vector currentTheta, Vector proposedTheta) {
        return 0.0;
    }
}