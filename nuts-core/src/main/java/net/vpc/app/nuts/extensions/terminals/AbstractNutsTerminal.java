package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.NutsFormattedPrintStream;
import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsTerminal;

public abstract class AbstractNutsTerminal implements NutsTerminal {

    @Override
    public NutsFormattedPrintStream getFormattedOut() {
        NutsPrintStream o = getOut();
        if(o instanceof NutsFormattedPrintStream){
            return (NutsFormattedPrintStream) o;
        }
        return new NutsDefaultFormattedPrintStream(o);
    }

    @Override
    public NutsFormattedPrintStream getFormattedErr() {
        NutsPrintStream o = getErr();
        if(o instanceof NutsFormattedPrintStream){
            return (NutsFormattedPrintStream) o;
        }
        return new NutsDefaultFormattedPrintStream(o);
    }

}
