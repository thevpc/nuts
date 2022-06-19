package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.util.NutsChronometer;
import net.thevpc.nuts.util.NutsProgressMonitorModel;

public class DefaultNutsProgressMonitorModel implements NutsProgressMonitorModel {
    protected NutsChronometer chronometer = new NutsChronometer();
    private boolean suspended = false;
    private boolean cancelled = false;
    private boolean started = false;
    private boolean completed = false;
    private boolean blocked = false;
    private String id;
    private String name;
    private NutsMessage description;
    private NutsMessage message;
    private long globalCount;
    private long globalDurationNanos;
    private double progress;
    private long partialCount;
    private long partialDurationNanos;
    private long length;
    private Throwable exception;

    public NutsChronometer getChronometer() {
        return chronometer;
    }

    public DefaultNutsProgressMonitorModel setChronometer(NutsChronometer chronometer) {
        this.chronometer = chronometer;
        return this;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    public DefaultNutsProgressMonitorModel setSuspended(boolean suspended) {
        this.suspended = suspended;
        return this;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public DefaultNutsProgressMonitorModel setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public DefaultNutsProgressMonitorModel setStarted(boolean started) {
        this.started = started;
        return this;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public DefaultNutsProgressMonitorModel setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    public DefaultNutsProgressMonitorModel setBlocked(boolean blocked) {
        this.blocked = blocked;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    public DefaultNutsProgressMonitorModel setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultNutsProgressMonitorModel setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NutsMessage getDescription() {
        return description;
    }

    public DefaultNutsProgressMonitorModel setDescription(NutsMessage description) {
        this.description = description;
        return this;
    }

    @Override
    public NutsMessage getMessage() {
        return message;
    }

    public DefaultNutsProgressMonitorModel setMessage(NutsMessage message) {
        this.message = message;
        return this;
    }

    public long getGlobalCount() {
        return globalCount;
    }

    public DefaultNutsProgressMonitorModel setGlobalCount(long globalCount) {
        this.globalCount = globalCount;
        return this;
    }

    public long getGlobalDurationNanos() {
        return globalDurationNanos;
    }

    public DefaultNutsProgressMonitorModel setGlobalDurationNanos(long globalDurationNanos) {
        this.globalDurationNanos = globalDurationNanos;
        return this;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    public DefaultNutsProgressMonitorModel setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    public long getPartialCount() {
        return partialCount;
    }

    public DefaultNutsProgressMonitorModel setPartialCount(long partialCount) {
        this.partialCount = partialCount;
        return this;
    }

    public long getPartialDurationNanos() {
        return partialDurationNanos;
    }

    public DefaultNutsProgressMonitorModel setPartialDurationNanos(long partialDurationNanos) {
        this.partialDurationNanos = partialDurationNanos;
        return this;
    }

    public long getLength() {
        return length;
    }

    public DefaultNutsProgressMonitorModel setLength(long length) {
        this.length = length;
        return this;
    }

    public Throwable getException() {
        return exception;
    }

    public DefaultNutsProgressMonitorModel setException(Throwable exception) {
        this.exception = exception;
        return this;
    }
}
