package net.thevpc.nuts.internal;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.concurrent.NCallable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

/**
 * NScopedWorkspace class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NScopedWorkspace {
    public static NScopedValue<NWorkspace> workspaceScopes = new NScopedValue<>();
    public static NWorkspace defaultSharedWorkspaceInstance;
    public static InheritableThreadLocal<NWorkspace> threadSharedWorkspaceInstanceScopes =new InheritableThreadLocal<>();

    /**
     * Shared workspace instance.
     *
     * @return shared workspace instance result
     */
    public static NWorkspace sharedWorkspaceInstance() {
        NWorkspace workspace = threadSharedWorkspaceInstanceScopes.get();
        if(workspace!=null){
            return workspace;
        }
        return defaultSharedWorkspaceInstance;
    }

    /**
     * Sets the shared workspace instance.
     *
     * @param sharedWorkspace shared workspace
     * @return set shared workspace instance result
     */
    public static NWorkspace setSharedWorkspaceInstance(NWorkspace sharedWorkspace) {
//        NWorkspace wold = threadSharedWorkspaceInstanceScopes.get();
        NWorkspace old = NScopedWorkspace.defaultSharedWorkspaceInstance;
        NScopedWorkspace.defaultSharedWorkspaceInstance = sharedWorkspace;
//        threadSharedWorkspaceInstanceScopes.set(sharedWorkspace);
        if(old==sharedWorkspace/* && wold==sharedWorkspace*/){
            return null;
        }
//        if(old!=sharedWorkspace) {
            return old;
//        }
//        return wold;
    }

    /**
     * Current workspace.
     *
     * @return current workspace result
     */
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

    /**
     * Run with.
     *
     * @param runnable runnable
     */
    public static void runWith(Runnable runnable) {
      /**
       * Run with.
       *
       * @param currentWorkspace().get() current workspace().get()
       * @param runnable runnable
       */
        runWith(currentWorkspace().get(), runnable);
    }

    /**
     * Call with.
     *
     * @param callable callable
     * @return call with result
     */
    public static <T> T callWith(NCallable<T> callable) {
        /**
         * Call with.
         *
         * @param currentWorkspace().get() current workspace().get()
         * @param callable callable
         * @return call with result
         */
        return callWith(currentWorkspace().get(), callable);
    }

    /**
     * Run with.
     *
     * @param ws ws
     * @param runnable runnable
     */
    public static void runWith(NWorkspace ws, Runnable runnable) {
        if (runnable != null) {
            if(ws == null) {
                runnable.run();
            }else {
                workspaceScopes.runWith(ws, runnable);
            }
        }
    }

    /**
     * Call with.
     *
     * @param ws ws
     * @param callable callable
     * @return call with result
     */
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
