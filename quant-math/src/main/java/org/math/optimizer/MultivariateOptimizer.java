package org.math.optimizer;

import org.math.function.MultivariateFunction;
import org.math.vector.Vector;

public interface MultivariateOptimizer {

    /**
     * Trouve le point θ qui minimise la fonction donnée.
     *
     * @param function La fonction à minimiser (ex: NLL)
     * @param initialGuess Point de départ [θ_0, θ_1, ...]
     * @return Le résultat d'optimisation contenant les paramètres optimaux
     */
    OptimizationResult optimize(MultivariateFunction function, Vector initialGuess);
}