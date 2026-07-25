package net.thevpc.nuts.mon;

import net.thevpc.nuts.core.NSessionProvider;

public interface NProgressHandlerEvent extends NSessionProvider {
    NProgressEventType eventType();

    String propertyName();

    NProgressMonitorModel model();
}
