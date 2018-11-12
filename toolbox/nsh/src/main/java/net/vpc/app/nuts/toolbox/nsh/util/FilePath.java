package net.vpc.app.nuts.toolbox.nsh.util;

import net.vpc.common.io.RuntimeIOException;
import net.vpc.common.io.URLUtils;
import net.vpc.common.strings.StringUtils;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FilePath {
    private String user;
    private String password;
    private String keyFile;
    private String protocol;
    private String host;
    private int port=-1;
    private String path;

    public FilePath(String protocol, String host, int port, String path, String user, String password, String keyFile) {
        this.user = user;
        this.password = password;
        this.keyFile = keyFile;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.path = path;
    }

    public FilePath(String path) {
        if (path.startsWith("ssh://")) {
            this.protocol = "ssh";
            Matcher m = Pattern.compile("^(?<protocol>(([a-zA-Z0-9_-]+)://))?((?<user>([^:?]+))@)?(?<host>[^:?/]+)(:(?<port>[0-9]+))?(?<path>(/[^?]*))(\\?(?<query>.+))?$").matcher(path);
            if(m.find()){
                user=m.group("user");
                host =m.group("host");
                port =m.group("port")==null?-1:Integer.parseInt(m.group("port"));
                this.path=m.group("path");
                String q=m.group("query");
                Map<String, String> qm = StringUtils.parseMap(q, "&");
                password=qm.get("password");
                keyFile=qm.get("key-file");
            }else{
                throw new IllegalArgumentException("Illegal ssh protocol format "+path);
            }
        } else if (path.startsWith("file:")) {
            File file = null;
            try {
                file = URLUtils.toFile(new URL(path));
            } catch (MalformedURLException e) {
                throw new RuntimeIOException(e);
            }
            this.protocol = "file";
            this.path = file.getPath();
        } else if (path.contains("://")) {
            this.protocol = "url";
            this.path = path;
        } else {
            this.protocol = "file";
            this.path = path;
        }
    }

    public FilePath(String protocol, String path) {
        this.protocol = protocol;
        this.path = path;
    }

    public String getPassword() {
        return password;
    }

    public String getKeyFile() {
        return keyFile;
    }

    public String getUser() {
        return user;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePath filePath = (FilePath) o;
        return port == filePath.port &&
                Objects.equals(user, filePath.user) &&
                Objects.equals(password, filePath.password) &&
                Objects.equals(keyFile, filePath.keyFile) &&
                Objects.equals(protocol, filePath.protocol) &&
                Objects.equals(host, filePath.host) &&
                Objects.equals(path, filePath.path);
    }

    @Override
    public int hashCode() {

        return Objects.hash(user, password, keyFile, protocol, host, port, path);
    }

    @Override
    public String toString() {
        return "FilePath{" +
                "user='" + user + '\'' +
                ", password='" + password + '\'' +
                ", keyFile='" + keyFile + '\'' +
                ", protocol='" + protocol + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                ", path='" + path + '\'' +
                '}';
    }


    public InputStream getInputStream() throws IOException {
        switch (getProtocol()) {
            case "file":
                return new FileInputStream(getPath());
            case "ssh":
                return new SShConnection(getUser(),getHost(),getPort(), getKeyFile(), getPassword()).getInputStream(getPath(), true);
            case "url":
                return new URL(getPath()).openStream();
        }
        throw new IOException("Unsupported protocol " + getProtocol());
    }

    public OutputStream getOutputStream() throws IOException {
        switch (getProtocol()) {
            case "file":
                return new FileOutputStream(getPath());
        }
        throw new IOException("Unsupported protocol " + getProtocol());
    }

    public void rm(boolean recurse) throws IOException {
        switch (getProtocol()) {
            case "file":
            {
                File from1 = new File(getPath());
                if (from1.isFile()) {
                    try {
                        Files.delete(from1.toPath());
                    } catch (IOException e) {
                        throw new RuntimeIOException(e);
                    }
                } else if (from1.isDirectory()) {
                    if (recurse) {
                        for (File file : from1.listFiles()) {
                            new FilePath("file", file.getPath()).rm(recurse);
                        }
                    }
                    try {
                        Files.delete(from1.toPath());
                    } catch (IOException e) {
                        throw new RuntimeIOException(e);
                    }
                }
                break;
            }
            case "ssh": {
                try(SShConnection session = new SShConnection(this)) {
                    session.rm(getPath(), recurse);
                }
                break;
            }
            case "url":
                throw new IOException("Unsupported protocol " + getProtocol());
        }
        throw new IOException("Unsupported protocol " + getProtocol());
    }

    public void mkdir(boolean parents) {
        switch (getProtocol()) {
            case "file":
                File from1 = new File(getPath());
                if (parents) {
                    from1.mkdirs();
                } else {
                    from1.mkdir();
                }
                break;
            case "ssh": {
                try (SShConnection c = new SShConnection(this)) {
                    c.mkdir(getPath(), parents);
                }
                break;
            }
            case "url":
                throw new RuntimeIOException("Unsupported protocols " + getProtocol());
            default:
                throw new RuntimeIOException("Unsupported protocols " + getProtocol());
        }
    }



}
