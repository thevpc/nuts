package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.util.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NIOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractNInputSource implements NInputSource {

    public AbstractNInputSource() {
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
    public NStream<String> lines() {
        return lines(null);
    }

    @Override
    public NStream<String> lines(Long from, Long to) {
        return lines(from, to, null);
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
        try (NStream<String> rl = reversedLines()) {
            List<String> list = rl.limit(count).collect(Collectors.toList());
            Collections.reverse(list);
            return NStream.ofStream(list.stream());
        }
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
    public NStream<String> reversedLines() {
        return reversedLines(null);
    }

    @Override
    public NStream<String> reversedLines(Charset cs) {
        // not effective, butthe best I can do for now
        NStream<String> s = lines(cs);
        List<String> list = s.collect(Collectors.toCollection(ArrayList::new));
        Collections.reverse(list);
        return NStream.ofStream(list.stream());
    }

    @Override
    public NStream<String> lines(Charset cs) {
        return CoreIOUtils.bufferedReaderToLinesStream(asBufferedReader(cs));
    }

    /**
     * this is teh default implementation of lines part of any input stream
     *
     * @param from 0-based inclusive index of the first line to return or null. when negative, should consider tail (-from)
     * @param to   0-based exclusive index of the last line to return or null. when negative, should consider tail (-to where -1 relates to the end of the stream)
     * @param cs   charset if provided , if ot use default
     * @return stream of lines
     */
    @Override
    public NStream<String> lines(Long from, Long to, Charset cs) {
        return CoreIOUtils.lines(this, from, to, cs);
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
            return CoreIOUtils.getDigest(input, algo);
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }
}
