package org.data.definitions.history;

import org.data.definitions.LoadingException;
import org.data.definitions.assets.DataContainer;
import org.data.definitions.assets.Instrument;
import org.series.timeserie.TimeFrame;

public interface Dataloader {

    DataContainer load(long start, long end, Instrument instrument, TimeFrame... timeFrame) throws LoadingException;

    String getLabel();
}
