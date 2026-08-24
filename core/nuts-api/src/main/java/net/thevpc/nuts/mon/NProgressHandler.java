package net.thevpc.nuts.mon;

/**
 * NProgressHandler interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NProgressHandler {
    /**
     * On event.
     *
     * @param event event
     */
    void onEvent(NProgressHandlerEvent event);
}
