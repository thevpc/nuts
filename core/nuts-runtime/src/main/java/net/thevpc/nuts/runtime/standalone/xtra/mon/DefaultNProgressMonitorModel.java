package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.time.NProgressMonitorModel;

public class DefaultNProgressMonitorModel implements NProgressMonitorModel {
    protected NChronometer chronometer = new NChronometer();
    private boolean suspended = false;
    private boolean cancelled = false;
    private boolean started = false;
    private boolean completed = false;
    private boolean blocked = false;
    private String id;
    private String name;
    private NMsg description;
    private NMsg message;
    private long globalCount;
    private long globalDurationNanos;
    private double progress;
    private long partialCount;
    private long partialDurationNanos;
    private long length;
    private Throwable exception;

    public NChronometer getChronometer() {
        return chronometer;
    }

    public DefaultNProgressMonitorModel setChronometer(NChronometer chronometer) {
        this.chronometer = chronometer;
        return this;
    }

    @Override
    public boolean isSuspended() {
        return suspended;
    }

    public DefaultNProgressMonitorModel setSuspended(boolean suspended) {
        this.suspended = suspended;
        return this;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public DefaultNProgressMonitorModel setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        return this;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    public DefaultNProgressMonitorModel setStarted(boolean started) {
        this.started = started;
        return this;
    }

    @Override
    public boolean isCompleted() {
        return completed;
    }

    public DefaultNProgressMonitorModel setCompleted(boolean completed) {
        this.completed = completed;
        return this;
    }

    @Override
    public boolean isBlocked() {
        return blocked;
    }

    public DefaultNProgressMonitorModel setBlocked(boolean blocked) {
        this.blocked = blocked;
        return this;
    }

    @Override
    public String getId() {
        return id;
    }

    public DefaultNProgressMonitorModel setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getName() {
        return name;
    }

    public DefaultNProgressMonitorModel setName(String name) {
        this.name = name;
        return this;
    }

    @Override
    public NMsg getDescription() {
        return description;
    }

    public DefaultNProgressMonitorModel setDescription(NMsg description) {
        this.description = description;
        return this;
    }

    @Override
    public NMsg getMessage() {
        return message;
    }

    public DefaultNProgressMonitorModel setMessage(NMsg message) {
        this.message = message;
        return this;
    }

    public long getGlobalCount() {
        return globalCount;
    }

    public DefaultNProgressMonitorModel setGlobalCount(long globalCount) {
        this.globalCount = globalCount;
        return this;
    }

    public long getGlobalDurationNanos() {
        return globalDurationNanos;
    }

    public DefaultNProgressMonitorModel setGlobalDurationNanos(long globalDurationNanos) {
        this.globalDurationNanos = globalDurationNanos;
        return this;
    }

    @Override
    public double getProgress() {
        return progress;
    }

    public DefaultNProgressMonitorModel setProgress(double progress) {
        this.progress = progress;
        return this;
    }

    public long getPartialCount() {
        return partialCount;
    }

    public DefaultNProgressMonitorModel setPartialCount(long partialCount) {
        this.partialCount = partialCount;
        return this;
    }

    public long getPartialDurationNanos() {
        return partialDurationNanos;
    }

    public DefaultNProgressMonitorModel setPartialDurationNanos(long partialDurationNanos) {
        this.partialDurationNanos = partialDurationNanos;
        return this;
    }

    public long getLength() {
        return length;
    }

    public DefaultNProgressMonitorModel setLength(long length) {
        this.length = length;
        return this;
    }

    public Throwable getException() {
        return exception;
    }

    public DefaultNProgressMonitorModel setException(Throwable exception) {
        this.exception = exception;
        return this;
    }
}
