package org.statistics.bayesien.multiparameters;

import org.math.Integrator.multivariate.MultivariateNumericalIntegrator;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.bayesien.monoparameter.SampleLikelihoodEvaluator;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class MultivariateBayesianNormalizingConstant {

    private static final Logger log = LoggerFactory.getLogger(MultivariateBayesianNormalizingConstant.class);

    private MultivariateBayesianNormalizingConstant() {
    }

    /**
     * Calcule la constante de normalisation c_n par intégration numérique multivariée :
     * c_n = \int_{\Theta} L_n(\theta) * f(\theta) d\theta_1 d\theta_2 ... d\theta_p
     *
     * @param data données observées stockées dans un Vector
     * @param model modèle paramétrique associé
     * @param multivariatePrior fonction de densité du prior multivarié f(\theta)
     * @param integrator algorithme d'intégration numérique multidimensionnel
     * @param thetaMin vecteur des bornes inférieures pour chaque paramètre
     * @param thetaMax vecteur des bornes supérieures pour chaque paramètre
     * @return la valeur de c_n
     */
    public static <D> double compute(
            Vector data,
            ParametricModel<D> model,
            MultivariateBayesianPrior multivariatePrior,
            MultivariateNumericalIntegrator integrator,
            Vector thetaMin,
            Vector thetaMax) {

        Objects.requireNonNull(data, "Data vector cannot be null");
        Objects.requireNonNull(model, "Model cannot be null");
        Objects.requireNonNull(multivariatePrior, "Multivariate prior cannot be null");
        Objects.requireNonNull(integrator, "MultivariateNumericalIntegrator cannot be null");
        Objects.requireNonNull(thetaMin, "Theta min vector cannot be null");
        Objects.requireNonNull(thetaMax, "Theta max vector cannot be null");

        log.trace("MultivariateBayesianNormalizingConstant called with data size: {}, dimension: {}",
                data.size(), multivariatePrior.getDimension());

        ToDoubleFunction<Vector> integrand = theta -> {
            double logLikelihood = SampleLikelihoodEvaluator.evaluateLogLikelihood(data, model, theta);

            if (Double.isInfinite(logLikelihood) && logLikelihood < 0) {
                return 0.0;
            }
            double likelihood = Math.exp(logLikelihood);

            double prior = multivariatePrior.density(theta);
            return likelihood * prior;
        };

        double cn = integrator.integrate(integrand, thetaMin, thetaMax);

        log.debug("Computed multivariate normalizing constant c_n = {} over {} dimensions", cn, multivariatePrior.getDimension());
        return cn;
    }
}