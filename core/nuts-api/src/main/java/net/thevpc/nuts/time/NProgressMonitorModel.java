package net.thevpc.nuts.time;

import net.thevpc.nuts.text.NMsg;

public interface NProgressMonitorModel {
    boolean isSuspended();

    boolean isCancelled();

    boolean isStarted();

    boolean isCompleted();

    boolean isBlocked();

    String getId();

    String getName();

    NMsg getDescription();

    NMsg getMessage();

    double getProgress();
}
