package org.statistics.bayesien.monoparameter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;

public class MonovariateBayesianPrior {

    private static final Logger log = LoggerFactory.getLogger(MonovariateBayesianPrior.class);

    private final ContinuousDistribution distribution;

    public MonovariateBayesianPrior(ContinuousDistribution distribution) {
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