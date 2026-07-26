package org.math.graph;

import lombok.Getter;
import lombok.Setter;
import org.math.vector.Vector;

@Getter
@Setter
public class GraphicSeries {

    private String name;
    private Vector xData;
    private Vector yData;
    private GraphicSeriesStyle style;

    public GraphicSeries(String name, Vector xData, Vector yData) {
        this(name, xData, yData, new GraphicSeriesStyle());
    }

    public GraphicSeries(String name, Vector xData, Vector yData, GraphicSeriesStyle style) {
        if (xData == null || yData == null) {
            throw new NullPointerException("Les vecteurs xData et yData ne peuvent pas être nulls.");
        }
        if (xData.size() != yData.size()) {
            throw new IllegalArgumentException("Les vecteurs X et Y doivent avoir la même dimension ("
                    + xData.size() + " vs " + yData.size() + ").");
        }
        this.name = name;
        this.xData = xData;
        this.yData = yData;
        this.style = (style != null) ? style : new GraphicSeriesStyle();
    }

    public int getItemCount() {
        return xData.size();
    }
}