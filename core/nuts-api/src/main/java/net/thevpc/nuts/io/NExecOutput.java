package net.thevpc.nuts.io;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.OutputStream;

/**
 * Represents an output stream, file, or buffer for capturing the
 * results of command execution.
 *
 * <p>It supports multiple redirection modes:
 * <ul>
 *     <li>{@link NRedirectType#STREAM} - capture to an OutputStream</li>
 *     <li>{@link NRedirectType#GRAB_STREAM} / {@link NRedirectType#GRAB_FILE} - capture to memory or file</li>
 *     <li>{@link NRedirectType#PIPE} - connect to another process</li>
 *     <li>{@link NRedirectType#INHERIT} - inherit parent's output</li>
 *     <li>{@link NRedirectType#PATH} - redirect to a file path</li>
 *     <li>{@link NRedirectType#NULL} - discard output</li>
 * </ul>
 *
 * <p>Grab modes allow retrieving captured output via {@link #getResultBytes()} or
 * {@link #getResultString()}.
 */
public class NExecOutput {
    private NRedirectType type;
    private OutputStream stream;
    private NPath path;
    private NPathOption[] options;
    private NInputSource result;
    private final long maxLines;
    private final long maxBytes;


    /**
     * Discards the command output (like `/dev/null`).
     *
     * @return a NExecOutput configured to discard output
     */
    public static NExecOutput ofNull() {
        return new NExecOutput(NRedirectType.NULL, null, null, null, -1, -1);
    }

    /**
     * Captures the command output into memory.
     * <p>
     * The result can be read later via {@link #getResultBytes()} or {@link #getResultString()}.
     *
     * @return a NExecOutput configured to capture output in memory
     */
    public static NExecOutput ofGrabMem() {
        return new NExecOutput(NRedirectType.GRAB_STREAM, null, null, null, -1, -1);
    }

    public static NExecOutput ofGrabMem(int maxBytes, int maxLines) {
        return new NExecOutput(NRedirectType.GRAB_STREAM, null, null, null, maxBytes, maxLines);
    }

    /**
     * Captures the command output into a temporary file.
     * <p>
     * Useful for commands producing large output that may not fit in memory.
     *
     * @return a NExecOutput configured to capture output to a temporary file
     */
    public static NExecOutput ofGrabFile() {
        return new NExecOutput(NRedirectType.GRAB_FILE, null, null, null, -1, -1);
    }

    /**
     * Inherits the parent process output streams.
     * <p>
     * Output will appear in the same console or terminal as the JVM process.
     *
     * @return a NExecOutput configured to inherit output
     */
    public static NExecOutput ofInherit() {
        return new NExecOutput(NRedirectType.INHERIT, null, null, null, -1, -1);
    }


    /**
     * Uses default redirection determined by the executor.
     *
     * @return a NExecOutput configured to use default redirection
     */
    public static NExecOutput ofRedirect() {
        return new NExecOutput(NRedirectType.REDIRECT, null, null, null, -1, -1);
    }

    /**
     * Redirects command output to a provided {@link NPrintStream}.
     * <p>
     * If the stream is null, defaults to {@link #ofInherit()}.
     *
     * @param stream the target print stream
     * @return a NExecOutput configured to write output to the given stream
     */
    public static NExecOutput ofStream(NPrintStream stream) {
        return stream == null ? ofInherit() : ofStream(stream.asOutputStream());
    }

    /**
     * Redirects command output to a provided {@link OutputStream}.
     * <p>
     * If the stream is null, defaults to {@link #ofInherit()}.
     *
     * @param stream the target output stream
     * @return a NExecOutput configured to write output to the given stream
     */
    public static NExecOutput ofStream(OutputStream stream) {
        return stream == null ? ofInherit() : new NExecOutput(NRedirectType.STREAM, stream, null, null, -1, -1);
    }

    /**
     * Redirects command output into a pipe to another process.
     *
     * @return a NExecOutput configured to pipe output
     */
    public static NExecOutput ofPipe() {
        return new NExecOutput(NRedirectType.PIPE, null, null, null, -1, -1);
    }


    /**
     * Redirects command output to the specified file path.
     *
     * @param path    the target file path
     * @param options optional path options (e.g., append)
     * @return a NExecOutput configured to write output to the file
     */
    public static NExecOutput ofPath(NPath path, NPathOption... options) {
        return path == null ? ofInherit() : new NExecOutput(NRedirectType.PATH, null, path, options, -1, -1);
    }

    /**
     * Redirects command output to the specified file path with optional append mode.
     *
     * @param path   the target file path
     * @param append if true, output is appended; otherwise, it overwrites the file
     * @return a NExecOutput configured to write output to the file
     */
    public static NExecOutput ofPath(NPath path, boolean append) {
        return path == null ? ofInherit() : new NExecOutput(NRedirectType.PATH, null, path, append ? null : new NPathOption[]{NPathOption.APPEND}, -1, -1);
    }

    /**
     * Redirects command output to the specified file path (overwrite mode).
     *
     * @param path the target file path
     * @return a NExecOutput configured to write output to the file
     */
    public static NExecOutput ofPath(NPath path) {
        return path == null ? ofInherit() : new NExecOutput(NRedirectType.PATH, null, path, null, -1, -1);
    }

    private NExecOutput(NRedirectType type, OutputStream stream, NPath path, NPathOption[] options, long maxBytes, long maxLines) {
        this.type = type;
        this.stream = stream;
        this.path = path;
        this.options = options == null ? new NPathOption[0] : options;
        this.maxLines = maxLines;
        this.maxBytes = maxBytes;
    }

    public long getMaxLines() {
        return maxLines;
    }

    public long getMaxBytes() {
        return maxBytes;
    }

    public NRedirectType getType() {
        return type;
    }

    public NOptional<NInputSource> getResultSource() {
        switch (getType()) {
            case GRAB_STREAM:
            case GRAB_FILE: {
                if (result != null) {
                    return NOptional.of(result);
                }
                return NOptional.ofEmpty(() -> NMsg.ofPlain("grabbed result is not available"));
            }
        }
        return NOptional.ofEmpty(() -> NMsg.ofPlain("no buffer was configured; should call setGrabOutString"));
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

    public NExecOutput setType(NRedirectType type) {
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
