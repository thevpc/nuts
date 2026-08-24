package net.thevpc.nuts.mon;

import net.thevpc.nuts.text.NMsg;

/**
 * NProgressMonitorModel interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NProgressMonitorModel {
    /**
     * Checks if is suspended.
     *
     * @return is suspended result
     */
    boolean isSuspended();

    /**
     * Checks if is cancelled.
     *
     * @return is cancelled result
     */
    boolean isCancelled();

    /**
     * Checks if is started.
     *
     * @return is started result
     */
    boolean isStarted();

    /**
     * Checks if is completed.
     *
     * @return is completed result
     */
    boolean isCompleted();

    /**
     * Checks if is blocked.
     *
     * @return is blocked result
     */
    boolean isBlocked();

    /**
     * Id.
     *
     * @return id result
     */
    String id();

    /**
     * Name.
     *
     * @return name result
     */
    String name();

    /**
     * Description.
     *
     * @return description result
     */
    NMsg description();

    /**
     * Message.
     *
     * @return message result
     */
    NMsg message();

    /**
     * Progress.
     *
     * @return progress result
     */
    double progress();
}
