package net.thevpc.nuts.reserved.io;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NRef;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NReservedPath {
    private String path;
    private NRef<Path> file;
    private NRef<URL> url;
    private NRef<Boolean> remote;

    public NReservedPath(String path) {
        if (NBlankable.isBlank(path)) {
            this.path = null;
            this.url = NRef.of(null);
            this.file = NRef.of(null);
        } else {
            this.path = path;
        }
    }

    public NReservedPath(URL path) {
        if (path == null) {
            this.path = null;
            this.url = NRef.of(null);
            this.file = NRef.of(null);
        } else {
            this.path = path.toString();
            this.url = NRef.of(path);
            this.file = NRef.of(NReservedIOUtils.toFile(path).toPath());
        }
    }

    public NReservedPath(Path path) {
        if (path == null) {
            this.path = null;
            this.url = NRef.of(null);
            this.file = NRef.of(null);
        } else {
            this.path = path.toString();
            this.file = NRef.of(path);
            try {
                this.url = NRef.of(path.toUri().toURL());
            } catch (MalformedURLException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

    public byte[] readAllBytes(NLog log) {
        if (this.path == null) {
            return null;
        }
        Path f = getFile();
        try {
            if (f != null) {
                if (Files.isRegularFile(f)) {
                    return Files.readAllBytes(f);
                } else {
                    return null;
                }
            }
            return NReservedIOUtils.loadStream(getInputSteam(log), log);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public InputStream getInputSteam(NLog log) {
        if (path == null) {
            return null;
        }
        return NReservedIOUtils.openStream(getUrl(), log);
    }

    public Path getFile() {
        if (file == null) {
            file = new NRef<>();
            if (path != null) {
                try {
                    file.set(NReservedIOUtils.toFile(path).toPath());
                } catch (Exception e) {
                    //
                }
            }
        }
        return file.get();
    }

    public URL getUrl() {
        if (url == null) {
            url = new NRef<>();
            if (path != null) {
                try {
                    url.set(new URL(path));
                } catch (Exception e) {
                    //
                }
            }
        }
        return url.get();
    }

    public boolean isRemote() {
        if (remote == null) {
            remote = new NRef<>();
            if (NBlankable.isBlank(path)) {
                remote.set(false);
                return false;
            }
            int i = path.indexOf(':');
            if (i >= 0) {
                String protocol = path.substring(0, i);
                if (protocol.length() == 1) {
                    // this is a file
                    remote.set(false);
                    return false;
                } else {
                    String sPath = path.substring(i + 1);
                    if (protocol.equalsIgnoreCase("http") || protocol.equalsIgnoreCase("https")) {
                        if (sPath.startsWith("//")) {
                            sPath = sPath.substring(2);
                            Pattern p = Pattern.compile("^(?<n>[^!:/\\\\?#]*).*");
                            Matcher m = p.matcher(sPath);
                            if (m.find()) {
                                String k = m.group("n");
                                if (k != null) {
                                    if (k.startsWith("127.")) {
                                        remote.set(false);
                                        return false;
                                    }
                                    if (k.equalsIgnoreCase("localhost")) {
                                        remote.set(false);
                                        return false;
                                    }
                                }
                            }
                        }
                        remote.set(true);
                        return true;
                    } else {
                        remote.set(true);
                        return true;
                    }
                }
            }
            remote.set(false);
            return false;

        }
        return remote.get();
    }

    public String getName() {
        Path f = getFile();
        if (f != null) {
            return f.normalize().getFileName().toString();
        }
        URL u = getUrl();
        String z = path;
        if (u != null) {
            z = u.getFile();
        }
        int o = z.lastIndexOf('/');
        if (o >= 0) {
            return z.substring(o + 1);
        }
        return z;
    }

    public NReservedPath resolve(String s) {
        Path f = getFile();
        if (f != null) {
            return new NReservedPath(f.resolve(s));
        }
        int o = s.indexOf('?');
        if (o >= 0) {
            return new NReservedPath(path.substring(0, o) + "/" + s + path.substring(o));
        }
        return new NReservedPath(path + "/" + s);
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }

    public NReservedPath toAbsolute() {
        Path file = getFile();
        if(file!=null){
            return new NReservedPath(file.normalize().toAbsolutePath());
        }
        return this;
    }

    public String getPath() {
        return path;
    }
}
