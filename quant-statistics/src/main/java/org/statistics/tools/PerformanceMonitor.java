package org.statistics.tools;

import org.slf4j.Logger;

import java.util.function.Supplier;

public final class PerformanceMonitor {

    private PerformanceMonitor() {
        // Empêche l'instanciation
    }

    /**
     * Mesure et log le temps d'exécution d'une tâche retournant un résultat.
     *
     * @param logger        Le logger SLF4J de la classe appelante
     * @param operationName Le nom de l'opération (pour les logs)
     * @param task          La tâche à exécuter (exprimée sous forme de lambda)
     * @param <T>           Le type de retour
     * @return Le résultat de la tâche
     */
    public static <T> T measure(Logger logger, String operationName, Supplier<T> task) {
        long startTime = System.nanoTime();
        T result;
        try {
            result = task.get();
        } finally {
            long durationMs = (System.nanoTime() - startTime) / 1_000_000;
            logger.info("[PERF] '{}' executed in {} ms", operationName, durationMs);
        }
        return result;
    }

    /**
     * Mesure et log le temps d'exécution d'une tâche ne retournant rien (void).
     *
     * @param logger        Le logger SLF4J de la classe appelante
     * @param operationName Le nom de l'opération
     * @param task          La tâche à exécuter
     */
    public static void measure(Logger logger, String operationName, Runnable task) {
        measure(logger, operationName, () -> {
            task.run();
            return null;
        });
    }
}