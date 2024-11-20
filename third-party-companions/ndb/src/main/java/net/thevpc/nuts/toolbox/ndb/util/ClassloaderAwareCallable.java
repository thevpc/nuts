package net.thevpc.nuts.toolbox.ndb.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.concurrent.NScheduler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

public class ClassloaderAwareCallable<V> {
    private InterruptedException error;
    private Context context;

    public ClassloaderAwareCallable(NSession session, ExecutorService executorService, ClassLoader classLoader) {
        this.context=new Context(session, executorService!=null?executorService: NScheduler.of().executorService(), classLoader);
    }

    public V runAndWaitFor(Function<Context,V> callable) {
        try {
            return this.context.getExecutorService().submit(new Callable<V>() {
                @Override
                public V call() throws Exception {
                    ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
                    Thread.currentThread().setContextClassLoader(context.getClassLoader());
                    try {
                        return callable.apply(context);
                    } finally {
                        Thread.currentThread().setContextClassLoader(initialClassLoader);
                    }
                }
            }).get();
        } catch (InterruptedException | ExecutionException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static class Context{
        private NSession session;
        private ExecutorService executorService;
        private ClassLoader classLoader;

        public Context(NSession session, ExecutorService executorService, ClassLoader classLoader) {
            this.session = session;
            this.executorService = executorService;
            this.classLoader = classLoader;
        }

        public NSession getSession() {
            return session;
        }

        public ExecutorService getExecutorService() {
            return executorService;
        }

        public ClassLoader getClassLoader() {
            return classLoader;
        }
    }
}
