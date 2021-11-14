package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class InvalidFilePath implements NutsPathSPI {

    private final String value;
    private final NutsSession session;

    public InvalidFilePath(String value, NutsSession session) {
        this.value = value == null ? "" : value;
        this.session = session;
    }

    private NutsPath toNutsPathInstance() {
        return new NutsPathFromSPI(this);
    }

    @Override
    public NutsStream<NutsPath> list() {
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        return new MyPathFormat(this);
    }

    @Override
    public String getName() {
        String[] pa = asPathArray();
        if (pa.length == 0) {
            return "";
        }
        return pa[pa.length - 1];
    }

    @Override
    public String getProtocol() {
        return "";
    }

    @Override
    public NutsPath resolve(String path) {
        String b = value;
        if (b.endsWith("/") || b.endsWith("\\")) {
            return NutsPath.of(b + path, session);
        }
        return NutsPath.of(b + "/" + path, session);
    }

    @Override
    public NutsPath resolve(NutsPath path) {
        return resolve(path == null ? null : path.toString());
    }

    @Override
    public NutsPath resolveSibling(String path) {
        if (path == null || path.isEmpty()) {
            return getParent();
        }
        if (isRoot()) {
            return NutsPath.of("/" + path, session);
        }
        return getParent().resolve(path);
    }

    @Override
    public NutsPath resolveSibling(NutsPath path) {
        return resolveSibling(path == null ? null : path.toString());
    }

    @Override
    public NutsPath toCompressedForm() {
        return null;
    }

    @Override
    public URL toURL() {
        try {
            if (DefaultNutsWorkspaceConfigModel.MOSTLY_URL_PATTERN.matcher(value).matches()) {
                return new URL(value);
            }
            return new URL("file:" + value);
        } catch (MalformedURLException e) {
            throw new NutsIOException(session, e);
        }
    }

    @Override
    public Path toFile() {
        try {
            return Paths.get(value);
        } catch (Exception ex) {
            throw new NutsIOException(session, ex);
        }
    }

    @Override
    public boolean isSymbolicLink() {
        return false;
    }

    @Override
    public boolean isOther() {
        return false;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public boolean isRegularFile() {
        return false;
    }

    public boolean exists() {
        return false;
    }

    public long getContentLength() {
        return -1;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getLocation() {
        return value;
    }

    public InputStream getInputStream() {
        throw new NutsIOException(session, NutsMessage.cstyle("path not found %s", this));
    }

    public OutputStream getOutputStream() {
        throw new NutsIOException(session, NutsMessage.cstyle("path not found %s", this));
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(boolean recurse) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete path %s", this));
    }

    @Override
    public void mkdir(boolean parents) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to create folder out of regular file %s", this));
    }

    @Override
    public Instant getLastModifiedInstant() {
        return null;
    }

    @Override
    public Instant getLastAccessInstant() {
        return null;
    }

    @Override
    public Instant getCreationInstant() {
        return null;
    }

    @Override
    public NutsPath getParent() {
        if (isRoot()) {
            return null;
        }
        StringBuilder sb = new StringBuilder(value);
        while (sb.length() > 0 && (sb.charAt(sb.length() - 1) == '/' || sb.charAt(sb.length() - 1) == '\\')) {
            sb.delete(sb.length() - 1, sb.length());
        }
        int x = value.lastIndexOf('/');
        int y = value.lastIndexOf('\\');
        if (y < x && y >= 0) {
            x = y;
        }
        if (x >= 0) {
            return NutsPath.of(sb.substring(0, x), getSession());
        }
        return null;
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath) {
        if (isAbsolute()) {
            return new NutsPathFromSPI(this);
        }
        if (basePath == null) {
            List<String> p = new ArrayList<>();
            p.addAll(Arrays.asList(asPathArray(System.getProperty("user.home"))));
            p.addAll(Arrays.asList(asPathArray(value)));
            return new NutsPathFromSPI(new InvalidFilePath(
                    "/" + String.join("/", p.toArray(new String[0]))
                    , session));
        }
        return basePath.toAbsolute().resolve(toString());
    }

    @Override
    public NutsPath normalize() {
        if (isAbsolute()) {
            return new NutsPathFromSPI(new InvalidFilePath(
                    "/" + String.join("/", normalizePath(asPathArray()))
                    , session));
        }
        List<String> p = new ArrayList<>();
        p.addAll(Arrays.asList(asPathArray(System.getProperty("user.home"))));
        p.addAll(Arrays.asList(asPathArray(value)));
        return new NutsPathFromSPI(new InvalidFilePath(
                "/" + String.join("/", normalizePath(p.toArray(new String[0])))
                , session));
    }

    @Override
    public boolean isAbsolute() {
        if (
                value.startsWith("/")
                        || value.startsWith("\\")
        ) {
            return true;
        }
        return DefaultNutsWorkspaceConfigModel.MOSTLY_URL_PATTERN.matcher(value).matches();
    }

    @Override
    public String owner() {
        return null;
    }

    @Override
    public String group() {
        return null;
    }

    @Override
    public Set<NutsPathPermission> permissions() {
        Set<NutsPathPermission> p = new LinkedHashSet<>();
        return Collections.unmodifiableSet(p);
    }

    @Override
    public void setPermissions(NutsPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NutsPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NutsPathPermission... permissions) {
    }

    @Override
    public boolean isName() {
        String[] pa = asPathArray();
        if (pa.length == 0) {
            return true;
        }
        if (pa.length > 1) {
            return false;
        }
        String v = pa[0];
        switch (v) {
            case "/":
            case "\\":
            case ".":
            case "..": {
                return false;
            }
        }
        for (char c : v.toCharArray()) {
            switch (c) {
                case '/':
                case '\\': {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int getPathCount() {
        String[] pa = asPathArray();
        return pa.length == 0 ? 1 : pa.length;
    }

    @Override
    public boolean isRoot() {
        return asPathArray().length == 0 && (value.contains("/") || value.contains("\\"));
    }

    @Override
    public NutsStream<NutsPath> walk(int maxDepth, NutsPathVisitOption[] options) {
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsPath subpath(int beginIndex, int endIndex) {
        String[] a = asPathArray();
        return NutsPath.of(String.join("/", Arrays.copyOfRange(a, beginIndex, endIndex)), getSession());
    }

    @Override
    public String[] getItems() {
        return asPathArray();
    }

    private String[] normalizePath(String[] aa) {
        List<String> p = new ArrayList<>(Arrays.asList(aa));
        for (int i = p.size() - 1; i >= 0; i--) {
            switch (p.get(i)) {
                case ".": {
                    p.remove(i);
                    i++;
                    break;
                }
                case "..": {
                    p.remove(i);
                    if (i > 0) {
                        p.remove(i);
                    }
                    break;
                }
                default: {
                    //do nothing
                }
            }
        }
        return p.toArray(new String[0]);
    }

    private String[] asPathArray(String s) {
        return Arrays.stream(value.split("[/\\\\]"))
                .filter(x -> x.length() == 0)
                .toArray(String[]::new)
                ;
    }

    private String[] asPathArray() {
        return asPathArray(value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InvalidFilePath urlPath = (InvalidFilePath) o;
        return Objects.equals(value, urlPath.value);
    }

    @Override
    public String toString() {
        return value;
    }

    private static class MyPathFormat implements NutsFormatSPI {

        private final InvalidFilePath p;

        public MyPathFormat(InvalidFilePath p) {
            this.p = p;
        }

        public NutsString asFormattedString() {
            return NutsTexts.of(p.getSession()).toText(p.value);
        }

        @Override
        public void print(NutsPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NutsCommandLine commandLine) {
            return false;
        }
    }
}
