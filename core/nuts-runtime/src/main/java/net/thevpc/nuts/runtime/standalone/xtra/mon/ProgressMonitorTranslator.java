package net.thevpc.nuts.runtime.standalone.xtra.mon;


import net.thevpc.nuts.time.NProgressHandler;
import net.thevpc.nuts.time.NProgressHandlerEvent;
import net.thevpc.nuts.time.NProgressMonitor;
import net.thevpc.nuts.util.NAssert;

/**
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 19 juil. 2007 00:27:15
 */
public class ProgressMonitorTranslator implements NProgressHandler {

    private double start;
    private double factor;
    private NProgressMonitor delegate;

    public ProgressMonitorTranslator(NProgressMonitor baseMonitor, double factor, double start) {
        this.delegate = baseMonitor;
        NAssert.requireNonNull(baseMonitor, "baseMonitor");
        this.factor = factor;
        this.start = start;
    }

    public NProgressMonitor getDelegate() {
        return delegate;
    }

    @Override
    public void onEvent(NProgressHandlerEvent event) {
        double progress = event.getModel().getProgress();
        double translatedProgress = Double.isNaN(progress) ? progress : (progress * factor + start);
//        double translatedProgress = (progress-start)/factor;
        if (!Double.isNaN(progress) && (translatedProgress < 0 || translatedProgress > 1)) {
            if (translatedProgress > 1 && translatedProgress < 1.1) {
                translatedProgress = 1;
//            } else {
//                System.err.println("ProgressMonitorTranslator : %= " + translatedProgress + "????????????");
            }
        }
        getDelegate().setProgress(translatedProgress, event.getModel().getMessage());
    }

//    @Override
//    public double getProgress() {
//        double d = (getDelegate().getProgress() - start) / factor;
//        return d < 0 ? 0 : d > 1 ? 1 : d;
//    }

}
