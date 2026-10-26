package org.data.yahoofinance;


import org.common.FetcherUrl;
import org.data.SavingException;
import org.data.csv.DataSaver;
import org.fin.definitions.LoadingException;

import org.fin.definitions.assets.Instrument;
import org.fin.definitions.candles.Candle;
import org.fin.definitions.history.DataLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.series.TimeTools;
import org.series.timeserie.TimeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class YahooFinanceLoader implements DataLoader {

    private final DataSaver[] savers;
    private final DataLoader defaultLoader;
    private static Logger logger = LoggerFactory.getLogger(YahooFinanceLoader.class);
    private static final int REST_TIME = 500; // délai d'attente en ms pour ne pas surcharcher l'API

    public YahooFinanceLoader() {
        this(null);
    }

    public YahooFinanceLoader(DataLoader defaultLoader, DataSaver... savers) {
        this.savers = savers;
        this.defaultLoader = defaultLoader;
    }

    @Override
    public List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        logger.debug(logLoading(start, end, instrument, timeFrame));
        if (timeFrame == TimeFrame.WK){
            try {
                logger.trace("[LOADER] {} not  in charge by {}, use {} instead",timeFrame.getLabel(), getLabel(), defaultLoader.getLabel());
                return defaultLoader.load(start, end, instrument, timeFrame);
            } catch (LoadingException e){
                logger.trace("[LOADER] Failed to load {} ({}) by using the loader {}. Using alternative methode ...", instrument.getLabel(), timeFrame.getLabel(), defaultLoader.getLabel());
            }
        }

        long startAdapted = start;
        long endAdapted = end;
        if (List.of(TimeFrame.D, TimeFrame.WK).contains(timeFrame)) {
            startAdapted = YahooFinanceTimeService.setHourAt(0, start, instrument.getZoneIdEnum().getZoneId());
            endAdapted = YahooFinanceTimeService.setHourAt(23, end, instrument.getZoneIdEnum().getZoneId());
        } else {
            // yahoo finance exclu la dernière valeur donc je rajoute une periode pour l'inclure Tick min 15h30 -> 15h30 et non 15h29
            endAdapted = end + TimeTools.fromDurationToLong(timeFrame.getDuration());
        }


        String urlStr = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=%s&period1=%d&period2=%d&events=dividends",
                instrument.getTicker(), YahooFinanceIntervalService.getTicker(timeFrame), startAdapted, endAdapted);

        logger.trace("[LOADER] - {} - url: {}",getLabel(), urlStr);
        FetcherUrl fetcherUrl = new FetcherUrl(urlStr);
        JSONObject root;
        String baseErrorMessage = String.format("(%s) Fetching data failed for %s (%s): ", getLabel(), instrument.getLabel(), timeFrame.getLabel());
        try {
            root = fetcherUrl.fetch();
            Thread.sleep(YahooFinanceLoader.REST_TIME);
        } catch (InterruptedException e) {
            throw new RuntimeException(baseErrorMessage + e.getMessage());
        }
        catch (Exception e) {
            throw new LoadingException(baseErrorMessage + e.getMessage());
        }
        JSONArray timestamps;
        try {
            timestamps = root.getJSONObject("chart")
                    .getJSONArray("result")
                    .getJSONObject(0)
                    .getJSONArray("timestamp");
        } catch (Exception e){
            logger.warn("[LOADER] - {} - wrong data format received for {} / {}", getLabel(), instrument.getLabel(), timeFrame.getLabel());
            throw new LoadingException(String.format("Mauvais format de données reçu: ", e.getMessage()));
        }

        long endDateReached = timestamps.getLong(timestamps.length() - 1);
        long startDateReached = timestamps.getLong(0);
        ZonedDateTime startZoneDateTime = TimeTools.fromLongToZonedDateTime(startDateReached, instrument.getZoneIdEnum().getZoneId());
        ZonedDateTime endZoneDateTime = TimeTools.fromLongToZonedDateTime(endDateReached, instrument.getZoneIdEnum().getZoneId());
        logger.trace("[LOADER] - {} - data received, start: {}, end: {}", getLabel(), startZoneDateTime, endZoneDateTime);
        endDateReached = adaptTimeStamp(endDateReached, timeFrame, instrument.getZoneIdEnum().getZoneId());

        if (endDateReached < end){
            logger.warn("[LOADER] - {} - end not reached, target: {}, got: {}", getLabel(),  TimeTools.fromLongToZonedDateTime(end, instrument.getZoneIdEnum().getZoneId()
            ),TimeTools.fromLongToZonedDateTime(endDateReached, instrument.getZoneIdEnum().getZoneId()));

            throw new LoadingException(String.format("(%s) Loading of %s (%s) failed to reach %s, got %s instead.",
                    getLabel(),
                    instrument.getLabel(),
                    timeFrame.getLabel(),
                    TimeTools.fromLongToZonedDateTime(end, instrument.getZoneIdEnum().getZoneId()
                    ), TimeTools.fromLongToZonedDateTime(endDateReached, instrument.getZoneIdEnum().getZoneId())));
        }



        JSONObject indicators = root.getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONObject("indicators")
                .getJSONArray("quote")
                .getJSONObject(0);

        JSONArray opens = indicators.getJSONArray("open");
        JSONArray highs = indicators.getJSONArray("high");
        JSONArray lows = indicators.getJSONArray("low");
        JSONArray closes = indicators.getJSONArray("close");
        JSONArray volumes = indicators.getJSONArray("volume");

        List<Candle> candles = new ArrayList<>();

        for (int i = 0; i < timestamps.length(); i++) {
            long ts = timestamps.getLong(i);
            ts = adaptTimeStamp(ts, timeFrame, instrument.getZoneIdEnum().getZoneId());
            if (ts < start || ts > end){
                continue;
            }
            try {
                double o = value(opens, i, instrument, timeFrame);
                double h = value(highs, i, instrument, timeFrame);
                double l = value(lows, i, instrument, timeFrame);
                double c = value(closes, i, instrument, timeFrame);
                double v = value(volumes, i, instrument, timeFrame);
                candles.add(new Candle(instrument,ts, o, h, l, c, v));
            } catch (LoadingException e) {
               // ne fais rien, sera traité par l'imputation
            } catch (IllegalArgumentException e){
                //Peut arriver sur certaines valeurs, ne rien faire, sera traité par l'imputation

            }

        }

        if (candles.isEmpty()){
            logger.warn("[LOADER] - {} - No data data received for {} / {}", getLabel(), instrument.getLabel(), timeFrame.getLabel());
            throw new LoadingException(String.format("(%s) No data available for %s, %s.",
                    getLabel(),
                    instrument.getLabel(),
                    timeFrame.getLabel()));
        }
        endDateReached = candles.getLast().timestamp();
        if (endDateReached != end){
            logger.warn("[LOADER] - {} - end not reached, target: {}, got: {}", getLabel(),  TimeTools.fromLongToZonedDateTime(end, instrument.getZoneIdEnum().getZoneId()
            ), TimeTools.fromLongToZonedDateTime(endDateReached, instrument.getZoneIdEnum().getZoneId()));

            throw new LoadingException(String.format("(%s) Loading of %s (%s) failed to reach %s, got %s instead.",
                    getLabel(),
                    instrument.getLabel(),
                    timeFrame.getLabel(),
                    TimeTools.fromLongToZonedDateTime(end, instrument.getZoneIdEnum().getZoneId()
                    ), TimeTools.fromLongToZonedDateTime(endDateReached, instrument.getZoneIdEnum().getZoneId())));
        }

        onSuccessLoading(instrument, timeFrame, candles);
        return candles;
    }

    private double value(JSONArray arr, int idx, Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        if (arr.isNull(idx)) {
            throw new LoadingException(
                    "(%s) Data loaded for %s (%s) contains null values".formatted(getLabel(), instrument.getLabel(), timeFrame.getLabel())
            );        }
        return arr.getDouble(idx);
    }
    /*
        Yahoofinance me retourne de données hebdo time stampé au Lundi
        donc 4 jours avant la date ffective de la valeur.
        Je corrige ce billet ici
     */
    private long adaptTimeStamp(long timestamp, TimeFrame timeFrame, ZoneId zoneId){
        if (timeFrame == TimeFrame.D) {
            return YahooFinanceTimeService.setHourAt(0, timestamp, zoneId);
        } else if (timeFrame == TimeFrame.WK) {
            return YahooFinanceTimeService.setAtFriday(timestamp, zoneId);
        }
        return timestamp;
    }

    @Override
    public String getLabel() {
        return "Loader Yahoo-Finance";
    }

    @Override
    public void onSuccessLoading(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) {
        logger.debug("[LOADER] - LOADING SUCCEED - {} ({}): {}", instrument.getLabel(), timeFrame.getLabel(), getLabel());
        if (this.savers != null){
            Arrays.stream(this.savers).forEach(s -> {
                try {
                    s.save(instrument, timeFrame, candles);
                } catch (SavingException e) {
                    logger.warn("[LOADER] Failed to save {} with {}", instrument.getLabel(), s.getLabel());
                }
            });
        }
    }


}
