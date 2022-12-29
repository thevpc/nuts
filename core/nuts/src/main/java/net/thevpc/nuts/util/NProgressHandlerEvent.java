package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;

public interface NProgressHandlerEvent {
    NProgressEventType getEventType();

    String getPropertyName();

    NProgressMonitorModel getModel();

    NSession getSession();
}
