package net.thevpc.nuts.net;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;

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

    public DefaultNConnectionString(String protocol, String userName, String password, String host, String port, String path, String queryString, Map<String, List<String>> queryMap) {
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
    public NConnectionString normalize() {
        TreeMap<String, List<String>> queryMap2 = null;
        boolean someChanges = false;
        if (queryMap != null) {
            for (Map.Entry<String, List<String>> q : queryMap.entrySet()) {
                List<String> list = q.getValue();
                if (queryMap2 == null) {
                    queryMap2 = new TreeMap<>();
                }
                queryMap2.put(q.getKey(), list);
            }
        }
        if (!someChanges && !Objects.equals(this.queryMap, queryMap2)) {
            someChanges = true;
        }
        String queryString2 = null;
        if (queryMap2 != null) {
            queryString2 = NStringMapFormat.URL_FORMAT.formatDuplicates(queryMap2);
        }
        if (!someChanges && !Objects.equals(this.queryString, queryString2)) {
            someChanges = true;
        }

        String protocol2 = NStringUtils.trimToNull(protocol);
        if (!someChanges && !Objects.equals(this.protocol, protocol2)) {
            someChanges = true;
        }

        String userName2 = NStringUtils.trimToNull(userName);
        if (!someChanges && !Objects.equals(this.userName, userName2)) {
            someChanges = true;
        }

        String host2 = NStringUtils.trimToNull(host);
        if (!someChanges && !Objects.equals(this.host, host2)) {
            someChanges = true;
        }

        String port2 = NStringUtils.trimToNull(port);
        if (!someChanges && !Objects.equals(this.port, port2)) {
            someChanges = true;
        }

        String path2 = NStringUtils.trimToNull(path);
        if (!someChanges && !Objects.equals(this.path, path2)) {
            someChanges = true;
        }

        if (!someChanges) {
            return this;
        }
        return new DefaultNConnectionString(protocol2, userName2, password, host2, port2, path2, queryString2, queryMap2);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNConnectionString that = (DefaultNConnectionString) o;
        return Objects.equals(protocol, that.protocol) && Objects.equals(userName, that.userName) && Objects.equals(password, that.password) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, userName, password, host, port, path);
    }


    @Override
    public NConnectionStringBuilder builder() {
        return new DefaultNConnectionStringBuilder(this);
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
    public NConnectionString getRoot() {
        return builder().setPath("/").build();
    }

    @Override
    public NConnectionString getParent() {
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
    public NConnectionString resolve(String child) {
        if (!NBlankable.isBlank(child)) {
            return builder().setPath(NStringUtils.pjoin("/", path, child)).build();
        }
        return this;
    }

    @Override
    public NConnectionString withPath(String path) {
        return builder().setPath(path).build();
    }
}
