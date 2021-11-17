package net.thevpc.nuts.runtime.standalone.io.path.spi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;

public abstract class AbstractPathSPIAdapter implements NutsPathSPI {

    protected final NutsSession session;
    protected NutsPath ref;

    protected AbstractPathSPIAdapter(NutsPath ref, NutsSession session) {
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
    public NutsStream<NutsPath> list(NutsPath basePath) {
        return ref.list();
    }

    @Override
    public NutsFormatSPI formatter(NutsPath basePath) {
        return new MyPathFormat(this);
    }

    @Override
    public String getName(NutsPath basePath) {
        return ref.getName();
    }

    @Override
    public String getProtocol(NutsPath basePath) {
        return ref.getProtocol();
    }

    @Override
    public NutsPath resolve(NutsPath basePath, String path) {
        return ref.resolve(path);
    }

    @Override
    public NutsPath resolve(NutsPath basePath, NutsPath path) {
        return ref.resolve(path);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, String path) {
        return ref.resolveSibling(path);
    }

    @Override
    public NutsPath resolveSibling(NutsPath basePath, NutsPath path) {
        return ref.resolveSibling(path);
    }

    @Override
    public NutsPath toCompressedForm(NutsPath basePath) {
        return null;
    }

    @Override
    public URL toURL(NutsPath basePath) {
        return ref.toURL();
    }

    @Override
    public Path toFile(NutsPath basePath) {
        return ref.toFile();
    }

    public boolean isSymbolicLink(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isOther(NutsPath basePath) {
        return false;
    }

    @Override
    public boolean isDirectory(NutsPath basePath) {
        return ref.isDirectory();
    }

    @Override
    public boolean isRegularFile(NutsPath basePath) {
        return ref.isRegularFile();
    }

    @Override
    public boolean exists(NutsPath basePath) {
        return ref.exists();
    }

    @Override
    public long getContentLength(NutsPath basePath) {
        return ref.getContentLength();
    }

    public String getContentEncoding(NutsPath basePath) {
        return ref.getContentEncoding();
    }

    public String getContentType(NutsPath basePath) {
        return ref.getContentType();
    }

    @Override
    public String getLocation(NutsPath basePath) {
        return ref.getLocation();
    }

    public InputStream getInputStream(NutsPath basePath) {
        return ref.getInputStream();
    }

    public OutputStream getOutputStream(NutsPath basePath) {
        return ref.getOutputStream();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public void delete(NutsPath basePath, boolean recurse) {
        ref.delete(recurse);
    }

    @Override
    public void mkdir(boolean parents, NutsPath basePath) {
        ref.delete(parents);
    }

    @Override
    public Instant getLastModifiedInstant(NutsPath basePath) {
        return ref.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant(NutsPath basePath) {
        return ref.getLastAccessInstant();
    }

    @Override
    public Instant getCreationInstant(NutsPath basePath) {
        return ref.getCreationInstant();
    }

    @Override
    public NutsPath getParent(NutsPath basePath) {
        return ref.getParent();
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath, NutsPath rootPath) {
        if(isAbsolute(basePath)){
            return basePath;
        }
        return basePath.toAbsolute();
    }

    @Override
    public NutsPath normalize(NutsPath basePath) {
        return basePath.normalize();
    }

    @Override
    public boolean isAbsolute(NutsPath basePath) {
        return ref.isAbsolute();
    }

    @Override
    public String owner(NutsPath basePath) {
        return ref.owner();
    }

    @Override
    public String group(NutsPath basePath) {
        return ref.group();
    }

    @Override
    public Set<NutsPathPermission> getPermissions(NutsPath basePath) {
        return ref.getPermissions();
    }

    @Override
    public void setPermissions(NutsPath basePath, NutsPathPermission... permissions) {
        ref.setPermissions(permissions);
    }

    @Override
    public void addPermissions(NutsPath basePath, NutsPathPermission... permissions) {
        ref.addPermissions(permissions);
    }

    @Override
    public void removePermissions(NutsPath basePath, NutsPathPermission... permissions) {
        ref.removePermissions(permissions);
    }

    @Override
    public boolean isName(NutsPath basePath) {
        return ref.isName();
    }

    @Override
    public int getPathCount(NutsPath basePath) {
        return ref.getPathCount();
    }

    @Override
    public boolean isRoot(NutsPath basePath) {
        return ref.isRoot();
    }

    @Override
    public NutsPath getRoot(NutsPath basePath) {
        if (isRoot(basePath)) {
            return basePath;
        }
        return ref.getRoot();
    }

    @Override
    public NutsStream<NutsPath> walk(NutsPath basePath, int maxDepth, NutsPathOption[] options) {
        return ref.walk(maxDepth,options);
    }

    @Override
    public NutsPath subpath(NutsPath basePath, int beginIndex, int endIndex) {
        return ref.subpath(beginIndex, endIndex);
    }

    @Override
    public String[] getItems(NutsPath basePath) {
        return ref.getItems();
    }

    @Override
    public void moveTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        ref.moveTo(other);
    }

    @Override
    public void copyTo(NutsPath basePath, NutsPath other, NutsPathOption... options) {
        ref.copyTo(other);
    }

    @Override
    public void walkDfs(NutsPath basePath, NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {
        ref.walkDfs(visitor,maxDepth,options);
    }

    private static class MyPathFormat implements NutsFormatSPI {

        private final AbstractPathSPIAdapter p;

        public MyPathFormat(AbstractPathSPIAdapter p) {
            this.p = p;
        }

        public NutsString asFormattedString() {
            NutsTextBuilder sb = NutsTextBuilder.of(p.getSession());
            sb.append(p.ref);
            return sb.build();
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
