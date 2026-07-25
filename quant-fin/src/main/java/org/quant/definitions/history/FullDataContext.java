package org.quant.definitions.history;

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
    private final long end;
    private final int initialSizeInstruments;
    private final List<TimeFrame> timeFrames;

    public FullDataContext(long start, long end, List<Dataloader> dataloaders, List<Instrument> instruments, List<TimeFrame> timeFrames) {
        this.end = end;
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
    public String getDescription() {
        double percent = initialSizeInstruments > 0
                ? MathUtil.round((double) getInstruments().size() * 100 / initialSizeInstruments, 1)
                : 0.0;

        String timeframesString = this.timeFrames.stream()
                .map(TimeFrame::getLabel)
                .collect(Collectors.joining(", "));

        return String.format("%d companies loaded (%.1f %%), timeframes: %s, end date: %s",
                getInstruments().size(),
                percent,
                timeframesString,
                TimeTools.fromLongToInstant(end)
        );
    }
}