package org.data.definitions.history;

import lombok.Getter;
import org.math.common.MathUtil;
import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.candles.CandleTimeSerie;
import org.data.definitions.assets.Instrument;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.imputation.ImputationStrategy;
import org.series.timeserie.TimeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class FullDataContext implements DataContext {

    private static final Logger logger = LoggerFactory.getLogger(FullDataContext.class);

    private final Map<Instrument, DataContainer> data = new HashMap<>();

    @Getter
    private final long end;
    @Getter
    private final long start;
    private final int initialSizeInstruments;
    @Getter
    private final TimeFrame[] timeFrames;

    public FullDataContext(long start, long end, ImputationStrategy imputationStrategy,  List<DataLoader> dataLoaders, List<Instrument> instruments, List<TimeFrame> timeFrames) {

        logger.debug("Full Context loading between {} and {} for {} instruments ({}), with: {}",
                TimeTools.fromLongToZonedDateTime(start, ZoneIdEnum.EUROPE_PARIS.getZoneId()),
                TimeTools.fromLongToZonedDateTime(end, ZoneIdEnum.EUROPE_PARIS.getZoneId()),
                instruments.size(),
                timeFrames.stream().map(TimeFrame::getLabel).collect(Collectors.joining(", ")),
                dataLoaders.stream().map(DataLoader::getLabel).collect(Collectors.joining(", "))
        );

        this.end = end;
        this.start = start;
        DataContainerFactory dataContainerFactory = new DataContainerFactory(imputationStrategy, dataLoaders.toArray(DataLoader[]::new));
        this.timeFrames =timeFrames.toArray(TimeFrame[]::new);
        this.initialSizeInstruments = instruments.size();
        for (Instrument instrument : instruments) {
            DataContainer dataContainer;
            try {
                dataContainer = dataContainerFactory.create(start, end, instrument,  this.timeFrames);
                data.put(instrument, dataContainer);
            } catch (LoadingException e) {
                logger.warn("Failed to load {}: {}", instrument.getLabel(), e.getMessage());
            }
        }
        logger.info("End of the loading context - {}", getDescription());
    }

    @Override
    public CandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame) {
        DataContainer container = data.get(instrument);
        if (container == null) {
            throw new IllegalArgumentException("No loaded data available for instrument: " + instrument.getLabel());
        }
        CandleTimeSerie candleTimeSerie = container.getCandleTimeSerie(timeFrame);
        if (candleTimeSerie == null) {
            throw new IllegalArgumentException(String.format("No loaded data available for time frame: %s for %s", instrument.getLabel(), instrument.getLabel()));
        }
        return  candleTimeSerie;
    }

    @Override
    public long getCurrentTimestamp() {
        return end;
    }

    @Override
    public Set<Instrument> getInstruments() {
        return Collections.unmodifiableSet(data.keySet());
    }

    @Override
    public double getPercentLoaded() {
        return  initialSizeInstruments > 0
                ? MathUtil.round((double) getInstruments().size() * 100 / initialSizeInstruments, 1)
                : 0.0;
    }
}