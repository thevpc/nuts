package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsSession;

public interface NutsProgressHandlerEvent {
    NutsProgressEventType getEventType();

    String getPropertyName();

    NutsProgressMonitorModel getModel();

    NutsSession getSession();
}
