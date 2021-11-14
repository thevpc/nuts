package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.core.expr.StringPlaceHolderParser;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.io.NutsWorkspaceVarExpansionFunction;
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
        return base.getContentEncoding();
    }

    @Override
    public String getContentType() {
        return base.getContentType();
    }

    @Override
    public String getName() {
        String n = base.getName();
        if (n == null) {
            String loc = getLocation();
            return loc == null ? "" : CoreIOUtils.getURLName(loc);
        }
        return n;
    }

    @Override
    public String getLocation() {
        return base.getLocation();
    }

    @Override
    public NutsPath resolve(String other) {
        if (NutsBlankable.isBlank(other)) {
            return this;
        }
        return base.resolve(other);
    }

    @Override
    public NutsPath resolveSibling(String other) {
        if (NutsBlankable.isBlank(other)) {
            return getParent();
        }
        return base.resolveSibling(other);
    }

    @Override
    public NutsPath resolve(NutsPath other) {
        return base.resolve(other);
    }

    @Override
    public NutsPath resolveSibling(NutsPath other) {
        return base.resolveSibling(other);
    }

    @Override
    public byte[] readAllBytes() {
        long len = getContentLength();
        int readSize=1024;
        if (len < 0) {
            //unknown size!
            ByteArrayOutputStream bos=new ByteArrayOutputStream();
            byte[] buffer = new byte[readSize];
            try (InputStream is = getInputStream()) {
                int count = 0;
                int offset = 0;
                while ((count = is.read(buffer, 0, readSize)) > 0) {
                    bos.write(buffer,0,count);
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
            if(offset<ilen){
                throw new NutsIOException(getSession(), NutsMessage.cstyle("premature read stop %s", this));
            }
            if(is.read()>=0){
                throw new NutsIOException(getSession(), NutsMessage.cstyle("invalid %s", this));
            }
            return buffer;
        } catch (IOException e) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to read file %s", this), e);
        }
    }

    @Override
    public NutsPath writeBytes(byte[] bytes) {
        try(OutputStream os=getOutputStream()){
            os.write(bytes);
        }catch (IOException ex){
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to write to %s",this));
        }
        return this;
    }

    @Override
    public String getProtocol() {
        String n = base.getProtocol();
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
        NutsPath n = base.toCompressedForm();
        if (n == null) {
            return new NutsCompressedPath(this);
        }
        return this;
    }

    @Override
    public URL toURL() {
        return base.toURL();
    }

    @Override
    public Path toFile() {
        return base.toFile();
    }

    @Override
    public NutsStream<NutsPath> list() {
        try {
            NutsStream<NutsPath> p = base.list();
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
        return InputStreamMetadataAwareImpl.of(base.getInputStream(),
                new NutsPathInputStreamMetadata(this)
        );
    }

    @Override
    public OutputStream getOutputStream() {
        return base.getOutputStream();
    }

    @Override
    public NutsPath delete(boolean recurse) {
        base.delete(recurse);
        return this;
    }

    @Override
    public NutsPath mkdir(boolean parents) {
        base.mkdir(parents);
        return this;
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
        return base.isOther();
    }

    @Override
    public boolean isSymbolicLink() {
        return base.isSymbolicLink();
    }

    @Override
    public boolean isDirectory() {
        return base.isDirectory();
    }

    @Override
    public boolean isRegularFile() {
        return base.isRegularFile();
    }

    @Override
    public boolean exists() {
        return base.exists();
    }

    @Override
    public long getContentLength() {
        return base.getContentLength();
    }

    @Override
    public Instant getLastModifiedInstant() {
        return base.getLastModifiedInstant();
    }

    @Override
    public Instant getLastAccessInstant() {
        return base.getLastAccessInstant();
    }

    @Override
    public Instant getCreationInstant() {
        return base.getCreationInstant();
    }

    @Override
    public NutsPath getParent() {
        return base.getParent();
    }

    @Override
    public boolean isAbsolute() {
        return base.isAbsolute();
    }

    @Override
    public NutsPath normalize() {
        return base.normalize();
    }

    @Override
    public NutsPath toAbsolute() {
        return toAbsolute((NutsPath) null);
    }

    @Override
    public NutsPath toAbsolute(String basePath) {
        return toAbsolute(basePath == null ? null : NutsPath.of(basePath, getSession()));
    }

    @Override
    public NutsPath toAbsolute(NutsPath basePath) {
        if (base.isAbsolute()) {
            return this;
        }
        return base.toAbsolute(basePath);
    }

    @Override
    public String owner() {
        return base.owner();
    }

    @Override
    public String group() {
        return base.group();
    }

    @Override
    public Set<NutsPathPermission> getPermissions() {
        return base.permissions();
    }

    @Override
    public NutsPath setPermissions(NutsPathPermission... permissions) {
        base.setPermissions(permissions);
        return this;
    }

    @Override
    public NutsPath addPermissions(NutsPathPermission... permissions) {
        base.addPermissions(permissions);
        return this;
    }

    @Override
    public NutsPath removePermissions(NutsPathPermission... permissions) {
        base.removePermissions(permissions);
        return this;
    }

    @Override
    public boolean isName() {
        return base.isName();
    }

    @Override
    public int getPathCount() {
        return base.getPathCount();
    }

    @Override
    public boolean isRoot() {
        return base.isRoot();
    }

    @Override
    public NutsStream<NutsPath> walk(int maxDepth, NutsPathVisitOption[] options) {
        return base.walk(maxDepth <= 0 ? Integer.MAX_VALUE : maxDepth,
                options == null ? new NutsPathVisitOption[0]
                        : Arrays.stream(options).filter(Objects::isNull)
                        .distinct()
                        .toArray(NutsPathVisitOption[]::new)
        );
    }

    @Override
    public NutsFormat formatter() {
        NutsFormatSPI fspi = base.getFormatterSPI();
        if (fspi != null) {
            return new DefaultFormatBase<NutsFormat>(getSession(), "path") {
                @Override
                public void print(NutsPrintStream out) {
                    fspi.print(out);
                }

                @Override
                public boolean configureFirst(NutsCommandLine commandLine) {
                    return fspi.configureFirst(commandLine);
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
    public String toString() {
        return base.toString();
    }

    @Override
    public NutsPath deleteTree() {
        base.delete(true);
        return this;
    }

    @Override
    public NutsPath subpath(int beginIndex, int endIndex) {
        return base.subpath(beginIndex,endIndex);
    }

    @Override
    public String getItem(int index) {
        return getItems()[index];
    }

    @Override
    public String[] getItems() {
        if(items==null){
            items=base.getItems();
        }
        return items==null?new String[0]:items;
    }

    @Override
    public NutsPath mkdirs() {
        base.mkdir(true);
        return this;
    }

    @Override
    public NutsPath mkdir() {
        base.mkdir(false);
        return this;
    }

    public NutsPath expandPath(Function<String, String> resolver) {
        NutsSession session = getSession();
        resolver=new EffectiveResolver(resolver, session);
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
                    return NutsPath.of(System.getProperty("user.home"),session);
                } else if (s.startsWith("~") && s.length() > 1 && (s.charAt(1) == '/' || s.charAt(1) == '\\')) {
                    return NutsPath.of(System.getProperty("user.home") + File.separator + s.substring(2),session);
                } else {
                    return NutsPath.of(s,session);
                }
            }
        }
        return NutsPath.of(s,session);
    }

    private static class EffectiveResolver implements Function<String, String> {
        NutsWorkspaceVarExpansionFunction fallback;
        Function<String, String> resolver;
        NutsSession session;

        public EffectiveResolver(Function<String, String> resolver,NutsSession session) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NutsPathFromSPI that = (NutsPathFromSPI) o;
        return Objects.equals(base, that.base);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), base);
    }
}
