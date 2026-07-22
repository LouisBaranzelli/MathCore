package org.statistics.probability.distributions.bivariate;

/**
 * Représente une loi de probabilité continue bivariée (à deux variables).
 *
 * <p>Dans un espace à deux dimensions, la fonction de densité de probabilité (PDF)
 * <i>f<sub>X,Y</sub>(x, y)</i> ne mesure pas une probabilité ponctuelle (qui est nulle
 * pour des variables continues), mais la <b>hauteur de la surface de distribution</b>
 * au point <i>(x, y)</i>.</p>
 *
 * <p>L'accès rapide et précis à cette valeur par la méthode {@link #density(double, double)}
 * est indispensable pour trois fonctionnalités majeures :</p>
 *
 * <ul>
 *   <li><b>1. Le Pricing d'Actifs et d'Options Exotiques :</b>
 *       Permet le calcul d'intégrales numériques 2D sur le payoff <i>H(x, y)</i> :
 *       <br>
 *       <code>Prix = e^(-rT) * ∬ H(x, y) * f<sub>X,Y</sub>(x, y) dx dy</code>
 *   </li>
 *   <li><b>2. La Calibration de Modèles (Maximum de Vraisemblance) :</b>
 *       Ajuste les paramètres θ à partir de données historiques en maximisant le produit :
 *       <br>
 *       <code>Likelihood(θ) = ∏ f<sub>X,Y</sub>(x<sub>t</sub>, y<sub>t</sub> ; θ)</code>
 *   </li>
 *   <li><b>3. Les Simulations et le Filtrage Conditionnel (Monte-Carlo & VaR) :</b>
 *       S'appuie sur la décomposition conditionnelle pour générer des scénarios de crise :
 *       <br>
 *       <code>f<sub>X,Y</sub>(x, y) = f<sub>X|Y</sub>(x|y) * f<sub>Y</sub>(y)</code>
 *   </li>
 * </ul>
 *
 * @author YourName
 */
public interface BivariateContinuousDistribution extends BivariateDistribution{

    double density(double x, double y);
}