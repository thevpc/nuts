package net.thevpc.nuts.runtime.core.io;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public abstract class NutsPathInput extends CoreIOUtils.AbstractMultiReadItem {

    public NutsPathInput(NutsPath value) {
        super(value.asString(),
                value.format(),
                value, value.isFilePath(), true, "nutsPath", value.getSession());
    }

    public NutsPath getNutsPath() {
        return (NutsPath) getSource();
    }

    @Override
    public Path getFilePath() {
        return getNutsPath().toFilePath();
    }

    @Override
    public URL getURL() {
        return getNutsPath().toURL();
    }

    @Override
    public void copyTo(Path path) {
        if (!Files.isRegularFile(this.getFilePath())) {
            throw createOpenError(new FileNotFoundException(this.getFilePath().toString()));
        }
        try {
            try (java.io.InputStream in = open()) {
                try (OutputStream os = Files.newOutputStream(path)) {
                    CoreIOUtils.copy(in, os);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public long length() {
        return getNutsPath().getContentLength();
    }

    @Override
    public String getContentType() {
        return null;
    }

    @Override
    public String getContentEncoding() {
        return null;
    }

    @Override
    public Instant getLastModifiedInstant() {
        return getNutsPath().getLastModifiedInstant();
    }

    @Override
    public String toString() {
        return getNutsPath().toString();
    }

}
