package org.statistics.bayesien.multiparameters.mcmc;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.Random;

public class GaussianRandomWalkProposal implements ProposalDistribution {

    private static final Logger log = LoggerFactory.getLogger(GaussianRandomWalkProposal.class);

    private final double stepSize;
    private final Random random;

    public GaussianRandomWalkProposal(double stepSize) {
        this.stepSize = stepSize;
        this.random = new Random();
    }

    @Override
    public Vector propose(Vector currentTheta) {
        log.trace("propose called with currentTheta dimension: {}", currentTheta.size());

        int dim = currentTheta.size();
        double[] proposedValues = new double[dim];

        for (int i = 0; i < dim; i++) {
            proposedValues[i] = currentTheta.getValue(i) + random.nextGaussian() * stepSize;
        }

        Vector proposed = new ArrayVector(proposedValues);
        log.debug("Proposed new theta via Gaussian random walk with stepSize={}", stepSize);
        return proposed;
    }
}