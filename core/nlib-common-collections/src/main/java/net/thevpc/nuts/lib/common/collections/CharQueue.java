package net.thevpc.nuts.lib.common.collections;

import java.io.EOFException;
import java.io.IOException;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.nio.CharBuffer;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class CharQueue implements CharSequence {

    private char[] content;
    private int increment;
    private int from;
    //    private int count;
    private int to;
    private boolean eof;
    private Map<String, Pattern> cachedPatterns = new HashMap<>();

    public CharQueue() {
        this(256);
    }

    public CharQueue(int initial) {
        this(initial, Math.min(initial, 256));
    }

    public CharQueue(int initial, int increment) {
        content = new char[initial];
        this.increment = increment;
    }

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

    public void write(String c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        int n = c.length();
        ensureAvailable(n);
        c.getChars(0, n, content, to);
        to += n;
    }

    public void write(CharSequence c) {
        write(c.toString());
    }

    public void write(CharBuffer c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        int n = c.length();
        ensureAvailable(n);
        c.get(content, to, n);
        to += n;
    }


    public synchronized void write(char[] c) {
        write(c, 0, c.length);
    }

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

    public synchronized void write(char c) {
        if (isEOF()) {
            throw new IllegalArgumentException("end");
        }
        ensureAvailable(1);
        content[to++] = c;
    }

    public int length() {
        return to - from;
    }

    public char peek() {
        if (to > from) {
            return content[from];
        }
        throw new UncheckedIOException(new EOFException());
    }

    public String peek(int count) {
        int c = length();
        if (count < c) {
            return new String(content, from, count);
        } else {
            return new String(content, from, c);
        }
    }

    public boolean canRead() {
        return from < to;
    }

    public boolean canReadByCount(int count) {
        return from + count - 1 < to;
    }

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

    public void skip(int count) {
        if (from + count < to) {
            from += count;
        } else {
            from = 0;
            to = 0;
        }
    }

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

    public NStringMatchResult peekPattern(String pattern) {
        return peekPattern(pattern, isEOF());
    }

    public void clear() {
        from = 0;
        to = 0;
    }

    private static class PatternInfo implements Comparable<PatternInfo> {
        private String pattern;
        private Consumer<NStringMatchResult> action;
        private Consumer<NStringMatchResult> fullMatchAction;
        private Consumer<NStringMatchResult> matchAction;
        private Consumer<NStringMatchResult> partialMatchAction;
        private NStringMatchResult result;

        public PatternInfo(String pattern) {
            this.pattern = pattern;
        }

        @Override
        public int compareTo(CharQueue.PatternInfo b) {
            CharQueue.PatternInfo a = this;
            int r = a.result.mode().compareTo(b.result.mode());
            if (r != 0) {
                return r;
            }
            switch (a.result.mode()) {
                case FULL_MATCH:
                case MATCH: {
                    return -Integer.compare(a.result.get().length(), b.result.get().length());
                }
            }
            return 0;
        }
    }

    public static class MultiPattern {
        LinkedHashMap<String, PatternInfo> map = new LinkedHashMap<>();
        boolean fully;
        Runnable noMatch;
        Consumer<NStringMatchResult> match;
        Consumer<NStringMatchResult> fullMatch;

        Consumer<NStringMatchResult> partialMatch;

        public MultiPattern onMatch(String pattern, Consumer<NStringMatchResult> action) {
            return on(pattern, true, action, NMatchType.MATCH);
        }

        public MultiPattern onMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
            return on(pattern, condition, action, NMatchType.MATCH);
        }

        public MultiPattern onPartialMatch(String pattern, Consumer<NStringMatchResult> action) {
            return on(pattern, true, action, NMatchType.PARTIAL_MATCH);
        }

        public MultiPattern onPartialMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
            return on(pattern, condition, action, NMatchType.PARTIAL_MATCH);
        }

        public MultiPattern onFullMatch(String pattern, Consumer<NStringMatchResult> action) {
            return on(pattern, true, action, NMatchType.FULL_MATCH);
        }

        public MultiPattern onFullMatch(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
            return on(pattern, condition, action, NMatchType.FULL_MATCH);
        }

        public MultiPattern on(String pattern, boolean condition, Consumer<NStringMatchResult> action) {
            return on(pattern, condition, action, null);
        }


        public MultiPattern on(String pattern, Consumer<NStringMatchResult> action) {
            return on(pattern, true, action, null);
        }

        public MultiPattern on(String pattern, boolean condition, Consumer<NStringMatchResult> action, NMatchType matchType) {
            if (action == null) {
                return this;
            }
            if (!condition) {
                return this;
            }
            PatternInfo nfo = map.get(pattern);
            if (nfo == null) {
                nfo = new PatternInfo(pattern);
                map.put(pattern, nfo);
            }
            if (matchType == null) {
                nfo.action = action;
            } else {
                switch (matchType) {
                    case FULL_MATCH: {
                        nfo.fullMatchAction = action;
                        break;
                    }
                    case MATCH: {
                        nfo.matchAction = action;
                        break;
                    }
                    case PARTIAL_MATCH: {
                        nfo.partialMatchAction = action;
                        break;
                    }
                    case NO_MATCH: {
                        throw new IllegalArgumentException("unsupported");
                    }
                }
            }
            return this;
        }

        public boolean isFully() {
            return fully;
        }

        public MultiPattern fully() {
            return setFully(true);
        }

        public MultiPattern setFully(boolean fully) {
            this.fully = fully;
            return this;
        }

        public MultiPattern onNoMatch(Runnable noMatch) {
            this.noMatch = noMatch;
            return this;
        }

        public MultiPattern onMatch(Consumer<NStringMatchResult> match) {
            this.match = match;
            return this;
        }

        public MultiPattern onPartialMatch(Consumer<NStringMatchResult> partialMatch) {
            this.partialMatch = partialMatch;
            return this;
        }

    }

    public NStringMatchResult doWithPattern(MultiPattern pattern) {
        List<PatternInfo> all = new ArrayList<>(pattern.map.values());
        if (all.isEmpty()) {
            throw new IllegalArgumentException("missing pattern");
        }
        for (PatternInfo patternInfo : all) {
            patternInfo.result = peekPattern(patternInfo.pattern, pattern.fully);
        }
        PatternInfo p = all.stream().min(PatternInfo::compareTo).get();
        NStringMatchResult r = p.result;
        switch (r.mode()) {
            case NO_MATCH: {
                if (pattern.noMatch != null) {
                    pattern.noMatch.run();
                }
                break;
            }
            case PARTIAL_MATCH: {
                if (pattern.partialMatch != null) {
                    pattern.partialMatch.accept(r);
                }
                if (p.partialMatchAction != null) {
                    p.partialMatchAction.accept(r);
                }
                if (p.action != null) {
                    p.action.accept(r);
                }
                break;
            }
            case MATCH: {
                if (pattern.match != null) {
                    pattern.match.accept(r);
                }
                if (p.matchAction != null) {
                    p.matchAction.accept(r);
                }
                if (p.action != null) {
                    p.action.accept(r);
                }
                break;
            }
            case FULL_MATCH: {
                if (pattern.fullMatch != null) {
                    pattern.fullMatch.accept(r);
                }
                if (p.fullMatchAction != null) {
                    p.fullMatchAction.accept(r);
                }
                if (p.action != null) {
                    p.action.accept(r);
                }
                break;
            }
        }
        return r;
    }

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

    public NStringMatchResult peekString(String value) {
        return peekString(value, isEOF());
    }

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

    public char read() {
        if (canRead()) {
            return content[from++];
        }
        throw new UncheckedIOException(new EOFException());
    }

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

    public boolean hasNext() {
        return to > from;
    }

    public boolean isEOF() {
        return eof;
    }

    public void eof(boolean eof) {
        this.eof = eof;
    }

    public int getIncrement() {
        return increment;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public int getAllocatedSize() {
        return content.length;
    }
}
