package net.thevpc.nuts.mon;

import net.thevpc.nuts.core.NSessionProvider;

/**
 * NProgressHandlerEvent interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NProgressHandlerEvent extends NSessionProvider {
    /**
     * Event type.
     *
     * @return event type result
     */
    NProgressEventType eventType();

    /**
     * Property name.
     *
     * @return property name result
     */
    String propertyName();

    /**
     * Model.
     *
     * @return model result
     */
    NProgressMonitorModel model();
}
