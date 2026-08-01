package org.data.definitions.history;

import org.data.definitions.candles.CandleTimeSerie;
import org.data.definitions.assets.Instrument;
import org.series.TimeTools;
import org.series.ZoneIdEnum;
import org.series.timeserie.TimeFrame;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public interface DataContext {

    CandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame);

    public long getCurrentTimestamp();

    public Set<Instrument> getInstruments();

    default public String getDescription(){

            String timeframesString = Arrays.stream(getTimeFrames())
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

    TimeFrame[] getTimeFrames();

    public long getStart();
    public long getEnd();
}
