package net.thevpc.nuts.runtime.standalone.session;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.spi.NutsSessionAware;
import net.thevpc.nuts.util.NutsUtils;

import java.util.Objects;

public class NutsSessionUtils {
    /**
     * used only for exceptions and logger when a session is not available
     *
     * @param ws workspace
     * @return default session
     */
    public static NutsSession defaultSession(NutsWorkspace ws) {
        return ((NutsWorkspaceExt) ws).defaultSession();
    }

    public static void checkSession(NutsWorkspace ws, NutsSession session) {
        NutsUtils.requireSession(session);
        if (!Objects.equals(session.getWorkspace().getUuid(), ws.getUuid())) {
            throw new NutsIllegalArgumentException(defaultSession(ws), NutsMessage.ofCstyle("invalid session %s != %s ; %s != %s ; %s != %s ; ",
                    session.getWorkspace().getName(), ws.getName(),
                    session.getWorkspace().getLocation(), ws.getLocation(),
                    session.getWorkspace().getUuid(), ws.getUuid()
            ));
        }
    }

    public static boolean setSession(Object o, NutsSession session) {
        if (o instanceof NutsSessionAware) {
            ((NutsSessionAware) o).setSession(session);
            return true;
        }
        return false;
    }
}
