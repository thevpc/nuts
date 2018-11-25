package net.vpc.app.nuts.extensions.terminals;

import net.vpc.app.nuts.NutsFormattedPrintStream;
import net.vpc.app.nuts.NutsTerminal;

import java.io.PrintStream;

public abstract class AbstractNutsTerminal implements NutsTerminal {

    @Override
    public PrintStream getFormattedOut() {
        PrintStream o = getOut();
        if(o instanceof NutsFormattedPrintStream){
            return o;
        }
        return new NutsDefaultFormattedPrintStream(o);
    }

    @Override
    public PrintStream getFormattedErr() {
        PrintStream o = getErr();
        if(o instanceof NutsFormattedPrintStream){
            return o;
        }
        return new NutsDefaultFormattedPrintStream(o);
    }

}
