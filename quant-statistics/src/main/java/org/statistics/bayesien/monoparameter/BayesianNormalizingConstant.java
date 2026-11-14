package org.statistics.bayesien.monoparameter;

import org.math.Integrator.monovariate.NumericalIntegrator;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.frequentist.estimator.parametric.model.ParametricModel;

import java.util.Objects;
import java.util.function.ToDoubleFunction;

public final class BayesianNormalizingConstant {

    private static final Logger log = LoggerFactory.getLogger(BayesianNormalizingConstant.class);

    private BayesianNormalizingConstant() {
    }

    /**
     * Calcule la constante de normalisation c_n par intégration numérique :
     * c_n = \int_{\Theta} L_n(theta) * f(theta) dtheta
     *
     * @param data données observées stockées dans un Vector
     * @param model modèle paramétrique associé
     * @param monovariateBayesianPrior fonction de densité de l'a priori f(theta)
     * @param integrator algorithme d'intégration numérique (ex: Simpson, Trapèzes)
     * @param thetaMin borne inférieure du domaine des paramètres
     * @param thetaMax borne supérieure du domaine des paramètres
     * @return la valeur de c_n
     */
    public static <D> double compute(
            Vector data,
            ParametricModel<D> model,
            MonovariateBayesianPrior monovariateBayesianPrior,
            NumericalIntegrator integrator,
            double thetaMin,
            double thetaMax) {

        log.trace("BayesianNormalizingConstant called with prior: {}, data size: {}, theta range: [{}, {}]",
                 monovariateBayesianPrior.getLabel(),data.size(), thetaMin, thetaMax);

        Objects.requireNonNull(data, "Data vector cannot be null");
        Objects.requireNonNull(model, "Model cannot be null");
        Objects.requireNonNull(monovariateBayesianPrior, "Prior density function cannot be null");
        Objects.requireNonNull(integrator, "NumericalIntegrator cannot be null");

        ToDoubleFunction<Double> integrand = theta -> {
            Vector params = ArrayVector.of(theta);
            double logLikelihood = SampleLikelihoodEvaluator.evaluateLogLikelihood(data, model, params);

            if (Double.isInfinite(logLikelihood) && logLikelihood < 0) {
                return 0;
            }
            double likelihood = Math.exp(logLikelihood);

            double prior = monovariateBayesianPrior.density(theta);
            return likelihood * prior;
        };

        double cn = integrator.integrate(integrand, thetaMin, thetaMax);

        log.debug("Computed normalizing constant c_n = {} over parameter range [{}, {}]", cn, thetaMin, thetaMax);
        return cn;
    }
}