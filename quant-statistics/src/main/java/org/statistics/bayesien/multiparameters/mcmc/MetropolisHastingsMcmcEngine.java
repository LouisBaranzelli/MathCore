package org.statistics.bayesien.multiparameters.mcmc;

import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.bayesien.monoparameter.SampleLikelihoodEvaluator;
import org.statistics.bayesien.multiparameters.MultivariateBayesianPrior;
import org.statistics.bayesien.multiparameters.ParameterSampleMatrix;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import java.util.Objects;
import java.util.Random;

/**
 * Moteur d'inférence bayésienne multiparamétrique par MCMC (Metropolis-Hastings).
 * Contourne la malédiction de la dimension en évitant le calcul de la constante de normalisation c_n.
 */
public class MetropolisHastingsMcmcEngine {

    private static final Logger log = LoggerFactory.getLogger(MetropolisHastingsMcmcEngine.class);

    private final ParametricModel<?> model;
    private final ProposalDistribution proposal;
    private final int numIterations;
    private final int burnIn;
    private final Random random;

    public MetropolisHastingsMcmcEngine(
            ParametricModel<?> model,
            ProposalDistribution proposal,
            int numIterations,
            int burnIn) {

        this.model = Objects.requireNonNull(model, "Model cannot be null");
        this.proposal = Objects.requireNonNull(proposal, "Proposal distribution cannot be null");

        if (numIterations <= 0 || burnIn < 0 || burnIn >= numIterations) {
            throw new IllegalArgumentException("Invalid iterations or burn-in configuration.");
        }

        this.numIterations = numIterations;
        this.burnIn = burnIn;
        this.random = new Random();
    }

    public ParameterSampleMatrix sample(MultivariateBayesianPrior prior, Vector data, Vector initialTheta) {
        log.trace("sample called with data size: {}, initialTheta dimension: {}, iterations: {}, burnIn: {}",
                data.size(), initialTheta.size(), numIterations, burnIn);

        Objects.requireNonNull(prior, "Prior cannot be null");
        Objects.requireNonNull(data, "Data vector cannot be null");
        Objects.requireNonNull(initialTheta, "Initial theta cannot be null");

        int dim = initialTheta.size();
        double[][] acceptedSamples = new double[numIterations - burnIn][dim];

        Vector currentTheta = initialTheta;
        double currentLogPosterior = computeUnnormalizedLogPosterior(currentTheta, data, prior);

        int acceptedCount = 0;
        int storedIndex = 0;

        for (int iter = 0; iter < numIterations; iter++) {
            Vector proposedTheta = proposal.propose(currentTheta);

            double proposedLogPosterior = computeUnnormalizedLogPosterior(proposedTheta, data, prior);

            // Calcul du ratio d'acceptation en log-espace
            double logAlpha = (proposedLogPosterior - currentLogPosterior)
                    + proposal.logProposalRatio(currentTheta, proposedTheta);

            if (Math.log(random.nextDouble()) < logAlpha) {
                currentTheta = proposedTheta;
                currentLogPosterior = proposedLogPosterior;
                if (iter >= burnIn) {
                    acceptedCount++;
                }
            }

            if (iter >= burnIn) {
                for (int d = 0; d < dim; d++) {
                    acceptedSamples[storedIndex][d] = currentTheta.getValue(d);
                }
                storedIndex++;
            }
        }

        double acceptanceRate = (double) acceptedCount / (numIterations - burnIn);
        log.debug("MCMC sampling completed. Acceptance rate: {:.2f}% over {} effective samples.",
                acceptanceRate * 100.0, storedIndex);

        return new ParameterSampleMatrix(acceptedSamples);
    }

    private double computeUnnormalizedLogPosterior(Vector theta, Vector data, MultivariateBayesianPrior prior) {
        if (!model.isValidParameterSet(theta)) {
            return Double.NEGATIVE_INFINITY;
        }

        double priorDensity = prior.density(theta);
        if (priorDensity <= 0.0) {
            return Double.NEGATIVE_INFINITY;
        }

        double logLikelihood = SampleLikelihoodEvaluator.evaluateLogLikelihood(data, model, theta);
        if (Double.isInfinite(logLikelihood) && logLikelihood < 0) {
            return Double.NEGATIVE_INFINITY;
        }

        return logLikelihood + Math.log(priorDensity);
    }
}