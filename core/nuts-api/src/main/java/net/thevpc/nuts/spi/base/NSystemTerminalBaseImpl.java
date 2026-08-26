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
      /**
       * Run.
       *
       * @param NTerminalCmd.CLEAR_LINE n terminal cmd.clear_line
       * @param out() out()
       */
        run(NTerminalCmd.CLEAR_LINE, out());
      /**
       * Run.
       *
       * @param NTerminalCmd.MOVE_LINE_START n terminal cmd.move_line_start
       * @param out() out()
       */
        run(NTerminalCmd.MOVE_LINE_START, out());
        return this;
    }

    @Override
    public NSystemTerminalBase clearScreen() {
      /**
       * Run.
       *
       * @param NTerminalCmd.CLEAR_SCREEN n terminal cmd.clear_screen
       * @param out() out()
       */
        run(NTerminalCmd.CLEAR_SCREEN, out());
        return this;
    }

    @Override
    public Cursor terminalCursor() {
      /**
       * Return.
       *
       * @param run(NTerminalCmd.GET_CURSOR run(n terminal cmd.get_cursor
       * @param out() out()
       */
        return (Cursor) run(NTerminalCmd.GET_CURSOR, out());
    }

    @Override
    public Size terminalSize() {
      /**
       * Return.
       *
       * @param run(NTerminalCmd.GET_SIZE run(n terminal cmd.get_size
       * @param out() out()
       */
        return (Size) run(NTerminalCmd.GET_SIZE, out());
    }
}
