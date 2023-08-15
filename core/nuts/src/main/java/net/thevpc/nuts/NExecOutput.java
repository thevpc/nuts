package net.thevpc.nuts;

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.OutputStream;

public class NExecOutput {
    private NExecRedirectType type;
    private OutputStream stream;
    private NPath path;
    private NPathOption[] options;
    private NInputSource result;

    public static NExecOutput ofNull() {
        return new NExecOutput(NExecRedirectType.NULL, null, null, null);
    }

    public static NExecOutput ofGrabMem() {
        return new NExecOutput(NExecRedirectType.GRAB_STREAM, null, null, null);
    }

    public static NExecOutput ofGrabFile() {
        return new NExecOutput(NExecRedirectType.GRAB_FILE, null, null, null);
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

    public NOptional<NInputSource> getResultSource() {
        switch (getType()) {
            case GRAB_STREAM:
            case GRAB_FILE: {
                if (result != null) {
                    return NOptional.of(result);
                }
                return NOptional.ofEmpty(s -> NMsg.ofPlain("grabbed result is not available"));
            }
        }
        return NOptional.ofEmpty(s -> NMsg.ofPlain("no buffer was configured; should call setGrabOutString"));
    }

    public byte[] getResultBytes() {
        NInputSource s = null;
        try {
            s = getResultSource().get();
            return s.readBytes();
        } finally {
            if (s != null) {
                s.dispose();
            }
        }
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

    public NExecOutput setType(NExecRedirectType type) {
        this.type = type;
        return this;
    }

    public NExecOutput setStream(OutputStream stream) {
        this.stream = stream;
        return this;
    }

    public NExecOutput setPath(NPath path) {
        this.path = path;
        return this;
    }

    public NExecOutput setOptions(NPathOption[] options) {
        this.options = options;
        return this;
    }

    public NInputSource getResult() {
        return result;
    }

    public NExecOutput setResult(NInputSource result) {
        this.result = result;
        return this;
    }
}
