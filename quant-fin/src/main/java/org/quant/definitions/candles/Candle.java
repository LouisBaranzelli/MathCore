package org.quant.definitions.candles;

import org.quant.definitions.assets.Instrument;

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

public record Candle(
        Instrument instrument,
        long timestamp,
        double open,
        double high,
        double low,
        double close,
        double volume
) {

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
}