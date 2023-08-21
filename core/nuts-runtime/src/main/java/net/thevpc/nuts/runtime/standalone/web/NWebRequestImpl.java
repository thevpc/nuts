package net.thevpc.nuts.runtime.standalone.web;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.web.*;

import java.util.*;

public class NWebRequestImpl implements NWebRequest {
    private String url;
    private NHttpMethod method;
    private Map<String, List<String>> headers;
    private Map<String, List<String>> parameters;
    private NInputSource body;
    private boolean oneWay;
    private Integer readTimeout;
    private Integer connectTimeout;
    private NSession session;
    private List<NWebRequestBody> parts = new ArrayList<>();
    private NWebCliImpl cli;

    public NWebRequestImpl(NWebCliImpl cli, NSession session, NHttpMethod method) {
        this.cli = cli;
        this.session = session;
        this.method = method == null ? NHttpMethod.GET : method;
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

    @Override
    public NWebRequestImpl setUrl(String url) {
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
    public NWebRequest get() {
        return setMethod(NHttpMethod.GET);
    }

    @Override
    public NWebRequest post() {
        return setMethod(NHttpMethod.POST);
    }

    @Override
    public NWebRequest patch() {
        return setMethod(NHttpMethod.PATCH);
    }

    @Override
    public NWebRequest options() {
        return setMethod(NHttpMethod.OPTIONS);
    }

    @Override
    public NWebRequest head() {
        return setMethod(NHttpMethod.HEAD);
    }

    @Override
    public NWebRequest connect() {
        return setMethod(NHttpMethod.CONNECT);
    }

    @Override
    public NWebRequest put() {
        return setMethod(NHttpMethod.PUT);
    }

    @Override
    public NWebRequest delete() {
        return setMethod(NHttpMethod.DELETE);
    }

    @Override
    public String getHeader(String name) {
        if (headers != null) {
            List<String> values = headers.get(name);
            if (values != null) {
                for (String value : values) {
                    return value;
                }
            }
        }
        return null;
    }

    @Override
    public List<String> getHeaders(String name) {
        List<String> all = new ArrayList<>();
        if (headers != null) {
            List<String> values = headers.get(name);
            if (values != null) {
                all.addAll(values);
            }
        }
        return all;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public NWebRequest setHeaders(Map<String, List<String>> headers) {
        this.headers = headers == null ? new HashMap<>() : headers;
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
        path.getLines().forEach(x -> {
            x = x.trim();
            if (!x.startsWith("#")) {
                NArg a = NArg.of(x);
                m.computeIfAbsent(a.key(), r -> new ArrayList<>()).add(String.valueOf(a.key()));
            }
        });
        return m;
    }

    private Map<String, List<String>> _mapFromJsonFile(NPath path) {
        Map<String, Object> map = NElements.of(session).parse(path.getReader(), Map.class);
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
        if (name != null && value != null) {
            if (this.headers == null) {
                this.headers = new LinkedHashMap<>();
            }
            this.headers.computeIfAbsent(name, s -> new ArrayList<>()).add(value);
        }
        return this;
    }

    @Override
    public NWebRequestImpl setHeader(String name, String value) {
        if (name != null) {
            if (value != null) {
                if (this.headers == null) {
                    this.headers = new LinkedHashMap<>();
                }
                List<String> list = this.headers.computeIfAbsent(name, s -> new ArrayList<>());
                list.clear();
                list.add(value);
            } else {
                if (this.headers != null) {
                    List<String> list = this.headers.computeIfAbsent(name, s -> new ArrayList<>());
                    list.clear();
                }
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
    public NInputSource getBody() {
        return body;
    }

    @Override
    public NWebRequestImpl setJsonBody(Object body) {
        if (body == null) {
            this.body = null;
        } else {
            this.body = NIO.of(session).ofInputSource(NElements.of(session).json()
                    .setValue(body).setNtf(false).formatPlain().getBytes());
        }
        setContentType("application/json");
        return this;
    }

    @Override
    public NWebRequest setBody(byte[] body) {
        this.body = body == null ? null : NIO.of(session).ofInputSource(body);
        return this;
    }

    @Override
    public NWebRequest setBody(NInputSource body) {
        this.body = body;
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
    public NWebRequest setContentTypeForm() {
        return setContentType("application/x-www-form-urlencoded");
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
}

