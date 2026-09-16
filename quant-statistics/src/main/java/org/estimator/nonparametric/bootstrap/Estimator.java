package org.estimator.nonparametric.bootstrap;

/**
 * Représente un estimateur statistique ponctuel (Point Estimator).
 * <p>
 * Un estimateur est une fonction pure et sans état (stateless) qui prend un échantillon
 * d'observations de type {@code S} et calcule une estimation d'un paramètre ou
 * d'une statistique de type {@code T}.
 * </p>
 *
 * @param <T> Le type du paramètre/statistique estimé(e) (ex: Double, RealMatrix, Distribution)
 * @param <S> Le type du conteneur d'échantillon (ex: double[], Vector[], Sample)
 */
@FunctionalInterface
public interface Estimator<T, S> {

    /**
     * Calcule l'estimation à partir de l'échantillon fourni.
     *
     * @param sample L'échantillon de données sous-jacent. Ne doit pas être nul ni vide.
     * @return L'estimation calculée de type {@code T}.
     * @throws IllegalArgumentException si l'échantillon ne respecte pas les préconditions
     *                                  exigées par l'estimateur (ex: taille minimale).
     */
    T estimate(S sample);
}