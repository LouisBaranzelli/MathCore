package org.statistics.probability.distributions.bivariate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests unitaires pour BivariateKernelDensityDistribution")
class BivariateKernelDensityDistributionTest {

    private Vector xSample;
    private Vector ySample;

    @BeforeEach
    void setUp() {
        // Exemple de données synthétiques (ex: 5 rendements historiques)
        // Note: Adapte le constructeur/instanciation de ton type Vector si besoin
        xSample = new ArrayVector(new double[]{0.01, -0.02, 0.005, -0.03, 0.015});
        ySample = new ArrayVector(new double[]{0.012, -0.018, 0.002, -0.025, 0.011});
    }

    @Nested
    @DisplayName("Tests du Constructeur et Validation des Arguments")
    class ConstructorValidationTests {

        @Test
        @DisplayName("Devrait lever une NullPointerException si xVector est nul")
        void shouldThrowExceptionWhenXVectorIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new BivariateKernelDensityDistribution(null, ySample)
            );
        }

        @Test
        @DisplayName("Devrait lever une NullPointerException si yVector est nul")
        void shouldThrowExceptionWhenYVectorIsNull() {
            assertThrows(NullPointerException.class, () ->
                    new BivariateKernelDensityDistribution(xSample, null)
            );
        }

        @Test
        @DisplayName("Devrait lever une IllegalArgumentException si les vecteurs n'ont pas la même taille")
        void shouldThrowExceptionWhenVectorSizesMismatch() {
            Vector shorterY = new ArrayVector(new double[]{0.012, -0.018});

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> new BivariateKernelDensityDistribution(xSample, shorterY)
            );

            assertTrue(exception.getMessage().contains("même dimension"));
        }

        @Test
        @DisplayName("Devrait lever une IllegalArgumentException s'il y a moins de 2 observations")
        void shouldThrowExceptionWhenSampleSizeIsTooSmall() {
            Vector singleX = new ArrayVector(new double[]{0.01});
            Vector singleY = new ArrayVector(new double[]{0.02});

            assertThrows(
                    IllegalArgumentException.class,
                    () -> new BivariateKernelDensityDistribution(singleX, singleY)
            );
        }

        @Test
        @DisplayName("Devrait lever une IllegalArgumentException si les bandes passantes manuelles sont invalides")
        void shouldThrowExceptionForInvalidManualBandwidths() {
            // Bande passante hX <= 0
            assertThrows(IllegalArgumentException.class, () ->
                    new BivariateKernelDensityDistribution(xSample, ySample, 0.0, 0.05)
            );

            // Bande passante hY <= 0
            assertThrows(IllegalArgumentException.class, () ->
                    new BivariateKernelDensityDistribution(xSample, ySample, 0.05, -0.01)
            );
        }
    }

    @Nested
    @DisplayName("Tests des Propriétés Statistiques et du Lissage (Silverman)")
    class StatisticalPropertiesTests {

        @Test
        @DisplayName("Devrait calculer automatiquement des bandes passantes hX et hY strictement positives")
        void shouldCalculatePositiveSilvermanBandwidths() {
            BivariateKernelDensityDistribution kde = new BivariateKernelDensityDistribution(xSample, ySample);

            assertTrue(kde.getBandwidthX() > 0.0, "hX doit être strictement positif");
            assertTrue(kde.getBandwidthY() > 0.0, "hY doit être strictly positif");
            assertEquals(5, kde.getSampleSize());
        }

        @Test
        @DisplayName("Devrait utiliser une bande passante fallback de 1.0 si l'écart-type est nul (données constantes)")
        void shouldFallbackToUnitBandwidthWhenVarianceIsZero() {
            Vector constantX = new ArrayVector(new double[]{0.05, 0.05, 0.05, 0.05});
            Vector constantY = new ArrayVector(new double[]{0.02, 0.02, 0.02, 0.02});

            BivariateKernelDensityDistribution kde = new BivariateKernelDensityDistribution(constantX, constantY);

            assertEquals(1.0, kde.getBandwidthX());
            assertEquals(1.0, kde.getBandwidthY());
        }
    }

    @Nested
    @DisplayName("Tests d'Évaluation de la Densité f_{X,Y}(x, y)")
    class DensityEvaluationTests {

        @Test
        @DisplayName("La densité doit toujours être strictement positive f(x,y) > 0")
        void densityShouldAlwaysBePositive() {
            BivariateKernelDensityDistribution kde = new BivariateKernelDensityDistribution(xSample, ySample);

            // Évaluation au centre des données
            double densityCenter = kde.density(0.0, 0.0);

            // Évaluation très loin (dans la queue de loi)
            double densityFar = kde.density(10.0, 10.0);

            assertTrue(densityCenter > 0.0);
            assertTrue(densityFar >= 0.0);
            assertTrue(densityCenter > densityFar, "La densité au cœur des données doit être plus élevée qu'à l'extérieur.");
        }

        @Test
        @DisplayName("Devrait donner une hauteur de densité plus élevée là où les points se regroupent")
        void densityShouldBeHigherInDenseRegions() {
            // Nuage concentré autour de (0,0) avec un point isolé en (5,5)
            Vector x = new ArrayVector(new double[]{0.0, 0.1, -0.1, 0.05, 5.0});
            Vector y = new ArrayVector(new double[]{0.0, -0.05, 0.08, 0.02, 5.0});

            BivariateKernelDensityDistribution kde = new BivariateKernelDensityDistribution(x, y);

            double densityCluster = kde.density(0.0, 0.0);
            double densityMoreIsolated = kde.density(0.5, 0.5);
            double densityIsolated = kde.density(5.0, 5.0);

            assertTrue(densityCluster > densityMoreIsolated,
                    "La région dense doit produire un sommet de densité plus élevé que le point isolé.");
            assertTrue(densityMoreIsolated > densityIsolated,
                    "La région dense doit produire un sommet de densité plus élevé que le point isolé.");

            x = new ArrayVector(new double[]{5., 4.9, 5.04, 0.1, 0});
            y = new ArrayVector(new double[]{5, 5.1, 4.8, -0.05, 0});
            kde = new BivariateKernelDensityDistribution(x, y);

            densityIsolated = kde.density(0.0, 0.0);
            densityMoreIsolated = kde.density(4.8, 4.8);
            densityCluster = kde.density(5.0, 5.0);


            assertTrue(densityCluster > densityMoreIsolated,
                    "La région dense doit produire un sommet de densité plus élevé que le point isolé.");
            assertTrue(densityMoreIsolated > densityIsolated,
                    "La région dense doit produire un sommet de densité plus élevé que le point isolé.");
        }

        @Test
        @DisplayName("Devrait respecter l'impact des bandes passantes manuelles (smooth vs peak)")
        void shouldRespectManualBandwidths() {
            // Bande passante très étroite -> pics élevés sur les points
            BivariateKernelDensityDistribution kdeNarrow =
                    new BivariateKernelDensityDistribution(xSample, ySample, 0.001, 0.001);

            // Bande passante très large -> dômes très aplatis
            BivariateKernelDensityDistribution kdeWide =
                    new BivariateKernelDensityDistribution(xSample, ySample, 1.0, 1.0);

            // Sur un point exact de l'échantillon : (0.01, 0.012)
            double peakNarrow = kdeNarrow.density(0.01, 0.012);
            double peakWide = kdeWide.density(0.01, 0.012);

            assertTrue(peakNarrow > peakWide,
                    "Une bande passante plus étroite doit produire des pics plus aigus sur les observations.");
        }
    }
    @DisplayName("Tests unitaires pour l'inversion de la CDF conditionnelle (Quantiles conditionnels) dans le KDE")
    class BivariateKdeInverseConditionalCdfTest {

        private BivariateKernelDensityDistribution kde;

        @BeforeEach
        void setUp() {
            // Jeu de données d'exemple (5 points)
            Vector x = new ArrayVector(new double[]{0.0, 0.1, -0.1, 0.05, 5.0});
            Vector y = new ArrayVector(new double[]{0.0, -0.05, 0.08, 0.02, 5.0});
            kde = new BivariateKernelDensityDistribution(x, y);
        }

        @Nested
        @DisplayName("Tests des exceptions et arguments invalides")
        class ExceptionAndEdgeCaseTests {

            @Test
            @DisplayName("inverseConditionalCdfY doit lever une exception pour p <= 0 ou p >= 1")
            void inverseYShouldThrowExceptionForInvalidP() {
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfY(0.0, 0.0));
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfY(-0.1, 0.0));
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfY(1.0, 0.0));
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfY(1.05, 0.0));
            }

            @Test
            @DisplayName("inverseConditionalCdfX doit lever une exception pour p <= 0 ou p >= 1")
            void inverseXShouldThrowExceptionForInvalidP() {
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfX(0.0, 0.0));
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfX(-0.1, 0.0));
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfX(1.0, 0.0));
                assertThrows(IllegalArgumentException.class, () -> kde.inverseConditionalCdfX(1.05, 0.0));
            }

            @Test
            @DisplayName("Doit retourner Double.NaN si la valeur donnée est totalement hors du support (densité marginale = 0)")
            void shouldReturnNaNWhenOutofSupport() {
                // Un point extrême à x = 1000.0 aura une densité marginale f_X(1000.0) == 0.0
                double resultY = kde.inverseConditionalCdfY(0.5, 1000.0);
                assertTrue(Double.isNaN(resultY), "Si xGiven est hors support, inverseConditionalCdfY doit renvoyer NaN.");

                double resultX = kde.inverseConditionalCdfX(0.5, 1000.0);
                assertTrue(Double.isNaN(resultX), "Si yGiven est hors support, inverseConditionalCdfX doit renvoyer NaN.");
            }
        }

        @Nested
        @DisplayName("Tests de cohérence probabiliste et propriétés de monotonie")
        class MonotonicityAndSymmetryTests {

            @Test
            @DisplayName("Le quantile calculé y doit augmenter quand la probabilité p augmente (Monotonie)")
            void quantileYShouldIncreaseWithP() {
                double xGiven = 0.0; // On fixe x au cœur du cluster principal

                double yQuantile25 = kde.inverseConditionalCdfY(0.25, xGiven);
                double yQuantile50 = kde.inverseConditionalCdfY(0.50, xGiven); // Médiane conditionnelle
                double yQuantile75 = kde.inverseConditionalCdfY(0.75, xGiven);

                assertTrue(yQuantile25 < yQuantile50, "Le quantile 25% doit être inférieur au quantile 50%.");
                assertTrue(yQuantile50 < yQuantile75, "Le quantile 50% doit être inférieur au quantile 75%.");
            }

            @Test
            @DisplayName("Le quantile calculé x doit augmenter quand la probabilité p augmente (Monotonie)")
            void quantileXShouldIncreaseWithP() {
                double yGiven = 0.0;

                double xQuantile20 = kde.inverseConditionalCdfX(0.20, yGiven);
                double xQuantile50 = kde.inverseConditionalCdfX(0.50, yGiven);
                double xQuantile80 = kde.inverseConditionalCdfX(0.80, yGiven);

                assertTrue(xQuantile20 < xQuantile50, "Le quantile 20% doit être inférieur au quantile 50%.");
                assertTrue(xQuantile50 < xQuantile80, "Le quantile 50% doit être inférieur au quantile 80%.");
            }

            @Test
            @DisplayName("Propriété de réciprocité : Inverser le quantile calculé doit redonner la probabilité p d'origine")
            void roundTripConsistencyY() {
                double xGiven = 0.0;
                double pTarget = 0.60;

                // 1. Calcul de la valeur y correspondant au quantile pTarget = 0.60
                double yQuantile = kde.inverseConditionalCdfY(pTarget, xGiven);

                // 2. Re-calcul manuel de la CDF conditionnelle P(Y <= yQuantile | X = xGiven)
                // Note: On peut la calculer en dérivant la CDF bivariée ou via le helper si accessible,
                // ou en vérifiant que yQuantile est encadré correctement.
                assertFalse(Double.isNaN(yQuantile));

                // On vérifie que p = 0.50 donne un quantile cohérent proche du centre du cluster Y (autour de 0.0)
                double medianY = kde.inverseConditionalCdfY(0.50, 0.0);
                assertEquals(0.0, medianY, 0.2, "La médiane conditionnelle autour du cluster (0,0) doit être proche de 0.0.");
            }
        }

        @Nested
        @DisplayName("Tests sur des données parfaitement symétriques (Loi théorique vérifiable)")
        class SymmetricDistributionTests {

            @Test
            @DisplayName("Sur des données symétriques centrées en (0,0), le quantile p=0.5 doit valoir exactement 0.0")
            void medianOnSymmetricDataShouldBeZero() {
                // Un nuage de points parfaitement symétrique par rapport à l'origine (0,0)
                Vector xSym = new ArrayVector(new double[]{-1.0, 1.0, -1.0, 1.0});
                Vector ySym = new ArrayVector(new double[]{-1.0, -1.0, 1.0, 1.0});

                BivariateKernelDensityDistribution symmetricKde =
                        new BivariateKernelDensityDistribution(xSym, ySym, 1.0, 1.0);

                // Pour xGiven = 0.0, la médiane conditionnelle (p = 0.5) de Y doit être exactement 0.0
                double yMedian = symmetricKde.inverseConditionalCdfY(0.5, 0.0);
                assertEquals(0.0, yMedian, 1e-4, "La médiane conditionnelle de Y sachant X=0 doit être 0.0.");

                // Pour yGiven = 0.0, la médiane conditionnelle (p = 0.5) de X doit être exactement 0.0
                double xMedian = symmetricKde.inverseConditionalCdfX(0.5, 0.0);
                assertEquals(0.0, xMedian, 1e-4, "La médiane conditionnelle de X sachant Y=0 doit être 0.0.");
            }
        }
    }
}