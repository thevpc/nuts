package net.thevpc.nuts.spi;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.text.NutsTerminalCommand;

public abstract class NutsSystemTerminalBaseImpl implements NutsSystemTerminalBase{

    @Override
    public NutsSystemTerminalBase resetLine(NutsSession session) {
        run(NutsTerminalCommand.CLEAR_LINE, session);
        run(NutsTerminalCommand.MOVE_LINE_START, session);
        return this;
    }

    @Override
    public NutsSystemTerminalBase clearScreen(NutsSession session) {
        run(NutsTerminalCommand.CLEAR_SCREEN, session);
        return this;
    }

    @Override
    public Cursor getTerminalCursor(NutsSession session) {
        //NutsWorkspaceUtils.checkSession(session.getWorkspace(), session);
        return (Cursor) run(NutsTerminalCommand.GET_CURSOR,session);
    }

    @Override
    public Size getTerminalSize(NutsSession session) {
        //NutsWorkspaceUtils.checkSession(session.getWorkspace(), session);
        return (Size) run(NutsTerminalCommand.GET_SIZE,session);
    }
}
