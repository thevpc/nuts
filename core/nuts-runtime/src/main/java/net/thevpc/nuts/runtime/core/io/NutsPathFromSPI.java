package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;

public class NutsPathFromSPI extends NutsPathBase {
    private final NutsPathSPI base;

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
            return CoreIOUtils.getURLName(toString());
        }
        return n;
    }

    @Override
    public String getLocation() {
        return base.getLocation();
    }

    @Override
    public NutsPath resolve(String other) {
        String[] others = Arrays.stream(NutsUtilStrings.trim(other).split("[/\\\\]"))
                .filter(x -> x.length() > 0).toArray(String[]::new);
        if (others.length == 0) {
            return this;
        }
        NutsPath n = base.resolve(String.join("/", others));
        if (n == null) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("unable to resolve %s", other));
        }
        return n;
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
    public NutsPath[] list() {
        try {
            NutsPath[] p = base.list();
            if (p != null) {
                return p;
            }
        } catch (Exception ex) {
            NutsLoggerOp.of(NutsPathFromSPI.class,getSession())
                    .verb(NutsLogVerb.WARNING)
                    .level(Level.WARNING)
                    .error(ex)
                    .log(
                            NutsMessage.jstyle("error execution {0}.list()", base.getClass().getName())
                    );
        }
        return new NutsPath[0];
    }

    @Override
    public InputStream getInputStream() {
        return InputStreamMetadataAwareImpl.of(base.getInputStream(),
                new NutsDefaultInputStreamMetadata(this)
        );
    }

//    @Override
//    public NutsInput input() {
//        return new NutsPathFromSPIInput();
//    }
//
//    @Override
//    public NutsOutput output() {
//        return new NutsPathOutput(null, this, getSession()) {
//            @Override
//            public OutputStream open() {
//                return base.getOutputStream();
//            }
//        };
//    }

    @Override
    public OutputStream getOutputStream() {
        return null;
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

//    private class NutsPathFromSPIInput extends NutsPathInput {
//        public NutsPathFromSPIInput() {
//            super(NutsPathFromSPI.this);
//        }
//
//        @Override
//        public InputStream open() {
//            return new InputStreamMetadataAwareImpl(base.inputStream(), new NutsDefaultInputStreamMetadata(getNutsPath().toString(),
//                    getNutsPath().getContentLength()));
//        }
//    }

    @Override
    public NutsPathBuilder builder() {
        return new DefaultPathBuilder(getSession(), this);
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
        return toAbsolute(basePath==null?null:NutsPath.of(basePath,getSession()));
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



}
