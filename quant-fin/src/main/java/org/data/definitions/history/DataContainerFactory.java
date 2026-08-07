package org.data.definitions.history;

import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.data.definitions.candles.CandleTimeSerie;
import org.data.definitions.candles.CompositeCandleTimeSerie;
import org.series.InvalidTimeSerieException;
import org.series.TimeTools;
import org.series.imputation.ImputationStrategy;
import org.series.timeserie.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public class DataContainerFactory {

    private final DataLoader[] dataLoaders;
    private final ImputationStrategy imputationStrategy;
    private final TimeGridPredicate timeGridPredicate;

    public DataContainerFactory(ImputationStrategy imputationStrategy,  TimeGridPredicate timeGridPredicate, DataLoader... loaders){
        this.dataLoaders = loaders;
        this.imputationStrategy = imputationStrategy;
        this.timeGridPredicate = timeGridPredicate;
    }

    public  DataContainer create(long start, long end, Instrument instrument, TimeFrame... timeFrames) throws LoadingException {
        DataContainer dataContainer = new DataContainer(instrument);
        for (TimeFrame timeFrame : timeFrames) {
            List<Candle> candles = useLoaders(instrument, timeFrame, start, end);

            Observation[] opens = new Observation[candles.size()];
            Observation[] closes = new Observation[candles.size()];
            Observation[] highs = new Observation[candles.size()];
            Observation[] lows = new Observation[candles.size()];
            Observation[] volumes = new Observation[candles.size()];

            for (int i = 0; i < candles.size(); i++) {
                long dateTime = candles.get(i).timestamp();
                opens[i] = new RawObservation(dateTime, candles.get(i).open());
                closes[i] = new RawObservation(dateTime, candles.get(i).close());
                highs[i] = new RawObservation(dateTime, candles.get(i).high());
                lows[i] = new RawObservation(dateTime, candles.get(i).low());
                volumes[i] = new RawObservation(dateTime, candles.get(i).volume());
            }

            Predicate<Long> timeGridValidDatePredicate = (date) -> timeGridPredicate.test(TimeTools.fromLongToZonedDateTime(date, instrument.getZoneIdEnum().getZoneId()), timeFrame);

            try {
                DoubleTimeSerie openTs = TimeSeriesFactory.create(opens,
                        imputationStrategy,
                        timeFrame,
                        timeGridValidDatePredicate
                );

                DoubleTimeSerie closeTs = TimeSeriesFactory.create(closes,
                        imputationStrategy,
                        timeFrame,
                        timeGridValidDatePredicate);

                DoubleTimeSerie highsTs = TimeSeriesFactory.create(highs,
                        imputationStrategy,
                        timeFrame,
                        timeGridValidDatePredicate);

                DoubleTimeSerie lowsTs = TimeSeriesFactory.create(lows,
                        imputationStrategy,
                        timeFrame,
                        timeGridValidDatePredicate);

                DoubleTimeSerie volumeTs = TimeSeriesFactory.create(volumes,
                        imputationStrategy,
                        timeFrame,
                        timeGridValidDatePredicate);

                CandleTimeSerie candleTimeSerie = new CompositeCandleTimeSerie(instrument, timeFrame, openTs, highsTs, lowsTs, closeTs, volumeTs);
                dataContainer.addData(candleTimeSerie);
            } catch (InvalidTimeSerieException e) {
                throw new LoadingException(e.getMessage());
            }

        }
        return  dataContainer;
    }

    private List<Candle> useLoaders(Instrument instrument, TimeFrame timeFrame, long start, long end) throws LoadingException {
        List<Candle> outputs = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        for (DataLoader loader: dataLoaders){
            try {
                outputs.addAll(loader.load(start, end, instrument,timeFrame));
            } catch (LoadingException e) {
                stringBuilder.append(e.getMessage());
                stringBuilder.append(";");
            }
        }
        if (outputs.isEmpty()){
            throw new LoadingException("failed to load: " + instrument.getLabel() + " with " + timeFrame.getLabel() + " because: " + stringBuilder.toString());
        }
        return outputs;
    }
}
