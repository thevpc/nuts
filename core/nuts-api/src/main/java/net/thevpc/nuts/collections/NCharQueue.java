package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NMatchType;
import net.thevpc.nuts.util.NMultiPattern;
import net.thevpc.nuts.util.NStringMatchResult;

import java.io.Reader;
import java.nio.CharBuffer;

public interface NCharQueue extends CharSequence {
    static NCharQueue of() {
        return NUtilsRPI.of().createCharQueue();
    }

    static NCharQueue of(int size) {
        return NUtilsRPI.of().createCharQueue(size);
    }

    static NCharQueue of(int size,int increment) {
        return NUtilsRPI.of().createCharQueue(size,increment);
    }

    static NCharQueue of(char[] content) {
        return NUtilsRPI.of().createCharQueue(content);
    }

    int write(Reader reader, int max);

    void write(String c);

    void write(CharSequence c);

    void write(CharBuffer c);

    void write(char[] c);

    void write(char[] c, int offset, int len);

    void write(char c);

    char peek();

    char peekAt(int index);

    String peek(int count);

    boolean canRead();

    boolean canReadByCount(int count);

    String read(int count);

    void skip(int count);

    NMatchType skipValue(String value);

    NStringMatchResult peekPattern(String pattern);

    void clear();

    NStringMatchResult doWithPattern(NMultiPattern pattern);

    NStringMatchResult peekPattern(String pattern, boolean fully);

    NStringMatchResult peekString(String value);

    NStringMatchResult peekString(String value, boolean fully);

    String readBlank();

    String readNewLine(boolean fully);

    char read();

    void ensureAvailable(int z);

    boolean hasNext();

    boolean isEOF();

    void eof(boolean eof);

    int increment();

    int from();

    int to();

    int allocatedSize();
}
