package net.thevpc.nuts.boot.reserved.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NBootPath {
    private String path;
    private NRef<Path> file;
    private NRef<URL> url;
    private NRef<Boolean> remote;

    public NBootPath(String path) {
        if (NBootUtils.isBlank(path)) {
            this.path = null;
            this.url = NRef.of(null);
            this.file = NRef.of(null);
        } else {
            this.path = path;
        }
    }

    public NBootPath(URL path) {
        if (path == null) {
            this.path = null;
            this.url = NRef.of(null);
            this.file = NRef.of(null);
        } else {
            this.path = path.toString();
            this.url = NRef.of(path);
            this.file = NRef.of(NBootUtils.toFile(path).toPath());
        }
    }

    public NBootPath(Path path) {
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

    public byte[] readAllBytes(NBootLog log) {
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
            return NBootUtils.loadStream(getInputSteam(log), log);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public InputStream getInputSteam(NBootLog log) {
        if (path == null) {
            return null;
        }
        return NBootUtils.openStream(getUrl(), log);
    }

    public Path getFile() {
        if (file == null) {
            file = new NRef<>();
            if (path != null) {
                try {
                    file.set(NBootUtils.toFile(path).toPath());
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
                    url.set(NBootUtils.urlOf(path));
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
            if (NBootUtils.isBlank(path)) {
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

    public NBootPath resolve(String s) {
        Path f = getFile();
        if (f != null) {
            return new NBootPath(f.resolve(s));
        }
        int o = s.indexOf('?');
        if (o >= 0) {
            return new NBootPath(path.substring(0, o) + "/" + s + path.substring(o));
        }
        return new NBootPath(path + "/" + s);
    }

    @Override
    public String toString() {
        return String.valueOf(path);
    }

    public NBootPath toAbsolute() {
        Path file = getFile();
        if(file!=null){
            return new NBootPath(file.normalize().toAbsolutePath());
        }
        return this;
    }

    public String getPath() {
        return path;
    }

    private static class NRef<T> {

        private T value;
        private boolean set;

        public static <T> NRef<T> of(T t) {
            return new NRef<>(t);
        }

        public static <T> NRef<T> of(T t, Class<T> type) {
            return new NRef<>(t);
        }

        public static <T> NRef<T> ofNull(Class<T> t) {
            return new NRef<>(null);
        }

        public static <T> NRef<T> ofNull() {
            return of(null);
        }

        public NRef() {
        }

        public NRef(T value) {
            this.value = value;
        }

        public T get() {
            return value;
        }

        public T orElse(T other) {
            if (value == null) {
                return other;
            }
            return value;
        }

        public void setNonNull(T value) {
            if (value != null) {
                set(value);
            }
        }

        public void set(T value) {
            this.value = value;
            this.set = true;
        }

        public void unset() {
            this.value = null;
            this.set = false;
        }

        public boolean isNotNull() {
            return value != null;
        }

        public boolean isEmpty() {
            return value == null || String.valueOf(value).isEmpty();
        }

        public boolean isNull() {
            return value == null;
        }

        public boolean isSet() {
            return set;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public boolean isValue(Object o) {
            return Objects.equals(value, o);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            NRef<?> nRef = (NRef<?>) o;
            return set == nRef.set && Objects.equals(value, nRef.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, set);
        }
    }
}
