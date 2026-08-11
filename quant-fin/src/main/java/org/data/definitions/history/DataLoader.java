package org.data.definitions.history;

import org.common.TriConsumer;
import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.series.TimeTools;
import org.series.timeserie.TimeFrame;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DataLoader {

    List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException;

    String getLabel();

    void onSuccessLoading(Instrument instrument, TimeFrame timeFrame, List<Candle> candles);

    default String logLoading(long start, long end, Instrument instrument, TimeFrame timeFrame){
        return String.format(
                "%s: loading of %s (%S) between %s and %s",
                getLabel(),
                instrument.getLabel(),
                timeFrame.getLabel(),
                TimeTools.fromLongToZonedDateTime(start, instrument.getZoneIdEnum().getZoneId()),
                TimeTools.fromLongToZonedDateTime(end, instrument.getZoneIdEnum().getZoneId())
        );
    }
}
