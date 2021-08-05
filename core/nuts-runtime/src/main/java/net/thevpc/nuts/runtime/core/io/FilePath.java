package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.io.FixedInputStreamMetadata;
import net.thevpc.nuts.runtime.bundles.io.InputStreamMetadataAwareImpl;
import net.thevpc.nuts.runtime.core.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.Objects;

public class FilePath extends NutsPathBase implements NutsPathSPI {
    private Path value;

    public FilePath(Path value, NutsSession session) {
        super(session);
        if (value == null) {
            throw new IllegalArgumentException("invalid value");
        }
        this.value = value;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public String getContentType() {
        try {
            return Files.probeContentType(value);
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePath urlPath = (FilePath) o;
        return Objects.equals(value, urlPath.value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public String getName() {
        return CoreIOUtils.getURLName(value.toString());
    }

    @Override
    public String asString() {
        return value.toString();
    }

    @Override
    public String getLocation() {
        return value.toString();
    }

    @Override
    public NutsPath toCompressedForm() {
        return new NutsCompressedPath(this);
    }

    public URL toURL() {
        try {
            return value.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Path toFilePath() {
        return value;
    }

    @Override
    public NutsInput input() {
        return new FilePathInput();
    }

    @Override
    public NutsOutput output() {
        return new NutsPathOutput(null, this, getSession()) {
            @Override
            public OutputStream open() {
                return outputStream();
            }
        };
    }

    @Override
    public void delete(boolean recurse) {
        if (Files.isRegularFile(value)) {
            try {
                Files.delete(value);
            } catch (IOException e) {
                throw new NutsIOException(getSession(), e);
            }
        } else if (Files.isDirectory(value)) {
            if (recurse) {
                CoreIOUtils.delete(getSession(), value);
            } else {
                try {
                    Files.delete(value);
                } catch (IOException e) {
                    throw new NutsIOException(getSession(), e);
                }
            }
        } else {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to delete path %s", value));
        }
    }

    @Override
    public void mkdir(boolean parents) {
        if (Files.isRegularFile(value)) {
            throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to create folder out of regular file %s", value));
        } else if (Files.isDirectory(value)) {
            return;
        } else {
            try {
                Files.createDirectories(value);
            } catch (IOException e) {
                throw new NutsIOException(getSession(), NutsMessage.cstyle("unable to create folders %s", value));
            }
        }
    }

    public boolean exists() {
        return Files.exists(value);
    }

    public long getContentLength() {
        try {
            return Files.size(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Instant getLastModifiedInstant() {
        FileTime r = null;
        try {
            r = Files.getLastModifiedTime(value);
            if (r != null) {
                return r.toInstant();
            }
        } catch (IOException e) {
            //
        }
        return null;
    }

    @Override
    public NutsFormat formatter() {
        return new MyPathFormat(this)
                .setSession(getSession())
                ;
    }

    @Override
    public NutsFormatSPI getFormatterSPI() {
        NutsFormat formatter = formatter();
        return new NutsFormatSPIFromNutsFormat(formatter);
    }

    public InputStream inputStream() {
        try {
            return Files.newInputStream(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream outputStream() {
        try {
            return Files.newOutputStream(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static class MyPathFormat extends DefaultFormatBase<NutsFormat> {
        private FilePath p;

        public MyPathFormat(FilePath p) {
            super(p.getSession().getWorkspace(), "path");
            this.p = p;
        }

        public NutsString asFormattedString() {
            if (p.value == null) {
                return getSession().getWorkspace().text().forPlain("");
            }
            return getSession().getWorkspace().text().toText(p.value);
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

    private class FilePathInput extends NutsPathInput {
        public FilePathInput() {
            super(FilePath.this);
        }

        @Override
        public InputStream open() {
            return new InputStreamMetadataAwareImpl(inputStream(), new FixedInputStreamMetadata(getNutsPath().toString(),
                    getNutsPath().getContentLength()));
        }
    }

}
