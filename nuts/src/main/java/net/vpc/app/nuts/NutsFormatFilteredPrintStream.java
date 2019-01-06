package net.vpc.app.nuts;


import java.io.PrintStream;

public interface NutsFormatFilteredPrintStream extends NutsComponent<Object> {
    PrintStream getUnformattedInstance();
}
