package net.thevpc.nuts.toolbox.ndb.util;

import net.thevpc.nuts.NBlankable;
import net.thevpc.nuts.NOptional;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginServerPath {
    private static Pattern PATTERN = Pattern.compile("((?<user>([a-z]+))(:(?<password>([a-z]+)))@)(?<server>([a-z0-9.-_]+))(:(?<port>([0-9]+)))(:(?<path>(/.*)))");
    private String user;
    private String password;
    private String server;
    private Integer port;
    private String path;

    public static NOptional<LoginServerPath> parse(String value) {
        if (value == null || NBlankable.isBlank(value)) {
            return NOptional.ofNamedEmpty("LoginServerPath");
        }
        value = value.trim();
        Matcher matcher = PATTERN.matcher(value);
        if (matcher.matches()) {
            LoginServerPath v = new LoginServerPath();
            String portString = matcher.group("port");
            v.setServer(matcher.group("server"));
            v.setUser(matcher.group("user"));
            v.setPassword(matcher.group("password"));
            v.setPort((portString == null || portString.isEmpty()) ? null : Integer.parseInt(portString));
            v.setPath(matcher.group("path"));
            return NOptional.ofNamed(v, "url");
        }
        return NOptional.ofNamedEmpty("url");
    }

    public LoginServerPath() {
    }

    public Integer getPort() {
        return port;
    }

    public LoginServerPath setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginServerPath setPassword(String password) {
        this.password = password;
        return this;
    }

    public String getUser() {
        return user;
    }

    public LoginServerPath setUser(String user) {
        this.user = user;
        return this;
    }

    public String getServer() {
        return server;
    }

    public LoginServerPath setServer(String server) {
        this.server = server;
        return this;
    }

    public String getPath() {
        return path;
    }

    public LoginServerPath setPath(String path) {
        this.path = path;
        return this;
    }

}
