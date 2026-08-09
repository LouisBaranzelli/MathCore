package org.data.yahoofinance;

import org.series.timeserie.TimeFrame;

public class YahooFinanceIntervalService {

    public static String getTicker(TimeFrame timeFrame){
        switch (timeFrame){
            case D -> {
                return "1d";
            }
            case WK -> {
                return "1wk";
            }
            case MO -> {
                return "1mo";
            }
            default -> throw new RuntimeException("à implémenter");
        }
    }
}
