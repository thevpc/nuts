package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.*;

import java.util.*;

public class DefaultProgressMonitor implements NutsProgressMonitor {
    public static final NutsMessage EMPTY_MESSAGE = NutsMessage.ofPlain("");
    private List<NutsProgressListener> listeners = new ArrayList<>();
    private DefaultNutsProgressMonitorModel model = new DefaultNutsProgressMonitorModel();
    private NutsSession session;
    private boolean strictComputationMonitor = true;
    private NutsProgressHandler spi;
    private NutsProgressMonitorInc incrementor;// = new DeltaProgressMonitorInc(1E-2);


    public DefaultProgressMonitor(String id, NutsProgressHandler spi, NutsProgressMonitorInc incrementor, NutsSession session) {
        model.setId(id == null ? UUID.randomUUID().toString() : id);
        this.spi = spi;
        this.incrementor = incrementor;
        this.session = session;
    }

    @Override
    public final NutsProgressMonitor setProgress(double progress, NutsMessage message) {
        setProgress(progress);
        if (message != null) {
            setMessage(message);
        }
        return this;
    }


    @Override
    public NutsProgressMonitor start(NutsMessage message) {
        if (!isStarted()) {
            model.setStarted(true);
            model.setProgress(0);
            model.getChronometer().start();
            setMessageIfNotNull(message);
            fireEvent(NutsProgressEventType.START, "started", false);
        }
        return this;
    }

    @Override
    public NutsProgressMonitor start() {
        return start(null);
    }


    public NutsProgressMonitor complete() {
        return complete(null);
    }

    public NutsProgressMonitor complete(NutsMessage message) {
        return setTerminated(true, message);
    }

    public NutsProgressMonitor undoComplete() {
        return undoComplete(null);
    }

    public NutsProgressMonitor undoComplete(NutsMessage message) {
        return setTerminated(false, message);
    }

    protected NutsProgressMonitor setTerminated(boolean terminated, NutsMessage message) {
        if (terminated) {
            if (!isCompleted()) {
                if (!isStarted()) {
                    model.setStarted(true);
                    model.getChronometer().start();
                    fireEvent(NutsProgressEventType.START, "started", false);
                }
                model.setProgress(1); // really?
                setMessageIfNotNull(message);
                boolean oldCompleted = model.isCompleted();
                if (!oldCompleted) {
                    model.setCompleted(true);
                    fireEvent(NutsProgressEventType.COMPLETE, "completed", oldCompleted);
                }
            }
        } else {
            if (isCompleted()) {
                model.setCompleted(false);
                fireEvent(NutsProgressEventType.UNDO_COMPLETE, "completed", false);
            }
        }
        return this;
    }

    @Override
    public NutsProgressMonitor cancel() {
        return cancel(null);
    }

    @Override
    public NutsProgressMonitor cancel(NutsMessage message) {
        return setCancelled(true, message);
    }

    public NutsProgressMonitor undoCancel() {
        return undoCancel(null);
    }

    @Override
    public NutsProgressMonitor undoCancel(NutsMessage message) {
        return setCancelled(false, message);
    }

    protected NutsProgressMonitor setCancelled(boolean cancel, NutsMessage message) {
        if (cancel) {
            if (!isCanceled()) {
                model.setCancelled(true);
                setMessageIfNotNull(message);
                fireEvent(NutsProgressEventType.CANCEL, "canceled", false);
            }
        } else {
            if (isCanceled()) {
                model.setCancelled(false);
                setMessageIfNotNull(message);
                fireEvent(NutsProgressEventType.UNDO_CANCEL, "canceled", true);
            }
        }
        return this;
    }

    @Override
    public NutsProgressMonitor undoSuspend() {
        return undoSuspend(null);
    }

    @Override
    public NutsProgressMonitor undoSuspend(NutsMessage message) {
        return setSuspended(false, null);
    }

    @Override
    public NutsProgressMonitor suspend() {
        return suspend(null);
    }

    @Override
    public NutsProgressMonitor suspend(NutsMessage message) {
        return setSuspended(true, null);
    }

    public NutsProgressMonitor setSuspended(boolean suspend, NutsMessage message) {
        if (suspend) {
            if (!isSuspended()) {
                model.setSuspended(true);
                setMessageIfNotNull(message);
                fireEvent(NutsProgressEventType.SUSPEND, "suspended", false);
            }
        } else {
            if (isSuspended()) {
                model.setSuspended(false);
                setMessageIfNotNull(message);
                fireEvent(NutsProgressEventType.UNDO_SUSPEND, "suspended", true);
            }
        }
        return this;
    }

    public boolean isSuspended() {
        return model.isSuspended();
    }

    public boolean isCompleted() {
        return model.isCompleted();
    }

    @Override
    public boolean isBlocked() {
        return model.isBlocked();
    }

    @Override
    public NutsProgressMonitor block() {
        return block(null);
    }

    @Override
    public NutsProgressMonitor block(NutsMessage message) {
        return setBlocked(true, message);
    }

    @Override
    public NutsProgressMonitor undoBlock() {
        return undoBlock(null);
    }

    @Override
    public NutsProgressMonitor undoBlock(NutsMessage message) {
        return setBlocked(false, message);
    }

    public NutsProgressMonitor setBlocked(boolean block, NutsMessage message) {
        if (block) {
            if (!model.isBlocked()) {
                model.setBlocked(true);
                setMessageIfNotNull(message);
                fireEvent(NutsProgressEventType.BLOCK, "blocked", false);
            }
        } else {
            if (model.isBlocked()) {
                model.setBlocked(false);
                setMessageIfNotNull(message);
                fireEvent(NutsProgressEventType.UNDO_BLOCK, "blocked", true);
            }
        }
        return this;
    }

    @Override
    public boolean isStarted() {
        return model.isStarted();
    }

    @Override
    public boolean isCanceled() {
        return model.isCancelled();
    }

    public void reset() {
        List<String> props = new ArrayList<>();
        if (isStarted()) {
            model.setStarted(false);
            props.add("started");
        }
        if (isCanceled()) {
            model.setCancelled(false);
            props.add("canceled");
        }
        if (isCompleted()) {
            model.setCompleted(false);
            props.add("completed");
        }
        if (isSuspended()) {
            model.setSuspended(false);
            props.add("suspended");
        }
        if (isBlocked()) {
            model.setBlocked(false);
            props.add("blocked");
        }
        if (!props.isEmpty()) {
            String property = String.join(",", props);
            model.getChronometer().reset();
            fireEvent(NutsProgressEventType.RESET, property, null);
        }
    }

    @Override
    public String getId() {
        return model.getId();
    }

    @Override
    public String getName() {
        return model.getName();
    }

    protected void setName(String name) {
        String old = this.model.getName();
        if (!Objects.equals(old, name)) {
            this.model.setName(name);
            fireEvent(NutsProgressEventType.UPDATE, "name", old);
        }
    }

    @Override
    public NutsMessage getDescription() {
        return model.getDescription();
    }

    protected NutsProgressMonitor setDescription(NutsMessage desc) {
        NutsMessage old = this.model.getDescription();
        if (!Objects.equals(old, desc)) {
            this.model.setDescription(desc);
            fireEvent(NutsProgressEventType.UPDATE, "description", old);
        }
        return this;
    }

    @Override
    public NutsProgressMonitor addListener(NutsProgressListener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public NutsProgressMonitor removeListener(NutsProgressListener listener) {
        listeners.remove(listener);
        return this;
    }

    @Override
    public NutsProgressListener[] getListeners() {
        return listeners.toArray(new NutsProgressListener[0]);
    }

    public NutsDuration getDuration() {
        return model.getChronometer().getDuration();
    }

    public NutsClock getStartClock() {
        return model.getChronometer().getStartClock();
    }


    @Override
    public final NutsProgressMonitor setMessage(NutsMessage message) {
        NutsMessage old = getMessage();
        if (setMessage0(message)) {
            fireEvent(NutsProgressEventType.MESSAGE, "message", old);
        }
        return this;
    }

    @Override
    public NutsMessage getMessage() {
        return model.getMessage();
    }

    @Override
    public boolean isIndeterminate() {
        return Double.isNaN(model.getProgress());
    }

    @Override
    public double getProgress() {
        return model.getProgress();
    }

    private final boolean setMessageIfNotNull(NutsMessage message) {
        if (message != null) {
            return setMessage0(message);
        }
        return false;
    }

    private final boolean setMessage0(NutsMessage message) {
        NutsMessage newMessage = message == null ? EMPTY_MESSAGE : message;
        if (!Objects.equals(getMessage(), newMessage)) {
            model.setMessage(newMessage);
            return true;
        }
        return false;
    }

    private void fireEvent(NutsProgressEventType state, String propertyName, Object oldValue) {
        spi.onEvent(new DefaultNutsProgressHandlerEvent(state, propertyName, model, session));
        for (NutsProgressListener listener : getListeners()) {
            switch (state) {
                case START: {
                    listener.onProgress(NutsProgressEvent.ofStart(null, getMessage(), -1, session));
                    break;
                }
                case COMPLETE: {
                    listener.onProgress(NutsProgressEvent.ofComplete(null, getMessage(), model.getGlobalCount(), model.getGlobalDurationNanos(), model.getProgress(), model.getPartialCount(), model.getPartialDurationNanos(), model.getLength(), model.getException(), session));
                    break;
                }
                default: {
                    listener.onProgress(NutsProgressEvent.ofProgress(null, getMessage(), model.getGlobalCount(), model.getGlobalDurationNanos(), model.getProgress(), model.getPartialCount(), model.getPartialDurationNanos(), model.getLength(), model.getException(), session));
                    break;
                }
            }
        }
    }

    @Override
    public final NutsProgressMonitor setProgress(double progress) {
        if (!isStarted()) {
            start();
        }
        if (isCanceled()) {
            return this;
        }
        if (isCompleted()) {
            if (progress < 1) {
                undoComplete();
            } else {
                return this;
            }
        }
        if (isSuspended()) {
            while (isSuspended()) {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    //ignore
                }
            }
        }
        if ((progress < 0 || progress > 1) && !Double.isNaN(progress)) {
            if (strictComputationMonitor) {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("invalid Progress value [0..1] : %s", progress));
            } else {
                if (progress < 0) {
                    progress = 0;
                } else if (progress > 1) {
                    progress = 1;
                }
            }
        }
        double oldProgress = this.getProgress();
        if (oldProgress != progress) {
            model.setProgress(progress);
            fireEvent(NutsProgressEventType.PROGRESS, "progress", oldProgress);
        }
        if (progress >= 1) {
            if (!isCompleted()) {
                complete();
            }
        }
        return this;
    }


    @Override
    public final NutsProgressMonitor setProgress(long i, long max) {
        return this.setProgress(i, max, null);
    }

    @Override
    public final NutsProgressMonitor setProgress(long i, long max, NutsMessage message) {
        return this.setProgress((1.0 * i / max), message);
    }

    @Override
    public final NutsProgressMonitor setProgress(long i, long maxi, long j, long maxj) {
        return this.setProgress(i, maxi, j, maxj, null);
    }


    @Override
    public final NutsProgressMonitor setProgress(long i, long j, long maxi, long maxj, NutsMessage message) {
        return this.setProgress(((1.0 * i * maxi) + j) / (maxi * maxj), message);
    }

    @Override
    public final NutsProgressMonitor inc() {
        inc(null);
        return this;
    }

    @Override
    public final NutsProgressMonitor inc(NutsMessage message) {
        NutsProgressMonitorInc incrementor = this.incrementor;
        if (incrementor == null) {
            throw new IllegalArgumentException("missing incrementor");
        }
        double oldProgress = getProgress();
        double newProgress = incrementor.inc(oldProgress);
        setProgress(newProgress, message);
        return this;
    }

    @Override
    public final NutsDuration getEstimatedTotalDuration() {
        double d = getProgress();
        NutsDuration spent = getDuration();
        if (spent == null) {
            return null;
        }
        return spent.mul(1 / d);
    }

    @Override
    public final NutsDuration getEstimatedRemainingDuration() {
        double d = getProgress();
        NutsDuration spent = getDuration();
        if (spent == null) {
            return null;
        }
        double d2 = (1 - d) / d;
        return spent.mul(d2);
    }

    @Override
    public NutsProgressMonitor setIndeterminate() {
        return setIndeterminate(null);
    }

    @Override
    public NutsProgressMonitor setIndeterminate(NutsMessage message) {
        return setProgress(Double.NaN, message);
    }

    public DefaultProgressMonitor setIncrementor(NutsProgressMonitorInc incrementor) {
        this.incrementor = incrementor;
        return this;
    }

    public NutsProgressMonitorInc getIncrementor() {
        return incrementor;
    }

    public NutsProgressHandler getSpi() {
        return spi;
    }


    @Override
    public NutsProgressMonitor translate(long index, long max) {
        return new DefaultProgressMonitor(null, new ProgressMonitorTranslator(this, 1.0 / max, index * (1.0 / max)), null, session);
    }

    @Override
    public NutsProgressMonitor translate(long i, long imax, long j, long jmax) {
        return new DefaultProgressMonitor(null, new ProgressMonitorTranslator(this, 1.0 / (imax * jmax), ((1.0 * i * imax) + j) / (imax * jmax)), null, session);
    }

    @Override
    public NutsProgressMonitor stepInto(NutsMessage message) {
        final NutsProgressMonitorInc incrementor = getIncrementor();
        if (incrementor == null) {
            throw new IllegalArgumentException("missing incrementor");
        }
        double a = getProgress();
        double b = incrementor.inc(a);
        if (message != null) {
            setMessage(message);
        }
        return translate(b - a, a);
    }

    @Override
    public NutsProgressMonitor stepInto(long index, long max) {
        return translate(1.0 / max, index * (1.0 / max));
    }

    @Override
    public NutsProgressMonitor temporize(long freq) {
        return new DefaultProgressMonitor(null, new FreqProgressHandler(this, freq), null, session);
    }

    @Override
    public NutsProgressMonitor incremental(long iterations) {
        setIncrementor(new LongIterationProgressMonitorInc(iterations));
        return this;
    }

    @Override
    public NutsProgressMonitor incremental(double delta) {
        this.setIncrementor(new DeltaProgressMonitorInc(delta));
        return this;
    }


    @Override
    public NutsProgressMonitor translate(double factor, double start) {
        return new DefaultProgressMonitor(null, new ProgressMonitorTranslator(this, factor, start), null, session);
    }

    /**
     * creates Monitors for each enabled Element or null if false
     *
     * @return NutsProgressMonitor[] array that contains nulls or  translated baseMonitor
     */
    @Override
    public NutsProgressMonitor[] split(int nbrElements) {
        double[] dd = new double[nbrElements];
        Arrays.fill(dd, 1);
        return split(dd);
    }

    @Override
    public NutsProgressMonitor[] split(double... weight) {
        NutsProgressMonitor[] all = new NutsProgressMonitor[weight.length];
        double[] coeffsOffsets = new double[weight.length];
        double[] xweight = new double[weight.length];
        double coeffsSum = 0;
        for (int i = 0; i < weight.length; i++) {
            if (weight[i] > 0) {
                coeffsSum += weight[i];
            }
        }
        double coeffsOffset = 0;
        for (int i = 0; i < weight.length; i++) {
            boolean enabledElement = weight[i] > 0;
            if (enabledElement) {
                coeffsOffsets[i] = coeffsOffset;
                xweight[i] = (weight[i] / coeffsSum);
                coeffsOffset += xweight[i];
            }
        }
        for (int i = 0; i < weight.length; i++) {
            boolean enabledElement = weight[i] > 0;
            if (enabledElement) {
                all[i] = translate(xweight[i], coeffsOffsets[i]);
            } else {
                all[i] = NutsProgressMonitors.of(session).ofSilent();
            }
        }
        return all;
    }


    @Override
    public boolean isSilent() {
        return getSpi() instanceof SilentProgressHandler;
    }
}
