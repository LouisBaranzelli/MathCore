package org.statistics.probability;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BayesTheoremeTest {

    @Test
    void testBayes() {

        SampleProbability<Integer> sampleProbability = new SampleProbability<>(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
        Event<Integer> pair = new Event<>((Integer i) -> i % 2 == 0);
        Event<Integer> supe7 = new Event<>((Integer i) -> i > 6);

        /*
        Cela se lit :
            "Quelle est la probabilité que le nombre soit pair, sachant que je sais déjà qu'il est supérieur à 3 ?"
             Dans le vocabulaire bayésien :
             Hypothèse = ce que tu veux connaître → A = "le nombre est pair".
             Preuve (evidence) = ce que tu observes → B = "le nombre est supérieur à 6".
         */
        double pairSachantquesupe7 = BayesTheoreme.posterior(sampleProbability, pair, supe7);
        assertEquals(1.0 / 2.0, pairSachantquesupe7);
    }
}