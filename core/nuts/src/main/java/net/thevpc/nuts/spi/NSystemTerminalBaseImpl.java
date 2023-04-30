package net.thevpc.nuts.spi;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTerminalCommand;

public abstract class NSystemTerminalBaseImpl implements NSystemTerminalBase {

    @Override
    public NSystemTerminalBase resetLine(NSession session) {
        run(NTerminalCommand.CLEAR_LINE, getOut(), session);
        run(NTerminalCommand.MOVE_LINE_START, getOut(), session);
        return this;
    }

    @Override
    public NSystemTerminalBase clearScreen(NSession session) {
        run(NTerminalCommand.CLEAR_SCREEN, getOut(), session);
        return this;
    }

    @Override
    public Cursor getTerminalCursor(NSession session) {
        //NutsWorkspaceUtils.checkSession(session.getWorkspace(), session);
        return (Cursor) run(NTerminalCommand.GET_CURSOR, getOut(), session);
    }

    @Override
    public Size getTerminalSize(NSession session) {
        //NutsWorkspaceUtils.checkSession(session.getWorkspace(), session);
        return (Size) run(NTerminalCommand.GET_SIZE, getOut(), session);
    }
}
