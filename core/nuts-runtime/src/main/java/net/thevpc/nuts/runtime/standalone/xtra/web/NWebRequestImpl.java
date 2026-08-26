package net.thevpc.nuts.runtime.standalone.xtra.web;

import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.net.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.net.URLEncoder;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

public class NWebRequestImpl implements NWebRequest {
    private String uri;
    private NHttpMethod method;
    private final DefaultNWebHeaders headers = new DefaultNWebHeaders();
    private Map<String, List<String>> parameters;
    private NInputSource requestBody;
    private boolean oneWay;
    private NDuration readTimeout;
    private NDuration connectTimeout;
    private final List<NWebRequestBody> parts = new ArrayList<>();
    private final DefaultNWebCli cli;
    private Map<String, Object> formData;
    private Map<String, String> urlEncoded;
    private Mode mode = Mode.NONE;

    private enum Mode {
        NONE,
        BODY,
        FORM_DATA,
        MULTIPART,
        URLENCODED,
    }

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
            case MULTIPART: {
                requestBody = null;
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
    public NWebRequest oneWay(boolean oneWay) {
        this.oneWay = oneWay;
        return this;
    }

    @Override
    public String uri() {
        return uri;
    }

    @Override
    public NWebRequestImpl uri(String url, Object... vars) {
        NAssert.requireNamedNonNull(url, "url");
        NAssert.requireNamedNonNull(vars, "vars");
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
        this.uri = sb.toString();
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
            addHeader("Cookie", cookie.name() + "=" + cookie.value());
        }
        return this;
    }

    @Override
    public NWebRequest uri(String url) {
        this.uri = url;
        return this;
    }

    @Override
    public NHttpMethod method() {
        return method;
    }

    @Override
    public NWebRequestImpl method(NHttpMethod method) {
        this.method = method == null ? NHttpMethod.GET : method;
        return this;
    }

    @Override
    public NWebRequest GET() {
        return method(NHttpMethod.GET);
    }

    @Override
    public NWebRequest POST() {
        return method(NHttpMethod.POST);
    }

    @Override
    public NWebRequest PATCH() {
        return method(NHttpMethod.PATCH);
    }

    @Override
    public NWebRequest OPTIONS() {
        return method(NHttpMethod.OPTIONS);
    }

    @Override
    public NWebRequest HEAD() {
        return method(NHttpMethod.HEAD);
    }

    @Override
    public NWebRequest TRACE() {
        return method(NHttpMethod.TRACE);
    }

    @Override
    public NWebRequest TRACE(String url) {
        return TRACE().uri(url);
    }

    @Override
    public NWebRequest CONNECT() {
        return method(NHttpMethod.CONNECT);
    }

    @Override
    public NWebRequest PUT() {
        return method(NHttpMethod.PUT);
    }

    @Override
    public NWebRequest DELETE() {
        return method(NHttpMethod.DELETE);
    }

    @Override
    public NWebRequest GET(String url) {
        return GET().uri(url);
    }

    @Override
    public NWebRequest POST(String url) {
        return POST().uri(url);
    }

    @Override
    public NWebRequest PATCH(String url) {
        return PATCH().uri(url);
    }

    @Override
    public NWebRequest OPTIONS(String url) {
        return OPTIONS().uri(url);
    }

    @Override
    public NWebRequest HEAD(String url) {
        return HEAD().uri(url);
    }

    @Override
    public NWebRequest CONNECT(String url) {
        return CONNECT().uri(url);
    }

    @Override
    public NWebRequest PUT(String url) {
        return PUT().uri(url);
    }

    @Override
    public NWebRequest DELETE(String url) {
        return DELETE().uri(url);
    }

    @Override
    public String header(String name) {
        return headers.getFirst(name);
    }

    @Override
    public List<String> headers(String name) {
        return headers.getOrEmpty(name);
    }

    @Override
    public Map<String, List<String>> headers() {
        return headers.toMap();
    }

    @Override
    public NWebRequest headers(Map<String, List<String>> headers) {
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
    public NWebRequest propsFileHeaders(NPath path) {
        headers(_mapFromPropsFile(path));
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
    public NWebRequest jsonFileHeaders(NPath path) {
        headers(_mapFromJsonFile(path));
        return this;
    }

    @Override
    public NWebRequest propsFileParameters(NPath path) {
        parameters(_mapFromPropsFile(path));
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
    public NWebRequest psonFileParameters(NPath path) {
        parameters(_mapFromJsonFile(path));
        return this;
    }

    private static Map<String, List<String>> _mapFromPropsFile(NPath path) {
        Map<String, List<String>> m = new LinkedHashMap<>();
        path.lines().forEach(x -> {
            x = NStringUtils.strip(x);
            if (!x.startsWith("#")) {
                NArg a = NArg.of(x);
                m.computeIfAbsent(a.key(), r -> new ArrayList<>()).add(String.valueOf(a.key()));
            }
        });
        return m;
    }

    private Map<String, List<String>> _mapFromJsonFile(NPath path) {
        Map<String, Object> map = NElementReader.ofJson().read(path.asReader(), Map.class);
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

    public NWebRequestImpl header(String name, String value) {
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
    public NWebRequestImpl addHeader(String name, String value) {
        this.headers.addHeader(name, value, DefaultNWebHeaders.Mode.ALWAYS);
        return this;
    }


    @Override
    public Map<String, List<String>> parameters() {
        return parameters;
    }

    @Override
    public NWebRequest parameters(Map<String, List<String>> parameters) {
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
    public NWebRequest parameter(String name, String value) {
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
    public NInputSource requestBody() {
        switch (mode) {
            case BODY:
                return requestBody;
            case URLENCODED: {
                contentType("application/x-www-form-urlencoded");
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
                                .append(URLEncoder.encode(NStringUtils.strip(e.getKey()), "UTF-8"))
                                .append("=")
                                .append(URLEncoder.encode(NStringUtils.strip(e.getValue()), "UTF-8"))
                        ;
                    } catch (UnsupportedEncodingException ex) {
                        throw new RuntimeException(ex);
                    }
                }
                return NInputSource.of(sb.toString().getBytes());
            }
            case FORM_DATA:
            case MULTIPART: {
                //setContentTypeFormUrlEncoded();
                SimpleWriter sw = new SimpleWriter(NIO.of().ofTempOutputStream());
                String boundary = "-------------------------------" + UUID.randomUUID();
                contentType("multipart/form-data; boundary=" + boundary);
                try {
                    if (mode == Mode.FORM_DATA) {
                        if (formData != null && !formData.isEmpty()) {
                            for (Map.Entry<String, Object> e : formData.entrySet()) {
                                sw.println("--" + boundary);
                                if (e.getValue() instanceof String) {
                                    sw.println("Content-Disposition: form-data; name=" + NLiteral.of(e.getKey()).toStringLiteral());
                                    sw.println();
                                    sw.println(e.getValue().toString());
                                } else if (e.getValue() instanceof NInputContentProvider) {
                                    NInputContentProvider npath = (NInputContentProvider) e.getValue();
                                    sw.println("Content-Disposition: form-data; name=" + NLiteral.of(e.getKey()).toStringLiteral()
                                            + "; filename=" + NLiteral.of(npath.name()).toStringLiteral());
                                    sw.println(("Content-Type: " + NStringUtils.firstNonBlank(npath.contentType(), "application/octet-stream")));
                                    sw.println();
                                    if (npath instanceof NPath) {
                                        ((NPath) npath).copyToOutputStream(sw.tos);
                                    } else {
                                        try (InputStream tis = npath.inputStream()) {
                                            NIOUtils.copy(tis, sw.tos);
                                        }
                                    }
                                }
                                sw.println();
                            }
                        }
                    } else {
                        if (parts != null && !parts.isEmpty()) {
                            for (NWebRequestBody part : parts) {
                                sw.println("--" + boundary);
                                sw.println("Content-Disposition: " + part.contentDisposition());
                                if (!NBlankable.isBlank(part.contentType())) {
                                    sw.println("Content-Type: " + part.contentType());
                                }
                                sw.println();
                                if (part.body() != null) {
                                    try (InputStream tis = part.body().inputStream()) {
                                        NIOUtils.copy(tis, sw.tos);
                                    }
                                } else if (part.stringValue() != null) {
                                    sw.println(part.stringValue());
                                }
                                sw.println();
                            }
                        }
                    }
                    sw.println("--" + boundary + "--");
                    sw.tos.close();
                } catch (IOException ex) {
                    throw new NIOException(ex);
                }
                return sw.tos;
            }
        }
        return null;
    }

    private static class SimpleWriter {
        private final NTempOutputStream tos;

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
    public NWebRequestImpl jsonRequestBody(Object body) {
        if (body == null) {
            this.requestBody = null;
            setMode(Mode.NONE);
        } else {
            this.requestBody = NInputSource.of(NElementWriter.ofJson().formatPlain(body).getBytes());
            setMode(Mode.BODY);
        }
        contentType("application/json");
        return this;
    }

    @Override
    public NWebRequest requestBody(byte[] body) {
        this.requestBody = body == null ? null : NInputSource.of(body);
        setMode(body == null ? Mode.NONE : Mode.BODY);
        return this;
    }

    @Override
    public NWebRequest requestBody(String body) {
        this.requestBody = body == null ? null : NInputSource.of(new StringReader(body));
        setMode(body == null ? Mode.NONE : Mode.BODY);
        return null;
    }

    @Override
    public NWebRequest requestBody(NInputSource body) {
        this.requestBody = body;
        setMode(Mode.BODY);
        return this;
    }

    @Override
    public NWebRequest contentLanguage(String contentLanguage) {
        return header("Content-Language", contentLanguage);
    }

    @Override
    public NWebRequest authorizationBearer(String authorizationBearer) {
        authorizationBearer = NStringUtils.stripToNull(authorizationBearer);
        if (authorizationBearer != null) {
            authorizationBearer = "Bearer " + authorizationBearer;
        }
        return authorization(authorizationBearer);
    }

    @Override
    public NWebRequest authorizationBasic(String username, String password) {
        return authorization("Basic "
                + Base64.getEncoder()
                .encodeToString(
                        (NStringUtils.firstNonNull(username, "")
                                + ":"
                                + NStringUtils.firstNonNull(password, "")
                        ).getBytes()
                )
        );
    }

    @Override
    public NWebRequest authorization(String authorization) {
        return header("Authorization", NStringUtils.stripToNull(authorization));
    }

    @Override
    public String authorization() {
        return header("Authorization");
    }

    @Override
    public String authorizationBearer() {
        String b = header("Authorization");
        if (b != null && b.toLowerCase().startsWith("bearer ")) {
            return NStringUtils.strip(b.substring("bearer ".length()));
        }
        return b;
    }

    @Override
    public String contentLanguage() {
        return header("Content-Language");
    }

    @Override
    public String contentType() {
        return header("Content-Type");
    }

    @Override
    public NWebRequest contentTypeFormUrlEncoded() {
        return contentType("application/x-www-form-urlencoded");
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
    public NWebRequest formData(String key, NInputContentProvider value) {
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
    public NWebRequest formData(String key, String value) {
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
    public NWebRequest formUrlEncoded(Map<String, String> m) {
        contentTypeFormUrlEncoded();
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
                            .append(URLEncoder.encode(NStringUtils.strip(e.getKey()), "UTF-8"))
                            .append("=")
                            .append(URLEncoder.encode(NStringUtils.strip(e.getValue()), "UTF-8"))
                    ;
                } catch (UnsupportedEncodingException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }
        requestBody(sb.toString().getBytes());
        return this;
    }

    @Override
    public NWebRequest contentType(String contentType) {
        return header("Content-Type", contentType);
    }

    @Override
    public NDuration readTimeout() {
        return readTimeout;
    }

    @Override
    public NWebRequest readTimeout(NDuration readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    @Override
    public NWebRequest timeout(NDuration timeout) {
        this.readTimeout = timeout;
        this.connectTimeout = timeout;
        return this;
    }

    @Override
    public NDuration connectTimeout() {
        return connectTimeout;
    }

    @Override
    public NWebRequest connectTimeout(NDuration duration) {
        this.connectTimeout = duration;
        return this;
    }

    @Override
    public List<NWebRequestBody> parts() {
        return parts;
    }

    @Override
    public NWebRequest addPart(NWebRequestBody body) {
        parts.add(body);
        setMode(Mode.MULTIPART);
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
        return addPart().name(name);
    }

    @Override
    public NWebRequest addPart(String name, String value) {
        return addPart().name(name).stringValue(value).end();
    }

    @Override
    public NWebRequest addPart(String name, String fileName, String contentType, NInputSource body) {
        return addPart().name(name).contentType(contentType).body(body).end();
    }

    public NWebRequest addPart(String name, File file) {
        return addPart(name, file == null ? null : file.getName(), null, NInputSource.of(file));
    }

    public NWebRequest addPart(String name, Path file) {
        return addPart(name, file == null ? null : file.getFileName().toString(), null, NInputSource.of(file));
    }

    public NWebRequest addPart(String name, NPath file) {
        return addPart(name, file == null ? null : file.name(), null, NInputSource.of(file));
    }

    public NWebRequest addPart(File file) {
        NAssert.requireNamedNonNull(file, "file");
        return addPart(file.getName(), file.getName(), null, NInputSource.of(file));
    }

    public NWebRequest addPart(Path file) {
        NAssert.requireNamedNonNull(file, "file");
        return addPart(file.getFileName().toString(), file.getFileName().toString(), null, NInputSource.of(file));
    }

    public NWebRequest addPart(NPath file) {
        NAssert.requireNamedNonNull(file, "file");
        return addPart(file.name(), file.name(), null, NInputSource.of(file));
    }

    @Override
    public NWebResponse run() {
        return cli.run(this);
    }

    @Override
    public CompletableFuture<NWebResponse> runAsync() {
        return runAsync(null);
    }

    @Override
    public CompletableFuture<NWebResponse> runAsync(Executor executor) {
        return cli.runAsync(this,executor);
    }

    public String effectiveUri() {
        return cli.formatURL(this, true);
    }

    @Override
    public String toString() {
        return effectiveUri();
    }

    @Override
    public NMsg toMsg() {
        return NMsg.ofC("%s %s",
                method == null ? NHttpMethod.GET : method,
                NMsg.ofStyledPath(effectiveUri())
        );
    }
}

