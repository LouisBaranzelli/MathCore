package org.statistics.estimator.nonparametric.test;

import org.math.random.RandomVectorFactory;
import org.math.vector.ArrayVector;
import org.math.vector.SliceVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.data.TwoVectorsStatistic;

import java.util.Arrays;
import java.util.Random;


/*
    Permet d'analyser si 2 échantillions font partie de la même distribution, exemple: comparer 2 backtests
 */
public class PermutationTest {

    private static final Logger log = LoggerFactory.getLogger(PermutationTest.class);

    private final int numberOfPermutations;
    private final Random random;

    public PermutationTest(int numberOfPermutations) {
        this(numberOfPermutations, 42);
    }

    public PermutationTest(int numberOfPermutations, long seed) {
        if (numberOfPermutations <= 0) {
            log.error("Number of permutations must be strictly positive: {}", numberOfPermutations);
            throw new IllegalArgumentException("Number of permutations must be strictly positive.");
        }
        this.numberOfPermutations = numberOfPermutations;
        this.random = new Random(seed);
    }

    /**
     * Exécute un test de permutation bilatéral (two-sided).
     *
     * @        param sampleA            Premier échantillon de données
     * @param vectorA            Deuxième échantillon de données
     * @param statistic          Fonction de calcul de la statistique de test (ex: différence de moyennes)
     * @return                   La p-valeur empirique
     */
    public double evaluate(Vector vectorA, Vector vectorB, TwoVectorsStatistic statistic) {


        double observedStat = statistic.compute(vectorA, vectorB);

        Vector combined = new ArrayVector(vectorA.toArray(), vectorB.toArray());

        int extremeCount = 0;
        Vector shuffled;
        for (int i = 0; i < numberOfPermutations; i++) {
            shuffled = RandomVectorFactory.shuffle(combined, random);
            Vector permA = new SliceVector(shuffled, 0, vectorA.size());
            Vector permB = new SliceVector(shuffled, vectorA.size(), vectorA.size() + vectorB.size());
            double permStat = statistic.compute(permA,permB);
            if (Math.abs(permStat) >= Math.abs(observedStat)) {
                extremeCount++;
            }
        }
        double pValue = (double) (extremeCount + 1) / (numberOfPermutations + 1);

        log.info("Permutation test completed: B = {}, extremeCount = {}, p-value = {}", numberOfPermutations, extremeCount, pValue);
        return pValue;
    }

}