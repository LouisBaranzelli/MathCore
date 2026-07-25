package org.quant.definitions.history;

import org.quant.definitions.LoadingException;
import org.quant.definitions.assets.DataContainer;
import org.quant.definitions.assets.Instrument;
import org.series.timeserie.TimeFrame;

public interface Dataloader {

    DataContainer load(long start, long end, Instrument instrument, TimeFrame... timeFrame) throws LoadingException;

    String getLabel();
}
