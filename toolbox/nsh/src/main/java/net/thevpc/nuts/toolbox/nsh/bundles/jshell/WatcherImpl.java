package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

public class WatcherImpl implements JShellContext.Watcher {
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
}
