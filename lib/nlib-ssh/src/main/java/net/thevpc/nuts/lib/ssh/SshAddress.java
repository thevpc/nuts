package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.NutsUtilStrings;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SshAddress {
    private static Pattern pattern = Pattern.compile("^(?<protocol>(([a-zA-Z0-9_-]+)://))?((?<user>([^:?]+))@)?(?<host>[^:?]+)(:(?<port>[0-9]+))?(\\?(?<query>.+))?$");
    private String user = null;
    private String host = null;
    private String password = null;
    private String keyFile = null;
    private int port = -1;

    public SshAddress(String url) {
        Matcher m = pattern.matcher(url);
        if (m.find()) {
            String protocol=m.group("protocol");
            if(protocol!=null && !protocol.equals("ssh://")){
                throw new IllegalArgumentException("Illegal ssh protocol format "+url);
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

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public int getPort() {
        return port;
    }

    public SshPath getPath(String path) {
        return new SshPath(
                user,host, port, keyFile, password, path
        );
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ssh://");
        if (!NutsUtilStrings.isBlank(user)) {
            sb.append(user).append("@");
        }
        sb.append(host);
        if (port >= 0) {
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
