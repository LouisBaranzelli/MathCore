package org.data.yahoofinance;

import org.series.timeserie.TimeFrame;

import static org.series.timeserie.TimeFrame.*;

public class YahooFinanceIntervalService {

    /**
     * Convertit un {@link TimeFrame} en chaîne d'intervalle reconnue par l'API Yahoo Finance.
     *
     * @param timeFrame L'unité de temps sélectionnée
     * @return L'intervalle au format Yahoo Finance (ex: "1m", "1h", "1d", "1wk")
     * @throws IllegalArgumentException Si le TimeFrame fourni est null ou non géré
     */
    public static String getInterval(TimeFrame timeFrame) {
        if (timeFrame == null) {
            throw new IllegalArgumentException("Le TimeFrame ne peut pas être null");
        }

        return switch (timeFrame) {
            case MI   -> "1m";
            case MI5  -> "5m";
            case MI15 -> "15m";
            case MI30 -> "30m";
            case HR   -> "1h";
            case D    -> "1d";
            case WK   -> "1wk";
        };
    }

    public static String getTicker(TimeFrame timeFrame) {
        return getInterval(timeFrame);
    }
}