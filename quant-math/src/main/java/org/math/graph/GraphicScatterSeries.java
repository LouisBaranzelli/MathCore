package org.math.graph;

import org.math.vector.Vector;

import java.awt.*;

public class GraphicScatterSeries extends GraphicSeries {

    private float markerSize; // Taille du point/marqueur en pixels

    public GraphicScatterSeries(String name, Vector xData, Vector yData) {
        this(name, xData, yData, Color.BLUE, 6.0f);
    }

    public GraphicScatterSeries(String name, Vector xData, Vector yData, Color color, float markerSize) {
        super(name, xData, yData);

        this.markerSize = markerSize;

        // Configuration par défaut pour un nuage de points
        this.getStyle().setColor(color);
        this.getStyle().setLineStyle(GraphicSeriesStyle.LineStyle.NONE); // Pas de ligne reliant les points
        this.getStyle().setMarkerStyle(GraphicSeriesStyle.MarkerStyle.CIRCLE); // Marqueurs circulaires
    }

    public float getMarkerSize() {
        return markerSize;
    }

    public GraphicScatterSeries setMarkerSize(float markerSize) {
        if (markerSize <= 0) {
            throw new IllegalArgumentException("La taille du marqueur doit être strictement positive.");
        }
        this.markerSize = markerSize;
        return this;
    }
}