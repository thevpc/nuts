package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NIllegalStateException;

/**
 * Tracks line/column position with bounded rewind capability.
 * Optimized for code generation: minimal memory, fail-fast semantics.
 * <p>
 * Memory footprint: 1 bit per rewindable special char (maxRewindDepth).
 * Example: maxRewindDepth=1024 → ~128 bytes history buffer.
 */
public final class DefaultNTextCursorTracker implements net.thevpc.nuts.io.NTextCursorTracker {
    private int line = 1;
    private int physicalColumn = 1;
    private int visualColumn = 1;
    private boolean lastCharWasCR = false;
    private final int tabSize;

    // Bounded LIFO history: ring buffer of booleans + stack pointer
    private final boolean[] historyBuffer; // null if unlimited
    private int historySize = 0;
    private final int maxRewindDepth;

    /**
     * Creates cursor with unlimited rewind depth (use cautiously for huge files).
     */
    public DefaultNTextCursorTracker() {
        this(4, 0);
    }

    /**
     * Creates cursor with bounded rewind depth.
     *
     * @param tabSize        number of visual columns per tab stop (>0)
     * @param maxRewindDepth maximum number of rewindable operations (0 = unlimited)
     */
    public DefaultNTextCursorTracker(int tabSize, int maxRewindDepth) {
        if (tabSize <= 0) throw new NIllegalArgumentException(NMsg.ofC("tabSize must be > 0"));
        if (maxRewindDepth < 0) throw new NIllegalArgumentException(NMsg.ofC("maxRewindDepth cannot be negative"));
        this.tabSize = tabSize;
        this.maxRewindDepth = maxRewindDepth;
        this.historyBuffer = (maxRewindDepth > 0) ? new boolean[maxRewindDepth] : null;
    }

    // --- Forward operations ---------------------------------------------------

    @Override
    public void consume(char c) {
        boolean needsHistory = (c == '\r' || c == '\n' || c == '\t');

        if (needsHistory && historyBuffer != null) {
            if (historySize >= maxRewindDepth) {
                throw new IllegalStateException(
                        "Max rewind depth exceeded (" + maxRewindDepth + "). " +
                                "Either: (1) increase maxRewindDepth, (2) call clearHistory() before deep rewinds, " +
                                "or (3) avoid rewinding beyond recent operations."
                );
            }
            historyBuffer[historySize++] = lastCharWasCR; // Push pre-consumption state
        }

        if (c == '\r') {
            advanceLine();
            lastCharWasCR = true;
        } else if (c == '\n') {
            if (!lastCharWasCR) advanceLine();
            lastCharWasCR = false;
        } else if (c == '\t') {
            physicalColumn++;
            visualColumn = ((visualColumn - 1) / tabSize + 1) * tabSize + 1;
            lastCharWasCR = false;
        } else {
            physicalColumn++;
            visualColumn++;
            lastCharWasCR = false;
        }
    }

    @Override
    public void consume(String s) {
        int len = s.length();
        for (int i = 0; i < len; i++) {
            consume(s.charAt(i));
        }
    }

    @Override
    public void consume(char[] buffer, int offset, int len) {
        if (len <= 0) {
            return;
        }
        int max = offset + len;
        for (int i = offset; i < max; i++) {
            consume(buffer[i]);
        }
    }

    private void advanceLine() {
        line++;
        physicalColumn = 1;
        visualColumn = 1;
    }

    // --- SINGLE UNIVERSAL REWIND METHOD ---------------------------------------

    /**
     * Rewinds the effect of consuming {@code c}.
     * MUST be called in strict reverse (LIFO) order of consumption.
     *
     * @throws IllegalStateException if insufficient history exists to rewind
     */
    @Override
    public void rewind(char c) {
        boolean needsHistory = (c == '\r' || c == '\n' || c == '\t');

        if (needsHistory) {
            if (historySize == 0) {
                throw new NIllegalStateException(NMsg.ofC("Cannot rewind '%s': no history available. " +
                                "Possible causes: (1) maxRewindDepth exceeded during consumption, " +
                                "(2) already rewound all available history, or (3) cursor created with maxRewindDepth=0.",escape(c))
                );
            }
            lastCharWasCR = historyBuffer[--historySize]; // Pop pre-consumption state
        }

        if (c == '\r') {
            if (line <= 1) throw new NIllegalStateException(NMsg.ofC("Cannot rewind CR at first line"));
            line--;
            physicalColumn = 1;
            visualColumn = 1;
        } else if (c == '\n') {
            // Line advanced only if NOT part of CRLF (determined by restored lastCharWasCR)
            if (!lastCharWasCR) {
                if (line <= 1) throw new NIllegalStateException(NMsg.ofC("Cannot rewind LF at first line"));
                line--;
                physicalColumn = 1;
                visualColumn = 1;
            }
            // If lastCharWasCR=true → was CRLF continuation → no line change to undo
        } else if (c == '\t') {
            if (physicalColumn <= 1) {
                throw new NIllegalStateException(NMsg.ofC("Cannot rewind TAB at line start"));
            }
            physicalColumn--;
            // Reconstruct visual column before tab expansion
            int prevStop = ((visualColumn - 2) / tabSize) * tabSize;
            visualColumn = Math.max(1, prevStop + 1);
        } else {
            if (physicalColumn <= 1) {
                throw new NIllegalStateException(NMsg.ofC("Cannot rewind character at line start"));
            }
            physicalColumn--;
            visualColumn--;
        }
    }

    // --- History management ---------------------------------------------------

    /**
     * Discards all rewind history. Use after reaching a stable generation point
     * where you know you won't rewind beyond this position.
     * <p>
     * Example pattern:
     * cursor.consume("public class Foo {\n");
     * cursor.clearHistory(); // Won't rewind past class declaration
     * // ... generate method bodies with conditional rewinds ...
     */
    @Override
    public void clearHistory() {
        historySize = 0;
    }

    /**
     * Returns current rewind depth (number of operations that can be rewound).
     */
    @Override
    public int rewindDepth() {
        return historySize;
    }

    /**
     * Returns maximum configured rewind depth (0 = unlimited).
     */
    @Override
    public int maxRewindDepth() {
        return maxRewindDepth;
    }

    // --- Position queries -----------------------------------------------------

    @Override
    public int line() {
        return line;
    }

    @Override
    public int physicalColumn() {
        return physicalColumn;
    }

    @Override
    public int visualColumn() {
        return visualColumn;
    }

    @Override
    public void reset() {
        line = 1;
        physicalColumn = 1;
        visualColumn = 1;
        lastCharWasCR = false;
        historySize = 0;
    }

    // --- Helpers --------------------------------------------------------------

    private static String escape(char c) {
        switch (c) {
            case '\r':
                return "\\r";
            case '\n':
                return "\\n";
            case '\t':
                return "\\t";
            default:
                return String.valueOf(c);
        }
    }
}