package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public class FilePath extends NutsPathBase {
    private Path value;

    public FilePath(Path value, NutsSession session) {
        super(session);
        if (value == null) {
            throw new IllegalArgumentException("invalid value");
        }
        this.value = value;
    }

    public boolean exists() {
        return Files.exists(value);
    }

    public long length() {
        try {
            return Files.size(value);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Path toFilePath() {
        return value;
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

    public URL toURL() {
        try {
            return value.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new UncheckedIOException(e);
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

    public String name() {
        return CoreIOUtils.getURLName(value.toString());
    }

    @Override
    public String location() {
        return value.toString();
    }

    @Override
    public NutsPath compressedForm() {
        return new NutsCompressedPath(this);
    }
}
