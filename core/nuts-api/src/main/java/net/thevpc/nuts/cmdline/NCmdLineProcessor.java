package net.thevpc.nuts.cmdline;

public interface NCmdLineProcessor {
    boolean process(NArg arg, NCmdLine cmdLine);
}
