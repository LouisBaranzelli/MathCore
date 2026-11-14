package org.statistics.bayesien.multiparameters;

import org.math.Integrator.multivariate.MultivariateNumericalIntegrator;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.bayesien.monoparameter.SampleLikelihoodEvaluator;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

/**
 * Moteur d'inférence bayésienne multivarié par intégration numérique (Grille / Produit cartésien).
 * Réservé aux dimensions faibles (ex: p = 2 pour estimer mu et sigma d'une loi normale)
 * en raison de la malédiction de la dimension.
 */
public class MultivariateGridBayesianInferenceEngine {

    private static final Logger log = LoggerFactory.getLogger(MultivariateGridBayesianInferenceEngine.class);

    private final ParametricModel<?> model;
    private final MultivariateNumericalIntegrator integrator;
    private final Vector thetaMin;
    private final Vector thetaMax;

    public MultivariateGridBayesianInferenceEngine(
            ParametricModel<?> model,
            MultivariateNumericalIntegrator integrator,
            Vector thetaMin,
            Vector thetaMax) {

        this.model = Objects.requireNonNull(model, "Model cannot be null");
        this.integrator = Objects.requireNonNull(integrator, "Integrator cannot be null");
        this.thetaMin = Objects.requireNonNull(thetaMin, "Theta min vector cannot be null");
        this.thetaMax = Objects.requireNonNull(thetaMax, "Theta max vector cannot be null");

        if (thetaMin.size() != thetaMax.size()) {
            throw new IllegalArgumentException("thetaMin and thetaMax must have the same dimension");
        }
    }

    /**
     * Calcule la densité a posteriori normalisée sous forme de fonction évaluable : theta -> f(theta | data).
     *
     * @param prior le prior multivarié f(theta)
     * @param data les données observées (scalaires)
     * @return une fonction représentant la densité a posteriori exacte sur le domaine
     */
    public ToDoubleFunction<Vector> computePosteriorDensity(MultivariateBayesianPrior prior, Vector data) {
        log.trace("computePosteriorDensity (multivariate grid) called with data size: {}", data != null ? data.size() : 0);

        Objects.requireNonNull(prior, "Prior cannot be null");
        Objects.requireNonNull(data, "Data vector cannot be null");

        // 1. Calcul de la constante de normalisation c_n via l'intégrateur multivarié
        double normalizingConstant = MultivariateBayesianNormalizingConstant.compute(
                data, model, prior, integrator, thetaMin, thetaMax
        );

        if (normalizingConstant <= 0.0 || Double.isNaN(normalizingConstant)) {
            log.error("Invalid normalizing constant computed: {}", normalizingConstant);
            throw new IllegalStateException("Normalizing constant must be strictly positive.");
        }

        // 2. Formulation de la densité a posteriori f(theta | data) = [ L(theta) * p(theta) ] / c_n
        ToDoubleFunction<Vector> posteriorDensity = theta -> {
            // Vérification des bornes
            for (int i = 0; i < theta.size(); i++) {
                if (theta.getValue(i) < thetaMin.getValue(i) || theta.getValue(i) > thetaMax.getValue(i)) {
                    return 0.0;
                }
            }

            if (!model.isValidParameterSet(theta)) {
                return 0.0;
            }

            double logLikelihood = SampleLikelihoodEvaluator.evaluateLogLikelihood(data, model, theta);
            if (Double.isInfinite(logLikelihood) && logLikelihood < 0) {
                return 0.0;
            }

            double likelihood = Math.exp(logLikelihood);
            double priorDensity = prior.density(theta);

            return (likelihood * priorDensity) / normalizingConstant;
        };

        log.debug("Multivariate posterior density formulated successfully over {} dimensions.", thetaMin.size());
        return posteriorDensity;
    }
}