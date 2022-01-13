package net.thevpc.nuts.installer.connector;

import com.google.gson.Gson;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

public class SimpleRecommendationConnector extends AbstractRecommendationConnector {
    @Override
    public <T> T post(String url, RequestQueryInfo ri, Class<T> resultType) {
        validateRequest(ri);
        try {
            URL url2 = new URL(ri.url + url);
            URLConnection con = url2.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Accept", "*/*");
            String loc = Locale.getDefault().toString();
            http.setRequestProperty("Accept-Language", loc);
            Gson gson = new Gson();
            String out = gson.toJson(ri.q);
            int length = out.length();
            http.setFixedLengthStreamingMode(length);
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out.getBytes(StandardCharsets.UTF_8));
            }
            return gson.fromJson(new InputStreamReader(http.getInputStream()), resultType);
        } catch (Exception ex) {
            throw new IllegalArgumentException("unexpected error : " + ex);
        }
    }

}
