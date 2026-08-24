package net.thevpc.nuts.spi;

/**
 * NExecTargetSPI interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NExecTargetSPI extends NComponent {
    /**
     * Exec.
     *
     * @param context context
     * @return exec result
     */
    int exec(NExecTargetCommandContext context);
}
