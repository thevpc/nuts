package net.vpc.app.nuts;

/**
 *
 */
public interface NutsCommandLineProcessor {
    /**
     * process the given option argument that was peeked from the command line.
     * Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     * @param argument peeked argument
     * @param cmdLine associated commandline
     * @return true if the argument can be processed, false otherwise.
     */
    boolean processOption(NutsArgument argument, NutsCommandLine cmdLine);

    /**
     * process the given non option argument that was peeked from the command line.
     * Implementations <strong>MUST</strong> call one of
     * the "next" methods to
     * @param argument peeked argument
     * @param cmdLine associated commandline
     * @return true if the argument can be processed, false otherwise.
     */
    boolean processNonOption(NutsArgument argument, NutsCommandLine cmdLine);

    /**
     * execute options, called after all options was processed and
     * cmdLine.isExecMode() return true.
     */
    void exec();
}
