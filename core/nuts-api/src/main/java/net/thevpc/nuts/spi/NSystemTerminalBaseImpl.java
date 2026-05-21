package net.thevpc.nuts.spi;

import net.thevpc.nuts.text.NTerminalCmd;

public abstract class NSystemTerminalBaseImpl implements NSystemTerminalBase {

    public NSystemTerminalBaseImpl() {
    }

    public abstract boolean isLastWasProgress() ;

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
