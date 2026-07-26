package org.math.graph;

import lombok.Getter;
import lombok.Setter;

import java.awt.*;

@Getter
@Setter
public class GraphicSeriesStyle {

    public enum LineStyle {
        SOLID, DASHED, DOTTED, NONE
    }

    public enum MarkerStyle {
        NONE, CIRCLE, SQUARE, CROSS
    }

    private Color color;
    private float strokeWidth;
    private LineStyle lineStyle;
    private MarkerStyle markerStyle;

    public GraphicSeriesStyle() {
        this(Color.BLUE, 1.5f, LineStyle.SOLID, MarkerStyle.NONE);
    }

    public GraphicSeriesStyle(Color color, float strokeWidth, LineStyle lineStyle, MarkerStyle markerStyle) {
        this.color = color;
        this.strokeWidth = strokeWidth;
        this.lineStyle = lineStyle;
        this.markerStyle = markerStyle;
    }
}
