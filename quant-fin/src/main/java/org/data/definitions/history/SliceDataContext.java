package org.data.definitions.history;

import lombok.Getter;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.CandleTimeSerie;
import org.data.definitions.candles.SliceCompositeCandleTimeSerie;
import org.series.TimeTools;
import org.series.timeserie.TimeFrame;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class SliceDataContext implements DataContext {

    private final DataContext source;

    @Getter
    private final long start;

    @Getter
    private final long end;

    public SliceDataContext(long start, long end, DataContext source) {

        this.source = Objects.requireNonNull(source, "Source DataContext cannot be null");

        if (start > end) {
            throw new IllegalArgumentException(String.format("Start timestamp (%d) cannot be greater than end timestamp (%d)", start, end));
        }

        if (start < source.getStart()) {
            throw new IllegalArgumentException(String.format("Slice start (%d) is before source start (%d)", start, source.getStart()));
        }

        if (end > source.getEnd()) {
            throw new IllegalArgumentException(String.format("Slice end (%d) is after source end (%d)", end, source.getEnd()));
        }

        this.start = start;
        this.end = end;
    }

    @Override
    public CandleTimeSerie getCandleTimeSerie(Instrument instrument, TimeFrame timeFrame) {
        CandleTimeSerie candleTimeSerie = source.getCandleTimeSerie(instrument, timeFrame);
        ZoneId zoneId = instrument.getZoneIdEnum().getZoneId();
        ZonedDateTime sliceStart = AlignerService.get(instrument).alignFloor(TimeTools.fromLongToZonedDateTime(start, zoneId), timeFrame);
        ZonedDateTime sliceEnd = AlignerService.get(instrument).alignFloor(TimeTools.fromLongToZonedDateTime(end, zoneId), timeFrame);
        return new SliceCompositeCandleTimeSerie(candleTimeSerie, TimeTools.fromZonedDateTimeToLong(sliceStart), TimeTools.fromZonedDateTimeToLong(sliceEnd));
    }

    @Override
    public long getCurrentTimestamp() {
        return end;
    }

    @Override
    public Set<Instrument> getInstruments() {
        return source.getInstruments();
    }

    @Override
    public double getPercentLoaded() {
        return source.getPercentLoaded();
    }

    @Override
    public TimeFrame[] getTimeFrames() {
        return source.getTimeFrames();
    }
}