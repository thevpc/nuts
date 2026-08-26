package net.thevpc.nuts.cmdline;

/**
 * NCmdLineProcessor interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCmdLineProcessor {
    /**
     * Process.
     *
     * @param cmdLine cmd line
     * @return process result
     */
    boolean process(NCmdLine cmdLine);
}
