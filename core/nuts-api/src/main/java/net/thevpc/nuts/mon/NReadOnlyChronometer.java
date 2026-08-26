package net.thevpc.nuts.mon;

import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.time.NDuration;

import java.time.temporal.ChronoUnit;

class NReadOnlyChronometer implements NChronometerView{
    private final NChronometer delegate;

    /**
     * N read only chronometer.
     *
     * @param delegate delegate
     * @return n read only chronometer result
     */
    public NReadOnlyChronometer(NChronometer delegate) {
        this.delegate = delegate;
    }


    @Override
    public String name() {
        return delegate.name();
    }

    @Override
    public boolean isStarted() {
        return delegate.isStarted();
    }

    @Override
    public boolean isStopped() {
        return delegate.isStopped();
    }

    @Override
    public boolean isSuspended() {
        return delegate.isSuspended();
    }

    @Override
    public NClock startClock() {
        return delegate.startClock();
    }

    @Override
    public NClock endClock() {
        return delegate.endClock();
    }

    @Override
    public NDuration duration() {
        return delegate.duration();
    }

    @Override
    public long durationMs() {
        return delegate.durationMs();
    }

    @Override
    public long durationNanos() {
        return delegate.durationNanos();
    }

    @Override
    public ChronoUnit smallestUnit() {
        return delegate.smallestUnit();
    }

    @Override
    public ChronoUnit largestUnit() {
        return delegate.largestUnit();
    }

    @Override
    public String toString(NDurationFormatMode mode) {
        return delegate.toString(mode);
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
