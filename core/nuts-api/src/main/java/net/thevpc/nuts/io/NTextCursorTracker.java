package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;

public interface NTextCursorTracker {
    static NTextCursorTracker of(){
        return NIORPI.of().createTextCursorTracker();
    }
    static NTextCursorTracker of(int tabSize, int maxRewindDepth){
        return NIORPI.of().createTextCursorTracker(tabSize,maxRewindDepth);
    }
    void consume(char c);

    void consume(String s);

    void consume(char[] buffer, int offset, int len);

    void rewind(char c);

    void clearHistory();

    int rewindDepth();

    int maxRewindDepth();

    int line();

    int physicalColumn();

    int visualColumn();

    void reset();
}
