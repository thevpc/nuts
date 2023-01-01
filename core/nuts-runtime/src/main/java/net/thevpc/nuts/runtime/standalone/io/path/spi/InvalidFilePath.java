package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class InvalidFilePath implements NPathSPI {

    private final String value;
    private final NSession session;

    public InvalidFilePath(String value, NSession session) {
        this.value = value == null ? "" : value;
        this.session = session;
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NPath basePath) {
        List<String> pa = asPathArray();
        if (pa.size() == 0) {
            return "";
        }
        return pa.get(pa.size() - 1);
    }

    @Override
    public String getProtocol(NPath basePath) {
        return "";
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        String b = value;
        if (b.endsWith("/") || b.endsWith("\\")) {
            return NPath.of(b + path, session);
        }
        return NPath.of(b + "/" + path, session);
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return resolve(basePath, path == null ? null : path.toString());
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        if (path == null || path.isEmpty()) {
            return getParent(basePath);
        }
        if (isRoot(basePath)) {
            return NPath.of("/" + path, session);
        }
        return getParent(basePath).resolve(path);
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return resolveSibling(basePath, path == null ? null : path.toString());
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NPath basePath) {
        try {
            if (URLPath.MOSTLY_URL_PATTERN.matcher(value).matches()) {
                return new URL(value);
            }
            return new URL("file:" + value);
        } catch (MalformedURLException e) {
            throw new NIOException(session, e);
        }
    }

    @Override
    public Path toFile(NPath basePath) {
        try {
            return Paths.get(value);
        } catch (Exception ex) {
            throw new NIOException(session, ex);
        }
    }

    @Override
    public boolean isSymbolicLink(NPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        return false;
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        return false;
    }

    public boolean exists(NPath basePath) {
        return false;
    }

    public long getContentLength(NPath basePath) {
        return -1;
    }

    @Override
    public String getContentEncoding(NPath basePath) {
        return null;
    }

    @Override
    public String getContentType(NPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NPath basePath) {
        return value;
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("path not found %s", this));
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("path not found %s", this));
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        throw new NIOException(getSession(), NMsg.ofCstyle("unable to delete path %s", this));
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        throw new NIOException(getSession(), NMsg.ofCstyle("unable to create folder out of regular file %s", this));
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        return null;
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        return null;
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        return null;
    }

    @Override
    public NPath getParent(NPath basePath) {
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
            return NPath.of(sb.substring(0, x), getSession());
        }
        return null;
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        //invalid, always return basePath
        return basePath;
    }

    @Override
    public NPath normalize(NPath basePath) {
        //invalid, always return basePath
        return basePath;
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        //invalid, always return false
        return false;
    }

    @Override
    public String owner(NPath basePath) {
        return null;
    }

    @Override
    public String group(NPath basePath) {
        return null;
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        Set<NPathPermission> p = new LinkedHashSet<>();
        return Collections.unmodifiableSet(p);
    }

    @Override
    public void setPermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public void addPermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public void removePermissions(NPath basePath, NPathPermission... permissions) {
    }

    @Override
    public boolean isName(NPath basePath) {
        List<String> pa = asPathArray();
        if (pa.size() == 0) {
            return true;
        }
        if (pa.size() > 1) {
            return false;
        }
        String v = pa.get(0);
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
    public int getPathCount(NPath basePath) {
        List<String> pa = asPathArray();
        return pa.size() == 0 ? 1 : pa.size();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        return asPathArray().size() == 0 && (value.contains("/") || value.contains("\\"));
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        return NStream.ofEmpty(getSession());
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        List<String> a = asPathArray();
        return NPath.of(String.join("/", a.subList(beginIndex, endIndex)), getSession());
    }

    @Override
    public List<String> getItems(NPath basePath) {
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

    private List<String> asPathArray(String s) {
        //invalid
        return Arrays.asList(s);
//        return Arrays.stream(value.split("[/\\\\]"))
//                .filter(x -> x.length() == 0)
//                .toArray(String[]::new)
//                ;
    }

    private List<String> asPathArray() {
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

    private static class MyPathFormat implements NFormatSPI {

        private final InvalidFilePath p;
        @Override
        public String getName() {
            return "path";
        }

        public MyPathFormat(InvalidFilePath p) {
            this.p = p;
        }

        public NString asFormattedString() {
            return NTexts.of(p.getSession()).ofText(p.value);
        }

        @Override
        public void print(NOutStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCommandLine commandLine) {
            return false;
        }
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("unable to move %s",this));
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(session, NMsg.ofCstyle("unable to copy %s",this));
    }
    @Override
    public NPath getRoot(NPath basePath) {
        if(isRoot(basePath)){
            return basePath;
        }
        NPath r = basePath.getParent();
        if(r!=null) {
            return r.getRoot();
        }
        return null;
    }

    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {

    }

    @Override
    public boolean isLocal(NPath basePath) {
        return true;
    }

    @Override
    public NPath toRelativePath(NPath basePath, NPath parentPath) {
        String child=basePath.getLocation();
        String parent=parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NPath.of(child,session);
        }
        return null;
    }
}
