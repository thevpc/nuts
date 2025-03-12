package net.thevpc.nuts.runtime.standalone.xtra.mon;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.NWorkspaceModel;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.time.*;

import java.util.*;

public class DefaultProgressMonitor implements NProgressMonitor {
    public static final NMsg EMPTY_MESSAGE = NMsg.ofPlain("");
    private List<NProgressListener> listeners = new ArrayList<>();
    private DefaultNProgressMonitorModel model = new DefaultNProgressMonitorModel();
    private boolean strictComputationMonitor = true;
    private NProgressHandler spi;
    private NProgressMonitorInc incrementor;// = new DeltaProgressMonitorInc(1E-2);


    public DefaultProgressMonitor(String id, NProgressHandler spi, NProgressMonitorInc incrementor) {
        model.setId(id == null ? UUID.randomUUID().toString() : id);
        this.spi = spi;
        this.incrementor = incrementor;
    }

    @Override
    public void runWithAll(Runnable... runnables) {
        NProgressMonitor mon = NProgressMonitor.of();
        mon.start();
        runnables = Arrays.stream(runnables).filter(x -> x != null).toArray(Runnable[]::new);
        NProgressMonitor[] mons = mon.split(runnables.length);
        for (int i = 0; i < mons.length; i++) {
            mons[i].runWith(runnables[i]);
        }
        mon.complete();
    }

    @Override
    public void runWithAll(Runnable[] runnables, double[] weights) {
        NAssert.requireEquals(runnables.length, weights.length, "runWithAll");
        int count = 0;
        for (int i = 0; i < runnables.length; i++) {
            if (runnables[i] != null) {
                count++;
            }
        }
        Runnable[] runnables2 = new Runnable[count];
        double[] weights2 = new double[count];
        count = 0;
        for (int i = 0; i < runnables.length; i++) {
            if (runnables[i] != null) {
                runnables2[i] = runnables[i];
                weights2[i] = weights[i];
                count++;
            }
        }
        NProgressMonitor mon = NProgressMonitor.of();
        mon.start();
        NProgressMonitor[] mons = mon.split(weights2);
        for (int i = 0; i < mons.length; i++) {
            mons[i].runWith(runnables2[i]);
        }
        mon.complete();
    }

    @Override
    public void runWith(Runnable runnable) {
        NWorkspaceModel m = NWorkspaceExt.of().getModel();
        Stack<NProgressMonitor> u = m.currentProgressMonitors.get();
        if (u == null) {
            u = new Stack<>();
            m.currentProgressMonitors.set(u);
        }
        u.push(this);
        start();
        try {
            runnable.run();
            complete();
        } finally {
            complete();
            u.pop();
        }
    }

    @Override
    public <T> T callWith(NCallable<T> runnable) {
        NWorkspaceModel m = NWorkspaceExt.of().getModel();
        Stack<NProgressMonitor> u = m.currentProgressMonitors.get();
        if (u == null) {
            u = new Stack<>();
            m.currentProgressMonitors.set(u);
        }
        u.push(this);
        try {
            return runnable.call();
        } finally {
            u.pop();
        }
    }

    @Override
    public final NProgressMonitor setProgress(double progress, NMsg message) {
        setProgress(progress);
        if (message != null) {
            setMessage(message);
        }
        return this;
    }


    @Override
    public NProgressMonitor start(NMsg message) {
        if (!isStarted()) {
            model.setStarted(true);
            model.setProgress(0);
            model.getChronometer().start();
            setMessageIfNotNull(message);
            fireEvent(NProgressEventType.START, "started", false);
        }
        return this;
    }

    @Override
    public NProgressMonitor start() {
        return start(null);
    }


    public NProgressMonitor complete() {
        return complete(null);
    }

    public NProgressMonitor complete(NMsg message) {
        return setTerminated(true, message);
    }

    public NProgressMonitor undoComplete() {
        return undoComplete(null);
    }

    public NProgressMonitor undoComplete(NMsg message) {
        return setTerminated(false, message);
    }

    protected NProgressMonitor setTerminated(boolean terminated, NMsg message) {
        if (terminated) {
            if (!isCompleted()) {
                if (!isStarted()) {
                    model.setStarted(true);
                    model.getChronometer().start();
                    fireEvent(NProgressEventType.START, "started", false);
                }
                model.setProgress(1); // really?
                setMessageIfNotNull(message);
                boolean oldCompleted = model.isCompleted();
                if (!oldCompleted) {
                    model.setCompleted(true);
                    fireEvent(NProgressEventType.COMPLETE, "completed", oldCompleted);
                }
            }
        } else {
            if (isCompleted()) {
                model.setCompleted(false);
                fireEvent(NProgressEventType.UNDO_COMPLETE, "completed", false);
            }
        }
        return this;
    }

    @Override
    public NProgressMonitor cancel() {
        return cancel(null);
    }

    @Override
    public NProgressMonitor cancel(NMsg message) {
        return setCancelled(true, message);
    }

    public NProgressMonitor undoCancel() {
        return undoCancel(null);
    }

    @Override
    public NProgressMonitor undoCancel(NMsg message) {
        return setCancelled(false, message);
    }

    protected NProgressMonitor setCancelled(boolean cancel, NMsg message) {
        if (cancel) {
            if (!isCanceled()) {
                model.setCancelled(true);
                setMessageIfNotNull(message);
                fireEvent(NProgressEventType.CANCEL, "canceled", false);
            }
        } else {
            if (isCanceled()) {
                model.setCancelled(false);
                setMessageIfNotNull(message);
                fireEvent(NProgressEventType.UNDO_CANCEL, "canceled", true);
            }
        }
        return this;
    }

    @Override
    public NProgressMonitor undoSuspend() {
        return undoSuspend(null);
    }

    @Override
    public NProgressMonitor undoSuspend(NMsg message) {
        return setSuspended(false, null);
    }

    @Override
    public NProgressMonitor suspend() {
        return suspend(null);
    }

    @Override
    public NProgressMonitor suspend(NMsg message) {
        return setSuspended(true, null);
    }

    public NProgressMonitor setSuspended(boolean suspend, NMsg message) {
        if (suspend) {
            if (!isSuspended()) {
                model.setSuspended(true);
                setMessageIfNotNull(message);
                fireEvent(NProgressEventType.SUSPEND, "suspended", false);
            }
        } else {
            if (isSuspended()) {
                model.setSuspended(false);
                setMessageIfNotNull(message);
                fireEvent(NProgressEventType.UNDO_SUSPEND, "suspended", true);
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
    public NProgressMonitor block() {
        return block(null);
    }

    @Override
    public NProgressMonitor block(NMsg message) {
        return setBlocked(true, message);
    }

    @Override
    public NProgressMonitor undoBlock() {
        return undoBlock(null);
    }

    @Override
    public NProgressMonitor undoBlock(NMsg message) {
        return setBlocked(false, message);
    }

    public NProgressMonitor setBlocked(boolean block, NMsg message) {
        if (block) {
            if (!model.isBlocked()) {
                model.setBlocked(true);
                setMessageIfNotNull(message);
                fireEvent(NProgressEventType.BLOCK, "blocked", false);
            }
        } else {
            if (model.isBlocked()) {
                model.setBlocked(false);
                setMessageIfNotNull(message);
                fireEvent(NProgressEventType.UNDO_BLOCK, "blocked", true);
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
            fireEvent(NProgressEventType.RESET, property, null);
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
            fireEvent(NProgressEventType.UPDATE, "name", old);
        }
    }

    @Override
    public NMsg getDescription() {
        return model.getDescription();
    }

    protected NProgressMonitor setDescription(NMsg desc) {
        NMsg old = this.model.getDescription();
        if (!Objects.equals(old, desc)) {
            this.model.setDescription(desc);
            fireEvent(NProgressEventType.UPDATE, "description", old);
        }
        return this;
    }

    @Override
    public NProgressMonitor addListener(NProgressListener listener) {
        listeners.add(listener);
        return this;
    }

    @Override
    public NProgressMonitor removeListener(NProgressListener listener) {
        listeners.remove(listener);
        return this;
    }

    @Override
    public NProgressListener[] getListeners() {
        return listeners.toArray(new NProgressListener[0]);
    }

    public NDuration getDuration() {
        return model.getChronometer().getDuration();
    }

    public NClock getStartClock() {
        return model.getChronometer().getStartClock();
    }


    @Override
    public final NProgressMonitor setMessage(NMsg message) {
        NMsg old = getMessage();
        if (setMessage0(message)) {
            fireEvent(NProgressEventType.MESSAGE, "message", old);
        }
        return this;
    }

    @Override
    public NMsg getMessage() {
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

    private final boolean setMessageIfNotNull(NMsg message) {
        if (message != null) {
            return setMessage0(message);
        }
        return false;
    }

    private final boolean setMessage0(NMsg message) {
        NMsg newMessage = message == null ? EMPTY_MESSAGE : message;
        if (!Objects.equals(getMessage(), newMessage)) {
            model.setMessage(newMessage);
            return true;
        }
        return false;
    }

    private void fireEvent(NProgressEventType state, String propertyName, Object oldValue) {
        spi.onEvent(new DefaultNProgressHandlerEvent(state, propertyName, model, NSession.of()));
        for (NProgressListener listener : getListeners()) {
            switch (state) {
                case START: {
                    listener.onProgress(NProgressEvent.ofStart(null, getMessage(), -1));
                    break;
                }
                case COMPLETE: {
                    listener.onProgress(NProgressEvent.ofComplete(null, getMessage(), model.getGlobalCount(), model.getGlobalDurationNanos(), model.getProgress(), model.getPartialCount(), model.getPartialDurationNanos(), model.getLength(), model.getException()));
                    break;
                }
                default: {
                    listener.onProgress(NProgressEvent.ofProgress(null, getMessage(), model.getGlobalCount(), model.getGlobalDurationNanos(), model.getProgress(), model.getPartialCount(), model.getPartialDurationNanos(), model.getLength(), model.getException()));
                    break;
                }
            }
        }
    }

    @Override
    public final NProgressMonitor setProgress(double progress) {
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
                throw new NIllegalArgumentException(NMsg.ofC("invalid Progress value [0..1] : %s", progress));
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
            fireEvent(NProgressEventType.PROGRESS, "progress", oldProgress);
        }
        if (progress >= 1) {
            if (!isCompleted()) {
                complete();
            }
        }
        return this;
    }


    @Override
    public final NProgressMonitor setProgress(long i, long max) {
        return this.setProgress(i, max, null);
    }

    @Override
    public final NProgressMonitor setProgress(long i, long max, NMsg message) {
        return this.setProgress((1.0 * i / max), message);
    }

    @Override
    public final NProgressMonitor setProgress(long i, long maxi, long j, long maxj) {
        return this.setProgress(i, maxi, j, maxj, null);
    }


    @Override
    public final NProgressMonitor setProgress(long i, long j, long maxi, long maxj, NMsg message) {
        return this.setProgress(((1.0 * i * maxi) + j) / (maxi * maxj), message);
    }

    @Override
    public final NProgressMonitor inc() {
        inc(null);
        return this;
    }

    @Override
    public final NProgressMonitor inc(NMsg message) {
        NProgressMonitorInc incrementor = this.incrementor;
        NAssert.requireNonNull(incrementor, "incrementor");
        double oldProgress = getProgress();
        double newProgress = incrementor.inc(oldProgress);
        setProgress(newProgress, message);
        return this;
    }

    @Override
    public final NDuration getEstimatedTotalDuration() {
        double d = getProgress();
        NDuration spent = getDuration();
        if (spent == null) {
            return null;
        }
        return spent.mul(1 / d);
    }

    @Override
    public final NDuration getEstimatedRemainingDuration() {
        double d = getProgress();
        NDuration spent = getDuration();
        if (spent == null) {
            return null;
        }
        double d2 = (1 - d) / d;
        return spent.mul(d2);
    }

    @Override
    public NProgressMonitor setIndeterminate() {
        return setIndeterminate(null);
    }

    @Override
    public NProgressMonitor setIndeterminate(NMsg message) {
        return setProgress(Double.NaN, message);
    }

    public DefaultProgressMonitor setIncrementor(NProgressMonitorInc incrementor) {
        this.incrementor = incrementor;
        return this;
    }

    public NProgressMonitorInc getIncrementor() {
        return incrementor;
    }

    public NProgressHandler getSpi() {
        return spi;
    }


    @Override
    public NProgressMonitor translate(long index, long max) {
        return new DefaultProgressMonitor(null, new ProgressMonitorTranslator(this, 1.0 / max, index * (1.0 / max)), null);
    }

    @Override
    public NProgressMonitor translate(long i, long imax, long j, long jmax) {
        return new DefaultProgressMonitor(null, new ProgressMonitorTranslator(this, 1.0 / (imax * jmax), ((1.0 * i * imax) + j) / (imax * jmax)), null);
    }

    @Override
    public NProgressMonitor stepInto(NMsg message) {
        final NProgressMonitorInc incrementor = getIncrementor();
        NAssert.requireNonNull(incrementor, "incrementor");
        double a = getProgress();
        double b = incrementor.inc(a);
        if (message != null) {
            setMessage(message);
        }
        return translate(b - a, a);
    }

    @Override
    public NProgressMonitor stepInto(long index, long max) {
        return translate(1.0 / max, index * (1.0 / max));
    }

    @Override
    public NProgressMonitor temporize(long freq) {
        return new DefaultProgressMonitor(null, new FreqProgressHandler(this, freq), null);
    }

    @Override
    public NProgressMonitor incremental(long iterations) {
        setIncrementor(new LongIterationProgressMonitorInc(iterations));
        return this;
    }

    @Override
    public NProgressMonitor incremental(double delta) {
        this.setIncrementor(new DeltaProgressMonitorInc(delta));
        return this;
    }


    @Override
    public NProgressMonitor translate(double factor, double start) {
        return new DefaultProgressMonitor(null, new ProgressMonitorTranslator(this, factor, start), null);
    }

    /**
     * creates Monitors for each enabled Element or null if false
     *
     * @return NutsProgressMonitor[] array that contains nulls or  translated baseMonitor
     */
    @Override
    public NProgressMonitor[] split(int nbrElements) {
        double[] dd = new double[nbrElements];
        Arrays.fill(dd, 1);
        return split(dd);
    }

    @Override
    public NProgressMonitor[] split(double... weight) {
        NProgressMonitor[] all = new NProgressMonitor[weight.length];
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
                all[i] = NProgressMonitors.of().ofSilent();
            }
        }
        return all;
    }


    @Override
    public boolean isSilent() {
        return getSpi() instanceof SilentProgressHandler;
    }
}
