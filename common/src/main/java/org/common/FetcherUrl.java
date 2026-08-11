package org.common;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetcherUrl {

    private final String url;

    public FetcherUrl(String url) {
        this.url = url;
    }

    private static Logger logger = LoggerFactory.getLogger(FetcherUrl.class);

    public JSONObject fetch() throws IOException {
        logger.trace("url: {}", this.url);
        URL url = new URL(this.url);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", "Mozilla/5.0");
        con.setConnectTimeout(5000);
        con.setReadTimeout(5000);

        if (con.getResponseCode() != 200) {
            throw new RuntimeException("HTTP " + con.getResponseCode() + " : " + con.getResponseMessage());
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
        StringBuilder json = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) json.append(line);
        reader.close();

        return new JSONObject(json.toString());
    }
}
