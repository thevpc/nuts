package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

public class InvalidFilePath implements NPathSPI {

    private final String value;
    private final NWorkspace workspace;

    public InvalidFilePath(String value, NWorkspace workspace) {
        this.value = value == null ? "" : value;
        this.workspace = workspace;
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        return NStream.ofEmpty();
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

    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        String b = value;
        if (b.endsWith("/") || b.endsWith("\\")) {
            return NPath.of(b + path);
        }
        return NPath.of(b + "/" + path);
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
            return NPath.of("/" + path);
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
    public NOptional<URL> toURL(NPath basePath) {
        try {
            if (URLPath.MOSTLY_URL_PATTERN.matcher(value).matches()) {
                return NOptional.of(CoreIOUtils.urlOf(value));
            }
            return NOptional.of(CoreIOUtils.urlOf("file:" + value));
        } catch (Exception e) {
            return NOptional.ofNamedError(NMsg.ofC("not an url %s", value));
        }
    }

    @Override
    public NOptional<Path> toPath(NPath basePath) {
        try {
            return NOptional.of(Paths.get(value));
        } catch (Exception ex) {
            return NOptional.ofNamedError(NMsg.ofC("not an path %s", value));
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
    public String getCharset(NPath basePath) {
        return null;
    }

    @Override
    public String getLocation(NPath basePath) {
        return value;
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(NMsg.ofC("path not found %s", this));
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        throw new NIOException(NMsg.ofC("path not found %s", this));
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        throw new NIOException(NMsg.ofC("unable to delete path %s", this));
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        throw new NIOException(NMsg.ofC("unable to create folder out of regular file %s", this));
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
            return NPath.of(sb.substring(0, x));
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
    public int getNameCount(NPath basePath) {
        List<String> pa = asPathArray();
        return pa.size() == 0 ? 1 : pa.size();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        return asPathArray().size() == 0 && (value.contains("/") || value.contains("\\"));
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        return NStream.ofEmpty();
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        List<String> a = asPathArray();
        return NPath.of(String.join("/", a.subList(beginIndex, endIndex)));
    }

    @Override
    public List<String> getNames(NPath basePath) {
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

        public NText asFormattedString() {
            return NText.of(p.value);
        }

        @Override
        public void print(NPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(NMsg.ofC("unable to move %s",this));
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        throw new NIOException(NMsg.ofC("unable to copy %s",this));
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
    public boolean isEqOrDeepChildOf(NPath basePath,NPath other) {
        return toRelativePath(basePath, other)!=null;
    }
    @Override
    public boolean startsWith(NPath basePath, String other) {
        return startsWith(basePath,NPath.of(other));
    }

    @Override
    public boolean startsWith(NPath basePath, NPath other) {
        return toRelativePath(basePath,other)!=null;
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return true;
    }

    @Override
    public NPath toRelativePath(NPath basePath, NPath parentPath) {
        String child=basePath.getLocation();
        String parent=parentPath.getLocation();
        return NPath.of(NIOUtils.toRelativePath(child, parent));
    }

    @Override
    public byte[] getDigest(NPath basePath, String algo) {
        return null;
    }

    @Override
    public int compareTo(NPath basePath, NPath other) {
        return basePath.toString().compareTo(other.toString());
    }
}
