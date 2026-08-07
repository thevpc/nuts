package net.thevpc.nuts.collections;

import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NMatchType;

import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.stream.IntStream;

public interface NByteQueue {
    static NByteQueue of() {
        return NUtilsRPI.of().createByteQueue();
    }

    static NByteQueue of(int size) {
        return NUtilsRPI.of().createByteQueue(size);
    }

    static NByteQueue of(byte[] content) {
        return NUtilsRPI.of().createByteQueue(content);
    }

    int write(InputStream reader, int max);

    void write(ByteBuffer c);

    void write(byte[] c);

    void write(byte[] c, int offset, int len);

    void write(byte c);

    boolean isEmpty();

    int length();

    byte peek();

    byte peekAt(int index);

    String peek(int count);

    boolean canRead();

    boolean canReadByCount(int count);

    String read(int count);

    void skip(int count);

    NMatchType skipValue(String value);

    void clear();

    byte read();

    void ensureAvailable(int z);

    byte byteAt(int index);

    IntStream bytes();

    boolean hasNext();

    boolean isEOF();

    void eof(boolean eof);

    int increment();

    int from();

    int to();

    int allocatedSize();
}
