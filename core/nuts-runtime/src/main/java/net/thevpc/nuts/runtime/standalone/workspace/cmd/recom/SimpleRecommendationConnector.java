package net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;

import java.io.IOException;
import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import net.thevpc.nuts.NutsIOException;

public class SimpleRecommendationConnector extends AbstractRecommendationConnector {
    @Override
    public <T> T post(String url, RequestQueryInfo ri, Class<T> resultType, NutsSession session) {
        validateRequest(ri, session);
        try {
            URL url2 = new URL(ri.server +url);
            URLConnection con = url2.openConnection();
            HttpURLConnection http = (HttpURLConnection) con;
            http.setRequestMethod("POST");
            http.setDoOutput(true);
            http.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            http.setRequestProperty("Accept", "*/*");
            String loc = session.getLocale();
            if(loc==null){
                loc=Locale.getDefault().toString();
            }
            http.setRequestProperty("Accept-Language",loc);
            NutsElements elems = NutsElements.of(session);
            String out = elems.setValue(ri.q).json().setNtf(false).format().filteredText();
            int length = out.length();
            http.setFixedLengthStreamingMode(length);
            http.connect();
            try (OutputStream os = http.getOutputStream()) {
                os.write(out.getBytes(StandardCharsets.UTF_8));
            }
            return elems.parse(http.getInputStream(), resultType);
        } catch (IOException ex) {
            throw new NutsIOException(session, NutsMessage.cstyle("recommendations are not available : %s",ex.toString()), ex);
        } catch (Exception ex) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected error : %s",ex.toString()), ex);
        }
    }

}
