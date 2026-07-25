package org.quant.definitions.history;

import lombok.Getter;
import org.math.common.MathUtil;
import org.quant.definitions.LoadingException;
import org.quant.definitions.assets.DataContainer;
import org.quant.definitions.candles.CandleTimeSerie;
import org.quant.definitions.assets.Instrument;
import org.series.TimeTools;
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
    private final List<TimeFrame> timeFrames;

    public FullDataContext(long start, long end, List<Dataloader> dataloaders, List<Instrument> instruments, List<TimeFrame> timeFrames) {
        this.end = end;
        this.start = start;
        this.timeFrames = List.copyOf(timeFrames);
        this.initialSizeInstruments = instruments.size();

        TimeFrame[] timeFrameArray = this.timeFrames.toArray(TimeFrame[]::new);

        for (Instrument instrument : instruments) {
            DataContainer dataContainer = null;

            for (Dataloader dataloader : dataloaders) {
                try {
                    dataContainer = dataloader.load(start, end, instrument, timeFrameArray);
                    if (dataContainer != null) {
                        break;
                    }
                } catch (LoadingException e) {
                    logger.debug("Failed to load {} with {}: {}", instrument.getLabel(), dataloader.getLabel(), e.getMessage());
                }
            }

            if (dataContainer == null) {
                logger.debug("Failed to load {}", instrument.getLabel());
            } else {
                data.put(instrument, dataContainer);
            }
        }
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