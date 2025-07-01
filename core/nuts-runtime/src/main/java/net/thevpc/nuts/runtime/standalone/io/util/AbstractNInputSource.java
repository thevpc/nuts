package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.util.NHex;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class AbstractNInputSource implements NInputSource {

    public AbstractNInputSource() {
    }

    @Override
    public String getName() {
        return getMetaData().getName().orNull();
    }

    @Override
    public String getContentType() {
        return getMetaData().getContentType().orNull();
    }

    @Override
    public String getCharset() {
        return getMetaData().getCharset().orNull();
    }


    @Override
    public NStream<String> lines() {
        return lines(null);
    }


    @Override
    public String readString() {
        return new String(readBytes());
    }

    @Override
    public byte[] readBytes() {
        try (InputStream in = getInputStream()) {
            return NIOUtils.readBytes(in);
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    @Override
    public BufferedReader getBufferedReader() {
        return getBufferedReader(null);
    }


    @Override
    public BufferedReader getBufferedReader(Charset cs) {
        Reader r = getReader(cs);
        if (r instanceof BufferedReader) {
            return (BufferedReader) r;
        }
        return new BufferedReader(r);
    }

    @Override
    public List<String> tail(int count, Charset cs) {
        try(NStream<String> rl=reversedLines()){
            List<String> list = rl.limit(count).collect(Collectors.toList());
            Collections.reverse(list);
            return list;
        }
    }

    @Override
    public List<String> head(int count) {
        return head(count, null);
    }

    @Override
    public List<String> head(int count, Charset cs) {
        return lines(cs).limit(count).collect(Collectors.toList());
    }

    @Override
    public List<String> tail(int count) {
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
        BufferedReader br = getBufferedReader(cs);
        try {
            return NStream.ofStream(br.lines().onClose(() -> {
                try {
                    br.close();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }));
        } catch (Error | RuntimeException e) {
            try {
                br.close();
            } catch (IOException ex) {
                try {
                    e.addSuppressed(ex);
                } catch (Throwable ignore) {
                }
            }
            throw e;
        }
    }

    @Override
    public Reader getReader() {
        return getReader(null);
    }

    @Override
    public Reader getReader(Charset cs) {
        CharsetDecoder decoder = nonNullCharset(cs).newDecoder();
        Reader reader = new InputStreamReader(getInputStream(), decoder);
        return new BufferedReader(reader);
    }

    protected Charset nonNullCharset(Charset c) {
        if (c == null) {
            return StandardCharsets.UTF_8;
        }
        return c;
    }

    @Override
    public String getDigestString() {
        return NHex.fromBytes(getDigest());
    }

    @Override
    public String getDigestString(String algo) {
        return NHex.fromBytes(getDigest(algo));
    }

    @Override
    public byte[] getDigest() {
        return getDigest(null);
    }

    @Override
    public byte[] getDigest(String algo) {
        if (NBlankable.isBlank(algo)) {
            algo = "SHA-1";
        }
        try (InputStream input = getInputStream()) {
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance(algo);
            } catch (NoSuchAlgorithmException ex) {
                throw new NIOException(ex);
            }
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = input.read(buffer);
                while (len != -1) {
                    sha1.update(buffer, 0, len);
                    len = input.read(buffer);
                }
            } catch (IOException e) {
                throw new NIOException(e);
            }
            return sha1.digest();

        } catch (IOException e) {
            throw new NIOException(e);
        }
    }
}
