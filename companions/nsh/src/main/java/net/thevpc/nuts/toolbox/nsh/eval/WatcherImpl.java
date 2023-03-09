package net.thevpc.nuts.toolbox.nsh.eval;

import net.thevpc.nuts.toolbox.nsh.eval.NShellContext;

public class WatcherImpl implements NShellContext.Watcher {
    boolean stopped;
    boolean askStopped;
    int threads;

    @Override
    public void stop() {
        if (!askStopped) {
            askStopped = true;
        }
    }

    @Override
    public boolean isStopped() {
        return stopped;
    }

    public boolean isAskStopped() {
        return askStopped;
    }
}
