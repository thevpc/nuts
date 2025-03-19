package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NOptional;
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

    protected final NWorkspace workspace;
    protected NPath ref;

    protected AbstractPathSPIAdapter(NPath ref, NWorkspace workspace) {
        this.workspace = workspace;
        this.ref = ref;
    }

    public NWorkspace getWorkspace() {
        return workspace;
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
    public NPath resolveSibling(NPath basePath, String path) {
        return ref.resolveSibling(path);
    }

    @Override
    public NPath toCompressedForm(NPath basePath) {
        return null;
    }

    @Override
    public NOptional<URL> toURL(NPath basePath) {
        return ref.toURL();
    }

    @Override
    public NOptional<Path> toPath(NPath basePath) {
        return ref.toPath();
    }

    @Override
    public NPathType type(NPath basePath) {
        return ref.type();
    }

    @Override
    public boolean exists(NPath basePath) {
        return ref.exists();
    }

    @Override
    public long contentLength(NPath basePath) {
        return ref.contentLength();
    }

    public String getContentEncoding(NPath basePath) {
        return ref.getContentEncoding();
    }

    public String getContentType(NPath basePath) {
        return ref.getContentType();
    }

    public String getCharset(NPath basePath) {
        return ref.getCharset();
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
    public Boolean isName(NPath basePath) {
        return ref.isName();
    }

    @Override
    public Integer getNameCount(NPath basePath) {
        return ref.getNameCount();
    }

    @Override
    public Boolean isRoot(NPath basePath) {
        return ref.isRoot();
    }

    @Override
    public boolean isLocal(NPath basePath) {
        return ref.isLocal();
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
        return null;
    }

    @Override
    public NPath subpath(NPath basePath, int beginIndex, int endIndex) {
        return ref.subpath(beginIndex, endIndex);
    }

    @Override
    public List<String> getNames(NPath basePath) {
        return ref.getNames();
    }

    @Override
    public boolean moveTo(NPath basePath, NPath other, NPathOption... options) {
        ref.moveTo(other);
        return true;
    }

    @Override
    public boolean copyTo(NPath basePath, NPath other, NPathOption... options) {
        ref.copyTo(other);
        return true;
    }

    @Override
    public boolean walkDfs(NPath basePath, NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        return false;
    }



    private static class MyPathFormat implements NFormatSPI {

        private final AbstractPathSPIAdapter p;

        public MyPathFormat(AbstractPathSPIAdapter p) {
            this.p = p;
        }

        public NText asFormattedString() {
            NTextBuilder sb = NTextBuilder.of();
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
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }
    }


    @Override
    public byte[] getDigest(NPath basePath, String algo) {
        return ref.getDigest();
    }

    @Override
    public Integer compareTo(NPath basePath, NPath other) {
        return ref.compareTo(other);
    }
}
