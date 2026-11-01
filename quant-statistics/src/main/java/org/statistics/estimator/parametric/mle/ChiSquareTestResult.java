package org.statistics.estimator.parametric.mle;

public class ChiSquareTestResult {
    private final double statistic;
    private final int degreesOfFreedom;
    private final double pValue;

    public ChiSquareTestResult(double statistic, int degreesOfFreedom, double pValue) {
        this.statistic = statistic;
        this.degreesOfFreedom = degreesOfFreedom;
        this.pValue = pValue;
    }

    public double getStatistic() { return statistic; }
    public int getDegreesOfFreedom() { return degreesOfFreedom; }
    public double getPValue() { return pValue; }

    /**
     * Indique si on rejette l'hypothèse nulle au seuil alpha (ex: 0.05).
     */
    public boolean rejectNull(double alpha) {
        return pValue < alpha;
    }
}