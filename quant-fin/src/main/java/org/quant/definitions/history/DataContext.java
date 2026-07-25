package org.quant.definitions.history;

import org.quant.definitions.candles.CandleTimeSerie;
import org.quant.definitions.assets.Instrument;
import org.quant.definitions.assets.Purchasable;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.timeserie.TimeFrame;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface DataContext {

    CandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame);

    public long getCurrentTimestamp();

    public Set<Instrument> getInstruments();

    default public String getDescription(){

            String timeframesString = getTimeFrames().stream()
                    .map(TimeFrame::getLabel)
                    .collect(Collectors.joining(", "));

            return String.format("Data context: %d companies loaded (%.1f %%), timeframes: %s, start date: %s, end date: %s",
                    getInstruments().size(),
                    getPercentLoaded(),
                    timeframesString,
                    TimeTools.fromLongToZonedDateTime(getStart(), ZoneIdEnum.EUROPE_PARIS.getZoneId()),
                    TimeTools.fromLongToZonedDateTime(getEnd(), ZoneIdEnum.EUROPE_PARIS.getZoneId())
            );
    }

    double getPercentLoaded();

    List<TimeFrame> getTimeFrames();

    public long getStart();
    public long getEnd();
}
