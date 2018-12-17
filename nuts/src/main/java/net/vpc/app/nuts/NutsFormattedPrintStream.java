package net.vpc.app.nuts;


import java.io.PrintStream;

public interface NutsFormattedPrintStream extends NutsComponent<Object> {
    PrintStream getUnformattedInstance();
}
