package net.thevpc.nuts.spi;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.text.NTerminalCmd;

public abstract class NSystemTerminalBaseImpl implements NSystemTerminalBase {

    public NSystemTerminalBaseImpl() {
    }

    @Override
    public NSystemTerminalBase resetLine() {
        run(NTerminalCmd.CLEAR_LINE, getOut());
        run(NTerminalCmd.MOVE_LINE_START, getOut());
        return this;
    }

    @Override
    public NSystemTerminalBase clearScreen() {
        run(NTerminalCmd.CLEAR_SCREEN, getOut());
        return this;
    }

    @Override
    public Cursor getTerminalCursor() {
        return (Cursor) run(NTerminalCmd.GET_CURSOR, getOut());
    }

    @Override
    public Size getTerminalSize() {
        return (Size) run(NTerminalCmd.GET_SIZE, getOut());
    }
}
