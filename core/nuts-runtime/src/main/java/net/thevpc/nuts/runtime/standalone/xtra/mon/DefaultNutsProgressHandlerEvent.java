package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.format.NutsPositionType;
import net.thevpc.nuts.util.NutsProgressEventType;
import net.thevpc.nuts.util.NutsProgressHandlerEvent;
import net.thevpc.nuts.util.NutsProgressMonitorModel;
import net.thevpc.nuts.util.NutsStringUtils;

public class DefaultNutsProgressHandlerEvent implements NutsProgressHandlerEvent {
    private NutsProgressEventType eventType;
    private String propertyName;
    private NutsProgressMonitorModel model;
    private NutsSession session;

    public DefaultNutsProgressHandlerEvent(NutsProgressEventType eventType, String propertyName, NutsProgressMonitorModel model, NutsSession session) {
        this.eventType = eventType;
        this.propertyName = propertyName;
        this.model = model;
        this.session = session;
    }

    @Override
    public NutsProgressEventType getEventType() {
        return eventType;
    }

    @Override
    public String getPropertyName() {
        return propertyName;
    }

    @Override
    public NutsProgressMonitorModel getModel() {
        return model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public String toString() {
        return NutsStringUtils.formatAlign(eventType.toString(), 13, NutsPositionType.FIRST)
                + " "
                + (model.isStarted() ? "S" : " ")
                + (model.isSuspended() ? "P" : " ")
                + (model.isBlocked() ? "B" : " ")
                + (model.isCancelled() ? "C" : " ")
                + (model.isCompleted() ? "T" : " ")
                + " " + model.getProgress()
                + " " + model.getMessage();
    }
}
