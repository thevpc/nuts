package net.thevpc.nuts.io;

import net.thevpc.nuts.internal.rpi.NIORPI;

/**
 * NTextCursorTracker interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NTextCursorTracker {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTextCursorTracker of(){
        return NIORPI.of().createTextCursorTracker();
    }
    /**
     * Creates a new instance of of.
     *
     * @param tabSize tab size
     * @param maxRewindDepth max rewind depth
     * @return of result
     */
    static NTextCursorTracker of(int tabSize, int maxRewindDepth){
        return NIORPI.of().createTextCursorTracker(tabSize,maxRewindDepth);
    }
    /**
     * Consume.
     *
     * @param c c
     */
    void consume(char c);

    /**
     * Consume.
     *
     * @param s s
     */
    void consume(String s);

    /**
     * Consume.
     *
     * @param buffer buffer
     * @param offset offset
     * @param len len
     */
    void consume(char[] buffer, int offset, int len);

    /**
     * Rewind.
     *
     * @param c c
     */
    void rewind(char c);

    /**
     * Clear history.
     */
    void clearHistory();

    /**
     * Rewind depth.
     *
     * @return rewind depth result
     */
    int rewindDepth();

    /**
     * Max rewind depth.
     *
     * @return max rewind depth result
     */
    int maxRewindDepth();

    /**
     * Line.
     *
     * @return line result
     */
    int line();

    /**
     * Physical column.
     *
     * @return physical column result
     */
    int physicalColumn();

    /**
     * Visual column.
     *
     * @return visual column result
     */
    int visualColumn();

    /**
     * Reset.
     */
    void reset();
}
