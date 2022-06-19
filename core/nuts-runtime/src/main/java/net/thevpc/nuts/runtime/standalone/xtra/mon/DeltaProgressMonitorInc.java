package net.thevpc.nuts.runtime.standalone.xtra.mon;

/**
 * @author taha.bensalah@gmail.com on 7/22/16.
 */
public class DeltaProgressMonitorInc implements NutsProgressMonitorInc {
    private double delta;

    public DeltaProgressMonitorInc(double delta) {
        this.delta = delta;
    }

    @Override
    public double inc(double last) {
        return last+delta;
    }
}
