package org.data.definitions.candles;

import org.data.definitions.assets.Instrument;
import org.data.definitions.assets.Stock;

import java.util.Random;

/**
 * Représente un chandelier japonais (Candlestick / OHLCV).
 *
 * @param instrument  L'instrument financier concerné (Stock, Index, Commodity, etc.)
 * @param timestamp   Horodatage du début du chandelier
 * @param open        Prix d'ouverture
 * @param high        Prix le plus haut
 * @param low         Prix le plus bas
 * @param close       Prix de fermeture
 * @param volume      Volume échangé durant la période
 */

public record Candle (
        Instrument instrument,
        long timestamp,
        double open,
        double high,
        double low,
        double close,
        double volume
) implements Comparable<Candle> {

    /**
     * Compact constructor pour valider la cohérence des prix OHLC.
     */
    public Candle {
        if (high < open || high < close || high < low) {
            throw new IllegalArgumentException("Le prix 'high' (" + high + ") doit être supérieur ou égal aux autres prix.");
        }
        if (low > open || low > close || low > high) {
            throw new IllegalArgumentException("Le prix 'low' (" + low + ") doit être inférieur ou égal aux autres prix.");
        }
        if (open < 0 || close < 0 || high < 0 || low < 0 || volume < 0) {
            throw new IllegalArgumentException("Les prix et le volume doivent être positifs.");
        }
    }


    public boolean isBullish() {
        return close > open;
    }

    public boolean isBearish() {
        return close < open;
    }

    public double getReturn() {
        if (open == 0.0) return 0.0;
        return (close - open) / open;
    }

    public double getRange() {
        return high - low;
    }

    public double getBodySize() {
        return Math.abs(close - open);
    }

    @Override
    public int compareTo(Candle o) {
        return Long.compare(timestamp, o.timestamp);
    }

    public static Candle randomCandle(long timestamp){
        Random random = new Random();
        double high = random.nextDouble();
        double low = high / 2;
        double open = (high - low) * 0.3 + low;
        double close = (high - low) * 0.5 + low;

        return new Candle(Stock.SU, timestamp, open,  high,  low, close,  random.nextDouble());
    }
}