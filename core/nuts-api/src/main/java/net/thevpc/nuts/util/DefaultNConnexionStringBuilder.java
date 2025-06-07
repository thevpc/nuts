package net.thevpc.nuts.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DefaultNConnexionStringBuilder implements Cloneable, NConnexionStringBuilder {
    private static Pattern CONNEXION_PATTERN = Pattern.compile(
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
    private String queryString;

    public DefaultNConnexionStringBuilder() {
    }

    public DefaultNConnexionStringBuilder(NConnexionString other) {
        if (other != null) {
            this.protocol = other.getProtocol();
            this.userName = other.getUserName();
            this.password = other.getPassword();
            this.host = other.getHost();
            this.port = other.getPort();
            this.path = other.getPath();
            this.queryString = other.getQueryString();
        }
    }

    @Override
    public NConnexionString build() {
        return new DefaultNConnexionString(protocol, userName, password, host, port, path, queryString, getQueryMap().orNull());
    }

    public static NOptional<NConnexionStringBuilder> of(String value) {
        if (value == null || NBlankable.isBlank(value)) {
            return NOptional.ofNamedEmpty("Connexion String");
        }
        value = value.trim();
        DefaultNConnexionStringBuilder v = new DefaultNConnexionStringBuilder();
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
            matcher = CONNEXION_PATTERN.matcher(value);
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
    public NConnexionStringBuilder setUserName(String userName) {
        this.userName = userName;
        return this;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public NConnexionStringBuilder setPassword(String password) {
        this.password = password;
        return this;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public NConnexionStringBuilder setHost(String host) {
        this.host = host;
        return this;
    }

    @Override
    public DefaultNConnexionStringBuilder getRoot() {
        return copy().setPath("/");
    }

    @Override
    public DefaultNConnexionStringBuilder getParent() {
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
    public DefaultNConnexionStringBuilder setPort(String port) {
        this.port = port;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean fileProtocol = "file".equals(protocol);
        if (!NBlankable.isBlank(protocol)) {
            sb.append(safeUrlEncode(NStringUtils.trim(protocol))).append(":");
            if (!fileProtocol) {
                sb.append("//");
                if (!NBlankable.isBlank(userName)) {
                    sb.append(safeUrlEncode(NStringUtils.trim(userName)));
                    if (!NBlankable.isBlank(safeUrlEncode(password))) {
                        sb.append(':');
                        sb.append(safeUrlEncode(password));
                    }
                    sb.append('@');
                }
                if (NBlankable.isBlank(host)) {
                    sb.append("localhost");
                } else {
                    sb.append(NStringUtils.trim(host));
                }
                if (!NBlankable.isBlank(port)) {
                    sb.append(":");
                    sb.append(safeUrlEncode(port));
                }
            }
        } else {
            if (!NBlankable.isBlank(userName)) {
                sb.append(safeUrlEncode(NStringUtils.trim(userName)));
                if (!NBlankable.isBlank(password)) {
                    sb.append(':');
                    sb.append(safeUrlEncode(password));
                }
                if (!NBlankable.isBlank(host) || !NBlankable.isBlank(port)) {
                    sb.append('@');
                }
            }
            if (!NBlankable.isBlank(host) || !NBlankable.isBlank(port)) {
                if (NBlankable.isBlank(host)) {
                    sb.append("localhost");
                } else {
                    sb.append(safeUrlEncode(NStringUtils.trim(host)));
                }
                if (!NBlankable.isBlank(port)) {
                    sb.append(":");
                    sb.append(safeUrlEncode(port));
                }
            }
        }

        if (!NBlankable.isBlank(path)) {
            if (!fileProtocol) {
                if (
                        (sb.length() == 0 || sb.charAt(sb.length() - 1) != '/')
                                && (path.charAt(0) != '/')
                ) {
                    sb.append('/');
                }
            }
            sb.append(path);
        }
        if (!NBlankable.isBlank(queryString)) {
            sb.append("?").append(queryString);
        }
        return sb.toString();
    }

    @Override
    public NConnexionStringBuilder setQueryMap(Map<String, List<String>> queryMap) {
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
                this.queryString = sb.toString();
            } else {
                this.queryString = "";
            }
        } else {
            this.queryString = null;
        }
        return this;
    }

    @Override
    public NOptional<Map<String, List<String>>> getQueryMap() {
        if (NBlankable.isBlank(queryString)) {
            return NOptional.ofNamedEmpty("queryMap");
        }
        NOptional<Map<String, List<String>>> qq = NStringMapFormat.URL_FORMAT.parseDuplicates(queryString == null ? "" : queryString);
        return qq.map(
                x -> {
                    Map<String, List<String>> r = new LinkedHashMap<>();
                    for (Map.Entry<String, List<String>> ee : x.entrySet()) {
                        r.put(safeUrlDecode(ee.getKey()),
                                Collections.unmodifiableList(ee.getValue().stream().map(DefaultNConnexionStringBuilder::safeUrlDecode).collect(Collectors.toList()))
                        );
                    }
                    return Collections.unmodifiableMap(r);
                }
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNConnexionStringBuilder that = (DefaultNConnexionStringBuilder) o;
        return Objects.equals(protocol, that.protocol) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, userName, password, host, port, path);
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public DefaultNConnexionStringBuilder setPath(String path) {
        this.path = path;
        return this;
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public DefaultNConnexionStringBuilder setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public NConnexionStringBuilder setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    @Override
    public DefaultNConnexionStringBuilder copy() {
        try {
            return (DefaultNConnexionStringBuilder) clone();
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
    public DefaultNConnexionStringBuilder resolve(String child) {
        if (!NBlankable.isBlank(child)) {
            return copy().setPath(NStringUtils.pjoin("/", path, child));
        }
        return this;
    }
}
