package net.thevpc.nuts.lib.ssh;


import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SshPath {
    private static Pattern pattern = Pattern.compile("^(?<protocol>(([a-zA-Z0-9_-]+)://))?((?<user>([^:?]+))@)?(?<host>[^:?/]+)(:(?<port>[0-9]+))?(?<path>([^?]*))(\\?(?<query>.+))?$");
    private String user = null;
    private String host = null;
    private String password = null;
    private String keyFile = null;
    private String path = null;
    private int port = -1;

    public SshPath(String url) {
        Matcher m = pattern.matcher(url);
        if(m.find()){
            String protocol=m.group("protocol");
            if(!protocol.equals("ssh://")){
                throw new IllegalArgumentException("Illegal ssh protocol format "+url);
            }
            user=m.group("user");
            host =m.group("host");
            port =m.group("port")==null?-1:Integer.parseInt(m.group("port"));
            this.path=m.group("path");
            String q=m.group("query");
            Map<String, String> qm = _StringUtils.parseMap(q, "&");
            password=qm.get("password");
            keyFile=qm.get("key-file");
        }else{
            throw new IllegalArgumentException("Illegal ssh protocol format "+url);
        }
    }

    public SshPath(String user, String host, int port, String keyFile, String password,String path) {
        this.user = user;
        this.host = host;
        this.port = port;
        this.keyFile = keyFile;
        this.password = password;
        this.path = path;
    }

    public SshPath setUser(String user) {
        return new SshPath(user, host, port, keyFile, password,path);
    }

    public SshPath setHost(String host) {
        return new SshPath(user, host, port, keyFile, password,path);
    }

    public SshPath setPassword(String password) {
        return new SshPath(user, host, port, keyFile, password,path);
    }

    public SshPath setKeyFile(String keyFile) {
        return new SshPath(user, host, port, keyFile, password,path);
    }

    public SshPath setPath(String path) {
        return new SshPath(user, host, port, keyFile, password,path);
    }

    public SshPath setPort(int port) {
        return new SshPath(user, host, port, keyFile, password,path);
    }

    public SshAddress toAddress(){
        return new SshAddress(
                user,host,port,keyFile,password
        );
    }

    public String getPath() {
        return path;
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

    public String toString() {
        StringBuilder sb = new StringBuilder("ssh://");
        if (!(user==null || user.trim().length()==0)) {
            sb.append(user).append("@");
        }
        sb.append(host);
        if (port >= 0) {
            sb.append(":").append(port);
        }
        if (!path.startsWith("/")) {
            sb.append('/');
        }
        sb.append(path);
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
