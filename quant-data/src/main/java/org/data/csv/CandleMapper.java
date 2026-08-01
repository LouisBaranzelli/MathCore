package org.data.csv;

import org.common.CsvMapper;
import org.data.definitions.assets.Instrument;
import org.data.definitions.assets.InstrumentFactory;
import org.data.definitions.candles.Candle;

import java.util.List;
import java.util.stream.Collectors;

public class CandleMapper implements CsvMapper<Candle> {

    private final String SEPARATOR = ",";

    @Override
    public String toCsvLine(Candle obj) {
        List<Object> values = List.of(obj.instrument(), obj.timestamp(), obj.open(), obj.high(), obj.low(), obj.close(), obj.volume());
        return values.stream().map(Object::toString).collect(Collectors.joining(SEPARATOR));
    }

    @Override
    public Candle fromCsvLine(String csvLine) {
        if (csvLine == null || csvLine.isEmpty()) {
            return null;
        }

        String[] parts = csvLine.split(SEPARATOR);
        Instrument instrument = InstrumentFactory.from(parts[0]);
        long timestamp = Long.parseLong(parts[1]);
        double open = Double.parseDouble(parts[2]);
        double hight = Double.parseDouble(parts[3]);
        double low = Double.parseDouble(parts[4]);
        double close = Double.parseDouble(parts[5]);
        double volume = Double.parseDouble(parts[6]);

        return new Candle(instrument, timestamp, open, hight, low, close, volume);

    }
}
