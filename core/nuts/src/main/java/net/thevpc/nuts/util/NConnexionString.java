package net.thevpc.nuts.util;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NConnexionString implements Cloneable {
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
    private String user;
    private String password;
    private String host;
    private String port;
    private String path;
    private String queryString;

    public static NOptional<NConnexionString> of(String value) {
        if (value == null || NBlankable.isBlank(value)) {
            return NOptional.ofNamedEmpty("Connexion String");
        }
        value = value.trim();
        NConnexionString v = new NConnexionString();
        Matcher e = PROTOCOLE_PATTERN.matcher(value);
        String protocol;
        String path;
        if (e.matches()) {
            protocol = e.group("protocol");
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
                v.setUser(matcher.group("user"));
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
            }else {
                v.setPath(pathAndQuery);
            }
        }
        return NOptional.of(v);
    }

    public NConnexionString() {
    }


    public String getUser() {
        return user;
    }

    public NConnexionString setUser(String user) {
        this.user = user;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NConnexionString setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getHost() {
        return host;
    }

    public NConnexionString setHost(String host) {
        this.host = host;
        return this;
    }

    public String getPort() {
        return port;
    }

    public NConnexionString setPort(String port) {
        this.port = port;
        return this;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        boolean fileProtocol = "file".equals(protocol);
        if (protocol != null && protocol.length() > 0) {
            sb.append(NStringUtils.trim(protocol)).append(":");
            if (!fileProtocol) {
                sb.append("//");
                if (!NBlankable.isBlank(user)) {
                    sb.append(NStringUtils.trim(user));
                    if (!NBlankable.isBlank(password)) {
                        sb.append(':');
                        sb.append(password);
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
                    sb.append(port);
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
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NConnexionString that = (NConnexionString) o;
        return Objects.equals(protocol, that.protocol) && Objects.equals(user, that.user) && Objects.equals(password, that.password) && Objects.equals(host, that.host) && Objects.equals(port, that.port) && Objects.equals(path, that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, user, password, host, port, path);
    }

    public String getPath() {
        return path;
    }

    public NConnexionString setPath(String path) {
        this.path = path;
        return this;
    }

    public String getProtocol() {
        return protocol;
    }

    public NConnexionString setProtocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public String getQueryString() {
        return queryString;
    }

    public NConnexionString setQueryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public NConnexionString copy() {
        try {
            return (NConnexionString) clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}
