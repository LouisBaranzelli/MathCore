package org.math.graph;

import org.math.vector.ArrayVector;
import org.math.vector.Vector;

import java.awt.Color;
import java.io.File;
import java.io.IOException;

public class GraphicTest {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      TEST COMPLET DU MODULE GRAPHIC      ");
        System.out.println("==========================================\n");

        // 1. Préparation des données (Signaux Mathématiques)
        int size = 100;
        double[] xVals = new double[size];
        double[] ySineVals = new double[size];
        double[] yCosVals = new double[size];

        for (int i = 0; i < size; i++) {
            xVals[i] = i * 0.1;                     // X va de 0.0 à 9.9
            ySineVals[i] = Math.sin(xVals[i]);       // Y1 = sin(x)
            yCosVals[i] = Math.cos(xVals[i]);        // Y2 = cos(x)
        }

        Vector xVector = new ArrayVector(xVals);
        Vector ySineVector = new ArrayVector(ySineVals);
        Vector yCosVector = new ArrayVector(yCosVals);

        // 2. Instanciation du Graphique
        Graphic graphic = new Graphic("Ondes Sinus & Cosinus", "Temps (s)", "Amplitude");

        graphic.setShowGrid(true);
        graphic.setShowLegend(true);

        // 3. Configuration de la première série (Sinus en Rouge continu)
        GraphicSeries sineSeries = graphic.addSeries("Sinus", xVector, ySineVector);
        GraphicSeriesStyle style =sineSeries.getStyle();
        style.setColor(Color.RED);
        style.setStrokeWidth(2.5f);
        style.setLineStyle(GraphicSeriesStyle.LineStyle.SOLID);

        // 4. Configuration de la seconde série (Cosinus en Bleu pointillé)
        GraphicSeries cosSeries = graphic.addSeries("Cosinus", xVector, yCosVector);
        style = cosSeries.getStyle();
        style .setColor(Color.BLUE);
        style.setStrokeWidth(1.5f);
        style.setLineStyle(GraphicSeriesStyle.LineStyle.DASHED);

        // 5. Affichage des métriques calculées dans la console
        System.out.printf("Bornes X : [%.2f ; %.2f]%n", graphic.getXMin(), graphic.getXMax());
        System.out.printf("Bornes Y : [%.2f ; %.2f]%n", graphic.getYMin(), graphic.getYMax());

        // 6. Test d'affichage visuel (Ouvre la fenêtre Swing)
        System.out.println("\n[1/2] Lancement de l'affichage interactif (show)...");
        graphic.show(900, 600);

        // 7. Test de sauvegarde sous forme d'image (PNG)
        System.out.println("[2/2] Exportation de l'image (save)...");
        String filePath = "test_graphique.png";
//        try {
////            graphic.save(filePath);
//            File imgFile = new File(filePath);
//            System.out.println("✔ Image sauvegardée avec succès : " + imgFile.getAbsolutePath());
//        } catch (IOException e) {
//            System.err.println("❌ Échec de la sauvegarde de l'image : " + e.getMessage());
//        }

        System.out.println("\n==========================================");
        System.out.println("          TESTS TERMINÉS !                ");
        System.out.println("==========================================");
    }
}