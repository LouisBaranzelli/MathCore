package org.statistics.bayesien;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;

public class BayesianPrior {

    private static final Logger log = LoggerFactory.getLogger(BayesianPrior.class);

    private final ContinuousDistribution distribution;

    public BayesianPrior(ContinuousDistribution distribution) {
        this.distribution = distribution;
    }

    public double density(double theta) {
        return distribution.density(theta);
    }

    public ContinuousDistribution getDistribution() {
        return distribution;
    }

    String getLabel(){
        return distribution.getLabel();
    }
}