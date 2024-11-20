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
    public static InheritableThreadLocal<NWorkspace> threadMainWorkspaceScopes=new InheritableThreadLocal<>();

    public static NWorkspace getMainWorkspace() {
        NWorkspace workspace = threadMainWorkspaceScopes.get();
        if(workspace!=null){
            return workspace;
        }
        return mainWorkspace;
    }

    public static NWorkspace setMainWorkspace(NWorkspace mainWorkspace) {
        NWorkspace wold = threadMainWorkspaceScopes.get();
        NWorkspace old = NWorkspaceScopes.mainWorkspace;
        NWorkspaceScopes.mainWorkspace = mainWorkspace;
        threadMainWorkspaceScopes.set(mainWorkspace);
        if(old==mainWorkspace && wold==mainWorkspace){
            return null;
        }
        if(old!=mainWorkspace) {
            return old;
        }
        return wold;
    }

    public static NOptional<NWorkspace> currentWorkspace() {
        Stack<NWorkspace> workspaces = workspaceScopes();
        NMsg emptyMessage = NMsg.ofPlain("missing current context workspace");
        if (workspaces.isEmpty()) {
            NWorkspace mainWorkspace0 = mainWorkspace;
            if(mainWorkspace0 !=null){
                return NOptional.of(mainWorkspace0);
            }
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
            runWith0(ws, ()->ws.currentSession().runWith(runnable));
        }
    }

    public static void runWith0(NWorkspace ws, NRunnable runnable) {
        if (runnable != null) {
            Stack<NWorkspace> workspaceScopes = workspaceScopes();
            if (!workspaceScopes.isEmpty()) {
                NWorkspace l = workspaceScopes.peek();
                if (l == ws) {
                    runnable.run();
                    return;
                }
            }
            try {
                workspaceScopes.push(ws);
                runnable.run();
            } finally {
                workspaceScopes.pop();
            }
        }
    }

    public static <T> T callWith(NWorkspace ws, NCallable<T> callable) {
        if (callable != null) {
            return callWith0(ws, ()->ws.currentSession().callWith(callable));
        }
        return null;
    }

    public static <T> T callWith0(NWorkspace ws, NCallable<T> callable) {
        if (callable != null) {
            Stack<NWorkspace> workspaceScopes = workspaceScopes();
            if (!workspaceScopes.isEmpty()) {
                NWorkspace l = workspaceScopes.peek();
                if (l == ws) {
                    return callable.call();
                }
            }
            try {
                workspaceScopes.push(ws);
                return callable.call();
            } finally {
                workspaceScopes.pop();
            }
        }
        return null;
    }

}
