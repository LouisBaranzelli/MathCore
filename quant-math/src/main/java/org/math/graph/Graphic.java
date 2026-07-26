package org.math.graph;

import lombok.Getter;
import lombok.Setter;
import org.math.graph.GraphicSeries;
import org.math.graph.GraphicSeriesStyle;
import org.math.vector.Vector;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Conteneur principal d'un graphique (titre, axes, quadrillage, séries de données).
 * Inclut la capacité d'affichage (show) et d'export sous forme d'image (save).
 */

@Getter
@Setter
public class Graphic {

    private String title;
    private String xAxisLabel;
    private String yAxisLabel;
    private boolean showGrid;
    private boolean showLegend;

    private final List<GraphicSeries> seriesList;

    public Graphic() {
        this("Graphique", "X", "Y");
    }

    public Graphic(String title, String xAxisLabel, String yAxisLabel) {
        this.title = title;
        this.xAxisLabel = xAxisLabel;
        this.yAxisLabel = yAxisLabel;
        this.showGrid = true;
        this.showLegend = true;
        this.seriesList = new ArrayList<>();
    }

    // --- Gestion des séries ---

    public Graphic addSeries(GraphicSeries series) {
        if (series == null) {
            throw new NullPointerException("La série ajoutée ne peut pas être null.");
        }
        this.seriesList.add(series);
        return this;
    }

    public GraphicSeries addSeries(String name, Vector x, Vector y) {
        GraphicSeries series = new GraphicSeries(name, x, y);
        addSeries(series);
        return series;
    }

    public boolean removeSeries(GraphicSeries series) {
        return this.seriesList.remove(series);
    }

    public void clearAllSeries() {
        this.seriesList.clear();
    }

    public List<GraphicSeries> getSeriesList() {
        return Collections.unmodifiableList(seriesList);
    }

    // --- Calcul des bornes globales ---

    public double getXMin() {
        return seriesList.stream().mapToDouble(s -> minOfVector(s.getXData())).min().orElse(0.0);
    }

    public double getXMax() {
        return seriesList.stream().mapToDouble(s -> maxOfVector(s.getXData())).max().orElse(1.0);
    }

    public double getYMin() {
        return seriesList.stream().mapToDouble(s -> minOfVector(s.getYData())).min().orElse(0.0);
    }

    public double getYMax() {
        return seriesList.stream().mapToDouble(s -> maxOfVector(s.getYData())).max().orElse(1.0);
    }

    private static double minOfVector(Vector vec) {
        if (vec == null || vec.size() == 0) return 0.0;
        double min = vec.getValue(0);
        for (int i = 1; i < vec.size(); i++) {
            if (vec.getValue(i) < min) min = vec.getValue(i);
        }
        return min;
    }

    private static double maxOfVector(Vector vec) {
        if (vec == null || vec.size() == 0) return 0.0;
        double max = vec.getValue(0);
        for (int i = 1; i < vec.size(); i++) {
            if (vec.getValue(i) > max) max = vec.getValue(i);
        }
        return max;
    }

    // --- Méthodes d'Affichage et d'Exportation ---

    /**
     * Ouvre une fenêtre pour afficher le graphique à l'écran.
     */
    public void show() {
        show(800, 600);
    }

    /**
     * Ouvre une fenêtre d'affichage avec dimensions spécifiques.
     */
    public void show(int width, int height) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(this.getTitle());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(width, height);
            frame.setLocationRelativeTo(null);
            frame.add(new GraphicPanel(this));
            frame.setVisible(true);
        });
    }

    /**
     * Sauvegarde le graphique sous forme d'image (PNG, JPG, etc.).
     */
    public void save(File file, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        GraphicPanel panel = new GraphicPanel(this);
        panel.setSize(width, height);

        Graphics2D g2 = image.createGraphics();
        panel.paintComponent(g2);
        g2.dispose();

        String fileName = file.getName();
        String format = "png";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < fileName.length() - 1) {
            format = fileName.substring(dotIndex + 1);
        }

        boolean written = ImageIO.write(image, format, file);
        if (!written) {
            throw new IOException("Format d'image non supporté : " + format);
        }
    }

    public void save(String filePath) throws IOException {
        save(new File(filePath), 800, 600);
    }

    // =========================================================================
    // CLASS INTERNE : Panneau de Rendu Swing
    // =========================================================================
    private static class GraphicPanel extends JPanel {
        private final Graphic graphic;

        public GraphicPanel(Graphic graphic) {
            this.graphic = graphic;
            setBackground(Color.WHITE);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int padding = 60;
            int width = getWidth() - 2 * padding;
            int height = getHeight() - 2 * padding;

            if (width <= 0 || height <= 0) return;

            double xMin = graphic.getXMin();
            double xMax = graphic.getXMax();
            double yMin = graphic.getYMin();
            double yMax = graphic.getYMax();

            // Titre principal
            g2.setColor(Color.BLACK);
            g2.setFont(new Font("SansSerif", Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int titleWidth = fm.stringWidth(graphic.getTitle());
            g2.drawString(graphic.getTitle(), (getWidth() - titleWidth) / 2, padding / 2);

            // Quadrillage
            if (graphic.isShowGrid()) {
                g2.setColor(new Color(230, 230, 230));
                for (int i = 0; i <= 10; i++) {
                    int x = padding + (i * width / 10);
                    int y = padding + (i * height / 10);
                    g2.drawLine(x, padding, x, padding + height);
                    g2.drawLine(padding, y, padding + width, y);
                }
            }

            // Axes
            g2.setColor(Color.DARK_GRAY);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(padding, padding + height, padding + width, padding + height); // Axe X
            g2.drawLine(padding, padding, padding, padding + height);                  // Axe Y

            // Dessin des séries
            for (GraphicSeries series : graphic.getSeriesList()) {
                GraphicSeriesStyle style = series.getStyle();

                Stroke stroke;
                if (style.getLineStyle() == GraphicSeriesStyle.LineStyle.DASHED) {
                    stroke = new BasicStroke(style.getStrokeWidth(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{8.0f, 4.0f}, 0.0f);
                } else {
                    stroke = new BasicStroke(style.getStrokeWidth());
                }

                g2.setColor(style.getColor());
                g2.setStroke(stroke);

                Vector xData = series.getXData();
                Vector yData = series.getYData();

                for (int i = 0; i < series.getItemCount() - 1; i++) {
                    int x1 = (int) (padding + (xData.getValue(i) - xMin) / (xMax - xMin) * width);
                    int y1 = (int) (padding + height - (yData.getValue(i) - yMin) / (yMax - yMin) * height);
                    int x2 = (int) (padding + (xData.getValue(i + 1) - xMin) / (xMax - xMin) * width);
                    int y2 = (int) (padding + height - (yData.getValue(i + 1) - yMin) / (yMax - yMin) * height);

                    g2.drawLine(x1, y1, x2, y2);
                }
            }

            // Légende
            if (graphic.isShowLegend()) {
                int legendX = padding + 20;
                int legendY = padding + 20;
                g2.setFont(new Font("SansSerif", Font.PLAIN, 12));

                for (GraphicSeries series : graphic.getSeriesList()) {
                    g2.setColor(series.getStyle().getColor());
                    g2.fillRect(legendX, legendY, 15, 10);
                    g2.setColor(Color.BLACK);
                    g2.drawString(series.getName(), legendX + 22, legendY + 10);
                    legendY += 20;
                }
            }
        }
    }
}