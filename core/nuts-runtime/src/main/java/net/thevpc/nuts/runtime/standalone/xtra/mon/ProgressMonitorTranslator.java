package net.thevpc.nuts.runtime.standalone.xtra.mon;


import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.util.*;

/**
 * @author Taha Ben Salah (taha.bensalah@gmail.com)
 * %creationtime 19 juil. 2007 00:27:15
 */
public class ProgressMonitorTranslator implements NutsProgressHandler {

    private double start;
    private double factor;
    private NutsProgressMonitor delegate;

    public ProgressMonitorTranslator(NutsProgressMonitor baseMonitor, double factor, double start) {
        this.delegate = baseMonitor;
        if (baseMonitor == null) {
            throw new NullPointerException("baseMonitor could not be null");
        }
        this.factor = factor;
        this.start = start;
    }

    public NutsProgressMonitor getDelegate() {
        return delegate;
    }

    @Override
    public void onEvent(NutsProgressHandlerEvent event) {
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
