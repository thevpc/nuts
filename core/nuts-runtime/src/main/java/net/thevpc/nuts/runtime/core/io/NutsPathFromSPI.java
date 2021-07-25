package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.FixedInputStreamMetadata;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Objects;

public class NutsPathFromSPI extends NutsPathBase {
    private NutsPathSPI base;

    public NutsPathFromSPI(NutsPathSPI base) {
        super(base.getSession());
        this.base = base;
    }

    @Override
    public String name() {
        String n = base.name();
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
    public NutsString asFormattedString() {
        //should implement better formatting...
        NutsString s = base.asFormattedString();
        if (s == null) {
            s = getSession().getWorkspace().text().forStyled(asString(), NutsTextStyle.path());
        }
        return s;
    }

    @Override
    public String location() {
        return base.location();
    }

    @Override
    public NutsPath compressedForm() {
        NutsPath n = base.compressedForm();
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
    public Path toFilePath() {
        return base.toFilePath();
    }

    @Override
    public NutsInput input() {
        return new NutsPathFromSPIInput();
    }

    @Override
    public NutsOutput output() {
        return new NutsPathOutput(null, this, getSession()) {
            @Override
            public OutputStream open() {
                return base.outputStream();
            }
        };
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
    public boolean exists() {
        return base.exists();
    }

    @Override
    public long length() {
        return base.length();
    }

    @Override
    public Instant lastModifiedInstant() {
        return base.lastModifiedInstant();
    }

    @Override
    public NutsFormat formatter() {
        NutsFormat f = base.formatter();
        if (f != null) {
            return f;
        }
        return super.formatter();
    }

    @Override
    public int hashCode() {
        return Objects.hash(base);
    }

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

    private class NutsPathFromSPIInput extends NutsPathInput {
        public NutsPathFromSPIInput() {
            super(NutsPathFromSPI.this);
        }

        @Override
        public InputStream open() {
            return new InputStreamMetadataAwareImpl(base.inputStream(), new FixedInputStreamMetadata(getNutsPath().toString(),
                    getNutsPath().length()));
        }
    }
}
