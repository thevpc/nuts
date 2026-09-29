package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.text.NTerminalCmd;

/**
 * NSystemTerminalBaseImpl class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public abstract class NSystemTerminalBaseImpl implements NSystemTerminalBase {

    /**
     * N system terminal base impl.
     *
     * @return n system terminal base impl result
     */
    public NSystemTerminalBaseImpl() {
    }

    /**
     * Checks if is last was progress.
     *
     * @return is last was progress result
     */
    public abstract boolean isLastWasProgress() ;

    /**
     * Last was progress.
     *
     * @param lastWasProgress last was progress
     * @return last was progress result
     */
    public abstract void lastWasProgress(boolean lastWasProgress) ;

    @Override
    public NSystemTerminalBase resetLine() {
        run(NTerminalCmd.CLEAR_LINE, out());
        run(NTerminalCmd.MOVE_LINE_START, out());
        return this;
    }

    @Override
    public NSystemTerminalBase clearScreen() {
        run(NTerminalCmd.CLEAR_SCREEN, out());
        return this;
    }

    @Override
    public Cursor terminalCursor() {
        return (Cursor) run(NTerminalCmd.GET_CURSOR, out());
    }

    @Override
    public Size terminalSize() {
        return (Size) run(NTerminalCmd.GET_SIZE, out());
    }
}
