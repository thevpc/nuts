package net.vpc.app.nuts.toolbox.nsh.util;

import net.vpc.app.nuts.toolbox.nsh.cmds.CpCommand;
import net.vpc.common.io.FileUtils;
import net.vpc.common.io.IOUtils;
import net.vpc.common.io.RuntimeIOException;
import net.vpc.common.io.URLUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class FilePath {
    private String user;
    private String protocol;
    private String server;
    private int port;
    private String path;

    public FilePath(String path) {
        if (path.startsWith("ssh://")) {
            this.protocol = "ssh";
            path = path.substring("ssh://".length());
            int x = path.indexOf("/");
            String userAndServerAndPort = x<0?path:path.substring(0, x);
            path = x<0?"":path.substring(x);
            x = userAndServerAndPort.indexOf(':');
            if (x > 0) {
                this.server = userAndServerAndPort.substring(0, x);
                this.port = Integer.parseInt(userAndServerAndPort.substring(x + 1));
            } else {
                this.server = userAndServerAndPort;
                this.port = 0;
            }
            x = this.server.indexOf("@");
            if (x > 0) {
                this.user = this.server.substring(0, x);
                this.server = this.server.substring(x + 1);
            }
            this.path = path;
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

    public String getUser() {
        return user;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public String getPath() {
        return path;
    }
}
