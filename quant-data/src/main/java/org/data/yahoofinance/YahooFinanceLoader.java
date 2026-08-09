package org.data.yahoofinance;


import org.common.FetcherUrl;
import org.data.SavingException;
import org.data.csv.DataSaver;
import org.data.definitions.LoadingException;
import org.data.definitions.TickEnum;
import org.data.definitions.TickService;
import org.data.definitions.assets.Instrument;
import org.data.definitions.candles.Candle;
import org.data.definitions.history.DataLoader;
import org.json.JSONArray;
import org.json.JSONObject;
import org.series.TimeTools;
import org.series.timeserie.TimeFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.IOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class YahooFinanceLoader implements DataLoader {

    private final DataSaver[] savers;
    private final DataLoader defaultLoader;
    private static Logger logger = LoggerFactory.getLogger(YahooFinanceLoader.class);

    public YahooFinanceLoader() {
        this(null);
    }

    public YahooFinanceLoader(DataLoader defaultLoader, DataSaver... savers) {
        this.savers = savers;
        this.defaultLoader = defaultLoader;
    }

    @Override
    public List<Candle> load(long start, long end, Instrument instrument, TimeFrame timeFrame) throws LoadingException {


        if (timeFrame == TimeFrame.MO || timeFrame == TimeFrame.WK){
            try {
                return defaultLoader.load(start, end, instrument, timeFrame);
            } catch (LoadingException e){
                logger.trace("Failed to load {} ({}) by using the loader {}", instrument.getLabel(), timeFrame.getLabel(), defaultLoader.getLabel());
            }
        }

        long startAdapted = start;
        long endAdapted = end;
        if (List.of(TimeFrame.D, TimeFrame.WK, TimeFrame.MO).contains(timeFrame)) {
            startAdapted = YahooFinanceTimeService.setHourAt(0, start, instrument.getZoneIdEnum().getZoneId());
            endAdapted = YahooFinanceTimeService.setHourAt(23, end, instrument.getZoneIdEnum().getZoneId());
        }


        String urlStr = String.format(
                "https://query1.finance.yahoo.com/v8/finance/chart/%s?interval=%s&period1=%d&period2=%d&events=dividends",
                instrument.getTicker(), YahooFinanceIntervalService.getTicker(timeFrame), startAdapted, endAdapted);

        FetcherUrl fetcherUrl = new FetcherUrl(urlStr);
        JSONObject root;
        try {
            root = fetcherUrl.fetch();
        } catch (IOException e) {
            throw new LoadingException(e.toString());
        }

        JSONArray timestamps = root.getJSONObject("chart")
                .getJSONArray("result")
                .getJSONObject(0)
                .getJSONArray("timestamp");

        long endDateReached = timestamps.getLong(timestamps.length() - 1);
        endDateReached = adaptTimeStamp(endDateReached, 0, timeFrame, instrument.getZoneIdEnum().getZoneId());

        if (endDateReached != end){
            throw new LoadingException(String.format("Loading of %s (%s) failed to reach %s, got %s instead.",
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
            ts = adaptTimeStamp(ts, 0, timeFrame, instrument.getZoneIdEnum().getZoneId());

            double o = value(opens, i, instrument, timeFrame);
            double h = value(highs, i, instrument, timeFrame);
            double l = value(lows, i, instrument, timeFrame);
            double c = value(closes, i, instrument, timeFrame);
            double v = value(volumes, i, instrument, timeFrame);

            candles.add(new Candle(instrument,ts, o, h, l, c, v));
        }
        return candles;
    }

    private double value(JSONArray arr, int idx, Instrument instrument, TimeFrame timeFrame) throws LoadingException {
        if (arr.isNull(idx)) {
            throw new LoadingException(
                    "Data loaded for %s (%s) contains null values".formatted(instrument.getLabel(), timeFrame.getLabel())
            );        }
        return arr.getDouble(idx);
    }

    private long adaptTimeStamp(long timestamp, int targetHour, TimeFrame timeFrame, ZoneId zoneId){
        if (List.of(TimeFrame.D, TimeFrame.WK, TimeFrame.MO).contains(timeFrame)) {
            return YahooFinanceTimeService.setHourAt(targetHour, timestamp, zoneId);
        }
        return timestamp;
    }

    @Override
    public String getLabel() {
        return "Loader Yahoo-Finance";
    }

    @Override
    public void onSuccessLoading(Instrument instrument, TimeFrame timeFrame, List<Candle> candles) {
        if (this.savers != null){
            Arrays.stream(this.savers).forEach(s -> {
                try {
                    s.save(instrument, timeFrame, candles);
                } catch (SavingException e) {
                    logger.warn("Failed to save {} with {}", instrument.getLabel(), s.getLabel());
                }
            });
        }
    }


}
