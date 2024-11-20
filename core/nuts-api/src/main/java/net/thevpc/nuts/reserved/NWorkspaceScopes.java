package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NRunnable;

import java.util.Stack;

public class NWorkspaceScopes {
    public static InheritableThreadLocal<Stack<NWorkspace>> workspaceScopes = new InheritableThreadLocal<>();
    public static NWorkspace mainWorkspace;

    public static NWorkspace getMainWorkspace() {
        return mainWorkspace;
    }

    public static NWorkspace setMainWorkspace(NWorkspace mainWorkspace) {
        NWorkspace old = NWorkspaceScopes.mainWorkspace;
        NWorkspaceScopes.mainWorkspace = mainWorkspace;
        if(old==mainWorkspace){
            return null;
        }
        return old;
    }

    public static NOptional<NWorkspace> currentWorkspace() {
        Stack<NWorkspace> workspaces = workspaceScopes();
        NMsg emptyMessage = NMsg.ofPlain("missing current context workspace");
        if (workspaces.isEmpty()) {
            return NOptional.ofEmpty(emptyMessage);
        }
        NWorkspace w = workspaces.peek();
        return NOptional.of(w);
    }

    private static Stack<NWorkspace> workspaceScopes() {
        InheritableThreadLocal<Stack<NWorkspace>> ss = workspaceScopes;
        Stack<NWorkspace> workspaces = ss.get();
        if (workspaces == null) {
            workspaces = new Stack<>();
            ss.set(workspaces);
        }
        return workspaces;
    }

    public static void runWith(NRunnable runnable) {
        runWith(currentWorkspace().get(), runnable);
    }

    public static <T> T callWith(NCallable<T> callable) {
        return callWith(currentWorkspace().get(), callable);
    }

    public static void runWith(NWorkspace ws, NRunnable runnable) {
        if (runnable != null) {
            Stack<NWorkspace> workspaceScopes = workspaceScopes();
            if (!workspaceScopes.isEmpty()) {
                NWorkspace l = workspaceScopes.peek();
                if (l == ws) {
                    ws.currentSession().runWith(runnable);
                    return;
                }
            }
            try {
                workspaceScopes.push(ws);
                ws.currentSession().runWith(runnable);
            } finally {
                workspaceScopes.pop();
            }
        }
    }

    public static <T> T callWith(NWorkspace ws, NCallable<T> callable) {
        if (callable != null) {
            Stack<NWorkspace> workspaceScopes = workspaceScopes();
            if (!workspaceScopes.isEmpty()) {
                NWorkspace l = workspaceScopes.peek();
                if (l == ws) {
                    return ws.currentSession().callWith(callable);
                }
            }
            try {
                workspaceScopes.push(ws);
                return ws.currentSession().callWith(callable);
            } finally {
                workspaceScopes.pop();
            }
        }
        return null;
    }

}
