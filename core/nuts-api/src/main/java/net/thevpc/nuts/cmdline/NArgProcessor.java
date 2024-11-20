package net.thevpc.nuts.cmdline;

public interface NArgProcessor<T> {
    void run(T value, NArg arg);
}
