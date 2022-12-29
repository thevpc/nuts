package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NTreeVisitor;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.path.spi.NPathSPIHelper;
import net.thevpc.nuts.runtime.standalone.io.path.spi.URLPath;
import net.thevpc.nuts.runtime.standalone.util.reflect.NUseDefaultUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.NStream;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;

public class NPathFromSPI extends NPathBase {
    private final NPathSPI base;
    private List<String> items;

    public NPathFromSPI(NPathSPI base) {
        super(base.getSession());
        this.base = base;
    }

    @Override
    public NPath copy() {
        return new NPathFromSPI(base).copyExtraFrom(this);
    }

    @Override
    public String getContentEncoding() {
        return base.getContentEncoding(this);
    }

    @Override
    public String getContentType() {
        return base.getContentType(this);
    }

    @Override
    public String getName() {
        String n = base.getName(this);
        if (n == null) {
            String loc = getLocation();
            return loc == null ? "" : URLPath.getURLName(loc);
        }
        return n;
    }

    @Override
    public String getLocation() {
        return base.getLocation(this);
    }

    @Override
    public NPath resolve(String other) {
        if (NBlankable.isBlank(other)) {
            return this;
        }
        return base.resolve(this, other);
    }

    @Override
    public NPath resolve(NPath other) {
        return base.resolve(this, other);
    }

    @Override
    public NPath resolveSibling(String other) {
        if (NBlankable.isBlank(other)) {
            return getParent();
        }
        return base.resolveSibling(this, other);
    }

    @Override
    public NPath resolveSibling(NPath other) {
        return base.resolveSibling(this, other);
    }

    @Override
    public byte[] readBytes(NPathOption...options) {
        long len = getContentLength();
        int readSize = 1024;
        if (len < 0) {
            //unknown size!
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[readSize];
            try (InputStream is = getInputStream(options)) {
                int count = 0;
                int offset = 0;
                while ((count = is.read(buffer, 0, readSize)) > 0) {
                    bos.write(buffer, 0, count);
                }
                return bos.toByteArray();
            } catch (IOException e) {
                throw new NIOException(getSession(), NMsg.ofCstyle("unable to read file %s", this), e);
            }
        }
        int ilen = (int) len;
        if (len > Integer.MAX_VALUE - 8) {
            throw new NIOException(getSession(), NMsg.ofCstyle("file is too large %s", this));
        }
        byte[] buffer = new byte[ilen];
        try (InputStream is = getInputStream(options)) {
            int count = 0;
            int offset = 0;
            while ((count = is.read(buffer, offset, ilen - offset)) > 0) {
                offset += count;
            }
            if (offset < ilen) {
                throw new NIOException(getSession(), NMsg.ofCstyle("premature read stop %s", this));
            }
            if (is.read() >= 0) {
                throw new NIOException(getSession(), NMsg.ofCstyle("invalid %s", this));
            }
            return buffer;
        } catch (IOException e) {
            throw new NIOException(getSession(), NMsg.ofCstyle("unable to read file %s", this), e);
        }
    }

    @Override
    public NPath writeBytes(byte[] bytes, NPathOption...options) {
        try (OutputStream os = getOutputStream()) {
            os.write(bytes);
        } catch (IOException ex) {
            throw new NIOException(getSession(), NMsg.ofCstyle("unable to write to %s", this));
        }
        return this;
    }

    @Override
    public String getProtocol() {
        String n = base.getProtocol(this);
        if (n == null) {
            String ts = base.toString();
            int i = ts.indexOf(':');
            if (i >= 0) {
                return ts.substring(0, i);
            }
            return null;
        }
        return n;
    }

    @Override
    public NPath toCompressedForm() {
        NPath n = base.toCompressedForm(this);
        if (n == null) {
            return new NCompressedPath(this, new DefaultNCompressedPathHelper());
        }
        return n;
    }

    @Override
    public URL toURL() {
        return base.toURL(this);
    }

    @Override
    public Path toFile() {
        return base.toFile(this);
    }

    @Override
    public NStream<NPath> stream() {
        NStream<NPath> p = base.list(this);
        if (p != null) {
            return p;
        }
        return NStream.ofEmpty(getSession());
    }

    @Override
    public InputStream getInputStream(NPathOption... options) {
        return (InputStream) NIO.of(getSession()).createInputSource(base.getInputStream(this, options), getInputMetaData());
    }

    @Override
    public OutputStream getOutputStream(NPathOption... options) {
        return (OutputStream) NIO.of(getSession()).createOutputTarget(base.getOutputStream(this, options), getOutputMetaData());
    }

    @Override
    public NPath deleteTree() {
        base.delete(this, true);
        return this;
    }

    @Override
    public NPath delete(boolean recurse) {
        base.delete(this, recurse);
        return this;
    }

    @Override
    public NPath mkdir(boolean parents) {
        base.mkdir(parents, this);
        return this;
    }

    @Override
    public NPath mkdirs() {
        base.mkdir(true, this);
        return this;
    }

    @Override
    public NPath mkdir() {
        base.mkdir(false, this);
        return this;
    }

    public NPath expandPath(Function<String, String> resolver) {
        NSession session = getSession();
        resolver = new EffectiveResolver(resolver, session);
        String s = StringPlaceHolderParser.replaceDollarPlaceHolders(toString(), resolver);
        if (s.length() > 0) {
            if (s.startsWith("~")) {
                NWorkspaceLocationManager locations = session.locations();
                if (s.equals("~~")) {
                    NPath nutsHome = locations.getHomeLocation(NStoreLocation.CONFIG);
                    return nutsHome.normalize();
                } else if (s.startsWith("~~") && s.length() > 2 && (s.charAt(2) == '/' || s.charAt(2) == '\\')) {
                    NPath nutsHome = locations.getHomeLocation(NStoreLocation.CONFIG);
                    return nutsHome.resolve(s.substring(3)).normalize();
                } else if (s.equals("~")) {
                    return NPath.ofUserHome(session);
                } else if (s.startsWith("~") && s.length() > 1 && (s.charAt(1) == '/' || s.charAt(1) == '\\')) {
                    return NPath.ofUserHome(session).resolve(s.substring(2));
                } else {
                    return NPath.of(s, session);
                }
            }
        }
        return NPath.of(s, session);
    }

    @Override
    public NPath mkParentDirs() {
        NPath p = getParent();
        if (p != null) {
            p.mkdir(true);
        }
        return this;
    }

    @Override
    public boolean isOther() {
        return base.isOther(this);
    }

    @Override
    public boolean isSymbolicLink() {
        return base.isSymbolicLink(this);
    }

    @Override
    public boolean isDirectory() {
        return base.isDirectory(this);
    }

    @Override
    public boolean isRegularFile() {
        return base.isRegularFile(this);
    }

    @Override
    public boolean isRemote() {
        return !base.isLocal(this);
    }

    @Override
    public boolean isLocal() {
        return base.isLocal(this);
    }

    @Override
    public boolean exists() {
        return base.exists(this);
    }

    @Override
    public long getContentLength() {
        return base.getContentLength(this);
    }

    @Override
    public Instant getLastModifiedInstant() {
        return base.getLastModifiedInstant(this);
    }

    @Override
    public Instant getLastAccessInstant() {
        return base.getLastAccessInstant(this);
    }

    @Override
    public Instant getCreationInstant() {
        return base.getCreationInstant(this);
    }

    @Override
    public NPath getParent() {
        return base.getParent(this);
    }

    @Override
    public boolean isAbsolute() {
        return base.isAbsolute(this);
    }

    @Override
    public NPath normalize() {
        return base.normalize(this);
    }

    @Override
    public NPath toAbsolute() {
        return toAbsolute((NPath) null);
    }

    @Override
    public NPath toAbsolute(String rootPath) {
        return toAbsolute(rootPath == null ? null : NPath.of(rootPath, getSession()));
    }

    @Override
    public NPath toAbsolute(NPath rootPath) {
        if (base.isAbsolute(this)) {
            return this;
        }
        return base.toAbsolute(this, rootPath);
    }

    @Override
    public NPath toRelative(NPath parentPath) {
        return base.toRelativePath(this, parentPath);
    }

    @Override
    public String owner() {
        return base.owner(this);
    }

    @Override
    public String group() {
        return base.group(this);
    }

    @Override
    public Set<NPathPermission> getPermissions() {
        return base.getPermissions(this);
    }

    @Override
    public NPath setPermissions(NPathPermission... permissions) {
        base.setPermissions(this, permissions);
        return this;
    }

    @Override
    public NPath addPermissions(NPathPermission... permissions) {
        base.addPermissions(this, permissions);
        return this;
    }

    @Override
    public NPath removePermissions(NPathPermission... permissions) {
        base.removePermissions(this, permissions);
        return this;
    }

    @Override
    public boolean isName() {
        return base.isName(this);
    }

    @Override
    public int getPathCount() {
        return base.getPathCount(this);
    }

    @Override
    public boolean isRoot() {
        return base.isRoot(this);
    }

    @Override
    public NStream<NPath> walk(int maxDepth, NPathOption[] options) {
        NPathOption[] options1 = options == null ? new NPathOption[0]
                : Arrays.stream(options).filter(Objects::isNull)
                .distinct()
                .toArray(NPathOption[]::new);
        if (maxDepth <= 0) {
            maxDepth = Integer.MAX_VALUE;
        }
        if (NUseDefaultUtils.isUseDefault(base.getClass(), "walk",
                NPath.class, int.class, NPathOption[].class)) {
            return NPathSPIHelper.walk(getSession(), this, maxDepth, options1);
        } else {
            return base.walk(this, maxDepth, options);
        }
    }

    @Override
    public NPath subpath(int beginIndex, int endIndex) {
        return base.subpath(this, beginIndex, endIndex);
    }

    @Override
    public String getItem(int index) {
        return getItems().get(index);
    }

    @Override
    public List<String> getItems() {
        if (items == null) {
            items = base.getItems(this);
        }
        return items == null ? Collections.emptyList() : items;
    }

    @Override
    public void moveTo(NPath other, NPathOption... options) {
        base.moveTo(this, other);
    }

    @Override
    public void copyTo(NPath other, NPathOption... options) {
        base.copyTo(this, other, options);
    }

    @Override
    public NPath getRoot() {
        return base.getRoot(this);
    }

    @Override
    public NPath walkDfs(NTreeVisitor<NPath> visitor, NPathOption... options) {
        return walkDfs(visitor, Integer.MAX_VALUE, options);
    }

    @Override
    public NPath walkDfs(NTreeVisitor<NPath> visitor, int maxDepth, NPathOption... options) {
        if (maxDepth <= 0) {
            maxDepth = Integer.MAX_VALUE;
        }
        if (NUseDefaultUtils.isUseDefault(base.getClass(), "walkDfs",
                NPath.class, NTreeVisitor.class, int.class, NPathOption[].class)) {
            NPathSPIHelper.walkDfs(getSession(), this, visitor, maxDepth, options);
        } else {
            base.walkDfs(this, visitor, maxDepth, options);
        }
        return this;
    }

    @Override
    public NStream<NPath> walkGlob(NPathOption... options) {
        return new DirectoryScanner(this, getSession()).stream();
    }

    @Override
    public NFormat formatter(NSession session) {
        NFormatSPI fspi = null;
        if (NUseDefaultUtils.isUseDefault(base.getClass(), "formatter",
                NPath.class)) {
        } else {
            fspi = base.formatter(this);
        }
        if (fspi != null) {
            return new NFormatFromSPI(fspi, getSession());
        }
        return super.formatter(session != null ? session : getSession());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NPathFromSPI that = (NPathFromSPI) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public String toString() {
        return base.toString();
    }

    private static class EffectiveResolver implements Function<String, String> {
        NWorkspaceVarExpansionFunction fallback;
        Function<String, String> resolver;
        NSession session;

        public EffectiveResolver(Function<String, String> resolver, NSession session) {
            this.session = session;
            this.resolver = resolver;
            fallback = NWorkspaceVarExpansionFunction.of(session);
        }

        @Override
        public String apply(String s) {
            if (resolver != null) {
                String v = resolver.apply(s);
                if (v != null) {
                    return v;
                }
            }
            return fallback.apply(s);
        }
    }

}
