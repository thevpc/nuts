package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.time.NProgressEventType;
import net.thevpc.nuts.time.NProgressHandlerEvent;
import net.thevpc.nuts.time.NProgressMonitorModel;
import net.thevpc.nuts.util.NStringUtils;

public class DefaultNProgressHandlerEvent implements NProgressHandlerEvent {
    private NProgressEventType eventType;
    private String propertyName;
    private NProgressMonitorModel model;
    private NSession session;

    public DefaultNProgressHandlerEvent(NProgressEventType eventType, String propertyName, NProgressMonitorModel model, NSession session) {
        this.eventType = eventType;
        this.propertyName = propertyName;
        this.model = model;
        this.session = session;
    }

    @Override
    public NProgressEventType eventType() {
        return eventType;
    }

    @Override
    public String propertyName() {
        return propertyName;
    }

    @Override
    public NProgressMonitorModel model() {
        return model;
    }

    @Override
    public NSession session() {
        return session;
    }

    @Override
    public String toString() {
        return NStringUtils.formatAlign(eventType.toString(), 13, NPositionType.FIRST)
                + " "
                + (model.isStarted() ? "S" : " ")
                + (model.isSuspended() ? "P" : " ")
                + (model.isBlocked() ? "B" : " ")
                + (model.isCancelled() ? "C" : " ")
                + (model.isCompleted() ? "T" : " ")
                + " " + model.progress()
                + " " + model.message();
    }
}
