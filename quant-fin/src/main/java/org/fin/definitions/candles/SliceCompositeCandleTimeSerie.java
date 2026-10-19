package org.fin.definitions.candles;

import lombok.AccessLevel;
import lombok.Getter;
import org.math.vector.Vector;
import org.fin.definitions.assets.Instrument;
import org.series.TimeTools;
import org.series.timeserie.DoubleTimeSerie;
import org.series.timeserie.SliceDoubleTimeSerie;
import org.series.timeserie.TimeFrame;

@Getter
public class SliceCompositeCandleTimeSerie implements CandleTimeSerie {

    private final CandleTimeSerie source;
    private final long start;
    private final long end;

    // 🔒 Empêche Lombok de générer un getIndexStart() et getIndexEnd()
    @Getter(AccessLevel.NONE)
    private final int indexStart;

    @Getter(AccessLevel.NONE)
    private final int indexEnd;

    public SliceCompositeCandleTimeSerie(CandleTimeSerie source, long timestampStart, long timestampEnd) {
        this.source = source;
        this.start = timestampStart;
        this.end = timestampEnd;

        int foundStart = -1;
        int foundEnd = -1;

        for (int i = 0; i < source.size(); i++) {
            long ts = source.getTimestamp(i);
            if (ts == timestampStart && foundStart == -1) {
                foundStart = i;
            }
            if (ts == timestampEnd) {
                foundEnd = i;
            }
        }

        if (foundStart == -1) {
            throw new IllegalArgumentException("Timestamp de début (" + timestampStart + ": " + TimeTools.fromLongToZonedDateTime(timestampStart, source.getInstrument().getZoneIdEnum().getZoneId()) + ") introuvable.");
        }
        if (foundEnd == -1) {
            throw new IllegalArgumentException("Timestamp de fin (" + timestampEnd + ": " + TimeTools.fromLongToZonedDateTime(timestampStart, source.getInstrument().getZoneIdEnum().getZoneId()) + ") introuvable.");
        }
        if (foundStart > foundEnd) {
            throw new IllegalArgumentException("Le timestamp de début doit être inférieur ou égal au timestamp de fin.");
        }

        this.indexStart = foundStart;

        /*
            Dans this, la valeur du timestamp inclut la derniere valeur
            Mais l'index de fin est exclusif (d'ou le +1)
        */
        this.indexEnd = foundEnd + 1;
    }

    @Override
    public int size() {
        return indexEnd - indexStart;
    }

    @Override
    public Candle getCandle(int index) {
        if (index < 0 || index >= size()) {
            throw new IndexOutOfBoundsException("Index hors limites : " + index);
        }
        return source.getCandle(indexStart + index);
    }

    @Override
    public DoubleTimeSerie getOpenTimeSerie() {
        return new SliceDoubleTimeSerie(source.getOpenTimeSerie(), indexStart, indexEnd);
    }

    @Override
    public DoubleTimeSerie getCloseTimeSerie() {
        return new SliceDoubleTimeSerie(source.getCloseTimeSerie(), indexStart, indexEnd);
    }

    @Override
    public DoubleTimeSerie getHighTimeSerie() {
        return new SliceDoubleTimeSerie(source.getHighTimeSerie(), indexStart, indexEnd);
    }

    @Override
    public DoubleTimeSerie getLowTimeSerie() {
        return new SliceDoubleTimeSerie(source.getLowTimeSerie(), indexStart, indexEnd);
    }

    @Override
    public DoubleTimeSerie getVolumeTimeSerie() {
        return new SliceDoubleTimeSerie(source.getVolumeTimeSerie(), indexStart, indexEnd);
    }

    @Override
    public Instrument getInstrument() {
        return source.getInstrument();
    }

    @Override
    public TimeFrame getTimeFrame() {
       return source.getTimeFrame();
    }

    @Override
    public Vector toVector() {
        return getCloseTimeSerie().toVector();
    }
}