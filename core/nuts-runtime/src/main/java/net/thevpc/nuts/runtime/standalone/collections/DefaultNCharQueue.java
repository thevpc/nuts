package net.thevpc.nuts.runtime.standalone.collections;

import net.thevpc.nuts.collections.NCharQueue;
import net.thevpc.nuts.util.NMatchType;
import net.thevpc.nuts.util.NMultiPattern;
import net.thevpc.nuts.util.NPatternInfo;
import net.thevpc.nuts.util.NStringMatchResult;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class DefaultNCharQueue implements NCharQueue {

    private char[] content;
    private final int increment;
    private int from;
    private int to;
    private boolean eof;
    private final Map<String, Pattern> cachedPatterns = new HashMap<>();

    public static NCharQueue of() {
        return new DefaultNCharQueue(256);
    }

    public static NCharQueue of(int size) {
        return new DefaultNCharQueue(size <= 0 ? 256 : size);
    }

    public static DefaultNCharQueue of(char[] content) {
        return new DefaultNCharQueue(content, -1, -1);
    }

    public DefaultNCharQueue() {
        this(256);
    }

    public DefaultNCharQueue(int initial) {
        this((initial <= 0 ? 256 : initial), Math.min((initial <= 0 ? 256 : initial), 256));
    }

    public DefaultNCharQueue(int initial, int increment) {
        content = new char[(initial <= 0 ? 256 : initial)];
        this.increment = increment<=0?Math.min(content.length, 256):increment;
    }

    public DefaultNCharQueue(char[] data, int initial, int increment) {
        int len = data.length;
        if (initial <= 0) {
            if (len == 0) {
                initial = 256;
            } else {
                initial = len;
            }
        }
        if (increment <= 0) {
            increment = Math.min(initial, 256);
        }
        this.content = new char[initial];
        this.increment = increment;
        this.to = len;
        if (len > 0) {
            System.arraycopy(data, 0, this.content, 0, len);
        }
    }

    @Override
    public int write(Reader reader, int max) {
        char[] all = new char[max];
        int count = 0;
        try {
            count = reader.read(all);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        if (count > 0) {
            write(all, 0, count);
        }
        return count;
    }

    @Override
    public void write(String c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        int n = c.length();
        ensureAvailable(n);
        c.getChars(0, n, content, to);
        to += n;
    }

    @Override
    public void write(CharSequence c) {
        write(c.toString());
    }

    @Override
    public void write(CharBuffer c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        int n = c.length();
        ensureAvailable(n);
        c.get(content, to, n);
        to += n;
    }


    @Override
    public synchronized void write(char[] c) {
        write(c, 0, c.length);
    }

    @Override
    public synchronized void write(char[] c, int offset, int len) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        ensureAvailable(len);
        try {
            System.arraycopy(c, offset, content, to, len);
        } catch (ArrayIndexOutOfBoundsException e) {
            throw e;
        }
        to += len;
    }

    @Override
    public synchronized void write(char c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        ensureAvailable(1);
        content[to++] = c;
    }

    public boolean isEmpty() {
        return to - from <= 0;
    }

    @Override
    public int length() {
        return to - from;
    }

    @Override
    public char peek() {
        if (to > from) {
            return content[from];
        }
        throw new UncheckedIOException(new EOFException());
    }


    @Override
    public char peekAt(int index) {
        if (index >= 0 && index < length()) {
            return content[from + index];
        }
        throw new UncheckedIOException(new EOFException());
    }

    @Override
    public String peek(int count) {
        int c = length();
        if (count < c) {
            return new String(content, from, count);
        } else {
            return new String(content, from, c);
        }
    }

    @Override
    public boolean canRead() {
        return from < to;
    }

    @Override
    public boolean canReadByCount(int count) {
        return from + count - 1 < to;
    }

    @Override
    public String read(int count) {
        if (from + count < to) {
            String s = new String(content, from, count);
            from += count;
            return s;
        } else {
            String s = new String(content, from, to - from);
            from = 0;
            to = 0;
            return s;
        }
    }

    @Override
    public void skip(int count) {
        if (from + count < to) {
            from += count;
        } else {
            from = 0;
            to = 0;
        }
    }

    @Override
    public NMatchType skipValue(String value) {
        int count = value.length();
        if (from + count < to) {
            for (int i = 0; i < count; i++) {
                if (value.charAt(i) != content[from + i]) {
                    return NMatchType.NO_MATCH;
                }
            }
            skip(count);
            return NMatchType.FULL_MATCH;
        }
        for (int i = 0; i < to; i++) {
            if (value.charAt(i) != content[from + i]) {
                return NMatchType.NO_MATCH;
            }
        }
        return NMatchType.PARTIAL_MATCH;
    }


    private Pattern pattern(String pattern) {
        return Pattern.compile("^" + pattern);
//        return cachedPatterns.computeIfAbsent("^" + pattern, Pattern::compile);
    }

    @Override
    public NStringMatchResult peekPattern(String pattern) {
        return peekPattern(pattern, isEOF());
    }

    @Override
    public void clear() {
        from = 0;
        to = 0;
    }

    @Override
    public NStringMatchResult doWithPattern(NMultiPattern pattern) {
        List<NPatternInfo> all = new ArrayList<>(pattern.map().values());
        if (all.isEmpty()) {
            throw new IllegalArgumentException("missing pattern");
        }
        for (NPatternInfo patternInfo : all) {
            patternInfo.result(peekPattern(patternInfo.pattern(), pattern.isFully()));
        }
        NPatternInfo p = all.stream().min(NPatternInfo::compareTo).get();
        NStringMatchResult r = p.result();
        switch (r.mode()) {
            case NO_MATCH: {
                if (pattern.noMatch() != null) {
                    pattern.noMatch().run();
                }
                break;
            }
            case PARTIAL_MATCH: {
                if (pattern.partialMatch() != null) {
                    pattern.partialMatch().accept(r);
                }
                if (p.partialMatchAction() != null) {
                    p.partialMatchAction().accept(r);
                }
                if (p.action() != null) {
                    p.action().accept(r);
                }
                break;
            }
            case MATCH: {
                if (pattern.match() != null) {
                    pattern.match().accept(r);
                }
                if (p.matchAction() != null) {
                    p.matchAction().accept(r);
                }
                if (p.action() != null) {
                    p.action().accept(r);
                }
                break;
            }
            case FULL_MATCH: {
                if (pattern.fullMatch() != null) {
                    pattern.fullMatch().accept(r);
                }
                if (p.fullMatchAction() != null) {
                    p.fullMatchAction().accept(r);
                }
                if (p.action() != null) {
                    p.action().accept(r);
                }
                break;
            }
        }
        return r;
    }

    @Override
    public NStringMatchResult peekPattern(String pattern, boolean fully) {
        Pattern p = pattern(pattern);
        Matcher matcher = p.matcher(this);
        if (matcher.find()) {
            if (matcher.hitEnd() && !fully) {
                return NStringMatchResult.ofMatch(matcher);
            } else {
                return NStringMatchResult.ofFullMatch(matcher);
            }
        } else if (matcher.hitEnd() && !fully) {
            return NStringMatchResult.ofPartialMatch(toString());
        } else {
            return NStringMatchResult.ofNoMatch();
        }
    }

    @Override
    public NStringMatchResult peekString(String value) {
        return peekString(value, isEOF());
    }

    @Override
    public NStringMatchResult peekString(String value, boolean fully) {
        int count = value.length();
        if (from + count <= to) {
            for (int i = 0; i < count; i++) {
                if (value.charAt(i) != content[from + i]) {
                    return NStringMatchResult.ofNoMatch();
                }
            }
            return NStringMatchResult.ofFullMatch(value);
        }
        if (!fully) {
            int length = length();
            for (int i = 0; i < length; i++) {
                if (value.charAt(i) != content[from + i]) {
                    return NStringMatchResult.ofNoMatch();
                }
            }
            return NStringMatchResult.ofPartialMatch(toString());
        }
        return NStringMatchResult.ofNoMatch();
    }

    @Override
    public String readBlank() {
        StringBuilder sb = new StringBuilder();
        while (hasNext()) {
            char c = peek();
            if (Character.isWhitespace(c)) {
                sb.append(read());
            } else {
                break;
            }
        }
        if (sb.length() > 0) {
            return sb.toString();
        }
        return null;
    }


    @Override
    public String readNewLine(boolean fully) {
        if (hasNext()) {
            char c = peek();
            switch (c) {
                case '\n': {
                    read();
                    return "" + c;
                }
                case '\r': {
                    read();
                    if (hasNext()) {
                        if (peek() == '\n') {
                            return "" + c + read();
                        }
                        return "" + c;
                    } else if (fully) {
                        return "" + c;
                    }
                    break;
                }
            }
        }
        return null;
    }

    @Override
    public char read() {
        if (canRead()) {
            return content[from++];
        }
        throw new UncheckedIOException(new EOFException());
    }

    @Override
    public void ensureAvailable(int z) {
        int currentEffLen = length();
        int newEffLen = currentEffLen + z;
        if (newEffLen > content.length) {
            char[] n = new char[newEffLen + increment];
            System.arraycopy(content, from, n, 0, currentEffLen);
            content = n;
            from = 0;
            to = currentEffLen;
            return;
        }
        int rightAvailable = content.length - to;
        if (z <= rightAvailable) {
            return;
        }
        System.arraycopy(content, from, content, 0, currentEffLen);
        from = 0;
        to = currentEffLen;
    }

    public String toString() {
        int c = length();
        return new String(content, from, c);
    }

    @Override
    public char charAt(int index) {
        if (index >= 0 && index < length()) {
            return content[from + index];
        }
        throw new IndexOutOfBoundsException("invalid index " + index);
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        int c = length();
        int c2 = end - start;
        if (c2 > c) {
            throw new IndexOutOfBoundsException();
        }
        return new String(content, from + start, c2);
    }

    @Override
    public IntStream chars() {
        return toString().chars();
    }

    @Override
    public IntStream codePoints() {
        return toString().codePoints();
    }

    @Override
    public boolean hasNext() {
        return to > from;
    }

    @Override
    public boolean isEOF() {
        return eof;
    }

    @Override
    public void eof(boolean eof) {
        this.eof = eof;
    }

    @Override
    public int increment() {
        return increment;
    }

    @Override
    public int from() {
        return from;
    }

    @Override
    public int to() {
        return to;
    }

    @Override
    public int allocatedSize() {
        return content.length;
    }
}
