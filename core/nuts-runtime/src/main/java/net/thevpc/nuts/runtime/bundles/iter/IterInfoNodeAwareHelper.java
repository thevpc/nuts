package net.thevpc.nuts.runtime.bundles.iter;

import net.thevpc.nuts.NutsSession;

public class IterInfoNodeAwareHelper {
    public static Runnable ofRunnable(IterInfoNode n, Runnable runnable) {
        return new RunnableAndInfoAware(runnable,n);
    }

    private static class RunnableAndInfoAware implements Runnable , IterInfoNodeAware{
        private Runnable base;
        private IterInfoNode nfo;

        public RunnableAndInfoAware(Runnable base, IterInfoNode nfo) {
            this.base = base;
            this.nfo = nfo;
        }

        @Override
        public IterInfoNode info(NutsSession session) {
            return nfo;
        }

        @Override
        public void run() {
            base.run();
        }
    }
}
