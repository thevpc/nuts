package net.thevpc.nuts;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPrintStream;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Objects;

public class NExecOutput {
    private NExecRedirectType type;
    private OutputStream stream;
    private NPath path;
    private NPathOption[] options;

    public static NExecOutput ofNull() {
        return new NExecOutput(NExecRedirectType.NULL, null, null, null);
    }
    public static NExecOutput ofGrabMem() {
        return new NExecOutput(NExecRedirectType.GRAB_STREAM, new ByteArrayOutputStream(), null, null);
    }

    public static NExecOutput ofGrabFile() {
        return new NExecOutput(NExecRedirectType.GRAB_FILE, new ByteArrayOutputStream(), null, null);
    }

    public static NExecOutput ofInherit() {
        return new NExecOutput(NExecRedirectType.INHERIT, null, null, null);
    }

    public static NExecOutput ofRedirect() {
        return new NExecOutput(NExecRedirectType.REDIRECT, null, null, null);
    }

    public static NExecOutput ofStream(NPrintStream stream) {
        return stream == null ? ofInherit() : ofStream(stream.asOutputStream());
    }

    public static NExecOutput ofStream(OutputStream stream) {
        return stream == null ? ofInherit() : new NExecOutput(NExecRedirectType.STREAM, stream, null, null);
    }

    public static NExecOutput ofPipe() {
        return new NExecOutput(NExecRedirectType.PIPE, null, null, null);
    }

    public static NExecOutput ofPath(NPath path, NPathOption... options) {
        return path == null ? ofInherit() : new NExecOutput(NExecRedirectType.PATH, null, path, options);
    }

    public static NExecOutput ofPath(NPath path, boolean append) {
        return path == null ? ofInherit() : new NExecOutput(NExecRedirectType.PATH, null, path, append ? null : new NPathOption[]{NPathOption.APPEND});
    }

    public static NExecOutput ofPath(NPath path) {
        return path == null ? ofInherit() : new NExecOutput(NExecRedirectType.PATH, null, path, null);
    }

    private NExecOutput(NExecRedirectType type, OutputStream stream, NPath path, NPathOption[] options) {
        this.type = type;
        this.stream = stream;
        this.path = path;
        this.options = options == null ? new NPathOption[0] : options;
    }

    public NExecRedirectType getType() {
        return type;
    }

    public byte[] getResultBytes() {
        switch (getType()){
            case GRAB_STREAM:
            case GRAB_FILE:
            {
                return ((ByteArrayOutputStream)getStream()).toByteArray();
            }
        }
        throw new IllegalStateException("");
    }
    public String getResultString() {
        return new String(getResultBytes());
    }

    public OutputStream getStream() {
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
        return "NExecOutput{" +
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
        NExecOutput that = (NExecOutput) o;
        return type == that.type && Objects.equals(stream, that.stream) && Objects.equals(path, that.path) && Arrays.equals(options, that.options);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(type, stream, path);
        result = 31 * result + Arrays.hashCode(options);
        return result;
    }
}
