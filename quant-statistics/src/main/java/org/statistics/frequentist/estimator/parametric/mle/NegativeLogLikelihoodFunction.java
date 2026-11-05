package org.statistics.frequentist.estimator.parametric.mle;

import org.statistics.frequentist.estimator.parametric.model.ParametricModel;
import org.math.function.MultivariateFunction;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.statistics.data.Sample;

public class NegativeLogLikelihoodFunction implements MultivariateFunction {

    private static final Logger log = LoggerFactory.getLogger(NegativeLogLikelihoodFunction.class);

    private final Sample data;
    private final ParametricModel<?> model;

    /*
    Notes:
    Calculer la probabilité des données sachant le modèle (la vraisemblance) est extrêmement utile, mais uniquement pour
     comparer deux modèles entre eux, pas pour juger un modèle dans l'absolu.C'est le principe du rapport de
     vraisemblance (le LRT dont on parlait) :

     Modèle A (Loi Normale) donne une probabilité de $10^{-50}$ à tes données.
     Modèle B (Loi de Student) donne une probabilité de $10^{-30}$ à tes données.
     Conclusion : Le modèle B est $10^{20}$ fois plus plausible que le modèle A.

     Mais cela ne te dit toujours pas si le modèle B est réellement correct ou si c'est juste le moins pire des deux.
     */

    public NegativeLogLikelihoodFunction(Sample data, ParametricModel<?> model) {
        this.data = data;
        this.model = model;
    }

    @Override
    public double evaluate(Vector params) {
        if (!model.isValidParameterSet(params)) {
            log.trace("Parameters out of bounds: {}", params);
            return Double.POSITIVE_INFINITY;
        }

        double logLikelihood = 0.0;
        for (double x : data) {
            double logP = model.logLikelihood(x, params);

            if (Double.isNaN(logP) || Double.isInfinite(logP)) {
                return Double.POSITIVE_INFINITY;
            }
            logLikelihood += logP;
        }

        return -logLikelihood;
    }
}