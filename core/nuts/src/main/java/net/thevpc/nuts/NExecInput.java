package net.thevpc.nuts;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;

import java.io.*;
import java.util.Arrays;
import java.util.Objects;

public class NExecInput {
    private NExecRedirectType type;
    private InputStream stream;
    private NPath path;
    private NPathOption[] options;

    public static NExecInput ofInherit() {
        return new NExecInput(NExecRedirectType.INHERIT, null, null, null);
    }

    public static NExecInput ofStream(InputStream stream) {
        return stream == null ? ofInherit() : new NExecInput(NExecRedirectType.STREAM, stream, null, null);
    }

    public static NExecInput ofPipe() {
        return new NExecInput(NExecRedirectType.PIPE, null, null, null);
    }

    public static NExecInput ofPath(NPath path) {
        return path == null ? ofInherit() : new NExecInput(NExecRedirectType.PATH, null, path, null);
    }

    public static NExecInput ofPath(NPath file, NPathOption... options) {
        if (file == null) {
            return ofInherit();
        }
        if (options == null || options.length == 0) {
            return ofPath(file);
        }
        options = Arrays.stream(options).filter(Objects::nonNull).toArray(NPathOption[]::new);
        if (options.length == 0) {
            return ofPath(file);
        }
        return ofStream(file.getInputStream(options));
    }

    private NExecInput(NExecRedirectType type, InputStream stream, NPath path, NPathOption[] options) {
        this.type = type;
        this.stream = stream;
        this.path = path;
        this.options = options == null ? new NPathOption[0] : Arrays.stream(options).filter(Objects::nonNull).toArray(NPathOption[]::new);
    }

    public NExecRedirectType getType() {
        return type;
    }

    public InputStream getStream() {
        return stream;
    }

    public NPath getPath() {
        return path;
    }

    public NPathOption[] getOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "NExecInput{" +
                "mode=" + type +
                ", stream=" + stream +
                ", path=" + path +
                ", options=" + Arrays.toString(options) +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NExecInput that = (NExecInput) o;
        return type == that.type && Objects.equals(stream, that.stream) && Objects.equals(path, that.path) && Arrays.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, stream, path);
        result = 31 * result + Arrays.hashCode(options);
        return result;
    }
}
