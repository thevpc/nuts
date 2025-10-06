package net.thevpc.nuts.internal;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public class NScopedWorkspace {
    public static NScopedValue<NWorkspace> workspaceScopes = new NScopedValue<>();
    public static NWorkspace defaultSharedWorkspaceInstance;
    public static InheritableThreadLocal<NWorkspace> threadSharedWorkspaceInstanceScopes =new InheritableThreadLocal<>();

    public static NWorkspace getSharedWorkspaceInstance() {
        NWorkspace workspace = threadSharedWorkspaceInstanceScopes.get();
        if(workspace!=null){
            return workspace;
        }
        return defaultSharedWorkspaceInstance;
    }

    public static NWorkspace setSharedWorkspaceInstance(NWorkspace sharedWorkspace) {
        NWorkspace wold = threadSharedWorkspaceInstanceScopes.get();
        NWorkspace old = NScopedWorkspace.defaultSharedWorkspaceInstance;
        NScopedWorkspace.defaultSharedWorkspaceInstance = sharedWorkspace;
        threadSharedWorkspaceInstanceScopes.set(sharedWorkspace);
        if(old==sharedWorkspace && wold==sharedWorkspace){
            return null;
        }
        if(old!=sharedWorkspace) {
            return old;
        }
        return wold;
    }

    public static NOptional<NWorkspace> currentWorkspace() {
        NWorkspace ws = workspaceScopes.get();
        if (ws==null) {
            NWorkspace shw = defaultSharedWorkspaceInstance;
            if(shw !=null){
                return NOptional.of(shw);
            }
            NMsg emptyMessage = NMsg.ofPlain("missing workspace in the current context. If not sure what does this mean, just call 'Nuts.require()'");
            return NOptional.ofEmpty(emptyMessage);
        }
        return NOptional.of(ws);
    }

    public static void runWith(Runnable runnable) {
        runWith(currentWorkspace().get(), runnable);
    }

    public static <T> T callWith(NCallable<T> callable) {
        return callWith(currentWorkspace().get(), callable);
    }

    public static void runWith(NWorkspace ws, Runnable runnable) {
        if (runnable != null) {
            if(ws == null) {
                runnable.run();
            }else {
                workspaceScopes.runWith(ws, runnable);
            }
        }
    }

    public static <T> T callWith(NWorkspace ws, NCallable<T> callable) {
        if (callable != null) {
            if(ws == null) {
                return callable.call();
            }else {
                return workspaceScopes.callWith(ws, callable);
            }
        }
        return null;
    }

}
