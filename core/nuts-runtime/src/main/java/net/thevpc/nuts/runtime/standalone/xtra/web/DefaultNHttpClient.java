package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.concurrent.NConcurrent;
import net.thevpc.nuts.concurrent.NInterruptedException;
import net.thevpc.nuts.concurrent.NTimeoutException;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.boot.internal.util.NBootLog;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NInputSourceBuilder;
import net.thevpc.nuts.io.NullInputStream;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.net.*;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
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
import java.net.*;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collectors;

@NComponentScope(NScopeType.PROTOTYPE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNHttpClient implements NHttpClient {
    private Executor executor;

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
        return NWorkspace.of().bootOptions()
                .customOptionArg("---connection-timeout").flatMap(y -> NDuration.of(y.stringValue()))
                .orElse(null);
    }

    public static NDuration getGlobalReadTimeout() {
        return NWorkspace.of().bootOptions()
                .customOptionArg("---connection-read-timeout").flatMap(y -> NDuration.of(y.stringValue()))
                .orElse(null);
    }

    public static NBootLog log;
    private String prefix;
    private Function<NHttpResponse, NHttpResponse> responsePostProcessor;
    private NDuration readTimeout;
    private NDuration connectTimeout;
    private final DefaultNWebHeaders headers = new DefaultNWebHeaders();

    public DefaultNHttpClient() {
        headers.addHeader("User-Agent", "nwebcli/" + NWorkspace.of().runtimeId().version(), DefaultNWebHeaders.Mode.ALWAYS);
    }

    public static InputStream prepareGlobalOpenStream(URL url) throws IOException {
        URLConnection c = null;
        c = url.openConnection();
        prepareGlobalConnection(c);
        return c.getInputStream();
    }

    public Executor executor() {
        return executor;
    }

    public NHttpClient executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public List<NHttpCookie> cookies() {
        List<String> li = headers.getOrEmpty("Cookie");
        return li.stream().map(x -> new DefaultNHttpCookie(x)).collect(Collectors.toList());
    }

    @Override
    public NHttpClient addHeader(String name, String value) {
        headers.addHeader(name, value, DefaultNWebHeaders.Mode.ALWAYS);
        return this;
    }

    @Override
    public NHttpClient header(String name, String value) {
        headers.addHeader(name, value, DefaultNWebHeaders.Mode.REPLACE);
        return this;
    }

    @Override
    public NHttpClient removeHeader(String name, String value) {
        headers.removeHeader(name, value);
        return this;
    }

    @Override
    public NHttpClient removeHeader(String name) {
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
        return li.stream().map(x -> new DefaultNHttpCookie(x)).anyMatch(x -> Objects.equals(x.name(), cookieName));
    }

    public Map<String, List<String>> headers() {
        return headers.toMap();
    }

    @Override
    public NHttpClient clearHeaders() {
        headers.clear();
        return this;
    }

    public NHttpClient clearCookies() {
        headers.removeHeader("Cookie");
        return this;
    }

    public NHttpClient removeCookies(NHttpCookie[] cookies) {
        if (cookies != null) {
            for (NHttpCookie cookie : cookies) {
                removeCookie(cookie);
            }
        }
        return this;
    }

    public NHttpClient removeCookie(NHttpCookie cookie) {
        if (cookie != null) {
            for (String s : headers.getOrEmpty("Cookie")) {
                if (Objects.equals(new DefaultNHttpCookie(s).name(), cookie.name())) {
                    headers.removeHeader("Cookie", s);
                }
            }
        }
        return this;
    }

    public NHttpClient removeCookie(String cookieName) {
        if (cookieName != null) {
            for (String s : headers.getOrEmpty("Cookie")) {
                if (Objects.equals(new DefaultNHttpCookie(s).name(), cookieName)) {
                    headers.removeHeader("Cookie", s);
                }
            }
        }
        return this;
    }

    @Override
    public NHttpClient addCookie(NHttpCookie cookie) {
        if (cookie != null) {
            for (String s : headers.getOrEmpty("Cookie")) {
                if (Objects.equals(new DefaultNHttpCookie(s).name(), cookie.name())) {
                    headers.removeHeader("Cookie", s);
                }
            }
            headers.addHeader("Cookie", DefaultNHttpCookie.formatCookie(cookie), DefaultNWebHeaders.Mode.ALWAYS);
        }
        return this;
    }

    @Override
    public NHttpClient addCookies(NHttpCookie... cookies) {
        if (cookies != null) {
            for (NHttpCookie cookie : cookies) {
                addCookie(cookie);
            }
        }
        return this;
    }

    @Override
    public Function<NHttpResponse, NHttpResponse> responsePostProcessor() {
        return responsePostProcessor;
    }

    @Override
    public NHttpClient responsePostProcessor(Function<NHttpResponse, NHttpResponse> responsePostProcessor) {
        this.responsePostProcessor = responsePostProcessor;
        return this;
    }

    @Override
    public String baseUri() {
        return prefix;
    }

    @Override
    public NHttpClient baseUri(String prefix) {
        this.prefix = prefix;
        return this;
    }

    @Override
    public NHttpRequest req(NHttpMethod method) {
        return new NHttpRequestImpl(this, method);
    }

    @Override
    public NHttpRequest GET() {
        return req(NHttpMethod.GET);
    }

    @Override
    public NHttpRequest POST() {
        return req(NHttpMethod.POST);
    }

    @Override
    public NHttpRequest PUT() {
        return req(NHttpMethod.PUT);
    }

    @Override
    public NHttpRequest DELETE() {
        return req(NHttpMethod.DELETE);
    }

    @Override
    public NHttpRequest PATCH() {
        return req(NHttpMethod.PATCH);
    }

    @Override
    public NHttpRequest OPTIONS() {
        return req(NHttpMethod.OPTIONS).OPTIONS();
    }

    @Override
    public NHttpRequest HEAD() {
        return req(NHttpMethod.HEAD).HEAD();
    }

    @Override
    public NHttpRequest CONNECT() {
        return req(NHttpMethod.CONNECT).CONNECT();
    }

    @Override
    public NHttpRequest TRACE() {
        return req(NHttpMethod.TRACE).TRACE();
    }

    @Override
    public NHttpRequest GET(String path) {
        return req(NHttpMethod.GET).GET(path);
    }

    @Override
    public NHttpRequest POST(String path) {
        return req(NHttpMethod.POST).POST(path);
    }

    @Override
    public NHttpRequest PUT(String path) {
        return req(NHttpMethod.PUT).PUT(path);
    }

    @Override
    public NHttpRequest DELETE(String path) {
        return req(NHttpMethod.DELETE).DELETE(path);
    }

    @Override
    public NHttpRequest PATCH(String path) {
        return req(NHttpMethod.PATCH).PATCH(path);
    }

    @Override
    public NHttpRequest OPTIONS(String path) {
        return req(NHttpMethod.OPTIONS).OPTIONS(path);
    }

    @Override
    public NHttpRequest HEAD(String path) {
        return req(NHttpMethod.HEAD).HEAD(path);
    }

    @Override
    public NHttpRequest CONNECT(String path) {
        return req(NHttpMethod.CONNECT).CONNECT(path);
    }

    @Override
    public NHttpRequest TRACE(String path) {
        return req(NHttpMethod.TRACE).TRACE(path);
    }

    public String formatURL(NHttpRequest r, boolean safe) {
        String p = r == null ? null : r.uri();
        if (p == null) {
            p = "";
        }
        StringBuilder u = new StringBuilder();
        if (prefix == null || p.startsWith("http:") || p.startsWith("https:")) {
            u.append(p);
        } else {
            if (p.isEmpty() || p.equals("/")) {
                u.append(prefix);
            } else {
                if (prefix.endsWith("/") && p.startsWith("/")) {
                    u.append(prefix).append(p.substring(1));
                } else if (!p.startsWith("/") && !prefix.endsWith("/")) {
                    u.append(prefix).append("/").append(p);
                } else {
                    u.append(prefix).append(p);
                }
            }
        }
        String bu = NStringUtils.strip(u.toString());
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

        if (r != null && r.parameters() != null && r.parameters().size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<String>> e : r.parameters().entrySet()) {
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

    public CompletableFuture<NHttpResponse> runAsync(NHttpRequest r, Executor executor) {
        if (executor == null) {
            executor = this.executor;
            if (executor == null) {
                executor = NConcurrent.executorService();
            }
        }
        return CompletableFuture.supplyAsync(() -> run(r), executor);
    }

    public NHttpResponse run(NHttpRequest r) {
        NAssert.requireNamedNonNull(r, "request");
        NAssert.requireNamedNonNull(r.method(), "method");
        NHttpMethod method = r.method();
        String spec = null;
        try {
            spec = formatURL(r, false);
            URL h = CoreIOUtils.urlOf(spec);
            HttpURLConnection uc = null;
            try {
                uc = (HttpURLConnection) h.openConnection();

                NDuration readTimeout1 = r.readTimeout();
                if (readTimeout1 == null) {
                    readTimeout1 = readTimeout();
                }
                if (readTimeout1 == null) {
                    readTimeout1 = getGlobalReadConnectionTimeoutOrDefault();
                }
                if (readTimeout1 != null) {
                    uc.setReadTimeout(
                            asMs(readTimeout1.toMillis())
                    );
                }

                NDuration connectTimeout1 = r.connectTimeout();
                if (connectTimeout1 == null) {
                    connectTimeout1 = connectTimeout();
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
                NInputSource requestBody = r.requestBody();

                headers.addHeadersMulti(r.headers(), DefaultNWebHeaders.Mode.ALWAYS);
                headers.addHeadersMulti(this.headers.toMap(), DefaultNWebHeaders.Mode.IF_EMPTY);

                for (Map.Entry<String, List<String>> e : headers.toMap().entrySet()) {
                    _writeHeader(uc, e.getKey(), e.getValue());
                }
                _setRequestMethod(uc, method);
                uc.setUseCaches(false);

                long bodyLength = (requestBody != null && requestBody.isKnownContentLength()) ? requestBody.contentLength() : -1;
                boolean someBody = requestBody != null;

                uc.setDoInput(!r.isOneWay());
                uc.setDoOutput(someBody);
                HttpURLConnection finalUc = uc;
                long startTime = System.nanoTime();
                Exception seenError = null;
                NHttpCode rCode = null;

                try {
                    if (someBody) {
                        if (bodyLength >= 0) {
                            uc.setFixedLengthStreamingMode(bodyLength);
                        }
                        NCp.of().from(requestBody).to(uc.getOutputStream()).run();
                    }
                    rCode = NHttpCode.of(uc.getResponseCode());
                } catch (Exception err) {
                    seenError = err;
                } finally {
                    if (seenError != null) {
                        if(seenError instanceof UnknownHostException){
                            NLog.of(DefaultNHttpClient.class).debug(NMsg.ofC("[%s] %s %s (%s)", "FAILED", method, spec, seenError)
                                    .withDurationNanos(System.nanoTime() - startTime)
                                    .withIntent(NMsgIntent.FAIL)
                            );
                        }else {
                            NLog.of(DefaultNHttpClient.class).debug(NMsg.ofC("[%s] %s %s (%s)", "FAILED", method, spec, seenError)
                                    .withDurationNanos(System.nanoTime() - startTime)
                                    .withIntent(NMsgIntent.FAIL)
                                    .withThrowable(seenError)
                            );
                        }
                    } else {
                        NLog.of(DefaultNHttpClient.class).debug(NMsg.ofC("[%s] %s %s", rCode == null ? "FAILED" : rCode, method, spec)
                                .withDurationNanos(System.nanoTime() - startTime)
                                .withIntent((rCode != null && rCode.isOk()) ? NMsgIntent.READ : NMsgIntent.FAIL)
                        );
                    }
                }
                if (seenError != null) {
                    throw new NIOException(NMsg.ofC("error loading %s (%s)", spec, seenError), seenError);
                }

                String rm = NStringUtils.strip(uc.getResponseMessage());
                if (rCode != null && !rCode.isOk() && rm.isEmpty()) {
                    rm = "Error " + rCode;
                }
                NHttpCode finalRCode = rCode;
                NHttpResponse httpResponse = new NHttpResponseImpl(
                        rCode,
                        NMsg.ofP(rm),
                        uc.getHeaderFields(),
                        () -> {
                            NInputSource bytes = null;
                            if (!r.isOneWay()) {
                                //TODO change me with a smart copy input source!
                                HttpURLConnection uc2 = finalUc;
                                InputStream is = null;
                                if (finalRCode != null && finalRCode.isError()) {
                                    is = finalUc.getErrorStream();
                                }
                                if (is == null) {
                                    try {
                                        is = finalUc.getInputStream();
                                    } catch (IOException e) {
                                        is = finalUc.getErrorStream();
                                    }
                                }
                                if (is == null) {
                                    is = NullInputStream.INSTANCE;
                                }
                                bytes = NInputSourceBuilder.of(is).closeAction(() -> {
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
                                long contentLength = finalUc.getContentLengthLong();
                                if (contentLength >= 0) {
                                    bytes.metaData().contentLength(contentLength);
                                }
                            }
                            return bytes;
                        }
                );
                if (responsePostProcessor != null) {
                    NHttpResponse newResp = responsePostProcessor.apply(httpResponse);
                    if (newResp != null) {
                        httpResponse = newResp;
                    }
                }
                addCookies(httpResponse.cookies().toArray(new NHttpCookie[0]));
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
            throw new NTimeoutException(NMsg.ofC("timed out loading %s (%s)", spec, ex), ex);
        } catch (InterruptedByTimeoutException | InterruptedIOException ex) {
            throw new NInterruptedException(NMsg.ofC("interrupt out loading %s (%s)", spec, ex), ex);
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
        switch (name.toUpperCase()) {
            case "COOKIE": {
                uc.setRequestProperty(name, String.join("; ", values));
                return;
            }
        }
        for (String s : values) {
            uc.addRequestProperty(name, s);
        }
    }

    private static void _setRequestMethod(HttpURLConnection uc, NHttpMethod method) throws IOException {
        String m = method.toString();
        try {
            uc.setRequestMethod(m);
        } catch (java.net.ProtocolException ex) {
            if (method == NHttpMethod.PATCH) {
                boolean success = false;
                try {
                    java.lang.reflect.Field methodField = HttpURLConnection.class.getDeclaredField("method");
                    methodField.setAccessible(true);
                    methodField.set(uc, "PATCH");
                    success = true;
                } catch (Exception ignored) {
                }
                if (!success) {
                    try {
                        java.lang.reflect.Field delegateField = uc.getClass().getDeclaredField("delegate");
                        delegateField.setAccessible(true);
                        Object delegate = delegateField.get(uc);
                        if (delegate instanceof HttpURLConnection) {
                            java.lang.reflect.Field methodField = HttpURLConnection.class.getDeclaredField("method");
                            methodField.setAccessible(true);
                            methodField.set(delegate, "PATCH");
                            success = true;
                        }
                    } catch (Exception ignored) {
                    }
                }
                if (!success) {
                    uc.setRequestMethod("POST");
                    uc.setRequestProperty("X-HTTP-Method-Override", "PATCH");
                }
            } else {
                throw ex;
            }
        }
    }

    @Override
    public NDuration readTimeout() {
        return readTimeout;
    }

    @Override
    public NHttpClient readTimeout(NDuration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public NDuration connectTimeout() {
        return connectTimeout;
    }

    @Override
    public NHttpClient connectTimeout(NDuration connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public NHttpClient timeout(NDuration timeout) {
        this.readTimeout = timeout;
        this.connectTimeout = timeout;
        return this;
    }

    public static String UNIFORM_HEADER(String h) {
        return NStringUtils.strip(h).toUpperCase();
    }

}
