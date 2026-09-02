package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.artifact.NVersionPart;
import net.thevpc.nuts.artifact.NVersionPartType;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.format.DefaultObjectWriterBase;
import net.thevpc.nuts.text.NObjectWriter;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractMultiReadNInputSource;
import net.thevpc.nuts.spi.NPathSPIAware;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.*;
import java.util.function.Function;

public abstract class NPathBase extends AbstractMultiReadNInputSource implements NPath, NPathSPIAware {

    public static final int BUFFER_SIZE = 8192;
    private final DefaultNPathMetadata omd = new DefaultNPathMetadata(this);
    private boolean deleteOnDispose;

    public NPathBase() {
        super();
    }

    @Override
    public InputStream inputStream() {
        return getInputStream();
    }

    @Override
    public OutputStream outputStream() {
        return getOutputStream();
    }

    protected NPath copyExtraFrom(NPath other) {
        this.deleteOnDispose = other.isDeleteOnDispose();
        if (other instanceof NPathBase) {
            omd.copyFrom(((NPathBase) other).omd);
        } else {
            omd.copyFrom(other.metaData());
            omd.copyFrom(other.metaData());
        }
        return this;
    }

    @Override
    public boolean isKnownContentLength() {
        return true;
    }

    @Override
    public PrintStream getPrintStream(Charset cs, NPathOption... options) {
        OutputStream out = getOutputStream(options);
        if (out instanceof PrintStream) {
            return (PrintStream) out;
        }
        try {
            return new PrintStream(out, false, nonNullCharset(cs).name());
        } catch (UnsupportedEncodingException e) {
            throw new NIllegalArgumentException(NMsg.ofP("unsupported encoding"), e);
        }
    }

    @Override
    public PrintStream getPrintStream(NPathOption... options) {
        OutputStream out = getOutputStream(options);
        if (out instanceof PrintStream) {
            return (PrintStream) out;
        }
        return new PrintStream(out);
    }

    @Override
    public NPrintStream getNPrintStream(NPathOption... options) {
        OutputStream out = getOutputStream(options);
        if (out instanceof NPrintStream) {
            return (NPrintStream) out;
        }
        return NPrintStream.of(out);
    }

    @Override
    public PrintStream getPrintStream() {
        OutputStream out = outputStream();
        if (out instanceof PrintStream) {
            return (PrintStream) out;
        }
        return new PrintStream(out);
    }

    @Override
    public BufferedReader getBufferedReader(NPathOption... options) {
        return getBufferedReader(null, options);
    }

    @Override
    public BufferedReader getBufferedReader(Charset cs, NPathOption... options) {
        Reader r = getReader(cs, options);
        if (r instanceof BufferedReader) {
            return (BufferedReader) r;
        }
        return new BufferedReader(r);
    }

    @Override
    public void copyToPrintStream(PrintStream other, NPathOption... options) {
        copyToPrintStream(other, null, options);
    }

    @Override
    public void copyToPrintStream(PrintStream other, Charset cs, NPathOption... options) {
        try (Reader reader = getReader(options)) {
            char[] buffer = new char[BUFFER_SIZE];
            int count;
            while ((count = reader.read(buffer)) > 0) {
                other.print(Arrays.copyOf(buffer, count));
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void copyToOutputStream(OutputStream other, NPathOption... options) {
        try (InputStream reader = inputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = reader.read(buffer)) > 0) {
                other.write(buffer, 0, count);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void copyFromInputStream(InputStream other, NPathOption... options) {
        try (OutputStream out = outputStream()) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int count;
            while ((count = other.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void copyFromInputStreamProvider(NInputStreamProvider other, NPathOption... options) {
        try (InputStream in = other.inputStream()) {
            try (OutputStream out = getOutputStream(options)) {
                byte[] buffer = new byte[BUFFER_SIZE];
                int count;
                while ((count = in.read(buffer)) > 0) {
                    out.write(buffer, 0, count);
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void copyFromReader(Reader other, NPathOption... options) {
        try (Writer writer = getWriter()) {
            char[] buffer = new char[BUFFER_SIZE];
            int count;
            while ((count = other.read(buffer)) > 0) {
                writer.write(buffer, 0, count);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void copyFromReader(Reader other, Charset charset, NPathOption... options) {
        try (Writer writer = getWriter(charset, options)) {
            char[] buffer = new char[BUFFER_SIZE];
            int count;
            while ((count = other.read(buffer)) > 0) {
                writer.write(buffer, 0, count);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public void copyFrom(NPath other, NPathOption... options) {
        NAssert.requireNamedNonNull(other, "other");
        other.copyTo(this, options);
    }

    @Override
    public void copyToWriter(Writer other, NPathOption... options) {
        copyToWriter(other, null, options);
    }

    @Override
    public void copyToWriter(Writer other, Charset cs, NPathOption... options) {
        try (Reader reader = asReader(cs)) {
            char[] buffer = new char[BUFFER_SIZE];
            int count;
            while ((count = reader.read(buffer)) > 0) {
                other.write(buffer, 0, count);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    @Override
    public boolean isUserCache() {
        return omd.isUserCache();
    }

    @Override
    public NPath userCache(boolean userCache) {
        this.omd.userCache(userCache);
        return this;
    }

    @Override
    public boolean isUserTemporary() {
        return omd.isUserTemporary();
    }

    @Override
    public NPath userTemporary(boolean temporary) {
        this.omd.userTemporary(temporary);
        return this;
    }

    @Override
    public NPathNameParts nameParts() {
        return nameParts(NPathExtensionType.SMART);
    }

    public NPath resolveSibling(NPathRenameOptions renameOptions) {
        if (renameOptions == null) {
            return this;
        }
        NPathExtensionType t = renameOptions.type();
        if (t == null) {
            t = NPathExtensionType.SMART;
        }
        String template = renameOptions.template();
        String extension = renameOptions.extension();
        if (!NBlankable.isBlank(template)) {
            return resolveSibling(
                    nameParts(t).toName(template)
            );
        }
        if (!NBlankable.isBlank(extension)) {
            return resolveSibling(
                    nameParts(t).toNameWithExtension(extension)
            );
        }
        return this;
    }

    @Override
    public NPath rename(Function<NPath, String> newNameResolver, NPathOption... options) {
        NPath p2 = this.resolveSibling(newNameResolver);
        moveTo(p2, options);
        return p2;
    }

    @Override
    public NPath resolveSibling(Function<NPath, String> newNameResolver) {
        return this.resolveSibling(newNameResolver.apply(this));
    }

    @Override
    public NPathNameParts nameParts(NPathExtensionType type) {
        if (type == null) {
            type = NPathExtensionType.SHORT;
        }
        switch (type) {
            case SMART: {
                return NPathNamePartsUtils.getSmartFileNameParts(name());
            }
            case LONG: {
                return NPathNamePartsUtils.getLongFileNameParts(name());
            }
            case SHORT: {
                return NPathNamePartsUtils.getShortFileNameParts(name());
            }
        }
        throw new NUnexpectedException(NMsg.ofC("%s not supported", type));
    }

    @Override
    public boolean isURL() {
        return toURL().isPresent();
    }

    @Override
    public boolean isFile() {
        return toFile().orNull() != null;
    }

    @Override
    public NPath delete() {
        return delete(false);
    }

    public NText toNutsString() {
        return NText.ofPlain(toString());
    }


    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NPathBase that = (NPathBase) o;
        return Objects.equals(toString(), that.toString());
    }

    @NScore(fixed = NScorable.DEFAULT_SCORE)
    public static class PathObjectWriter extends DefaultObjectWriterBase<NObjectWriter> {


        public PathObjectWriter() {
            super("path");
        }

        @Override
        public void print(Object aValue, NPrintStream out) {
            out.print(NText.ofStyled(((NPathBase) aValue).toNutsString(), NTextStyle.path()));
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }

    }

    @Override
    public NStream<NPath> walk() {
        return walk(Integer.MAX_VALUE, new NPathOption[0]);
    }

    @Override
    public NStream<NPath> walk(NPathOption... options) {
        return walk(Integer.MAX_VALUE, options);
    }

    @Override
    public NStream<NPath> walk(int maxDepth) {
        return walk(maxDepth <= 0 ? Integer.MAX_VALUE : maxDepth, new NPathOption[0]);
    }

    @Override
    public Writer getWriter() {
        return getWriter(null, new NPathOption[0]);
    }

    @Override
    public BufferedWriter getBufferedWriter() {
        return getBufferedWriter(null, new NPathOption[0]);
    }

    @Override
    public Writer getWriter(NPathOption... options) {
        return getWriter(null, options);
    }

    @Override
    public Writer getWriter(Charset charset, NPathOption... options) {
        return new OutputStreamWriter(getOutputStream(options), nonNullCharset(charset));
    }

    @Override
    public BufferedWriter getBufferedWriter(NPathOption... options) {
        Writer w = getWriter(options);
        if (w instanceof BufferedWriter) {
            return (BufferedWriter) w;
        }
        return new BufferedWriter(w);
    }

    @Override
    public BufferedWriter getBufferedWriter(Charset charset, NPathOption... options) {
        Writer w = getWriter(charset, options);
        if (w instanceof BufferedWriter) {
            return (BufferedWriter) w;
        }
        return new BufferedWriter(w);
    }

    @Override
    public Reader getReader(NPathOption... options) {
        return asReader(null);
    }

    @Override
    public Reader getReader(Charset cs, NPathOption... options) {
        CharsetDecoder decoder = nonNullCharset(cs).newDecoder();
        Reader reader = new InputStreamReader(getInputStream(options), decoder);
        return new BufferedReader(reader);
    }

    @Override
    public boolean isHttp() {
        if (!isURL()) {
            return false;
        }
        String s = toString();
        return s.startsWith("http://") || s.startsWith("https://");
    }

    @Override
    public NContentMetadata metaData() {
        return omd.metaData();
    }

    @Override
    public boolean isMultiRead() {
        return true;
    }

    @Override
    public void dispose() {
        if (isDeleteOnDispose()) {
            this.deleteTree();
        }
    }

    @Override
    public NPath writeString(String string, Charset cs, NPathOption... options) {
        return writeBytes(string == null ? new byte[0] : string.getBytes(nonNullCharset(cs)));
    }

    @Override
    public NPath writeString(String string, NPathOption... options) {
        return writeString(string, null, options);
    }

    @Override
    public String readString(NPathOption... options) {
        return readString(null, options);
    }

    @Override
    public String readString(Charset cs, NPathOption... options) {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[BUFFER_SIZE];
        try (Reader reader = getReader(cs, options)) {
            while (true) {
                int len = reader.read(buffer);
                if (len > 0) {
                    sb.append(buffer, 0, len);
                } else {
                    break;
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return sb.toString();
    }

    @Override
    public List<NPath> list() {
        return stream().toList();
    }

    @Override
    public void deleteOnDispose(boolean deleteOnDispose) {
        this.deleteOnDispose = deleteOnDispose;
    }

    @Override
    public boolean isDeleteOnDispose() {
        return deleteOnDispose;
    }

    protected static NPath unwrapPath(NPath other) {
        if (other instanceof NCompressedPathBase) {
            other = ((NCompressedPathBase) other).getBase();
        }
        if (other instanceof NCompressedPath) {
            other = ((NCompressedPath) other).getBase();
        }
        return other;
    }

    @Override
    public byte[] getDigest(String algo) {
        NPathType type = type();
        switch (type) {
            case NOT_FOUND:
                return new byte[0];
            case DIRECTORY: {
                NDigest d = NDigest.of();
                d.algorithm(algo);
                d.source(type().name().getBytes());
                for (NPath nPath : list()) {
                    d.source(nPath.name().getBytes());
                }
                return d.computeBytes();
            }
            case FILE: {
                NDigest d = NDigest.of();
                d.algorithm(algo);
                d.source(type().name().getBytes());
                d.source(this);
                return d.computeBytes();
            }
            default: {
                NDigest d = NDigest.of();
                d.algorithm(algo);
                d.source(type().name().getBytes());
                return d.computeBytes();
            }
        }
    }

}
