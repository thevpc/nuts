package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.io.util.AbstractMultiReadNInputSource;
import net.thevpc.nuts.spi.NPathSPIAware;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public abstract class NPathBase extends AbstractMultiReadNInputSource implements NPath, NPathSPIAware {

    public static final int BUFFER_SIZE = 8192;
    private DefaultNPathMetadata omd = new DefaultNPathMetadata(this);
    private boolean deleteOnDispose;

    public NPathBase() {
        super();
    }

    @Override
    public InputStream getInputStream() {
        return getInputStream(new NPathOption[0]);
    }

    @Override
    public OutputStream getOutputStream() {
        return getOutputStream(new NPathOption[0]);
    }

    protected NPath copyExtraFrom(NPath other) {
        this.deleteOnDispose = other.isDeleteOnDispose();
        if (other instanceof NPathBase) {
            omd.copyFrom(((NPathBase) other).omd);
        } else {
            omd.copyFrom(other.getMetaData());
            omd.copyFrom(other.getMetaData());
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
            throw new NIllegalArgumentException(NMsg.ofPlain("unsupported encoding"), e);
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
    public PrintStream getPrintStream() {
        OutputStream out = getOutputStream();
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
        try (InputStream reader = getInputStream()) {
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
        try (OutputStream out = getOutputStream()) {
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
        try (InputStream in = other.getInputStream()) {
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
        NAssert.requireNonNull(other, "other");
        other.copyTo(this, options);
    }

    @Override
    public void copyToWriter(Writer other, NPathOption... options) {
        copyToWriter(other, null, options);
    }

    @Override
    public void copyToWriter(Writer other, Charset cs, NPathOption... options) {
        try (Reader reader = getReader(cs)) {
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
    public NPath setUserCache(boolean userCache) {
        this.omd.setUserCache(userCache);
        return this;
    }

    @Override
    public boolean isUserTemporary() {
        return omd.isUserTemporary();
    }

    @Override
    public NPath setUserTemporary(boolean temporary) {
        this.omd.setUserTemporary(temporary);
        return this;
    }

    @Override
    public NPathNameParts getNameParts() {
        return getNameParts(NPathExtensionType.LONG);
    }

    @Override
    public NPathNameParts getNameParts(NPathExtensionType type) {
        if (type == null) {
            type = NPathExtensionType.SHORT;
        }
        switch (type) {
            case SMART: {
                return getSmartFileNameParts();
            }
            case LONG: {
                String n = getName();
                int i = n.indexOf('.');
                if (i < 0) {
                    return new NPathNameParts(n, "", "", NPathExtensionType.LONG);
                }
                return new NPathNameParts(n.substring(0, i), n.substring(i + 1), n.substring(i), NPathExtensionType.LONG);
            }
            case SHORT: {
                String n = getName();
                int i = n.lastIndexOf('.');
                if (i < 0) {
                    return new NPathNameParts(n, "", "", NPathExtensionType.SHORT);
                }
                return new NPathNameParts(n.substring(0, i), n.substring(i + 1), n.substring(i), NPathExtensionType.SHORT);
            }
        }
        throw new NUnexpectedException(NMsg.ofC("%s not supported", type));
    }

    public NPathNameParts getSmartFileNameParts() {
        String n = getName();
        int li = n.indexOf('.');
        if (li < 0) {
            return new NPathNameParts(n, "", "", NPathExtensionType.SMART);
        }
        NLiteral[] vals = NVersion.get(n).get().split();
        int lastDot = -1;
        for (int i = vals.length - 1; i >= 0; i--) {
            NLiteral v = vals[i];
            String u = v.asString().get();
            if (u.equals(".")) {
                if (i == vals.length - 1) {
                    return rebuildSmartParts(vals, i);
                }
                NLiteral v2 = vals[i + 1];
                if (v2.asNumber().isPresent()) {
                    //check if the part before is also a number
                    if (i > 0 && vals[i - 1].asNumber().isPresent()) {
                        if (i + 1 == vals.length - 1) {
                            return rebuildSmartParts(vals, i + 2);
                        } else if (vals[i + 1].asString().get().equals(".")) {
                            return rebuildSmartParts(vals, i + 1);
                        }
                    }
                } else {
                    //continue
                }
                if (lastDot == -1) {
                    lastDot = i;
                } else {
                    break;
                }
            }
        }
        if (lastDot < 0) {
            return new NPathNameParts(n, "", ".", NPathExtensionType.SMART);
        }
        return rebuildSmartParts(vals, lastDot);
    }

    private NPathNameParts rebuildSmartParts(NLiteral[] vals, int split) {
        String fe = concatSmartParts(vals, split, vals.length);
        String e = fe.startsWith(".") ? fe.substring(1) : fe;

        return new NPathNameParts(
                concatSmartParts(vals, 0, split),
                e,
                fe,
                NPathExtensionType.SMART
        );
    }

    private String concatSmartParts(NLiteral[] vals, int from, int to) {
        StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(vals[i].asString().get());
        }
        return sb.toString();
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

    public static class PathFormat extends DefaultFormatBase<NFormat> {

        private final NPathBase p;

        public PathFormat(NPathBase p) {
            super("path");
            this.p = p;
        }

        @Override
        public void print(NPrintStream out) {
            out.print(NText.ofStyled(p.toNutsString(), NTextStyle.path()));
        }

        @Override
        public boolean configureFirst(NCmdLine cmdLine) {
            return false;
        }

        @Override
        public int getSupportLevel(NSupportLevelContext context) {
            return NConstants.Support.DEFAULT_SUPPORT;
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
        return getWriter((Charset) null, options);
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
        return getReader((Charset) null);
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
    public NContentMetadata getMetaData() {
        return omd.getMetaData();
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
        return writeBytes(string.getBytes(nonNullCharset(cs)));
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
    public void setDeleteOnDispose(boolean deleteOnDispose) {
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
                d.setAlgorithm(algo);
                d.addSource(type().name().getBytes());
                for (NPath nPath : list()) {
                    d.addSource(nPath.getName().getBytes());
                }
                return d.computeBytes();
            }
            case FILE: {
                NDigest d = NDigest.of();
                d.setAlgorithm(algo);
                d.addSource(type().name().getBytes());
                d.addSource(this);
                return d.computeBytes();
            }
            default:{
                NDigest d = NDigest.of();
                d.setAlgorithm(algo);
                d.addSource(type().name().getBytes());
                return d.computeBytes();
            }
        }
    }

}
