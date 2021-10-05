package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
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
            return CoreIOUtils.getURLName(asString());
        }
        return n;
    }

    @Override
    public String asString() {
        return base.asString();
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
        return base.toFilePath();
    }

    @Override
    public NutsPath[] getChildren() {
        try {
            NutsPath[] p = base.getChildren();
            if (p != null) {
                return p;
            }
        } catch (Exception ex) {
            getSession().log().of(NutsPathFromSPI.class)
                    .with()
                    .verb(NutsLogVerb.WARNING)
                    .level(Level.WARNING)
                    .error(ex)
                    .log(
                            NutsMessage.jstyle("error execution {0}.children()", base.getClass().getName())
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
    public void delete(boolean recurse) {
        base.delete(recurse);
    }

    @Override
    public void mkdir(boolean parents) {
        base.mkdir(parents);
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
    public NutsPathBuilder builder() {
        return new DefaultPathBuilder(getSession(), this);
    }

    @Override
    public NutsPath getParent() {
        return base.getParent();
    }

    @Override
    public NutsFormat formatter() {
        NutsFormatSPI fspi = base.getFormatterSPI();
        if (fspi != null) {
            return new DefaultFormatBase<NutsFormat>(getSession().getWorkspace(), "path") {
                @Override
                public void print(NutsPrintStream out) {
                    fspi.print(out);
                }

                @Override
                public boolean configureFirst(NutsCommandLine commandLine) {
                    return fspi.configureFirst(commandLine);
                }
            }.setSession(getSession());
        }
        return super.formatter();
    }

    @Override
    public int hashCode() {
        return Objects.hash(base);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPathFromSPI that = (NutsPathFromSPI) o;
        return base.equals(that.base);
    }

    @Override
    public String toString() {
        return base.toString();
    }
}
