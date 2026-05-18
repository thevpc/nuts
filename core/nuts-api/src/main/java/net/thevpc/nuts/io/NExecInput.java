package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NGetter;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Represents the input for a command execution.
 *
 * <p>Supports multiple sources:
 * <ul>
 *     <li>{@link NRedirectType#STREAM} - read from an InputStream</li>
 *     <li>{@link NRedirectType#PATH} - read from a file path</li>
 *     <li>{@link NRedirectType#PIPE} - input from another process</li>
 *     <li>{@link NRedirectType#INHERIT} - inherit parent's input</li>
 *     <li>{@link NRedirectType#NULL} - no input</li>
 * </ul>
 */
public class NExecInput {
    private final NRedirectType type;
    private final InputStream stream;
    private final NPath path;
    private final NPathOption[] options;

    public static NExecInput ofNull() {
        return new NExecInput(NRedirectType.NULL, null, null, null);
    }

    public static NExecInput ofInherit() {
        return new NExecInput(NRedirectType.INHERIT, null, null, null);
    }

    public static NExecInput ofStream(InputStream stream) {
        return stream == null ? ofInherit() : new NExecInput(NRedirectType.STREAM, stream, null, null);
    }

    public static NExecInput ofBytes(byte[] bytes) {
        return bytes == null ? ofInherit() : new NExecInput(NRedirectType.STREAM, new ByteArrayInputStream(bytes), null, null);
    }

    public static NExecInput ofString(String string) {
        return string == null ? ofInherit() : new NExecInput(NRedirectType.STREAM, new ByteArrayInputStream(string.getBytes()), null, null);
    }

    public static NExecInput ofPipe() {
        return new NExecInput(NRedirectType.PIPE, null, null, null);
    }

    public static NExecInput ofPath(NPath path) {
        return path == null ? ofInherit() : new NExecInput(NRedirectType.PATH, null, path, null);
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

    private NExecInput(NRedirectType type, InputStream stream, NPath path, NPathOption[] options) {
        this.type = type;
        this.stream = stream;
        this.path = path;
        this.options = options == null ? new NPathOption[0] : Arrays.stream(options).filter(Objects::nonNull).toArray(NPathOption[]::new);
    }

    @NGetter

    public NRedirectType type() {
        return type;
    }

    public InputStream inputStream() {
        return stream;
    }

    public NPath path() {
        return path;
    }

    public List<NPathOption> options() {
        return Arrays.asList(options);
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
