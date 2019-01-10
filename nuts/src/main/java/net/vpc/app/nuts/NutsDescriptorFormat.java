package net.vpc.app.nuts;

import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;

public interface NutsDescriptorFormat {
    boolean isPretty();

    NutsDescriptorFormat setPretty(boolean pretty);

    String format(NutsDescriptor descriptor);

    void format(NutsDescriptor descriptor, File file) throws NutsIOException;

    void format(NutsDescriptor descriptor, PrintStream out) throws NutsIOException;

    void format(NutsDescriptor descriptor, OutputStream out) throws NutsIOException;

    void format(NutsDescriptor descriptor, Writer out) throws NutsIOException;
}
