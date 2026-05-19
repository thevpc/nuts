package net.thevpc.nuts.runtime.standalone.io.inputstream;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.util.NHex;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NTempOutputStreamImpl extends NTempOutputStream {

    DefaultNContentMetadata md = new DefaultNContentMetadata();
    boolean mem = true;
    long maxSize = 1024 * 1024 * 10;
    long contentLength = 0;
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    NPath file;
    OutputStream fos;
    Consumer<InputStream> onCompleted;
    boolean closed = false;

    public NTempOutputStreamImpl setOnCompleted(Consumer<InputStream> onCompleted) {
        this.onCompleted = onCompleted;
        return this;
    }

    @Override
    public void write(int b) throws IOException {
        if (mem) {
            bos.write(b);
            contentLength++;
            ensureBucket();
        } else {
            fos.write(b);
        }
    }

    @Override
    public void write(byte[] b) throws IOException {
        if (mem) {
            bos.write(b);
            contentLength += b.length;
            ensureBucket();
        } else {
            fos.write(b);
            contentLength += b.length;
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (mem) {
            bos.write(b, off, len);
            contentLength += len;
            ensureBucket();
        } else {
            fos.write(b);
            contentLength += len;
        }
    }

    @Override
    public void flush() throws IOException {
        if (mem) {
            bos.flush();
        } else {
            fos.flush();
        }
    }

    private void ensureBucket() {
        if (mem && contentLength > maxSize) {
            file = NPath.ofTempFile();
            byte[] currBytes = bos.toByteArray();
            bos = null;
            mem = false;
            fos = file.outputStream();
            try {
                fos.write(currBytes);
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        if (fos != null) {
            fos.close();
            if (onCompleted != null) {
                try (InputStream in = file.inputStream()) {
                    onCompleted.accept(in);
                }
            }
            file.delete();
        } else {
            if (onCompleted != null) {
                try (InputStream in = new ByteArrayInputStream(bos.toByteArray())) {
                    onCompleted.accept(in);
                }
            }
        }
        closed = true;
    }

    @Override
    public String name() {
        return metaData().name().orNull();
    }

    @Override
    public String contentType() {
        return metaData().contentType().orNull();
    }

    @Override
    public String charset() {
        return metaData().charset().orNull();
    }

    @Override
    public NStream<String> reversedLines(Charset cs) {
        if (mem) {
            NStream<String> s = lines(cs);
            List<String> list = s.collect(Collectors.toCollection(ArrayList::new));
            Collections.reverse(list);
            return NStream.ofStream(list.stream());
        } else {
            return file.reversedLines(cs);
        }
    }

    @Override
    public NStream<String> reversedLines() {
        return reversedLines(null);
    }

    @Override
    public NStream<String> lines() {
        return lines(null);
    }

    @Override
    public NStream<String> lines(Long from, Long to) {
        return lines(from, to, null);
    }

    @Override
    public NStream<String> lines(Long from, Long to, Charset cs) {
        return CoreIOUtils.lines(this,from,to,cs);
    }

    @Override
    public String readString() {
        return new String(readBytes());
    }

    @Override
    public byte[] readBytes() {
        try (InputStream in = inputStream()) {
            return NIOUtils.readBytes(in);
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public BufferedReader asBufferedReader() {
        return asBufferedReader(null);
    }

    @Override
    public BufferedReader asBufferedReader(Charset cs) {
        Reader r = asReader(cs);
        if (r instanceof BufferedReader) {
            return (BufferedReader) r;
        }
        return new BufferedReader(r);
    }

    @Override
    public NStream<String> tail(long count, Charset cs) {
        LinkedList<String> lines = new LinkedList<>();
        BufferedReader br = asBufferedReader(cs);
        String line;
        try {
            int count0 = 0;
            while ((line = br.readLine()) != null) {
                lines.add(line);
                count0++;
                if (count0 > count) {
                    lines.remove();
                }
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
        return NStream.ofStream(lines.stream());
    }

    @Override
    public NStream<String> head(long count) {
        return head(count, null);
    }

    @Override
    public NStream<String> head(long count, Charset cs) {
        return lines(cs).limit(count);
    }

    @Override
    public NStream<String> tail(long count) {
        return tail(count, null);
    }

    @Override
    public NStream<String> lines(Charset cs) {
        return CoreIOUtils.bufferedReaderToLinesStream(asBufferedReader(cs));
    }

    @Override
    public Reader asReader() {
        return asReader(null);
    }

    @Override
    public Reader asReader(Charset cs) {
        CharsetDecoder decoder = nonNullCharset(cs).newDecoder();
        Reader reader = new InputStreamReader(inputStream(), decoder);
        return new BufferedReader(reader);
    }

    protected Charset nonNullCharset(Charset c) {
        if (c == null) {
            return StandardCharsets.UTF_8;
        }
        return c;
    }

    @Override
    public String digestString() {
        return NHex.fromBytes(digest());
    }

    @Override
    public String getDigestString(String algo) {
        return NHex.fromBytes(getDigest(algo));
    }

    @Override
    public byte[] digest() {
        return getDigest(null);
    }

    @Override
    public byte[] getDigest(String algo) {
        try (InputStream input = inputStream()) {
            return CoreIOUtils.getDigest(input,algo);
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public boolean isMultiRead() {
        return true;
    }

    @Override
    public boolean isKnownContentLength() {
        return true;
    }

    @Override
    public long contentLength() {
        return contentLength;
    }

    @Override
    public NContentMetadata metaData() {
        return md;
    }

    @Override
    public InputStream inputStream() {
        try {
            flush();
        } catch (IOException e) {
            throw new NIOException(e);
        }
        if (mem) {
            return new ByteArrayInputStream(bos.toByteArray());
        } else {
            return file.inputStream();
        }
    }

    @Override
    public void dispose() {
        try {
            this.close();
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }
}
