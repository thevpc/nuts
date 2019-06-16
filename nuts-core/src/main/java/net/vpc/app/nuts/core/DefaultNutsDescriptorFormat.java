package net.vpc.app.nuts.core;

import net.vpc.app.nuts.core.util.NutsConfigurableHelper;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFormat;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.NutsCommandLine;

public class DefaultNutsDescriptorFormat implements NutsDescriptorFormat {

    private NutsWorkspace ws;
    private boolean compact;

    public DefaultNutsDescriptorFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDescriptorFormat compact(boolean compact) {
        return setCompact(compact);
    }

    @Override
    public NutsDescriptorFormat compact() {
        return compact(true);
    }

    @Override
    public boolean isCompact() {
        return compact;
    }


    @Override
    public NutsDescriptorFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    @Override
    public String toString(NutsDescriptor descriptor) {
        return format(descriptor);
    }

    @Override
    public String format(NutsDescriptor descriptor) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b);
        print(descriptor, w);
        try {
            w.flush();
        } catch (IOException e) {
            //
        }
        return new String(b.toByteArray());
    }

    @Override
    public void print(NutsDescriptor descriptor, OutputStream os) throws UncheckedIOException {
        OutputStreamWriter o = new OutputStreamWriter(os);
        print(descriptor, o);
        try {
            o.flush();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void println(NutsDescriptor descriptor, OutputStream os) throws UncheckedIOException {
        OutputStreamWriter o = new OutputStreamWriter(os);
        println(descriptor, o);
        try {
            o.flush();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void print(NutsDescriptor descriptor, Writer out) throws UncheckedIOException {
        ws.format().json().compact(isCompact()).print(descriptor, out);

    }

    @Override
    public void println(NutsDescriptor descriptor, Writer out) throws UncheckedIOException {
        ws.format().json().compact(isCompact()).print(descriptor, out);
        try {
            out.write("\n");
            out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void print(NutsDescriptor descriptor, PrintStream out) throws UncheckedIOException {
        PrintWriter out1 = new PrintWriter(out);
        print(descriptor, out1);
        out1.flush();
    }

    @Override
    public void println(NutsDescriptor descriptor, PrintStream out) throws UncheckedIOException {
        PrintWriter out1 = new PrintWriter(out);
        println(descriptor, out1);
        out1.flush();
    }

    @Override
    public void print(NutsDescriptor descriptor, File file) throws UncheckedIOException {
        print(descriptor, file.toPath());
    }

    @Override
    public void println(NutsDescriptor descriptor, File file) throws UncheckedIOException {
        println(descriptor, file.toPath());
    }

    @Override
    public void print(NutsDescriptor descriptor) {
        print(descriptor, ws.io().getTerminal());
    }

    @Override
    public void println(NutsDescriptor descriptor) {
        println(descriptor, ws.io().getTerminal());
    }

    @Override
    public void print(NutsDescriptor descriptor, NutsTerminal terminal) {
        print(descriptor, terminal.out());
    }

    @Override
    public void println(NutsDescriptor descriptor, NutsTerminal terminal) {
        println(descriptor, terminal.out());
    }

    @Override
    public void print(NutsDescriptor descriptor, Path file) throws UncheckedIOException {
        try {
            Files.createDirectories(file.getParent());
            try (Writer os = Files.newBufferedWriter(file)) {
                print(descriptor, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void println(NutsDescriptor descriptor, Path file) throws UncheckedIOException {
        try {
            Files.createDirectories(file.getParent());
            try (Writer os = Files.newBufferedWriter(file)) {
                println(descriptor, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public final NutsDescriptorFormat configure(boolean skipUnsupported, String... args) {
        return NutsConfigurableHelper.configure(this, ws, skipUnsupported, args,"descriptor-format");
    }

    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, ws,skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        return false;
    }

}
