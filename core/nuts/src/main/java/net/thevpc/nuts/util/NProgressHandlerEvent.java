package net.thevpc.nuts.util;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NSessionProvider;

public interface NProgressHandlerEvent extends NSessionProvider {
    NProgressEventType getEventType();

    String getPropertyName();

    NProgressMonitorModel getModel();
}
