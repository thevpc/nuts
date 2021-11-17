package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.bundles.reflect.NutsUseDefaultUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.io.path.spi.NutsPathSPIHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceVarExpansionFunction;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.*;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Level;

public class NutsPathFromSPI extends NutsPathBase {
    private final NutsPathSPI base;
    private String[] items;

    public NutsPathFromSPI(NutsPathSPI base) {
        super(base.getSession());
        this.base = base;
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
            return loc == null ? "" : CoreIOUtils.getURLName(loc);
        }
        return n;
    }

    @Override
    public String getLocation() {
        return base.getLocation(this);
    }

    @Override
    public NutsPath resolve(String other) {
        if (NutsBlankable.isBlank(other)) {
            return this;
        }
        return base.resolve(this, other);
    }

    @Override
    public NutsPath resolve(NutsPath other) {
        return base.resolve(this, other);
    }

    @Override
    public NutsPath resolveSibling(String other) {
        if (NutsBlankable.isBlank(other)) {
            return getParent();
        }
        return base.resolveSibling(this, other);
    }

    @Override
    public NutsPath resolveSibling(NutsPath other) {
        return base.resolveSibling(this, other);
    }

    @Override
    public byte[] readAllBytes() {
        long len = getContentLength();
        int readSize = 1024;
        if (len < 0) {
            //unknown size!
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] buffer = new byte[readSize];
            try (InputStream is = getInputStream()) {
                int count = 0;
                int offset = 0;
                while ((count = is.read(buffer, 0, readSize)) > 0) {
                    bos.write(buffer, 0, count);
                }
                return bos.toByteArray();
            } catch (IOException e) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to read file %s", this), e);
            }
        }
        int ilen = (int) len;
        if (len > Integer.MAX_VALUE - 8) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("file is too large %s", this));
        }
        byte[] buffer = new byte[ilen];
        try (InputStream is = getInputStream()) {
            int count = 0;
            int offset = 0;
            while ((count = is.read(buffer, offset, ilen - offset)) > 0) {
                offset += count;
            }
            if (offset < ilen) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("premature read stop %s", this));
            }
            if (is.read() >= 0) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("invalid %s", this));
            }
            return buffer;
        } catch (IOException e) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to read file %s", this), e);
        }
    }

    @Override
    public NutsPath writeBytes(byte[] bytes) {
        try (OutputStream os = getOutputStream()) {
            os.write(bytes);
        } catch (IOException ex) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to write to %s", this));
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
    public NutsPath toCompressedForm() {
        NutsPath n = base.toCompressedForm(this);
        if (n == null) {
            return new NutsCompressedPath(this);
        }
        return this;
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
    public NutsStream<NutsPath> list() {
        try {
            NutsStream<NutsPath> p = base.list(this);
            if (p != null) {
                return p;
            }
        } catch (Exception ex) {
            NutsLoggerOp.of(NutsPathFromSPI.class, getSession())
                    .verb(NutsLogVerb.WARNING)
                    .level(Level.WARNING)
                    .error(ex)
                    .log(
                            NutsMessage.jstyle("error execution {0}.list()", base.getClass().getName())
                    );
        }
        return NutsStream.ofEmpty(getSession());
    }

    @Override
    public InputStream getInputStream() {
        return InputStreamMetadataAwareImpl.of(base.getInputStream(this),
                new NutsPathInputStreamMetadata(this)
        );
    }

    @Override
    public OutputStream getOutputStream() {
        return base.getOutputStream(this);
    }

    @Override
    public NutsPath deleteTree() {
        base.delete(this, true);
        return this;
    }

    @Override
    public NutsPath delete(boolean recurse) {
        base.delete(this, recurse);
        return this;
    }

    @Override
    public NutsPath mkdir(boolean parents) {
        base.mkdir(parents, this);
        return this;
    }

    @Override
    public NutsPath mkdirs() {
        base.mkdir(true, this);
        return this;
    }

    @Override
    public NutsPath mkdir() {
        base.mkdir(false, this);
        return this;
    }

    public NutsPath expandPath(Function<String, String> resolver) {
        NutsSession session = getSession();
        resolver = new EffectiveResolver(resolver, session);
        String s = StringPlaceHolderParser.replaceDollarPlaceHolders(toString(), resolver);
        if (s.length() > 0) {
            if (s.startsWith("~")) {
                NutsWorkspaceLocationManager locations = session.locations();
                if (s.equals("~~")) {
                    NutsPath nutsHome = locations.getHomeLocation(NutsStoreLocation.CONFIG);
                    return nutsHome.normalize();
                } else if (s.startsWith("~~") && s.length() > 2 && (s.charAt(2) == '/' || s.charAt(2) == '\\')) {
                    NutsPath nutsHome = locations.getHomeLocation(NutsStoreLocation.CONFIG);
                    return nutsHome.resolve(s.substring(3)).normalize();
                } else if (s.equals("~")) {
                    return NutsPath.of(System.getProperty("user.home"), session);
                } else if (s.startsWith("~") && s.length() > 1 && (s.charAt(1) == '/' || s.charAt(1) == '\\')) {
                    return NutsPath.of(System.getProperty("user.home") + File.separator + s.substring(2), session);
                } else {
                    return NutsPath.of(s, session);
                }
            }
        }
        return NutsPath.of(s, session);
    }

    @Override
    public NutsPath mkParentDirs() {
        NutsPath p = getParent();
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
    public NutsPath getParent() {
        return base.getParent(this);
    }

    @Override
    public boolean isAbsolute() {
        return base.isAbsolute(this);
    }

    @Override
    public NutsPath normalize() {
        return base.normalize(this);
    }

    @Override
    public NutsPath toAbsolute() {
        return toAbsolute((NutsPath) null);
    }

    @Override
    public NutsPath toAbsolute(String rootPath) {
        return toAbsolute(rootPath == null ? null : NutsPath.of(rootPath, getSession()));
    }

    @Override
    public NutsPath toAbsolute(NutsPath rootPath) {
        if (base.isAbsolute(this)) {
            return this;
        }
        return base.toAbsolute(this, rootPath);
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
    public Set<NutsPathPermission> getPermissions() {
        return base.getPermissions(this);
    }

    @Override
    public NutsPath setPermissions(NutsPathPermission... permissions) {
        base.setPermissions(this, permissions);
        return this;
    }

    @Override
    public NutsPath addPermissions(NutsPathPermission... permissions) {
        base.addPermissions(this, permissions);
        return this;
    }

    @Override
    public NutsPath removePermissions(NutsPathPermission... permissions) {
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
    public NutsStream<NutsPath> walk(int maxDepth, NutsPathOption[] options) {
        NutsPathOption[] options1 = options == null ? new NutsPathOption[0]
                : Arrays.stream(options).filter(Objects::isNull)
                .distinct()
                .toArray(NutsPathOption[]::new);
        if (maxDepth <= 0) {
            maxDepth = Integer.MAX_VALUE;
        }
        if (NutsUseDefaultUtils.isUseDefault(base.getClass(), "walk",
                NutsPath.class, int.class, NutsPathOption[].class)) {
            return NutsPathSPIHelper.walk(getSession(), this, maxDepth, options1);
        } else {
            return base.walk(this, maxDepth, options);
        }
    }

    @Override
    public NutsPath subpath(int beginIndex, int endIndex) {
        return base.subpath(this, beginIndex, endIndex);
    }

    @Override
    public String getItem(int index) {
        return getItems()[index];
    }

    @Override
    public String[] getItems() {
        if (items == null) {
            items = base.getItems(this);
        }
        return items == null ? new String[0] : items;
    }

    @Override
    public void moveTo(NutsPath other, NutsPathOption... options) {
        base.moveTo(this, other);
    }

    @Override
    public void copyTo(NutsPath other, NutsPathOption... options) {
        base.copyTo(this, other, options);
    }

    @Override
    public NutsPath getRoot() {
        return base.getRoot(this);
    }

    @Override
    public NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor, NutsPathOption... options) {
        return walkDfs(visitor, Integer.MAX_VALUE, options);
    }

    @Override
    public NutsPath walkDfs(NutsTreeVisitor<NutsPath> visitor, int maxDepth, NutsPathOption... options) {
        if (maxDepth <= 0) {
            maxDepth = Integer.MAX_VALUE;
        }
        if (NutsUseDefaultUtils.isUseDefault(base.getClass(), "walkDfs",
                NutsPath.class, NutsTreeVisitor.class, int.class, NutsPathOption[].class)) {
            NutsPathSPIHelper.walkDfs(getSession(), this, visitor, maxDepth, options);
        } else {
            base.walkDfs(this, visitor, maxDepth, options);
        }
        return this;
    }

    @Override
    public NutsFormat formatter() {
        NutsFormatSPI fspi=null;
        if (NutsUseDefaultUtils.isUseDefault(base.getClass(), "formatter",
                NutsPath.class)) {
        } else {
            fspi = base.formatter(this);
        }
        if (fspi != null) {
            NutsFormatSPI finalFspi = fspi;
            return new DefaultFormatBase<NutsFormat>(getSession(), "path") {
                @Override
                public void print(NutsPrintStream out) {
                    finalFspi.print(out);
                }

                @Override
                public boolean configureFirst(NutsCommandLine commandLine) {
                    return finalFspi.configureFirst(commandLine);
                }

                @Override
                public int getSupportLevel(NutsSupportLevelContext context) {
                    return DEFAULT_SUPPORT;
                }
            };
        }
        return super.formatter();
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
        NutsPathFromSPI that = (NutsPathFromSPI) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public String toString() {
        return base.toString();
    }

    private static class EffectiveResolver implements Function<String, String> {
        NutsWorkspaceVarExpansionFunction fallback;
        Function<String, String> resolver;
        NutsSession session;

        public EffectiveResolver(Function<String, String> resolver, NutsSession session) {
            this.session = session;
            this.resolver = resolver;
            fallback = new NutsWorkspaceVarExpansionFunction(session);
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
