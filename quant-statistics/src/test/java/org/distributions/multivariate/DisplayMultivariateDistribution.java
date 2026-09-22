package org.distributions.multivariate;

import org.distributions.multivariate.MultivariateNormalDistribution;
import org.math.graph.Graphic;
import org.math.graph.GraphicSeries;
import org.math.matrix.DenseMatrix;
import org.math.matrix.Matrix;
import org.math.vector.ArrayVector;
import org.math.vector.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.data.Sample;
import org.distributions.monovariate.continuous.EmpiricalDistribution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


public class DisplayMultivariateDistribution {

    public static void main(String[] args) {
        Logger logger = LoggerFactory.getLogger(DisplayMultivariateDistribution.class);
        int sampleSize = 1000;
        Graphic graphic = new Graphic();
        Matrix matrixCovariance = new DenseMatrix(new double[][]{{5, -3.},{-3, 5.}});
        Vector means = new ArrayVector(10, 10);
        MultivariateNormalDistribution multivariateNormalDistribution = new MultivariateNormalDistribution(means, matrixCovariance);
        Vector[] results = multivariateNormalDistribution.sample(sampleSize);
        for(int dim=0; dim<multivariateNormalDistribution.getDimension();dim++){

            double mean = means.getValue(dim);
            double stdr = Math.sqrt(matrixCovariance.get(dim, dim));

            double[] distribution = new double[sampleSize];
            for (int i=0; i<sampleSize; i++){
                distribution[i] = results[i].getValue(dim);
            }

            EmpiricalDistribution empiricalDistribution = new EmpiricalDistribution(new Sample(distribution));
            double stepSize = 0.1;
            List<Double> x = new ArrayList<>();
            List<Double> y = new ArrayList<>();
            for (double step=mean -2 * stdr; step<mean +2 * stdr; step+=stepSize){
                x.add(step);
                y.add(empiricalDistribution.cdf(step));
            }
            Vector xVector = new ArrayVector(x.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray());
            Vector yVector = new ArrayVector(y.stream()
                    .mapToDouble(Double::doubleValue)
                    .toArray());

            graphic.addSeries(new GraphicSeries("dim: " + dim,xVector, yVector));
        }
        for (int i=0; i<10; i++){

            Vector sample = multivariateNormalDistribution.sample();

            String result = IntStream.range(0, sample.size())
                    .mapToObj(dim -> String.format("%.4f", sample.getValue(dim)))
                    .collect(Collectors.joining(", "));

            logger.info("Intricated values: [{}]", result);
        }
        graphic.show();

    }


}
