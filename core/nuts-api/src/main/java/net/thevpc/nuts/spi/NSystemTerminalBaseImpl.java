package net.thevpc.nuts.spi;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.text.NTerminalCmd;

public abstract class NSystemTerminalBaseImpl implements NSystemTerminalBase {
    private NWorkspace workspace;

    public NSystemTerminalBaseImpl(NWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public NSystemTerminalBase resetLine() {
        run(NTerminalCmd.CLEAR_LINE, getOut());
        run(NTerminalCmd.MOVE_LINE_START, getOut());
        return this;
    }

    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NSystemTerminalBase clearScreen() {
        run(NTerminalCmd.CLEAR_SCREEN, getOut());
        return this;
    }

    @Override
    public Cursor getTerminalCursor() {
        //NutsWorkspaceUtils.checkSession(session.getWorkspace(), session);
        return (Cursor) run(NTerminalCmd.GET_CURSOR, getOut());
    }

    @Override
    public Size getTerminalSize() {
        //NutsWorkspaceUtils.checkSession(session.getWorkspace(), session);
        return (Size) run(NTerminalCmd.GET_SIZE, getOut());
    }
}
