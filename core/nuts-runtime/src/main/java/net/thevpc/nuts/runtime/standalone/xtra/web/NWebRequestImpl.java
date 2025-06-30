package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.web.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;
import java.util.function.Consumer;

public class NWebRequestImpl implements NWebRequest {
    private String url;
    private NHttpMethod method;
    private DefaultNWebHeaders headers = new DefaultNWebHeaders();
    private Map<String, List<String>> parameters;
    private NInputSource requestBody;
    private boolean oneWay;
    private Integer readTimeout;
    private Integer connectTimeout;
    private List<NWebRequestBody> parts = new ArrayList<>();
    private DefaultNWebCli cli;
    private Map<String, Object> formData;
    private Map<String, String> urlEncoded;
    private Mode mode = Mode.NONE;

    private static enum Mode {
        NONE,
        BODY,
        FORM_DATA,
        URLENCODED,
    }

    ;

    public NWebRequestImpl(DefaultNWebCli cli, NHttpMethod method) {
        this.cli = cli;
        this.method = method == null ? NHttpMethod.GET : method;
    }

    protected NWebRequestImpl setMode(Mode mode) {
        if (mode != null) {
            this.mode = mode;
        }
        switch (this.mode) {
            case NONE: {
                requestBody = null;
                formData = null;
                urlEncoded = null;
                break;
            }
            case BODY: {
                //body=null;
                formData = null;
                urlEncoded = null;
                break;
            }
            case FORM_DATA: {
                requestBody = null;
                //formData=null;
                urlEncoded = null;
                break;
            }
            case URLENCODED: {
                requestBody = null;
                formData = null;
                //urlEncoded=null;
                break;
            }
        }
        return this;
    }

    @Override
    public boolean isOneWay() {
        return oneWay;
    }

    @Override
    public NWebRequest setOneWay(boolean oneWay) {
        this.oneWay = oneWay;
        return this;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public NWebRequestImpl setUrl(String url, Object... vars) {
        NAssert.requireNonNull(url, "url");
        NAssert.requireNonNull(vars, "vars");
        NStringBuilder sb = new NStringBuilder();
        char[] charArray = url.toCharArray();
        char last = '\0';
        int index = 0;
        boolean inParams = false;
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (c) {
                case '?': {
                    inParams = true;
                    last = '?';
                    sb.append(c);
                    break;
                }
                case '/': {
                    if (inParams) {
                        sb.append(c);
                    } else {
                        if (sb.endsWith(":/")) {
                            // okkay
                        } else if (sb.endsWith('/')) {
                            // ignore
                        } else {
                            sb.append('/');
                            last = c;
                        }
                    }
                    break;
                }
                case '{': {
                    if (inParams) {
                        sb.append(c);
                    } else {
                        if (i + 1 < charArray.length) {
                            switch (charArray[i + 1]) {
                                case '}': {
                                    last = 's';
                                    if (index >= vars.length) {
                                        throw new IllegalArgumentException(NMsg.ofC("missing var at index %s in %s", index, url).toString());
                                    }
                                    if (!NBlankable.isBlank(vars[index])) {
                                        sb.append(NHttpUrlEncoder.encodeObject(vars[index]));
                                    } else {
                                        if (!sb.endsWith("://") && sb.endsWith('/')) {
                                            sb.removeLast();
                                        }
                                    }
                                    i++;
                                    index++;
                                    break;
                                }
                                default: {
                                    sb.append('{').append(charArray[i + 1]);
                                    i++;
                                    last = 'a';
                                    break;
                                }
                            }
                        } else {
                            sb.append(c);
                            last = 'a';
                        }
                    }
                    break;
                }
                case '%': {
                    if (inParams) {
                        sb.append(c);
                    } else {
                        if (i + 1 < charArray.length) {
                            switch (charArray[i + 1]) {
                                case 's': {
                                    last = 's';
                                    if (index >= vars.length) {
                                        throw new IllegalArgumentException(NMsg.ofC("missing var at index %s in %s", index, url).toString());
                                    }
                                    if (!NBlankable.isBlank(vars[index])) {
                                        sb.append(NHttpUrlEncoder.encodeObject(vars[index]));
                                    } else {
                                        if (!sb.endsWith("://") && sb.endsWith('/')) {
                                            sb.removeLast();
                                        }
                                    }
                                    i++;
                                    index++;
                                    break;
                                }
                                default: {
                                    sb.append('%').append(charArray[i + 1]);
                                    i++;
                                    last = 'a';
                                    break;
                                }
                            }
                        } else {
                            sb.append(c);
                            last = 'a';
                        }
                    }
                    break;
                }
                default: {
                    sb.append(c);
                    last = 'a';
                }
            }
        }
        this.url = sb.toString();
        return this;
    }

    public NWebRequest addCookies(NWebCookie[] cookies) {
        if (cookies != null) {
            for (NWebCookie cookie : cookies) {
                addCookie(cookie);
            }
        }
        return this;
    }

    public NWebRequest addCookie(NWebCookie cookie) {
        if (cookie != null) {
            addHeader("Cookie", cookie.getName() + "=" + cookie.getValue());
        }
        return this;
    }

    @Override
    public NWebRequest setUrl(String url) {
        this.url = url;
        return this;
    }

    @Override
    public NHttpMethod getMethod() {
        return method;
    }

    @Override
    public NWebRequestImpl setMethod(NHttpMethod method) {
        this.method = method == null ? NHttpMethod.GET : method;
        return this;
    }

    @Override
    public NWebRequest GET() {
        return setMethod(NHttpMethod.GET);
    }

    @Override
    public NWebRequest POST() {
        return setMethod(NHttpMethod.POST);
    }

    @Override
    public NWebRequest PATCH() {
        return setMethod(NHttpMethod.PATCH);
    }

    @Override
    public NWebRequest OPTIONS() {
        return setMethod(NHttpMethod.OPTIONS);
    }

    @Override
    public NWebRequest HEAD() {
        return setMethod(NHttpMethod.HEAD);
    }

    @Override
    public NWebRequest trace() {
        return setMethod(NHttpMethod.TRACE);
    }

    @Override
    public NWebRequest trace(String url) {
        return trace().setUrl(url);
    }

    @Override
    public NWebRequest connect() {
        return setMethod(NHttpMethod.CONNECT);
    }

    @Override
    public NWebRequest PUT() {
        return setMethod(NHttpMethod.PUT);
    }

    @Override
    public NWebRequest DELETE() {
        return setMethod(NHttpMethod.DELETE);
    }

    @Override
    public NWebRequest GET(String url) {
        return GET().setUrl(url);
    }

    @Override
    public NWebRequest POST(String url) {
        return POST().setUrl(url);
    }

    @Override
    public NWebRequest PATCH(String url) {
        return PATCH().setUrl(url);
    }

    @Override
    public NWebRequest OPTIONS(String url) {
        return OPTIONS().setUrl(url);
    }

    @Override
    public NWebRequest HEAD(String url) {
        return HEAD().setUrl(url);
    }

    @Override
    public NWebRequest connect(String url) {
        return connect().setUrl(url);
    }

    @Override
    public NWebRequest PUT(String url) {
        return PUT().setUrl(url);
    }

    @Override
    public NWebRequest DELETE(String url) {
        return DELETE().setUrl(url);
    }

    @Override
    public String getHeader(String name) {
        return headers.getFirst(name);
    }

    @Override
    public List<String> getHeaders(String name) {
        return headers.getOrEmpty(name);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers.toMap();
    }

    @Override
    public NWebRequest setHeaders(Map<String, List<String>> headers) {
        this.headers.clear();
        return this;
    }

    @Override
    public NWebRequest addHeaders(Map<String, List<String>> headers) {
        if (headers != null) {
            for (Map.Entry<String, List<String>> e : headers.entrySet()) {
                String k = e.getKey();
                if (k != null && e.getValue() != null && !e.getValue().isEmpty()) {
                    for (String v : e.getValue()) {
                        addHeader(k, v);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NWebRequest addParameters(Map<String, List<String>> parameters) {
        if (parameters != null) {
            for (Map.Entry<String, List<String>> e : parameters.entrySet()) {
                String k = e.getKey();
                if (k != null && e.getValue() != null && !e.getValue().isEmpty()) {
                    for (String v : e.getValue()) {
                        addHeader(k, v);
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NWebRequest setPropsFileHeaders(NPath path) {
        setHeaders(_mapFromPropsFile(path));
        return this;
    }

    @Override
    public NWebRequest addPropsFileHeaders(NPath path) {
        addHeaders(_mapFromPropsFile(path));
        return this;
    }

    @Override
    public NWebRequest addJsonFileHeaders(NPath path) {
        Map<String, List<String>> newHeaders = _mapFromJsonFile(path);
        addHeaders(newHeaders);
        return this;
    }

    @Override
    public NWebRequest setJsonFileHeaders(NPath path) {
        setHeaders(_mapFromJsonFile(path));
        return this;
    }

    @Override
    public NWebRequest setPropsFileParameters(NPath path) {
        setParameters(_mapFromPropsFile(path));
        return this;
    }

    @Override
    public NWebRequest addPropsFileParameters(NPath path) {
        addParameters(_mapFromPropsFile(path));
        return this;
    }

    @Override
    public NWebRequest addJsonFileParameters(NPath path) {
        Map<String, List<String>> newHeaders = _mapFromJsonFile(path);
        addParameters(newHeaders);
        return this;
    }

    @Override
    public NWebRequest setJsonFileParameters(NPath path) {
        setParameters(_mapFromJsonFile(path));
        return this;
    }

    private static Map<String, List<String>> _mapFromPropsFile(NPath path) {
        Map<String, List<String>> m = new LinkedHashMap<>();
        path.lines().forEach(x -> {
            x = x.trim();
            if (!x.startsWith("#")) {
                NArg a = NArg.of(x);
                m.computeIfAbsent(a.key(), r -> new ArrayList<>()).add(String.valueOf(a.key()));
            }
        });
        return m;
    }

    private Map<String, List<String>> _mapFromJsonFile(NPath path) {
        Map<String, Object> map = NElementParser.ofJson().parse(path.getReader(), Map.class);
        Map<String, List<String>> newHeaders = new LinkedHashMap<>();
        for (Map.Entry<String, Object> e : map.entrySet()) {
            String k = e.getKey();
            Object v = e.getValue();
            if (v instanceof String) {
                newHeaders.computeIfAbsent(k, r -> new ArrayList<>()).add((String) v);
            } else if (v instanceof List) {
                for (Object o : ((List) v)) {
                    newHeaders.computeIfAbsent(k, r -> new ArrayList<>()).add(String.valueOf(o));
                }
            } else if (v instanceof Object[]) {
                for (Object o : ((Object[]) v)) {
                    newHeaders.computeIfAbsent(k, r -> new ArrayList<>()).add(String.valueOf(o));
                }
            }
        }
        return newHeaders;
    }


    @Override
    public NWebRequestImpl addHeader(String name, String value) {
        this.headers.addHeader(name, value, DefaultNWebHeaders.Mode.ALWAYS);
        return this;
    }

    @Override
    public NWebRequestImpl setHeader(String name, String value) {
        if (name != null) {
            if (value != null) {
                this.headers.addHeader(name, value, DefaultNWebHeaders.Mode.ALWAYS);
            } else {
                this.headers.removeHeader(name);
            }
        }
        return this;
    }


    @Override
    public Map<String, List<String>> getParameters() {
        return parameters;
    }

    @Override
    public NWebRequest setParameters(Map<String, List<String>> parameters) {
        this.parameters = parameters == null ? new LinkedHashMap<>() : parameters;
        return this;
    }

    @Override
    public NWebRequest doWith(Consumer<NWebRequest> any) {
        if (any != null) {
            any.accept(this);
        }
        return this;
    }

    @Override
    public NWebRequest addParameter(String name, String value) {
        if (value != null) {
            if (this.parameters == null) {
                this.parameters = new LinkedHashMap<>();
            }
            this.parameters.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
        }
        return this;
    }

    @Override
    public NWebRequest setParameter(String name, String value) {
        if (value != null) {
            if (this.parameters == null) {
                this.parameters = new LinkedHashMap<>();
            }
            List<String> list = this.parameters.computeIfAbsent(name, s -> new ArrayList<>());
            list.clear();
            list.add(value);
        } else {
            if (this.parameters != null) {
                List<String> list = this.parameters.computeIfAbsent(name, s -> new ArrayList<>());
                list.clear();
            }
        }
        return this;
    }

    @Override
    public NInputSource getRequestBody() {
        switch (mode) {
            case BODY:
                return requestBody;
            case URLENCODED: {
                setContentType("application/x-www-form-urlencoded");
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (Map.Entry<String, String> e : urlEncoded.entrySet()) {
                    if (first) {
                        first = false;
                    } else {
                        sb.append("&");
                    }
                    try {
                        sb
                                .append(URLEncoder.encode(NStringUtils.trim(e.getKey()), "UTF-8"))
                                .append("=")
                                .append(URLEncoder.encode(NStringUtils.trim(e.getValue()), "UTF-8"))
                        ;
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                return NInputSource.of(sb.toString().getBytes());
            }
            case FORM_DATA: {
                //setContentTypeFormUrlEncoded();
                SimpleWriter sw = new SimpleWriter(NIO.of().ofTempOutputStream());
                if (formData != null && !formData.isEmpty()) {
                    String boundary = "-------------------------------" + UUID.randomUUID().toString();
                    setContentType("multipart/form-data; boundary=" + boundary);
                    try {
                        sw.println(boundary);
                        for (Map.Entry<String, Object> e : formData.entrySet()) {
                            if (e.getValue() instanceof String) {
                                sw.println("Content-Disposition: form-data; name=" + NLiteral.of(e.getKey()).toStringLiteral());
                                sw.println();
                                sw.println(e.getValue().toString());

                            } else if (e.getValue() instanceof NInputContentProvider) {
                                NInputContentProvider npath = (NInputContentProvider) e.getValue();
                                sw.println("Content-Disposition: form-data; name=" + NLiteral.of(e.getKey()).toStringLiteral()
                                        + " ; filename=" + NLiteral.of(npath.getName()).toStringLiteral());
                                sw.println(("Content-Type: " + NStringUtils.firstNonBlank(npath.getContentType(), "application/octet-stream")));
                                sw.println();
                                if (npath instanceof NPath) {
                                    ((NPath) npath).copyToOutputStream(sw.tos);
                                } else {
                                    try (InputStream tis = npath.getInputStream()) {
                                        NIOUtils.copy(tis, sw.tos);
                                    }
                                }
                            }
                            sw.println();
                            sw.println(boundary);
                        }
                    } catch (IOException ex) {
                        throw new NIOException(ex);
                    }
                }
                return sw.tos;
            }
        }
        return null;
    }

    private static class SimpleWriter {
        private NTempOutputStream tos;

        public SimpleWriter(NTempOutputStream tos) {
            this.tos = tos;
        }

        public void println() {
            try {
                tos.write("\r\n".getBytes());
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }

        public void println(String text) {
            try {
                tos.write(text.getBytes());
                tos.write("\r\n".getBytes());
            } catch (IOException e) {
                throw new NIOException(e);
            }
        }
    }

    @Override
    public NWebRequestImpl setJsonRequestBody(Object body) {
        if (body == null) {
            this.requestBody = null;
            setMode(Mode.NONE);
        } else {
            this.requestBody = NInputSource.of(NElementWriter.ofJson().toString(body).getBytes());
            setMode(Mode.BODY);
        }
        setContentType("application/json");
        return this;
    }

    @Override
    public NWebRequest setRequestBody(byte[] body) {
        this.requestBody = body == null ? null : NInputSource.of(body);
        setMode(body == null ? Mode.NONE : Mode.BODY);
        return this;
    }

    @Override
    public NWebRequest setRequestBody(String body) {
        this.requestBody = body == null ? null : NInputSource.of(new StringReader(body));
        setMode(body == null ? Mode.NONE : Mode.BODY);
        return null;
    }

    @Override
    public NWebRequest setRequestBody(NInputSource body) {
        this.requestBody = body;
        setMode(Mode.BODY);
        return this;
    }

    @Override
    public NWebRequestImpl setContentLanguage(String contentLanguage) {
        return setHeader("Content-Language", contentLanguage);
    }

    @Override
    public NWebRequestImpl setAuthorizationBearer(String authorizationBearer) {
        authorizationBearer = NStringUtils.trimToNull(authorizationBearer);
        if (authorizationBearer != null) {
            authorizationBearer = "Bearer " + authorizationBearer;
        }
        return setAuthorization(authorizationBearer);
    }

    @Override
    public NWebRequestImpl setAuthorization(String authorization) {
        return setHeader("Authorization", NStringUtils.trimToNull(authorization));
    }

    @Override
    public String getAuthorization() {
        return getHeader("Authorization");
    }

    @Override
    public String getAuthorizationBearer() {
        String b = getHeader("Authorization");
        if (b != null && b.toLowerCase().startsWith("bearer ")) {
            return b.substring("bearer ".length()).trim();
        }
        return b;
    }

    @Override
    public String getContentLanguage() {
        return getHeader("Content-Language");
    }

    @Override
    public String getContentType() {
        return getHeader("Content-Type");
    }

    @Override
    public NWebRequest setContentTypeFormUrlEncoded() {
        return setContentType("application/x-www-form-urlencoded");
    }


    @Override
    public NWebRequest addFormUrlEncoded(String key, String value) {
        if (value == null) {
            return this;
        }
        if (urlEncoded == null) {
            urlEncoded = new LinkedHashMap<>();
        }
        urlEncoded.put(key, value);
        setMode(Mode.URLENCODED);
        return this;
    }

    @Override
    public NWebRequest addFormUrlEncoded(Map<String, String> value) {
        if (value == null) {
            return this;
        }
        if (urlEncoded == null) {
            urlEncoded = new LinkedHashMap<>();
        }
        setMode(Mode.URLENCODED);
        urlEncoded.putAll(value);
        return this;
    }

    @Override
    public NWebRequest addFormData(String key, NInputContentProvider value) {
        if (value == null) {
            return this;
        }
        if (formData == null) {
            formData = new LinkedHashMap<>();
        }
        formData.put(key, value);
        setMode(Mode.FORM_DATA);
        return this;
    }

    @Override
    public NWebRequest addFormData(String key, String value) {
        if (value == null) {
            return this;
        }
        if (formData == null) {
            formData = new LinkedHashMap<>();
        }
        formData.put(key, value);
        setMode(Mode.FORM_DATA);
        return this;
    }

    @Override
    public NWebRequest setFormData(String key, NInputContentProvider value) {
        return addFormData(key, value);
    }

    @Override
    public NWebRequest setFormData(String key, String value) {
        return addFormData(key, value);
    }

    @Override
    public NWebRequest setFormUrlEncoded(Map<String, String> m) {
        setContentTypeFormUrlEncoded();
        StringBuilder sb = new StringBuilder();
        if (m != null) {
            boolean first = true;
            for (Map.Entry<String, String> e : m.entrySet()) {
                if (first) {
                    first = false;
                } else {
                    sb.append("&");
                }
                try {
                    sb
                            .append(URLEncoder.encode(NStringUtils.trim(e.getKey()), "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(NStringUtils.trim(e.getValue()), "UTF-8"))
                    ;
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        setRequestBody(sb.toString().getBytes());
        return this;
    }

    @Override
    public NWebRequest setContentType(String contentType) {
        return setHeader("Content-Type", contentType);
    }

    @Override
    public Integer getReadTimeout() {
        return readTimeout;
    }

    @Override
    public NWebRequest setReadTimeout(Integer readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    @Override
    public NWebRequest setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
        return this;
    }

    @Override
    public List<NWebRequestBody> getParts() {
        return parts;
    }

    @Override
    public NWebRequest addPart(NWebRequestBody body) {
        parts.add(body);
        return this;
    }

    @Override
    public NWebRequestBody addPart() {
        NWebRequestBody part = new NWebRequestBodyImpl(this);
        addPart(part);
        return part;
    }

    @Override
    public NWebRequestBody addPart(String name) {
        return addPart().setName(name);
    }

    @Override
    public NWebRequest addPart(String name, String value) {
        return addPart().setName(name).setStringValue(value).end();
    }

    @Override
    public NWebRequest addPart(String name, String fileName, String contentType, NInputSource body) {
        return addPart().setName(name).setContentType(contentType).setBody(body).end();
    }

    @Override
    public NWebResponse run() {
        return cli.run(this);
    }

    public String getEffectiveUrl() {
        return cli.formatURL(this, true);
    }

    @Override
    public String toString() {
        return getEffectiveUrl();
    }

    @Override
    public NMsg toMsg() {
        return NMsg.ofC("%s %s",
                method == null ? NHttpMethod.GET : method,
                NMsg.ofStyledPath(getEffectiveUrl())
        );
    }
}

