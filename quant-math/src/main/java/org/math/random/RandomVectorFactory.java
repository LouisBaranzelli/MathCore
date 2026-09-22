package org.math.random;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.util.random.RandomGenerator;
import java.util.random.RandomGeneratorFactory;

public class RandomVectorFactory {
    /**
     * Générateur déterministe d'échantillons i.i.d. de la loi Uniforme(0, 1)
     */
    public static Vector generateUniformSample(int n, long seed) {
        RandomGenerator rng = RandomGeneratorFactory.of("L64X128MixRandom").create(seed);
        double[] data = new double[n];
        for (int i = 0; i < n; i++) {
            data[i] = rng.nextDouble();
        }
        return new ArrayVector(data);
    }
}
