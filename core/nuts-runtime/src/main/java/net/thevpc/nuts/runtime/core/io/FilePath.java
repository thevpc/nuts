package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.*;
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
import java.util.Arrays;
import java.util.Objects;

public class FilePath extends NutsPathBase implements NutsPathSPI {
    private final Path value;

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
    public NutsPath resolve(String other) {
        String[] others = Arrays.stream(NutsUtilStrings.trim(other).split("[/\\\\]"))
                .filter(x -> x.length() > 0).toArray(String[]::new);
        if (others.length > 0) {
            Path value2 = value;
            for (String s : others) {
                value2 = value2.resolve(s);
            }
            return new FilePath(value2, getSession());
        }
        return this;
    }

    @Override
    public String getProtocol() {
        return "";
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
    public Path toFile() {
        return value;
    }

    @Override
    public NutsPath[] getChildren() {
        if (Files.isDirectory(value)) {
            try {
                return Files.list(value).map(x -> getSession().io().path(x)).toArray(NutsPath[]::new);
            } catch (IOException e) {
                //
            }
        }
        return new NutsPath[0];
    }

    public InputStream getInputStream() {
        try {
            return InputStreamMetadataAwareImpl.of(Files.newInputStream(value),
                    new NutsDefaultInputStreamMetadata(this)
            );
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public OutputStream getOutputStream() {
        try {
            return Files.newOutputStream(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

//    @Override
//    public NutsOutput output() {
//        return new NutsPathOutput(null, this, getSession()) {
//            @Override
//            public OutputStream open() {
//                return getOutputStream();
//            }
//        };
//    }

    @Override
    public boolean isDirectory() {
        return Files.isDirectory(value);
    }

    @Override
    public boolean isRegularFile() {
        return Files.isRegularFile(value);
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
    public NutsPathBuilder builder() {
        return new DefaultPathBuilder(getSession(), this);
    }

    @Override
    public NutsPath getParent() {
        Path p = value.getParent();
        if (p == null) {
            return null;
        }
        return getSession().io().path(p);
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

//    private class FilePathInput extends NutsPathInput {
//        public FilePathInput() {
//            super(FilePath.this);
//        }
//
//        @Override
//        public InputStream open() {
//            return new InputStreamMetadataAwareImpl(inputStream(), new NutsDefaultInputStreamMetadata(getNutsPath().toString(),
//                    getNutsPath().getContentLength()));
//        }
//    }

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

    private static class MyPathFormat extends DefaultFormatBase<NutsFormat> {
        private final FilePath p;

        public MyPathFormat(FilePath p) {
            super(p.getSession().getWorkspace(), "path");
            this.p = p;
        }

        public NutsString asFormattedString() {
            if (p.value == null) {
                return getSession().text().ofPlain("");
            }
            return getSession().text().toText(p.value);
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
