package net.thevpc.nuts.runtime.standalone.io;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePath urlPath = (FilePath) o;
        return Objects.equals(value, urlPath.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    public String getName() {
        return CoreIOUtils.getURLName(value.toString());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
