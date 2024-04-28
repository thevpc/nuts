package net.thevpc.nuts.runtime.standalone.web;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NInputSourceBuilder;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.web.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.channels.InterruptedByTimeoutException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNWebCli implements NWebCli {

    private String prefix;
    private Function<NWebResponse, NWebResponse> responsePostProcessor;
    private Integer readTimeout;
    private Integer connectTimeout;
    private NSession session;

    public DefaultNWebCli(NSession session) {
        this.session = session;
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
        return new NWebRequestImpl(this, session, NHttpMethod.GET);
    }

    @Override
    public NWebRequest req(NHttpMethod method) {
        return new NWebRequestImpl(this, session, method);
    }

    @Override
    public NWebRequest get() {
        return req().get();
    }

    @Override
    public NWebRequest post() {
        return req().post();
    }

    @Override
    public NWebRequest put() {
        return req().put();
    }

    @Override
    public NWebRequest delete() {
        return req().delete();
    }

    @Override
    public NWebRequest patch() {
        return req().patch();
    }

    @Override
    public NWebRequest options() {
        return req().options();
    }

    @Override
    public NWebRequest head() {
        return req().head();
    }

    @Override
    public NWebRequest connect() {
        return req().connect();
    }

    @Override
    public NWebRequest trace() {
        return null;
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
            URL h = new URL(spec);
            HttpURLConnection uc = null;
            try {
                uc = (HttpURLConnection) h.openConnection();

                Integer readTimeout1 = r.getReadTimeout();
                if (readTimeout1 == null) {
                    readTimeout1 = getReadTimeout();
                }
                if (readTimeout1 != null) {
                    uc.setReadTimeout(readTimeout1);
                }

                Integer connectTimeout1 = r.getConnectTimeout();
                if (connectTimeout1 == null) {
                    connectTimeout1 = getConnectTimeout();
                }

                if (connectTimeout1 != null) {
                    uc.setConnectTimeout(connectTimeout1);
                }
                Map<String, List<String>> headers = new LinkedHashMap<>();
                Map<String, List<String>> rHeaders = r.getHeaders();
                if (rHeaders != null) {
                    for (Map.Entry<String, List<String>> e : rHeaders.entrySet()) {
                        if (e.getKey() != null && e.getValue() != null) {
                            headers.computeIfAbsent(e.getKey(), g -> new ArrayList<>())
                                    .addAll(e.getValue());
                        }
                    }
                }
                for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                    for (String s : e.getValue()) {
                        uc.setRequestProperty(e.getKey(), s);
                    }
                }
                uc.setRequestMethod(method.toString());
                uc.setUseCaches(false);

                NInputSource requestBody = r.getBody();
                long bodyLength = requestBody == null ? -1 : requestBody.getContentLength();
                boolean someBody = requestBody != null && bodyLength > 0;

                uc.setDoInput(!r.isOneWay());
                uc.setDoOutput(someBody);
                if (someBody) {
                    uc.setRequestProperty("Content-Length", String.valueOf(bodyLength));
                    NCp.of(session).from(requestBody).to(uc.getOutputStream()).run();
                }
                NInputSource bytes = null;
                if (!r.isOneWay()) {
                    //TODO change me with a smart copy input source!
                    HttpURLConnection uc2 = uc;
                    bytes = NInputSourceBuilder.of(uc.getInputStream(),session).setCloseAction(() -> {
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
//                    byte[] byteArrayResult = NCp.of(session).from(uc.getInputStream()).getByteArrayResult();
//                    bytes = NIO.of(session).ofInputSource(byteArrayResult);
                    long contentLength = uc.getContentLengthLong();
                    if (contentLength >= 0) {
                        bytes.getMetaData().setContentLength(contentLength);
                    }
                }
                NWebResponse httpResponse = new NWebResponseImpl(
                        uc.getResponseCode(),
                        NMsg.ofPlain(NStringUtils.trim(uc.getResponseMessage())),
                        uc.getHeaderFields(),
                        bytes,
                        session
                );
                if (responsePostProcessor != null) {
                    NWebResponse newResp = responsePostProcessor.apply(httpResponse);
                    if (newResp != null) {
                        httpResponse = newResp;
                    }
                }
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
            throw new UncheckedIOException("timed out loading " + spec, ex);
        } catch (InterruptedByTimeoutException ex) {
            throw new UncheckedIOException("interrupt loading " + spec, ex);
        } catch (InterruptedIOException ex) {
            throw new UncheckedIOException("interrupt loading " + spec, ex);
        } catch (IOException ex) {
            throw new UncheckedIOException("error loading " + spec, ex);
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

}
