package net.thevpc.nuts.runtime.standalone.net;

import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.util.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

@NImmutable
public class DefaultNConnectionString implements NConnectionString {

    private String protocol;
    private String userName;
    private String password;
    private String host;
    private String port;
    private String path;
    private String queryString;
    private Map<String, List<String>> queryMap;

    public DefaultNConnectionString() {
        this.queryMap = Collections.emptyMap();
    }

    public DefaultNConnectionString(String protocol, String userName, String password, String host, String port, String path, Map<String, List<String>> queryMap) {
        this.protocol = protocol;
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.queryMap = DefaultNConnectionStringBuilder.prepareQueryMap(queryMap, false);
        this.queryString = DefaultNConnectionStringBuilder.serializeQueryMap(this.queryMap);
    }

    public DefaultNConnectionString(String protocol, String userName, String password, String host, String port, String path, String queryString) {
        this.protocol = protocol;
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.queryString = queryString;
        this.queryMap = DefaultNConnectionStringBuilder.deserializeQueryMap(queryString);
    }

    @Override
    public NConnectionString normalize() {
        return builder().normalized(true).build();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNConnectionString that = (DefaultNConnectionString) o;
        return Objects.equals(protocol, that.protocol) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(path, that.path) && Objects.equals(queryString, that.queryString) && Objects.equals(queryMap, that.queryMap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, userName, password, host, port, path, queryString, queryMap);
    }

    @Override
    public NConnectionStringBuilder builder() {
        return new DefaultNConnectionStringBuilder(this);
    }

    @Override
    public String protocol() {
        return protocol;
    }

    @Override
    public String userName() {
        return userName;
    }

    @Override
    public String password() {
        return password;
    }

    @Override
    public String host() {
        return host;
    }

    @Override
    public String port() {
        return port;
    }

    @Override
    public String path() {
        return path;
    }

    @Override
    public String queryString() {
        return queryString;
    }

    @Override
    public NOptional<Map<String, List<String>>> queryMap() {
        return NOptional.ofNamed(queryMap, "queryMap");
    }

    @Override
    public NConnectionString root() {
        return builder().path("/").build();
    }

    @Override
    public NConnectionString parent() {
        String ppath = path;
        if (NBlankable.isBlank(ppath) || "/".equals(ppath)) {
            return null;
        }
        while (ppath.endsWith("/")) {
            ppath = ppath.substring(0, ppath.length() - 1);
        }
        if (ppath.isEmpty()) {
            return builder().path("/").build();
        }
        int i = ppath.lastIndexOf('/');
        if (i <= 0) {
            ppath = "/";
        } else {
            ppath = ppath.substring(0, i + 1);
        }
        return builder().path(ppath).build();
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
    public List<String> names() {
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
    public NConnectionString resolve(String child) {
        if (!NBlankable.isBlank(child)) {
            return builder().path(NStringUtils.pjoin("/", path, child)).build();
        }
        return this;
    }

    @Override
    public NConnectionString withPath(String path) {
        return builder().path(path).build();
    }
}
