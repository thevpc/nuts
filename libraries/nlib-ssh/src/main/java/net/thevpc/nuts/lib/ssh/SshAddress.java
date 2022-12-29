package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.NBlankable;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SshAddress {
    private static final Pattern pattern = Pattern.compile("^(?<protocol>(([a-zA-Z0-9_-]+)://))?((?<user>([^:?]+))@)?(?<host>[^:?]+)(:(?<port>[0-9]+))?(\\?(?<query>.+))?$");
    private final String user;
    private final String host;
    private final String password;
    private final String keyFile;
    private final int port;

    public SshAddress(String url) {
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            String protocol = m.group("protocol");
            if (protocol != null && !protocol.equals("ssh://")) {
                throw new IllegalArgumentException("Illegal ssh protocol format " + url);
            }
            user = m.group("user");
            host = m.group("host");
            port = m.group("port") == null ? -1 : Integer.parseInt(m.group("port"));
            String q = m.group("query");
            Map<String, String> qm = _StringUtils.parseMap(q, ",");
            password = qm.get("password");
            keyFile = qm.get("key-file");
        } else {
            throw new IllegalArgumentException("Illegal ssh protocol format " + url);
        }
    }

    public SshAddress(String user, String host, int port, String keyFile, String password) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.keyFile = keyFile;
        this.password = password;
    }

    public String getUser() {
        return user;
    }

    public SshAddress setUser(String user) {
        return new SshAddress(user, host, port, keyFile, password);
    }

    public String getHost() {
        return host;
    }

    public SshAddress setHost(String host) {
        return new SshAddress(user, host, port, keyFile, password);
    }

    public String getPassword() {
        return password;
    }

    public SshAddress setPassword(String password) {
        return new SshAddress(user, host, port, keyFile, password);
    }

    public String getKeyFile() {
        return keyFile;
    }

    public SshAddress setKeyFile(String keyFile) {
        return new SshAddress(user, host, port, keyFile, password);
    }

    public int getPort() {
        return port;
    }

    public SshAddress setPort(int port) {
        return new SshAddress(user, host, port, keyFile, password);
    }

    public SshPath getPath(String path) {
        return new SshPath(
                user, host, port, keyFile, password, path
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ssh://");
        if (!NBlankable.isBlank(user)) {
            sb.append(user).append("@");
        }
        sb.append(host);
        if (port > 0) {
            sb.append(":").append(port);
        }
        if (password != null || keyFile != null) {
            sb.append("?");
            boolean first = true;
            if (password != null) {
                first = false;
                sb.append("password=").append(password);
            }
            if (keyFile != null) {
                if (!first) {
                    sb.append(',');
                }
                sb.append("key-file=").append(keyFile);
            }
        }
        return sb.toString();
    }

}
