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
import org.series.timegrid.TimeFrameAligner;
import org.series.timeserie.*;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class DataContainerFactory {

    private final DataLoader[] dataLoaders;
    private final ImputationStrategy imputationStrategy;

    public DataContainerFactory(ImputationStrategy imputationStrategy, DataLoader... loaders){
        this.dataLoaders = loaders;
        this.imputationStrategy = imputationStrategy;
    }

    public  DataContainer create(long start, long end, Instrument instrument, TimeFrame... timeFrames) throws LoadingException {
        DataContainer dataContainer = new DataContainer(instrument);
        for (TimeFrame timeFrame : timeFrames) {

            ZoneId zoneId = instrument.getZoneIdEnum().getZoneId();
            TimeFrameAligner aligner = AlignerService.get(instrument);
            long roundStart = TimeTools.fromZonedDateTimeToLong(aligner.alignFloor(TimeTools.fromLongToZonedDateTime(start, zoneId), timeFrame));
            long roundEnd = TimeTools.fromZonedDateTimeToLong(AlignerService.get(instrument).alignFloor(TimeTools.fromLongToZonedDateTime(end, zoneId), timeFrame));

            List<Candle> candles = useLoaders(instrument, timeFrame, roundStart, roundEnd);

            Observation[] opens = new Observation[candles.size()];
            Observation[] closes = new Observation[candles.size()];
            Observation[] highs = new Observation[candles.size()];
            Observation[] lows = new Observation[candles.size()];
            Observation[] volumes = new Observation[candles.size()];

            for (int i = 0; i < candles.size(); i++) {
                long dateTime = candles.get(i).timestamp();
                opens[i] = new RawObservation(dateTime, candles.get(i).open(), instrument.getZoneIdEnum().getZoneId());
                closes[i] = new RawObservation(dateTime, candles.get(i).close(),  instrument.getZoneIdEnum().getZoneId());
                highs[i] = new RawObservation(dateTime, candles.get(i).high(),  instrument.getZoneIdEnum().getZoneId());
                lows[i] = new RawObservation(dateTime, candles.get(i).low(),  instrument.getZoneIdEnum().getZoneId());
                volumes[i] = new RawObservation(dateTime, candles.get(i).volume(),  instrument.getZoneIdEnum().getZoneId());
            }
            Predicate<Long> timeGridValidDatePredicate = AlignerService.getValidDatePredicateBasedOnAligner(instrument, timeFrame);

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
                return outputs;
            } catch (LoadingException e) {
                stringBuilder.append(e.getMessage());
                stringBuilder.append(";");
            }
        }

        throw new LoadingException(stringBuilder.toString());


    }
}
