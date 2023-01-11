package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSessionAware;
import net.thevpc.nuts.util.NAssert;

import java.util.Objects;

public class NSessionUtils {
    /**
     * used only for exceptions and logger when a session is not available
     *
     * @param ws workspace
     * @return default session
     */
    public static NSession defaultSession(NWorkspace ws) {
        return ((NWorkspaceExt) ws).defaultSession();
    }

    public static void checkSession(NWorkspace ws, NSession session) {
        NAssert.requireSession(session);
        if (!Objects.equals(session.getWorkspace().getUuid(), ws.getUuid())) {
            throw new NIllegalArgumentException(defaultSession(ws), NMsg.ofC("invalid session %s != %s ; %s != %s ; %s != %s ; ",
                    session.getWorkspace().getName(), ws.getName(),
                    session.getWorkspace().getLocation(), ws.getLocation(),
                    session.getWorkspace().getUuid(), ws.getUuid()
            ));
        }
    }

    public static boolean setSession(Object o, NSession session) {
        if (o instanceof NSessionAware) {
            ((NSessionAware) o).setSession(session);
            return true;
        }
        return false;
    }
}
