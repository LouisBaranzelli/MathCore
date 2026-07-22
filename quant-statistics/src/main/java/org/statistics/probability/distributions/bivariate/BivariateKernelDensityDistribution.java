package org.statistics.probability.distributions.bivariate;

import org.math.vector.Vector;
import org.statistics.probability.DescriptiveStatistics;

import java.util.Objects;

/**
 * Estimation de la densité par noyau bivarié (Bivariate Kernel Density Estimation - KDE).
 *
 * <p>Cette classe reconstruit une fonction de densité continue f(x, y) à partir de deux
 * vecteurs d'observations historiques X et Y, sans faire d'hypothèse paramétrique
 * sur la forme de la distribution.</p>
 */
public class BivariateKernelDensityDistribution implements BivariateContinuousDistribution {

    private final Vector xData;
    private final Vector yData;
    private final int n;

    // Bandes passantes (Bandwidths)
    private final double hX;
    private final double hY;

    // Facteur de normalisation précalculé pour optimiser les performances
    private final double normalizationFactor;

    /**
     * Construit une distribution KDE bivariée à partir de deux séries de données.
     * Les bandes passantes hX et hY sont calculées automatiquement via la règle de Silverman.
     *
     * @param xVector Tableau des observations pour la variable X
     * @param yVector Tableau des observations pour la variable Y (doit avoir la même taille que xVector)
     */
    public BivariateKernelDensityDistribution(Vector xVector, Vector yVector) {
        Objects.requireNonNull(xVector, "Le tableau xVector ne peut pas être nul.");
        Objects.requireNonNull(yVector, "Le tableau yVector ne peut pas être nul.");

        if (xVector.getSize() != yVector.getSize()) {
            throw new IllegalArgumentException(
                    "Les tableaux xVector et yVector doivent avoir la même dimension. ("
                            + xVector.getSize() + " != " + yVector.getSize() + ")"
            );
        }

        if (xVector.getSize() < 2) {
            throw new IllegalArgumentException("Il faut au moins 2 observations pour estimer un KDE.");
        }

        this.xData = xVector; // Invariabilité pour garantir la sécurité du thread
        this.yData = yVector;
        this.n = xVector.getSize();

        // 1. Calcul automatique des bandes passantes hX et hY (Silverman bivarié)
        this.hX = calculateSilvermanBandwidth(this.xData);
        this.hY = calculateSilvermanBandwidth(this.yData);

        // 2. Précalcul de la constante 1 / (n * hX * hY * 2 * PI) pour accélérer la méthode density()
        this.normalizationFactor = 1.0 / (n * hX * hY * 2.0 * Math.PI);
    }

    /**
     * Permet d'imposer manuellement des bandes passantes personnalisées (ex: pour du lissage fin).
     */
    public BivariateKernelDensityDistribution(Vector xVector, Vector yVector, double hX, double hY) {
        if (hX <= 0 || hY <= 0) {
            throw new IllegalArgumentException("Les bandes passantes doivent être strictement positives.");
        }
        this.xData = xVector;
        this.yData = yVector;
        this.n = xData.getSize();
        this.hX = hX;
        this.hY = hY;
        this.normalizationFactor = 1.0 / (n * hX * hY * 2.0 * Math.PI);
    }

    /**
     * Évalue la densité conjointe f_{X,Y}(x, y) au point exact (x, y).
     *
     * Complexité : O(N) où N est le nombre d'observations historiques.
     */
    @Override
    public double density(double x, double y) {
        double sum = 0.0;

        // Boucle sur toutes les données historiques pour cumuler la hauteur des dômes
        for (int i = 0; i < n; i++) {
            // Distance normalisée par la bande passante
            double uX = (x - this.xData.getValue(i)) / hX;
            double uY = (y - this.yData.getValue(i)) / hY;

            // Kernel Gaussien Produit : exp( -0.5 * (uX^2 + uY^2) )
            // Équivalent à : gaussianKernel(uX) * gaussianKernel(uY)
            double kernelValue = Math.exp(-0.5 * (uX * uX + uY * uY));

            sum += kernelValue;
        }

        return normalizationFactor * sum;
    }

    // =========================================================================
    //  METHODES PRIVEES : CALCUL DES BANDES PASSANTES (SILVERMAN) ET STATS
    // =========================================================================

    /**
     * Calcule la bande passante optimale 'h' selon la règle de Silverman adaptée au cas 2D :
     * h = sigma * n^(-1 / 6)
     */
    private double calculateSilvermanBandwidth(Vector vector) {
        double stdDev = DescriptiveStatistics.standardDeviation(vector);

        if (stdDev == 0.0) {
            return 1.0; // Sécurité si toutes les données sont identiques pour éviter la division par zéro
        }

        // Pour un KDE 2D avec noyau gaussien, la puissance optimale de N est -1/6 (au lieu de -1/5 en 1D)
        return stdDev * Math.pow(n, -1.0 / 6.0);
    }

    private double calculateStandardDeviation(double[] data) {
        double mean = 0.0;
        for (double val : data) {
            mean += val;
        }
        mean /= data.length;

        double sumSquaredDiff = 0.0;
        for (double val : data) {
            double diff = val - mean;
            sumSquaredDiff += diff * diff;
        }

        return Math.sqrt(sumSquaredDiff / (data.length - 1));
    }

    public double getBandwidthX() { return hX; }
    public double getBandwidthY() { return hY; }
    public int getSampleSize() { return n; }

    /**
     * Évalue la CDF conjointe F_{X,Y}(x, y) = P(X <= x AND Y <= y).
     * Calcul exact analytique par sommation des CDF gaussiennes.
     */
    @Override
    public double cdf(double x, double y) {
        double sum = 0.0;

        for (int i = 0; i < n; i++) {
            double uX = (x - this.xData.getValue(i)) / hX;
            double uY = (y - this.yData.getValue(i)) / hY;

            // Produit des CDF 1D de chaque dôme : Phi(uX) * Phi(uY)
            sum += standardNormalCdf(uX) * standardNormalCdf(uY);
        }

        return sum / n;
    }

    /**
     * Calcule la valeur y (quantile) telle que P(Y <= y | X = xGiven) = p.
     * Utilise une recherche dichotomique (Binary Search) sur la CDF conditionnelle.
     */
    @Override
    public double inverseConditionalCdfY(double p, double xGiven) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("La probabilité p doit être strictement comprise entre 0 et 1.");
        }

        // Bornes de recherche basées sur la plage des données réelles Y (+/- 5 bandes passantes)
        double low = DescriptiveStatistics.min(yData) - 5.0 * hY;
        double high = DescriptiveStatistics.max(yData) + 5.0 * hY;
        double tol = 1e-6; // Précision du quantile

        // Densité marginale f_X(xGiven)
        double densityXGiven = calculateMarginalDensityX(xGiven);
        if (densityXGiven == 0.0) {
            return Double.NaN; // Hors du support des données
        }

        while ((high - low) > tol) {
            double mid = (low + high) / 2.0;

            // P(Y <= mid | X = xGiven) = (d/dx F(xGiven, mid)) / f_X(xGiven)
            double currentP = calculateConditionalCdfY(xGiven, mid, densityXGiven);

            if (currentP < p) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return (low + high) / 2.0;
    }

    /**
     * Calcule la valeur x (quantile) telle que P(X <= x | Y = yGiven) = p.
     */
    @Override
    public double inverseConditionalCdfX(double p, double yGiven) {
        if (p <= 0.0 || p >= 1.0) {
            throw new IllegalArgumentException("La probabilité p doit être strictement comprise entre 0 et 1.");
        }

        double low = DescriptiveStatistics.min(xData) - 5.0 * hX;
        double high = DescriptiveStatistics.max(xData) + 5.0 * hX;
        double tol = 1e-6;

        double densityYGiven = calculateMarginalDensityY(yGiven);
        if (densityYGiven == 0.0) {
            return Double.NaN;
        }

        while ((high - low) > tol) {
            double mid = (low + high) / 2.0;
            double currentP = calculateConditionalCdfX(mid, yGiven, densityYGiven);

            if (currentP < p) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return (low + high) / 2.0;
    }

    // =========================================================================
    //  OUTILS MATHEMATIQUES PRIVES (CDF Normale 1D & Conditionnelles)
    // =========================================================================

    /**
     * Calcule P(Y <= y | X = xGiven).
     */
    private double calculateConditionalCdfY(double xGiven, double y, double densityXGiven) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double uX = (xGiven - this.xData.getValue(i)) / hX;
            double uY = (y - this.yData.getValue(i)) / hY;

            // f_X(x_i) * Phi(uY)
            sum += Math.exp(-0.5 * uX * uX) * standardNormalCdf(uY);
        }
        double numerator = sum / (n * hX * Math.sqrt(2.0 * Math.PI));
        return numerator / densityXGiven;
    }

    /**
     * Calcule P(X <= x | Y = yGiven).
     */
    private double calculateConditionalCdfX(double x, double yGiven, double densityYGiven) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double uX = (x - this.xData.getValue(i)) / hX;
            double uY = (yGiven - this.yData.getValue(i)) / hY;

            sum += Math.exp(-0.5 * uY * uY) * standardNormalCdf(uX);
        }
        double numerator = sum / (n * hY * Math.sqrt(2.0 * Math.PI));
        return numerator / densityYGiven;
    }

    private double calculateMarginalDensityX(double x) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double uX = (x - this.xData.getValue(i)) / hX;
            sum += Math.exp(-0.5 * uX * uX);
        }
        return sum / (n * hX * Math.sqrt(2.0 * Math.PI));
    }

    private double calculateMarginalDensityY(double y) {
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
            double uY = (y - this.yData.getValue(i)) / hY;
            sum += Math.exp(-0.5 * uY * uY);
        }
        return sum / (n * hY * Math.sqrt(2.0 * Math.PI));
    }

    /**
     * CDF Normale Standard Phi(z) = P(Z <= z).
     */
    private static double standardNormalCdf(double z) {
        return 0.5 * (1.0 + erf(z / Math.sqrt(2.0)));
    }

    /**
     * Fonction d'erreur Gaussienne (Erf) - Approximation ultra-précise (Abramowitz & Stegun).
     */
    private static double erf(double x) {
        double sign = (x < 0) ? -1.0 : 1.0;
        x = Math.abs(x);

        double a1 =  0.254829592;
        double a2 = -0.284496736;
        double a3 =  1.421413741;
        double a4 = -1.453152027;
        double a5 =  1.061405429;
        double p  =  0.3275911;

        double t = 1.0 / (1.0 + p * x);
        double y = 1.0 - (((((a5 * t + a4) * t) + a3) * t + a2) * t + a1) * t * Math.exp(-x * x);

        return sign * y;
    }
}
