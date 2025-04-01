package net.thevpc.nuts.runtime.standalone.web;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.boot.reserved.cmdline.NBootArg;
import net.thevpc.nuts.boot.reserved.util.NBootLog;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NInputSourceBuilder;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.web.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.*;
import java.util.function.Function;

@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNWebCli implements NWebCli {

    public static URLConnection prepareGlobalConnection(URLConnection c) {
        int connectionTimout = getGlobalConnectionTimeoutOrDefault();
        int readTimout = getGlobalReadConnectionTimeoutOrDefault();
        c.setConnectTimeout(connectionTimout);
        c.setReadTimeout(readTimout);
        return c;
    }

    public static int getGlobalConnectionTimeoutOrDefault() {
        Integer v = getGlobalConnectionTimeout();
        if (v == null) {
            return 1000;
        }
        return v;
    }

    public static Integer getGlobalReadConnectionTimeoutOrDefault() {
        Integer v = getGlobalReadTimeout();
        if (v == null) {
            return getGlobalConnectionTimeoutOrDefault();
        }
        return v;
    }

    public static Integer getGlobalConnectionTimeout() {
        Integer i = NWorkspace.of().getBootOptions()
                .getCustomOptions().orElse(new ArrayList<>()).stream().map(x -> NBootArg.of(x))
                .filter(x -> Objects.equals(x.getOptionName(), "---connection-timeout")).map(x -> x.getIntValue())
                .filter(x -> x != null)
                .findFirst().orElse(null);
        if (i != null) {
            if (i <= 0) {
                return null;
            }
        }
        return i;
    }

    public static Integer getGlobalReadTimeout() {
        Integer i = NWorkspace.of().getBootOptions()
                .getCustomOptions().orElse(new ArrayList<>()).stream().map(x -> NBootArg.of(x))
                .filter(x -> Objects.equals(x.getOptionName(), "---connection-read-timeout")).map(x -> x.getIntValue())
                .filter(x -> x != null)
                .findFirst().orElse(null);
        if (i != null) {
            if (i <= 0) {
                return null;
            }
        }
        return i;
    }

    public static NBootLog log;
    private String prefix;
    private Function<NWebResponse, NWebResponse> responsePostProcessor;
    private Integer readTimeout;
    private Integer connectTimeout;
    private DefaultNWebHeaders headers = new DefaultNWebHeaders();

    public DefaultNWebCli() {
        headers.addHeader("User-Agent", "nwebcli/" + NWorkspace.of().getRuntimeId().getVersion(), DefaultNWebHeaders.Mode.ALWAYS);
    }

    public static InputStream prepareGlobalOpenStream(URL url) throws IOException {
        URLConnection c = null;
        c = url.openConnection();
        prepareGlobalConnection(c);
        return c.getInputStream();
    }

    @Override
    public NWebCookie[] getCookies() {
        List<String> li = headers.getOrEmpty("Cookie");
        return li.stream().map(x -> new DefaultNWebCookie(x)).toArray(NWebCookie[]::new);
    }

    @Override
    public NWebCli addHeader(String name, String value) {
        headers.addHeader(name, value, DefaultNWebHeaders.Mode.ALWAYS);
        return this;
    }

    @Override
    public NWebCli setHeader(String name, String value) {
        headers.addHeader(name, value, DefaultNWebHeaders.Mode.REPLACE);
        return this;
    }

    @Override
    public NWebCli removeHeader(String name, String value) {
        headers.removeHeader(name, value);
        return this;
    }

    @Override
    public NWebCli removeHeader(String name) {
        headers.removeHeader(name);
        return this;
    }

    @Override
    public boolean containsHeader(String name) {
        return headers.containsHeader(name);
    }

    @Override
    public boolean containsCookie(String cookieName) {
        List<String> li = headers.getOrEmpty("Cookie");
        return li.stream().map(x -> new DefaultNWebCookie(x)).anyMatch(x -> Objects.equals(x.getName(), cookieName));
    }

    public Map<String, List<String>> getHeaders() {
        return headers.toMap();
    }

    @Override
    public NWebCli clearHeaders() {
        headers.clear();
        return this;
    }

    public NWebCli clearCookies() {
        headers.removeHeader("Cookie");
        return this;
    }

    public NWebCli removeCookies(NWebCookie[] cookies) {
        if (cookies != null) {
            for (NWebCookie cookie : cookies) {
                removeCookie(cookie);
            }
        }
        return this;
    }

    public NWebCli removeCookie(NWebCookie cookie) {
        if (cookie != null) {
            for (String s : headers.getOrEmpty("Cookie")) {
                if (Objects.equals(new DefaultNWebCookie(s).getName(), cookie.getName())) {
                    headers.removeHeader("Cookie", s);
                }
            }
        }
        return this;
    }

    public NWebCli removeCookie(String cookieName) {
        if (cookieName != null) {
            for (String s : headers.getOrEmpty("Cookie")) {
                if (Objects.equals(new DefaultNWebCookie(s).getName(), cookieName)) {
                    headers.removeHeader("Cookie", s);
                }
            }
        }
        return this;
    }

    @Override
    public NWebCli addCookie(NWebCookie cookie) {
        if (cookie != null) {
            for (String s : headers.getOrEmpty("Cookie")) {
                if (Objects.equals(new DefaultNWebCookie(s).getName(), cookie.getName())) {
                    headers.removeHeader("Cookie", s);
                }
            }
            headers.addHeader("Cookie", DefaultNWebCookie.formatCookie(cookie), DefaultNWebHeaders.Mode.ALWAYS);
        }
        return this;
    }

    @Override
    public NWebCli addCookies(NWebCookie[] cookies) {
        if (cookies != null) {
            for (NWebCookie cookie : cookies) {
                addCookie(cookie);
            }
        }
        return this;
    }

    @Override
    public Function<NWebResponse, NWebResponse> getResponsePostProcessor() {
        return responsePostProcessor;
    }

    @Override
    public NWebCli setResponsePostProcessor(Function<NWebResponse, NWebResponse> responsePostProcessor) {
        this.responsePostProcessor = responsePostProcessor;
        return this;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public NWebCli setPrefix(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public NWebRequest req() {
        return new NWebRequestImpl(this, NHttpMethod.GET);
    }

    @Override
    public NWebRequest req(NHttpMethod method) {
        return new NWebRequestImpl(this, method);
    }

    @Override
    public NWebRequest GET() {
        return req().GET();
    }

    @Override
    public NWebRequest POST() {
        return req().POST();
    }

    @Override
    public NWebRequest PUT() {
        return req().PUT();
    }

    @Override
    public NWebRequest DELETE() {
        return req().DELETE();
    }

    @Override
    public NWebRequest PATCH() {
        return req().PATCH();
    }

    @Override
    public NWebRequest OPTIONS() {
        return req().OPTIONS();
    }

    @Override
    public NWebRequest HEAD() {
        return req().HEAD();
    }

    @Override
    public NWebRequest CONNECT() {
        return req().connect();
    }

    @Override
    public NWebRequest TRACE() {
        return req().trace();
    }

    @Override
    public NWebRequest GET(String path) {
        return req().GET(path);
    }

    @Override
    public NWebRequest POST(String path) {
        return req().POST(path);
    }

    @Override
    public NWebRequest PUT(String path) {
        return req().PUT(path);
    }

    @Override
    public NWebRequest DELETE(String path) {
        return req().DELETE(path);
    }

    @Override
    public NWebRequest PATCH(String path) {
        return req().PATCH(path);
    }

    @Override
    public NWebRequest OPTIONS(String path) {
        return req().OPTIONS(path);
    }

    @Override
    public NWebRequest HEAD(String path) {
        return req().HEAD(path);
    }

    @Override
    public NWebRequest CONNECT(String path) {
        return req().connect(path);
    }

    @Override
    public NWebRequest TRACE(String path) {
        return req().trace(path);
    }

    public String formatURL(NWebRequest r, boolean safe) {
        String p = r.getUrl();
        StringBuilder u = new StringBuilder();
        if (prefix == null || p.startsWith("http:") || p.startsWith("https:")) {
            u.append(p);
        } else {
            if (p.isEmpty() || p.equals("/")) {
                u.append(prefix);
            } else {
                if (!p.startsWith("/") && !prefix.endsWith("/")) {
                    u.append(prefix).append("/").append(p);
                } else {
                    u.append(prefix).append(p);
                }
            }
        }
        String bu = u.toString().trim();
        if (bu.isEmpty() || bu.equals("/")) {
            if (!safe) {
                throw new IllegalArgumentException("missing url : " + bu);
            }
        }
        if (!bu.startsWith("http://")
                && !bu.startsWith("https://")) {
            if (!safe) {
                throw new IllegalArgumentException("unsupported url : " + bu);
            }
        }

        if (r.getParameters() != null && r.getParameters().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> e : r.getParameters().entrySet()) {
                String k = e.getKey();
                List<String> values = e.getValue();
                if (values != null && values.size() > 0) {
                    for (String v : values) {
                        if (sb.length() > 0) {
                            sb.append("&");
                        }
                        sb.append(NHttpUrlEncoder.encode(k))
                                .append("=")
                                .append(NHttpUrlEncoder.encode(v));
                    }
                }
            }
            if (sb.length() > 0) {
                if (u.indexOf("?") >= 0) {
                    u.append("&").append(sb);
                } else {
                    u.append("?").append(sb);
                }
            }
        }
        return u.toString();
    }

    public NWebResponse run(NWebRequest r) {
        NAssert.requireNonNull(r, "request");
        NAssert.requireNonNull(r.getMethod(), "method");
        NHttpMethod method = r.getMethod();
        String spec = null;
        try {
            spec = formatURL(r, false);
            URL h = CoreIOUtils.urlOf(spec);
            HttpURLConnection uc = null;
            try {
                uc = (HttpURLConnection) h.openConnection();

                Integer readTimeout1 = r.getReadTimeout();
                if (readTimeout1 == null) {
                    readTimeout1 = getReadTimeout();
                }
                if (readTimeout1 == null) {
                    readTimeout1 = getGlobalReadConnectionTimeoutOrDefault();
                }
                if (readTimeout1 != null) {
                    uc.setReadTimeout(readTimeout1);
                }

                Integer connectTimeout1 = r.getConnectTimeout();
                if (connectTimeout1 == null) {
                    connectTimeout1 = getConnectTimeout();
                }
                if (connectTimeout1 == null) {
                    connectTimeout1 = getGlobalConnectionTimeoutOrDefault();
                }
                if (connectTimeout1 != null) {
                    uc.setConnectTimeout(connectTimeout1);
                }
                DefaultNWebHeaders headers = new DefaultNWebHeaders();
                headers.addHeadersMulti(r.getHeaders(), DefaultNWebHeaders.Mode.ALWAYS);
                headers.addHeadersMulti(this.headers.toMap(), DefaultNWebHeaders.Mode.IF_EMPTY);

                for (Map.Entry<String, List<String>> e : headers.toMap().entrySet()) {
                    _writeHeader(uc, e.getKey(), e.getValue());
                }
                uc.setRequestMethod(method.toString());
                uc.setUseCaches(false);

                NInputSource requestBody = r.getRequestBody();
                long bodyLength = requestBody == null ? -1 : requestBody.contentLength();
                boolean someBody = requestBody != null && bodyLength > 0;

                uc.setDoInput(!r.isOneWay());
                uc.setDoOutput(someBody);
                if (someBody) {
                    uc.setRequestProperty("Content-Length", String.valueOf(bodyLength));
                    NCp.of().from(requestBody).to(uc.getOutputStream()).run();
                }
                NInputSource bytes = null;
                if (!r.isOneWay()) {
                    //TODO change me with a smart copy input source!
                    HttpURLConnection uc2 = uc;
                    bytes = NInputSourceBuilder.of(uc.getInputStream()).setCloseAction(() -> {
                                // close connexion when fully read!
                                if (uc2 != null) {
                                    try {
                                        uc2.disconnect();
                                    } catch (Exception e) {
                                        //
                                    }
                                }
                            }
                    ).createInputSource();
//                    byte[] byteArrayResult = NCp.of().from(uc.getInputStream()).getByteArrayResult();
//                    bytes = NIO.of().ofInputSource(byteArrayResult);
                    long contentLength = uc.getContentLengthLong();
                    if (contentLength >= 0) {
                        bytes.getMetaData().setContentLength(contentLength);
                    }
                }
                NWebResponse httpResponse = new NWebResponseImpl(
                        uc.getResponseCode(),
                        NMsg.ofPlain(NStringUtils.trim(uc.getResponseMessage())),
                        uc.getHeaderFields(),
                        bytes
                );
                if (responsePostProcessor != null) {
                    NWebResponse newResp = responsePostProcessor.apply(httpResponse);
                    if (newResp != null) {
                        httpResponse = newResp;
                    }
                }
                addCookies(httpResponse.getCookies());
                return httpResponse;
            } finally {
                if (r.isOneWay()) {
                    // just close any connexion
                    if (uc != null) {
                        try {
                            uc.disconnect();
                        } catch (Exception e) {
                            //
                        }
                    }
                }
            }
        } catch (SocketTimeoutException ex) {
            throw new UncheckedIOException("timed out loading " + spec + " (" + ex.getMessage() + ")", ex);
        } catch (InterruptedByTimeoutException | InterruptedIOException ex) {
            throw new UncheckedIOException("interrupt loading " + spec + " (" + ex.getMessage() + ")", ex);
        } catch (UncheckedIOException ex) {
            throw new UncheckedIOException(new IOException("error loading " + spec + " (" + ex.getMessage() + ")"));
        } catch (IOException ex) {
            throw new UncheckedIOException("error loading " + spec + " (" + ex.getMessage() + ")", ex);
        }
    }

    private void _writeHeader(HttpURLConnection uc, String name, List<String> values) {
        if (values == null || values.isEmpty()) {
            return;
        }
        // Sets the general request property. If a property with
        // the key already exists, overwrite its value with the new value.
        //NOTE: HTTP requires all request properties which can legally have
        // multiple instances with the same key to use a comma-separated list
        // syntax which enables multiple properties to be appended into a single property.
        //
        switch (name.toUpperCase()) {
            case "COOKIE": {
                uc.setRequestProperty(name, String.join(" ; ", values));
                return;
            }
        }
        for (String s : values) {
            uc.setRequestProperty(name, s);
        }

    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public NWebCli setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public NWebCli setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public static String UNIFORM_HEADER(String h) {
        return NStringUtils.trim(h).toUpperCase();
    }

}
