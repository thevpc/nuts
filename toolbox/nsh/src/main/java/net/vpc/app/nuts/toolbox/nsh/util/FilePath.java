package net.vpc.app.nuts.toolbox.nsh.util;

import net.vpc.common.io.RuntimeIOException;
import net.vpc.common.io.URLUtils;
import net.vpc.common.ssh.*;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;

public abstract class FilePath {
    //    private String user;
//    private String password;
//    private String keyFile;
    private String protocol;
    //    private String host;
//    private int port=-1;
//    private String path;
    protected String path0;

//    public FilePath(String protocol, String host, int port, String path, String user, String password, String keyFile) {
//        this.user = user;
//        this.password = password;
//        this.keyFile = keyFile;
//        this.protocol = protocol;
//        this.host = host;
//        this.port = port;
//        this.path = path;
//    }


    public static FilePath of(String path) {
        if (path.startsWith("ssh://")) {
            return new SshFilePath(path);
        } else if (path.startsWith("file:")) {
            return new LocalFilePath(path);
        } else if (path.contains("://")) {
            return new URLFilePath(path);
        } else {
            return new LocalFilePath(path);
        }
    }

    protected FilePath(String path) {
        this.path0 = path;
    }

//    public String getPath() {
//        return protocol;
//    }


    public abstract InputStream getInputStream() throws IOException;

    public abstract OutputStream getOutputStream() throws IOException;

    public abstract void rm(boolean recurse) throws IOException;

    public abstract void mkdir(boolean parents);

    public abstract String getProtocol();

    public abstract String getPath();


    public static class LocalFilePath extends FilePath {
        private File file;

        public LocalFilePath(String path) {
            super(path);
            if (path.startsWith("file:")) {
                try {
                    file = URLUtils.toFile(new URL(path));
                } catch (MalformedURLException e) {
                    throw new RuntimeIOException(e);
                }
            } else {
                file = new File(path);
            }
        }

        @Override
        public String getPath() {
            return path0;
        }

        public String toString() {
            return file.getPath();
        }

        public InputStream getInputStream() throws IOException {
            return new FileInputStream(file);
        }

        public OutputStream getOutputStream() throws IOException {
            return new FileOutputStream(file);
        }

        @Override
        public void rm(boolean recurse) throws IOException {
            File from1 = ((File) file);
            if (from1.isFile()) {
                try {
                    Files.delete(from1.toPath());
                } catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            } else if (from1.isDirectory()) {
                if (recurse) {
                    for (File file : from1.listFiles()) {
                        FilePath.of(file.getPath()).rm(recurse);
                    }
                }
                try {
                    Files.delete(from1.toPath());
                } catch (IOException e) {
                    throw new RuntimeIOException(e);
                }
            }
        }

        @Override
        public void mkdir(boolean parents) {
            File from1 = file;
            if (parents) {
                from1.mkdirs();
            } else {
                from1.mkdir();
            }
        }

        @Override
        public String getProtocol() {
            return "file";
        }

        public File getFile() {
            return file;
        }
    }

    public static class SshFilePath extends FilePath {
        private SshPath sshPath;
        private SshListener listener;

        public SshFilePath(String path) {
            super(path);
            sshPath = new SshPath(path);
        }

        public SshListener getListener() {
            return listener;
        }

        public SshFilePath setListener(SshListener listener) {
            this.listener = listener;
            return this;
        }

        public String toString() {
            return toSshPath().toString();
        }

        public SshPath toSshPath() {
            return new SshPath(path0);
        }

        public SshAddress toSshAddress() {
            return toSshPath().toAddress();
        }

        public InputStream getInputStream() {
            return new SshFileInputStream(sshPath);
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Unsupported protocol " + getProtocol());
        }

        public void rm(boolean recurse) {
            try (SShConnection session = new SShConnection(toSshAddress())
                 .addListener(listener)
            ) {
                session.rm(toSshPath().getPath(), recurse);
            }
        }

        public void mkdir(boolean parents) {
            try (SShConnection c = new SShConnection(toSshAddress())
                    .addListener(listener)
            ) {
                c.mkdir(toSshPath().getPath(), parents);
            }
        }

        public String getProtocol() {
            return "ssh";
        }

        public SshPath getSshPath() {
            return sshPath;
        }

        @Override
        public String getPath() {
            return sshPath.getPath();
        }
    }

    public static class URLFilePath extends FilePath {
        private URL url;

        public URLFilePath(String path) {
            super(path);
            try {
                this.url = new URL(path);
            } catch (MalformedURLException e) {
                throw new RuntimeIOException(e);
            }
        }

        public String toString() {
            return url.toString();
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return url.openStream();
        }

        @Override
        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Unsupported protocol " + getProtocol());
        }

        public void rm(boolean recurse) throws IOException {
            throw new IOException("Unsupported protocol " + getProtocol());
        }

        public void mkdir(boolean parents) {
            throw new RuntimeIOException("Unsupported protocols " + getProtocol());
        }

        public String getProtocol() {
            return "url";
        }

        public URL getURL() {
            return url;
        }

        @Override
        public String getPath() {
            return url.getFile();
        }
    }

}
