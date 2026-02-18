package net.thevpc.nuts.runtime.standalone.net;

import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNConnectionStringBuilder implements Cloneable, NConnectionStringBuilder {
    private static Pattern CONNECTION_PATTERN = Pattern.compile(
            "((?<user>([a-zA-Z]([a-zA-Z0-9_-])*))(:(?<password>([^@]+)))?@)?" +
                    "((?<server>([a-zA-Z0-9._-]+))(:(?<port>[0-9]+))?)" +
                    "(?<path>([/:].*))?"
    );
    private static Pattern PROTOCOLE_PATTERN = Pattern.compile(
            "(?<protocol>[a-zA-Z]([a-zA-Z0-9_+-])*):(?<path>(/.*))"
    );
    private static Pattern NAME_PATTERN = Pattern.compile("[a-zA-Z]([a-zA-Z0-9_-])*");
    private String protocol;
    private String userName;
    private String password;
    private String host;
    private String port;
    private String path;
    private boolean normalized;
    private Map<String, List<String>> queryMap;

    public DefaultNConnectionStringBuilder() {
    }

    public DefaultNConnectionStringBuilder(NConnectionString other) {
        if (other != null) {
            this.protocol = other.getProtocol();
            this.userName = other.getUserName();
            this.password = other.getPassword();
            this.host = other.getHost();
            this.port = other.getPort();
            this.path = other.getPath();
            this.queryMap = prepareQueryMap(other.getQueryMap().orNull(), false);
        }
    }

    @Override
    public NConnectionString build() {
        if (normalized) {
            Map<String, List<String>> queryMap2 = DefaultNConnectionStringBuilder.prepareQueryMap(queryMap, true);
            String protocol2 = NStringUtils.trimToNull(protocol);
            String userName2 = NStringUtils.trimToNull(userName);
            String host2 = NStringUtils.trimToNull(host);
            String port2 = NStringUtils.trimToNull(port);
            String path2 = NStringUtils.trimToNull(path);
            return new DefaultNConnectionString(protocol2, userName2, password, host2, port2, path2, queryMap2);
        } else {
            return new DefaultNConnectionString(protocol, userName, password, host, port, path, queryMap);
        }
    }

    public static NOptional<NConnectionStringBuilder> of(String value) {
        if (value == null || NBlankable.isBlank(value)) {
            return NOptional.ofNamedEmpty("Connection String");
        }
        value = value.trim();
        DefaultNConnectionStringBuilder v = new DefaultNConnectionStringBuilder();
        Matcher e = PROTOCOLE_PATTERN.matcher(value);
        String protocol;
        String path;
        if (e.matches()) {
            protocol = safeUrlDecode(e.group("protocol"));
            path = e.group("path");
            if (path.startsWith("//") && !"file".equals(protocol)) {
                path = path.substring(2);
            }
        } else {
            protocol = null;
            path = value;
        }
        value = path;
        v.setProtocol(protocol);
        Matcher matcher = NAME_PATTERN.matcher(value);
        String pathAndQuery = null;
        if (matcher.matches()) {
            if (protocol == null) {
                pathAndQuery = "/" + value;
            } else {
                v.setHost(value);
            }
        } else {
            matcher = CONNECTION_PATTERN.matcher(value);
            if (matcher.matches()) {
                v.setUserName(matcher.group("user"));
                v.setPassword(matcher.group("password"));
                v.setHost(matcher.group("server"));
                v.setPort(matcher.group("port"));
                String spath = matcher.group("path");
                if (spath != null && spath.startsWith(":")) {
                    spath = spath.substring(1);
                }
                pathAndQuery = spath;
            } else {
                pathAndQuery = value;
            }
        }
        if (pathAndQuery != null) {
            int i = pathAndQuery.indexOf('?');
            if (i >= 0) {
                String p = pathAndQuery.substring(0, i);
                String q = pathAndQuery.substring(i + 1);
                v.setPath(p);
                v.setQueryString(q);
            } else {
                v.setPath(pathAndQuery);
            }
        }
        return NOptional.of(v);
    }


    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public NConnectionStringBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public NConnectionStringBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public NConnectionStringBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public DefaultNConnectionStringBuilder getRoot() {
        return copy().setPath("/");
    }

    @Override
    public DefaultNConnectionStringBuilder getParent() {
        String ppath = path;
        if (NBlankable.isBlank(ppath) || "/".equals(ppath)) {
            return null;
        }
        while (ppath.endsWith("/")) {
            ppath = ppath.substring(0, ppath.length() - 1);
        }
        if (ppath.isEmpty()) {
            return copy().setPath("/");
        }
        int i = ppath.lastIndexOf('/');
        if (i <= 0) {
            ppath = "/";
        } else {
            ppath = ppath.substring(0, i + 1);
        }
        return copy().setPath(ppath);
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public DefaultNConnectionStringBuilder setPort(String port) {
        this.port = port;
        return this;
    }

    public String toString() {
        return build().toString();
    }

    @Override
    public NConnectionStringBuilder setQueryMap(Map<String, List<String>> queryMap) {
        this.queryMap = prepareQueryMap(queryMap, false);
        return this;
    }

    static Map<String, List<String>> prepareQueryMap(Map<String, List<String>> queryMap, boolean normalize) {
        if (queryMap == null) {
            return null;
        }
        if (normalize) {
            TreeMap<String, List<String>> queryMap2 = null;
            for (Map.Entry<String, List<String>> q : queryMap.entrySet()) {
                List<String> list = q.getValue();
                if (list != null) {
                    list = list.stream().filter(x -> x != null).collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        if (queryMap2 == null) {
                            queryMap2 = new TreeMap<>();
                        }
                        queryMap2.put(q.getKey(), list);
                    }
                }
            }
            return queryMap2;
        } else {
            LinkedHashMap<String, List<String>> queryMap2 = null;
            for (Map.Entry<String, List<String>> q : queryMap.entrySet()) {
                List<String> list = q.getValue();
                if (list != null) {
                    list = list.stream().filter(x -> x != null).collect(Collectors.toList());
                    if (!list.isEmpty()) {
                        if (queryMap2 == null) {
                            queryMap2 = new LinkedHashMap<>();
                        }
                        queryMap2.put(q.getKey(), list);
                    }
                }
            }
            return queryMap2;
        }
    }

    static Map<String, List<String>> deserializeQueryMap(String queryString) {
        if (NBlankable.isBlank(queryString)) {
            return null;
        }
        NOptional<Map<String, List<String>>> qq = NStringMapFormat.URL_FORMAT.parseDuplicates(queryString);
        return qq.map(
                x -> {
                    Map<String, List<String>> r = new LinkedHashMap<>();
                    for (Map.Entry<String, List<String>> ee : x.entrySet()) {
                        r.put(safeUrlDecode(ee.getKey()),
                                Collections.unmodifiableList(ee.getValue().stream().map(DefaultNConnectionStringBuilder::safeUrlDecode).collect(Collectors.toList()))
                        );
                    }
                    return Collections.unmodifiableMap(r);
                }
        ).orElse(null);
    }

    static String serializeQueryMap(Map<String, List<String>> queryMap) {
        if (queryMap != null) {
            NStringBuilder sb = new NStringBuilder();
            for (Map.Entry<String, List<String>> e : queryMap.entrySet()) {
                String k = e.getKey();
                if (k != null && !k.isEmpty()) {
                    List<String> v = e.getValue();
                    if (v != null) {
                        v = v.stream().filter(y -> y != null).collect(Collectors.toList());
                    } else {
                        v = new ArrayList<>();
                    }
                    if (v.isEmpty()) {
                        v.add("");
                    }
                    for (String s : v) {
                        if (!sb.isEmpty()) {
                            sb.append("&");
                        }
                        sb.append(safeUrlEncode(k));
                        sb.append("=");
                        sb.append(safeUrlEncode(s));
                    }
                }
            }
            if (!sb.isEmpty()) {
                return sb.toString();
            } else {
                return "";
            }
        } else {
            return null;
        }
    }

    @Override
    public NOptional<Map<String, List<String>>> getQueryMap() {
        return NOptional.ofNamed(queryMap, "queryMap");
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNConnectionStringBuilder that = (DefaultNConnectionStringBuilder) o;
        return Objects.equals(protocol, that.protocol) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(path, that.path) && Objects.equals(queryMap, that.queryMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, userName, password, host, port, path, queryMap);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public DefaultNConnectionStringBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public DefaultNConnectionStringBuilder setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public String getQueryString() {
        return serializeQueryMap(queryMap);
    }

    @Override
    public NConnectionStringBuilder setQueryString(String queryString) {
        this.queryMap = deserializeQueryMap(queryString);
        return this;
    }

    @Override
    public NConnectionStringBuilder setQueryParam(String param, String value) {
        if (param == null) {
            param = "";
        }
        if (value == null) {
            if (queryMap != null) {
                queryMap.remove(param);
            }
        } else {
            if (queryMap == null) {
                queryMap = new LinkedHashMap<>();
                List<String> a = new ArrayList<>();
                a.add(value);
                queryMap.put(param, a);
            } else {
                List<String> a = queryMap.computeIfAbsent(param, n -> new ArrayList<>());
                if (a.size() == 1 && Objects.equals(value, a.get(0))) {
                    //do nothing
                } else if (a.isEmpty()) {
                    a.add(value);
                } else {
                    a.clear();
                    a.add(value);
                }
            }
        }
        return this;
    }

    @Override
    public NConnectionStringBuilder addQueryParam(String param, String value) {
        if (param == null) {
            param = "";
        }
        if (value == null) {
            return this;
        } else {
            if (queryMap == null) {
                queryMap = new LinkedHashMap<>();
                List<String> a = new ArrayList<>();
                a.add(value);
                queryMap.put(param, a);
            } else {
                List<String> a = queryMap.computeIfAbsent(param, n -> new ArrayList<>());
                a.add(value);
            }
        }
        return this;
    }

    @Override
    public NConnectionStringBuilder addUniqueQueryParam(String param, String value) {
        if (param == null) {
            param = "";
        }
        if (value == null) {
            return this;
        } else {
            if (queryMap == null) {
                queryMap = new LinkedHashMap<>();
                List<String> a = new ArrayList<>();
                a.add(value);
                queryMap.put(param, a);
            } else {
                List<String> a = queryMap.computeIfAbsent(param, n -> new ArrayList<>());
                if (!a.contains(value)) {
                    a.add(value);
                }
            }
        }
        return this;
    }

    @Override
    public NConnectionStringBuilder clearQueryParam(String param) {
        if (param == null) {
            param = "";
        }
        if (queryMap != null) {
            queryMap.remove(param);
        }
        return this;
    }

    @Override
    public NOptional<String> getQueryParam(String param) {
        if (param == null) {
            param = "";
        }
        String finalParam = param;
        return NOptional.ofFirst(getQueryParams(param), () -> NMsg.ofC("missing '%s'", finalParam));
    }

    @Override
    public List<String> getQueryParams(String param) {
        if (queryMap != null) {
            if (param == null) {
                param = "";
            }
            List<String> all = queryMap.get(param);
            if (all != null) {
                return new ArrayList<>(all);
            }
        }
        return Collections.emptyList();
    }

    @Override
    public NConnectionStringBuilder setNormalized(boolean normalized) {
        this.normalized = normalized;
        return this;
    }

    @Override
    public boolean isNormalized() {
        return normalized;
    }

    @Override
    public DefaultNConnectionStringBuilder copy() {
        try {
            DefaultNConnectionStringBuilder c = (DefaultNConnectionStringBuilder) clone();
            c.queryMap = prepareQueryMap(queryMap, false);
            return c;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<String> getNames() {
        return NStringUtils.split(path, "/", true, true)
                .stream().map(s -> s).collect(Collectors.toList());
    }

    private static String safeUrlDecode(String s) {
        try {
            return URLDecoder.decode(s == null ? "" : s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String safeUrlEncode(String s) {
        try {
            return URLEncoder.encode(s == null ? "" : s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public DefaultNConnectionStringBuilder resolve(String child) {
        if (!NBlankable.isBlank(child)) {
            return copy().setPath(NStringUtils.pjoin("/", path, child));
        }
        return this;
    }

}
