package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
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

    @Override
    public NutsStream<NutsPath> list(NutsPath basePath) {
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NutsPath basePath) {
        String[] pa = asPathArray();
        if (pa.length == 0) {
            return "";
        }
        return pa[pa.length - 1];
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return "";
    }

    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        String b = value;
        if (b.endsWith("/") || b.endsWith("\\")) {
            return NutsPath.of(b + path, session);
        }
        return NutsPath.of(b + "/" + path, session);
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        return resolve(basePath, path == null ? null : path.toString());
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        if (path == null || path.isEmpty()) {
            return getParent(basePath);
        }
        if (isRoot(basePath)) {
            return NutsPath.of("/" + path, session);
        }
        return getParent(basePath).resolve(path);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        return resolveSibling(basePath, path == null ? null : path.toString());
    }

    @Override
    public NutsPath toCompressedForm(NutsPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NutsPath basePath) {
        try {
            if (URLPath.MOSTLY_URL_PATTERN.matcher(value).matches()) {
                return new URL(value);
            }
            return new URL("file:" + value);
        } catch (MalformedURLException e) {
            throw new NutsIOException(session, e);
        }
    }

    @Override
    public Path toFile(NutsPath basePath) {
        try {
            return Paths.get(value);
        } catch (Exception ex) {
            throw new NutsIOException(session, ex);
        }
    }

    @Override
    public boolean isSymbolicLink(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        return false;
    }

    public boolean exists(NutsPath basePath) {
        return false;
    }

    public long getContentLength(NutsPath basePath) {
        return -1;
    }

    @Override
    public String getContentEncoding(NutsPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NutsPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NutsPath basePath) {
        return value;
    }

    public InputStream getInputStream(NutsPath basePath) {
        throw new NutsIOException(session, NutsMessage.cstyle("path not found %s", this));
    }

    public OutputStream getOutputStream(NutsPath basePath) {
        throw new NutsIOException(session, NutsMessage.cstyle("path not found %s", this));
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(NutsPath basePath, boolean recurse) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete path %s", this));
    }

    @Override
    public void mkdir(boolean parents, NutsPath basePath) {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to create folder out of regular file %s", this));
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        return null;
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        if (isRoot(basePath)) {
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
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        //invalid, always return basePath
        return basePath;
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        //invalid, always return basePath
        return basePath;
    }

    @Override
    public boolean isAbsolute(NutsPath basePath) {
        //invalid, always return false
        return false;
    }

    @Override
    public String owner(NutsPath basePath) {
        return null;
    }

    @Override
    public String group(NutsPath basePath) {
        return null;
    }

    @Override
    public Set<NutsPathPermission> getPermissions(NutsPath basePath) {
        Set<NutsPathPermission> p = new LinkedHashSet<>();
        return Collections.unmodifiableSet(p);
    }

    @Override
    public void setPermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NutsPath basePath, NutsPathPermission... permissions) {
    }

    @Override
    public boolean isName(NutsPath basePath) {
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
    public int getPathCount(NutsPath basePath) {
        String[] pa = asPathArray();
        return pa.length == 0 ? 1 : pa.length;
    }

    @Override
    public boolean isRoot(NutsPath basePath) {
        return asPathArray().length == 0 && (value.contains("/") || value.contains("\\"));
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex) {
        String[] a = asPathArray();
        return NutsPath.of(String.join("/", Arrays.copyOfRange(a, beginIndex, endIndex)), getSession());
    }

    @Override
    public String[] getItems(NutsPath basePath) {
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
        //invalid
        return new String[]{s};
//        return Arrays.stream(value.split("[/\\\\]"))
//                .filter(x -> x.length() == 0)
//                .toArray(String[]::new)
//                ;
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

    @Override
    public void moveTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        throw new NutsIOException(session,NutsMessage.cstyle("unable to move %s",this));
    }

    @Override
    public void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        throw new NutsIOException(session,NutsMessage.cstyle("unable to copy %s",this));
    }
    @Override
    public NutsPath getRoot(NutsPath basePath) {
        if(isRoot(basePath)){
            return basePath;
        }
        return basePath.getParent().getRoot();
    }

    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {

    }

    @Override
    public boolean isLocal(NutsPath basePath) {
        return true;
    }

    @Override
    public NutsPath toRelativePath(NutsPath basePath, NutsPath parentPath) {
        String child=basePath.getLocation();
        String parent=parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NutsPath.of(child,session);
        }
        return null;
    }
}
