package net.thevpc.nuts.util;

import net.thevpc.nuts.NutsMessage;

public interface NutsProgressMonitorModel {
    boolean isSuspended();

    boolean isCancelled();

    boolean isStarted();

    boolean isCompleted();

    boolean isBlocked();

    String getId();

    String getName();

    NutsMessage getDescription();

    NutsMessage getMessage();

    double getProgress();
}
