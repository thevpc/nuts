package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.boot.internal.util.NBootLog;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NInputSourceBuilder;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.net.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.NMsg;

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
import java.util.stream.Collectors;

@NComponentScope(NScopeType.PROTOTYPE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNWebCli implements NWebCli {

    public static URLConnection prepareGlobalConnection(URLConnection c) {
        NDuration connectionTimeout = getGlobalConnectionTimeoutOrDefault();
        NDuration readTimeout = getGlobalReadConnectionTimeoutOrDefault();
        c.setConnectTimeout(connectionTimeout == null ? 0 : asMs(connectionTimeout.toMillis()));
        c.setReadTimeout(readTimeout == null ? 0 : asMs(readTimeout.toMillis()));
        return c;
    }

    public static NDuration getGlobalConnectionTimeoutOrDefault() {
        NDuration v = getGlobalConnectionTimeout();
        if (v == null) {
            return NDuration.ofSeconds(30);
        }
        return v;
    }

    public static NDuration getGlobalReadConnectionTimeoutOrDefault() {
        NDuration v = getGlobalReadTimeout();
        if (v == null) {
//            return getGlobalConnectionTimeoutOrDefault();
            return NDuration.ofSeconds(30);
        }
        return v;
    }

    public static NDuration getGlobalConnectionTimeout() {
        return NWorkspace.of().getBootOptions()
                .getCustomOptionArg("---connection-timeout").flatMap(y -> NDuration.parse(y.stringValue()))
                .orElse(null);
    }

    public static NDuration getGlobalReadTimeout() {
        return NWorkspace.of().getBootOptions()
                .getCustomOptionArg("---connection-read-timeout").flatMap(y -> NDuration.parse(y.stringValue()))
                .orElse(null);
    }

    public static NBootLog log;
    private String prefix;
    private Function<NWebResponse, NWebResponse> responsePostProcessor;
    private NDuration readTimeout;
    private NDuration connectTimeout;
    private final DefaultNWebHeaders headers = new DefaultNWebHeaders();

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
    public List<NWebCookie> getCookies() {
        List<String> li = headers.getOrEmpty("Cookie");
        return li.stream().map(x -> new DefaultNWebCookie(x)).collect(Collectors.toList());
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
    public NWebCli addCookies(NWebCookie... cookies) {
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
    public NWebRequest req(NHttpMethod method) {
        return new NWebRequestImpl(this, method);
    }

    @Override
    public NWebRequest GET() {
        return req(NHttpMethod.GET);
    }

    @Override
    public NWebRequest POST() {
        return req(NHttpMethod.POST);
    }

    @Override
    public NWebRequest PUT() {
        return req(NHttpMethod.PUT);
    }

    @Override
    public NWebRequest DELETE() {
        return req(NHttpMethod.DELETE);
    }

    @Override
    public NWebRequest PATCH() {
        return req(NHttpMethod.PATCH);
    }

    @Override
    public NWebRequest OPTIONS() {
        return req(NHttpMethod.OPTIONS).OPTIONS();
    }

    @Override
    public NWebRequest HEAD() {
        return req(NHttpMethod.HEAD).HEAD();
    }

    @Override
    public NWebRequest CONNECT() {
        return req(NHttpMethod.CONNECT).CONNECT();
    }

    @Override
    public NWebRequest TRACE() {
        return req(NHttpMethod.TRACE).TRACE();
    }

    @Override
    public NWebRequest GET(String path) {
        return req(NHttpMethod.GET).GET(path);
    }

    @Override
    public NWebRequest POST(String path) {
        return req(NHttpMethod.POST).POST(path);
    }

    @Override
    public NWebRequest PUT(String path) {
        return req(NHttpMethod.PUT).PUT(path);
    }

    @Override
    public NWebRequest DELETE(String path) {
        return req(NHttpMethod.DELETE).DELETE(path);
    }

    @Override
    public NWebRequest PATCH(String path) {
        return req(NHttpMethod.PATCH).PATCH(path);
    }

    @Override
    public NWebRequest OPTIONS(String path) {
        return req(NHttpMethod.OPTIONS).OPTIONS(path);
    }

    @Override
    public NWebRequest HEAD(String path) {
        return req(NHttpMethod.HEAD).HEAD(path);
    }

    @Override
    public NWebRequest CONNECT(String path) {
        return req(NHttpMethod.CONNECT).CONNECT(path);
    }

    @Override
    public NWebRequest TRACE(String path) {
        return req(NHttpMethod.TRACE).TRACE(path);
    }

    public String formatURL(NWebRequest r, boolean safe) {
        String p = r.getUri();
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
        NAssert.requireNamedNonNull(r, "request");
        NAssert.requireNamedNonNull(r.getMethod(), "method");
        NHttpMethod method = r.getMethod();
        String spec = null;
        try {
            spec = formatURL(r, false);
            URL h = CoreIOUtils.urlOf(spec);
            HttpURLConnection uc = null;
            try {
                uc = (HttpURLConnection) h.openConnection();

                NDuration readTimeout1 = r.getReadTimeout();
                if (readTimeout1 == null) {
                    readTimeout1 = getReadTimeout();
                }
                if (readTimeout1 == null) {
                    readTimeout1 = getGlobalReadConnectionTimeoutOrDefault();
                }
                if (readTimeout1 != null) {
                    uc.setReadTimeout(
                            asMs(readTimeout1.toMillis())
                    );
                }

                NDuration connectTimeout1 = r.getConnectTimeout();
                if (connectTimeout1 == null) {
                    connectTimeout1 = getConnectTimeout();
                }
                if (connectTimeout1 == null) {
                    connectTimeout1 = getGlobalConnectionTimeoutOrDefault();
                }
                if (connectTimeout1 != null) {
                    uc.setConnectTimeout(
                            asMs(connectTimeout1.toMillis())
                    );
                }
                DefaultNWebHeaders headers = new DefaultNWebHeaders();

                //must be called before writing headers!
                NInputSource requestBody = r.getRequestBody();

                headers.addHeadersMulti(r.getHeaders(), DefaultNWebHeaders.Mode.ALWAYS);
                headers.addHeadersMulti(this.headers.toMap(), DefaultNWebHeaders.Mode.IF_EMPTY);

                for (Map.Entry<String, List<String>> e : headers.toMap().entrySet()) {
                    _writeHeader(uc, e.getKey(), e.getValue());
                }
                uc.setRequestMethod(method.toString());
                uc.setUseCaches(false);

                long bodyLength = requestBody == null || requestBody.isKnownContentLength() ? -1 : requestBody.contentLength();
                boolean someBody = requestBody != null;

                uc.setDoInput(!r.isOneWay());
                uc.setDoOutput(someBody);
                HttpURLConnection finalUc = uc;
                long startTime = System.nanoTime();
                Exception seenError = null;
                NHttpCode rCode = null;

                try {
                    if (someBody) {
                        if (requestBody.isKnownContentLength()) {
                            uc.setFixedLengthStreamingMode(bodyLength);
                        }
                        NCp.of().from(requestBody).to(uc.getOutputStream()).run();
                    }
                    rCode = NHttpCode.of(uc.getResponseCode());
                } catch (Exception err) {
                    seenError = err;
                } finally {
                    if (seenError != null) {
                        NLog.of(DefaultNWebCli.class).debug(NMsg.ofC("[%s] %s %s (%s)", "FAILED", method, spec, seenError)
                                .withDurationNanos(System.nanoTime() - startTime)
                                .withIntent(NMsgIntent.FAIL)
                                .withThrowable(seenError)
                        );
                    } else {
                        NLog.of(DefaultNWebCli.class).debug(NMsg.ofC("[%s] %s %s", rCode == null ? "FAILED" : rCode, method, spec)
                                .withDurationNanos(System.nanoTime() - startTime)
                                .withIntent((rCode != null && rCode.isOk()) ? NMsgIntent.READ : NMsgIntent.FAIL)
                                .withThrowable(seenError)
                        );
                    }
                }
                if (seenError != null) {
                    throw new NIOException(NMsg.ofC("error loading %s (%s)", spec, seenError), seenError);
                }

                String rm = NStringUtils.trim(uc.getResponseMessage());
                if (rCode != null && !rCode.isOk() && rm.isEmpty()) {
                    rm = "Error " + rCode;
                }
                NWebResponse httpResponse = new NWebResponseImpl(
                        rCode,
                        NMsg.ofPlain(rm),
                        uc.getHeaderFields(),
                        () -> {
                            NInputSource bytes = null;
                            if (!r.isOneWay()) {
                                //TODO change me with a smart copy input source!
                                HttpURLConnection uc2 = finalUc;
                                try {
                                    bytes = NInputSourceBuilder.of(finalUc.getInputStream()).setCloseAction(() -> {
                                                // close connection when fully read!
                                                if (uc2 != null) {
                                                    try {
                                                        uc2.disconnect();
                                                    } catch (Exception e) {
                                                        //
                                                    }
                                                }
                                            }
                                    ).createInputSource();

                                } catch (IOException e) {
                                    throw new NIOException(e);
                                }
//                    byte[] byteArrayResult = NCp.of().from(uc.getInputStream()).getByteArrayResult();
//                    bytes = NIO.of().ofInputSource(byteArrayResult);
                                long contentLength = finalUc.getContentLengthLong();
                                if (contentLength >= 0) {
                                    bytes.getMetaData().setContentLength(contentLength);
                                }
                            }
                            return bytes;
                        }
                );
                if (responsePostProcessor != null) {
                    NWebResponse newResp = responsePostProcessor.apply(httpResponse);
                    if (newResp != null) {
                        httpResponse = newResp;
                    }
                }
                addCookies(httpResponse.getCookies().toArray(new NWebCookie[0]));
                return httpResponse;
            } finally {
                if (r.isOneWay()) {
                    // just close any connection
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
            throw new NIOException(NMsg.ofC("timed out loading %s (%s)", spec, ex), ex);
        } catch (InterruptedByTimeoutException | InterruptedIOException ex) {
            throw new NIOException(NMsg.ofC("interrupt out loading %s (%s)", spec, ex), ex);
        } catch (UncheckedIOException | IOException ex) {
            throw new NIOException(NMsg.ofC("error loading %s (%s)", spec, ex), ex);
        }
    }

    private static int asMs(long a) {
        if (a < 0) {
            return 0;
        }
        if (a > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) a;
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
                uc.setRequestProperty(name, String.join("; ", values));
                return;
            }
        }
        for (String s : values) {
            uc.setRequestProperty(name, s);
        }

    }

    @Override
    public NDuration getReadTimeout() {
        return readTimeout;
    }

    @Override
    public NWebCli readTimeout(NDuration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public NDuration getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public NWebCli connectTimeout(NDuration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public NWebCli timeout(NDuration timeout) {
        this.readTimeout = timeout;
        this.connectTimeout = timeout;
        return this;
    }

    public static String UNIFORM_HEADER(String h) {
        return NStringUtils.trim(h).toUpperCase();
    }

}
