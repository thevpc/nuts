package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPathPermission;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NStream;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public abstract class AbstractPathSPIAdapter implements NPathSPI {

    protected final NSession session;
    protected NPath ref;

    protected AbstractPathSPIAdapter(NPath ref, NSession session) {
        this.session = session;
        this.ref = ref;
    }

    @Override
    public int hashCode() {
        return ref.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractPathSPIAdapter urlPath = (AbstractPathSPIAdapter) o;
        return Objects.equals(ref, urlPath.ref);
    }

    @Override
    public String toString() {
        return ref.toString();
    }

    @Override
    public NStream<NPath> list(NPath basePath) {
        return ref.stream();
    }

    @Override
    public NFormatSPI formatter(NPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NPath basePath) {
        return ref.getName();
    }

    @Override
    public String getProtocol(NPath basePath) {
        return ref.getProtocol();
    }

    @Override
    public NPath resolve(NPath basePath, String path) {
        return ref.resolve(path);
    }

    @Override
    public NPath resolve(NPath basePath, NPath path) {
        return ref.resolve(path);
    }

    @Override
    public NPath resolveSibling(NPath basePath, String path) {
        return ref.resolveSibling(path);
    }

    @Override
    public NPath resolveSibling(NPath basePath, NPath path) {
        return ref.resolveSibling(path);
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NPath basePath) {
        return ref.toURL();
    }

    @Override
    public Path toFile(NPath basePath) {
        return ref.toFile();
    }

    public boolean isSymbolicLink(NPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NPath basePath) {
        return ref.isDirectory();
    }

    @Override
    public boolean isRegularFile(NPath basePath) {
        return ref.isRegularFile();
    }

    @Override
    public boolean exists(NPath basePath) {
        return ref.exists();
    }

    @Override
    public long getContentLength(NPath basePath) {
        return ref.getContentLength();
    }

    public String getContentEncoding(NPath basePath) {
        return ref.getContentEncoding();
    }

    public String getContentType(NPath basePath) {
        return ref.getContentType();
    }

    @Override
    public String getLocation(NPath basePath) {
        return ref.getLocation();
    }

    public InputStream getInputStream(NPath basePath, NPathOption... options) {
        return ref.getInputStream(options);
    }

    public OutputStream getOutputStream(NPath basePath, NPathOption... options) {
        return ref.getOutputStream(options);
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public void delete(NPath basePath, boolean recurse) {
        ref.delete(recurse);
    }

    @Override
    public void mkdir(boolean parents, NPath basePath) {
        ref.delete(parents);
    }

    @Override
    public Instant getLastModifiedInstant(NPath basePath) {
        return ref.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant(NPath basePath) {
        return ref.getLastAccessInstant();
    }

    @Override
    public Instant getCreationInstant(NPath basePath) {
        return ref.getCreationInstant();
    }

    @Override
    public NPath getParent(NPath basePath) {
        return ref.getParent();
    }

    @Override
    public NPath toAbsolute(NPath basePath, NPath rootPath) {
        if (isAbsolute(basePath)) {
            return basePath;
        }
        return basePath.toAbsolute();
    }

    @Override
    public NPath normalize(NPath basePath) {
        return basePath.normalize();
    }

    @Override
    public boolean isAbsolute(NPath basePath) {
        return ref.isAbsolute();
    }

    @Override
    public String owner(NPath basePath) {
        return ref.owner();
    }

    @Override
    public String group(NPath basePath) {
        return ref.group();
    }

    @Override
    public Set<NPathPermission> getPermissions(NPath basePath) {
        return ref.getPermissions();
    }

    @Override
    public void setPermissions(NPath basePath, NPathPermission... permissions) {
        ref.setPermissions(permissions);
    }

    @Override
    public void addPermissions(NPath basePath, NPathPermission... permissions) {
        ref.addPermissions(permissions);
    }

    @Override
    public void removePermissions(NPath basePath, NPathPermission... permissions) {
        ref.removePermissions(permissions);
    }

    @Override
    public boolean isName(NPath basePath) {
        return ref.isName();
    }

    @Override
    public int getPathCount(NPath basePath) {
        return ref.getPathCount();
    }

    @Override
    public boolean isRoot(NPath basePath) {
        return ref.isRoot();
    }

    @Override
    public NPath getRoot(NPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return ref.getRoot();
    }

    @Override
    public NStream<NPath> walk(NPath basePath, int maxDepth, NPathOption[] options) {
        return ref.walk(maxDepth, options);
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return ref.subpath(beginIndex, endIndex);
    }

    @Override
    public List<String> getItems(NPath basePath) {
        return ref.getItems();
    }

    @Override
    public void moveTo(NPath basePath, NPath other, NPathOption... options) {
        ref.moveTo(other);
    }

    @Override
    public void copyTo(NPath basePath, NPath other, NPathOption... options) {
        ref.copyTo(other);
    }

    @Override
    public void walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        ref.walkDfs(visitor, maxDepth, options);
    }

    @Override
    public NPath toRelativePath(NPath basePath, NPath parentPath) {
        String child = basePath.getLocation();
        String parent = parentPath.getLocation();
        if (child.startsWith(parent)) {
            child = child.substring(parent.length());
            if (child.startsWith("/") || child.startsWith("\\")) {
                child = child.substring(1);
            }
            return NPath.of(child, session);
        }
        return null;
    }

    private static class MyPathFormat implements NFormatSPI {

        private final AbstractPathSPIAdapter p;

        public MyPathFormat(AbstractPathSPIAdapter p) {
            this.p = p;
        }

        public NString asFormattedString() {
            NTextBuilder sb = NTextBuilder.of(p.getSession());
            sb.append(p.ref);
            return sb.build();
        }
        @Override
        public String getName() {
            return "path";
        }

        @Override
        public void print(NPrintStream out) {
            out.print(asFormattedString());
        }

        @Override
        public boolean configureFirst(NCmdLine commandLine) {
            return false;
        }
    }
}
