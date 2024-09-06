package net.thevpc.nuts.time;

import net.thevpc.nuts.NSessionProvider;

public interface NProgressHandlerEvent extends NSessionProvider {
    NProgressEventType getEventType();

    String getPropertyName();

    NProgressMonitorModel getModel();
}
