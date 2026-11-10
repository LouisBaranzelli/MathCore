package org.statistics.bayesien;

import org.math.Integrator.NumericalIntegrator;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.distributions.monovariate.continuous.ContinuousDistribution;
import org.statistics.distributions.monovariate.continuous.GridDiscretizedDistribution;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import java.util.Objects;
import java.util.Random;
import java.util.function.DoubleUnaryOperator;

/**
 * Moteur d'inférence bayésienne univarié par grille.
 * Responsable uniquement de la formulation mathématique du a posteriori (vraisemblance x prior / c_n).
 */
public class GridBayesianInferenceEngine implements BayesianInferenceEngine {

    /*

    Permettre l'estimation de la distribution a posteriori $f(\theta \vert{} x^n)$ d'un paramètre scalaire
    unique $\theta \in [\theta_{\min}, \theta_{\max}]$ lorsque le modèle statistique n'est pas conjugué et
    que l'intégration analytique est impossible.

    Dimensionnalité (1D strict) : Cette implémentation est limitée aux modèles à un seul paramètre ($\dim(\theta) = 1$).
    Pour des espaces multidimensionnels ($\dim(\theta) \ge 2$), la grille souffre de la malédiction de la dimension ($M^d$)
    et nécessite l'utilisation de méthodes stochastiques (MCMC / Metropolis-Hastings).


     */

    private static final Logger log = LoggerFactory.getLogger(GridBayesianInferenceEngine.class);

    private final ParametricModel<?> model;
    private final NumericalIntegrator integrator;
    private final double thetaMin;
    private final double thetaMax;
    private final int gridPoints;
    private final Random random;

    public GridBayesianInferenceEngine(
            ParametricModel<?> model, // model avec 1 paramètre
            NumericalIntegrator integrator,
            double thetaMin,
            double thetaMax,
            int gridPoints) {
        this(model, integrator, thetaMin, thetaMax, gridPoints, new Random());
    }

    public GridBayesianInferenceEngine(
            ParametricModel<?> model,
            NumericalIntegrator integrator,
            double thetaMin,
            double thetaMax,
            int gridPoints,
            Random random) {

        if (thetaMin >= thetaMax) {
            throw new IllegalArgumentException("thetaMin must be strictly less than thetaMax");
        }
        if (gridPoints < 2) {
            throw new IllegalArgumentException("gridPoints must be at least 2");
        }
        this.model = Objects.requireNonNull(model, "Model cannot be null");
        this.integrator = Objects.requireNonNull(integrator, "Integrator cannot be null");
        this.thetaMin = thetaMin;
        this.thetaMax = thetaMax;
        this.gridPoints = gridPoints;
        this.random = Objects.requireNonNull(random, "Random cannot be null");
    }

    @Override
    public ContinuousDistribution computePosterior(BayesianPrior prior, Vector data) {
        log.trace("computePosteriorDistribution called with data size: {}", data != null ? data.size() : 0);

        Objects.requireNonNull(prior, "Prior cannot be null");
        Objects.requireNonNull(data, "Data vector cannot be null");

        double normalizingConstant = BayesianNormalizingConstant.compute(
                data, model, prior, integrator, thetaMin, thetaMax
        );

        if (normalizingConstant <= 0.0 || Double.isNaN(normalizingConstant)) {
            log.error("Invalid normalizing constant computed: {}", normalizingConstant);
            throw new IllegalStateException("Normalizing constant must be strictly positive.");
        }

        DoubleUnaryOperator posteriorDensity = theta -> {
            if (theta < thetaMin || theta > thetaMax) return 0.0;
            double logLikelihood = SampleLikelihoodEvaluator.evaluateLogLikelihood(data, model, ArrayVector.of(theta));
            if (Double.isInfinite(logLikelihood) && logLikelihood < 0) return 0.0;

            double likelihood = Math.exp(logLikelihood);
            return (likelihood * prior.density(theta)) / normalizingConstant;
        };

        log.debug("Posterior density formulated successfully over parameter range [{}, {}]", thetaMin, thetaMax);

        return GridDiscretizedDistribution.fromDensity(posteriorDensity, thetaMin, thetaMax, gridPoints, integrator);
    }
}