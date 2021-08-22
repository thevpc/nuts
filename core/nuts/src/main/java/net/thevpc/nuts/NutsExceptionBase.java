package net.thevpc.nuts;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Base Nuts Exception Interface. Parent of all Nuts defined Exceptions.
 *
 * @author thevpc
 * @app.category Exceptions
 * @since 0.5.4
 */
public interface NutsExceptionBase {
    NutsMessage getFormattedMessage();

    NutsString getFormattedString();

    String getMessage();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NutsWorkspace getWorkspace();

    NutsSession getSession();

    static NutsExceptionBase detectExceptionBase(Throwable th) {
        Set<Throwable> visited = new HashSet<>();
        Stack<Throwable> stack = new Stack<>();
        if (th != null) {
            stack.push(th);
        }
        while (!stack.isEmpty()) {
            Throwable a = stack.pop();
            if (visited.add(a)) {
                if (th instanceof NutsExceptionBase) {
                    return ((NutsExceptionBase) th);
                }
                Throwable c = th.getCause();
                if (c != null) {
                    stack.add(c);
                }
            }
        }
        return null;
    }

    static NutsSession detectSession(Throwable th) {
        NutsExceptionBase e = detectExceptionBase(th);
        if (e != null) {
            return e.getSession();
        }
        return null;
    }
}
