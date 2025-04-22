package net.thevpc.nuts.util;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultNConnexionString implements NConnexionString {

    private String protocol;
    private String userName;
    private String password;
    private String host;
    private String port;
    private String path;
    private String queryString;
    private Map<String, List<String>> queryMap;

    public DefaultNConnexionString() {
        this.queryMap = Collections.emptyMap();
    }

    public DefaultNConnexionString(String protocol, String userName, String password, String host, String port, String path, String queryString, Map<String, List<String>> queryMap) {
        this.protocol = protocol;
        this.userName = userName;
        this.password = password;
        this.host = host;
        this.port = port;
        this.path = path;
        this.queryString = queryString;
        this.queryMap = queryMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNConnexionString that = (DefaultNConnexionString) o;
        return Objects.equals(protocol, that.protocol) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, userName, password, host, port, path);
    }


    @Override
    public NConnexionStringBuilder builder() {
        return new DefaultNConnexionStringBuilder(this);
    }

    @Override
    public String getProtocol() {
        return protocol;
    }

    @Override
    public String getUserName() {
        return userName;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getHost() {
        return host;
    }

    @Override
    public String getPort() {
        return port;
    }

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public NOptional<Map<String, List<String>>> getQueryMap() {
        return NOptional.ofNamed(queryMap, "queryMap");
    }

    @Override
    public NConnexionString getRoot() {
        return builder().setPath("/").build();
    }

    @Override
    public NConnexionString getParent() {
        String ppath = path;
        if (NBlankable.isBlank(ppath) || "/".equals(ppath)) {
            return null;
        }
        while (ppath.endsWith("/")) {
            ppath = ppath.substring(0, ppath.length() - 1);
        }
        if (ppath.isEmpty()) {
            return builder().setPath("/").build();
        }
        int i = ppath.lastIndexOf('/');
        if (i <= 0) {
            ppath = "/";
        } else {
            ppath = ppath.substring(0, i + 1);
        }
        return builder().setPath(ppath).build();
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
    public NConnexionString resolve(String child) {
        if (!NBlankable.isBlank(child)) {
            return builder().setPath(NStringUtils.pjoin("/", path, child)).build();
        }
        return this;
    }
}
