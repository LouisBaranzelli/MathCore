package org.math.optimizer;

/**
 * Définit les raisons possibles d'arrêt d'un optimiseur.
 */
public enum TerminationCriterion {

    /** Convergence atteinte sur la valeur de la fonction objectif. */
    CONVERGED_VALUE_TOL(true, "Convergence reached on function value tolerance."),

    /** Nombre maximal d'itérations atteint sans convergence. */
    MAX_ITERATIONS_REACHED(false, "Maximum number of iterations reached."),

    /** Nombre maximal d'évaluations de la fonction atteint. */
    MAX_EVALUATIONS_REACHED(false, "Maximum number of function evaluations reached."),

    /** Arrêt prématuré en raison de valeurs invalides (NaN, Infinis). */
    INVALID_NUMBER_ENCOUNTERED(false, "Encountered NaN or Infinite value during optimization.");

    private final boolean success;
    private final String description;

    TerminationCriterion(boolean success, String description) {
        this.success = success;
        this.description = description;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getDescription() {
        return description;
    }
}