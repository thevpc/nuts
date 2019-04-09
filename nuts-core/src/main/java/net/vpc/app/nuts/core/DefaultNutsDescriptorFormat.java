package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFormat;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.NutsTerminal;

public class DefaultNutsDescriptorFormat implements NutsDescriptorFormat {

    private NutsWorkspace ws;
    private boolean pretty;

    public DefaultNutsDescriptorFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsDescriptorFormat pretty(boolean pretty) {
        return setPretty(pretty);
    }

    @Override
    public NutsDescriptorFormat pretty() {
        return pretty(true);
    }

    @Override
    public boolean isPretty() {
        return pretty;
    }

    @Override
    public NutsDescriptorFormat setPretty(boolean pretty) {
        this.pretty = pretty;
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
        ws.io().writeJson(descriptor, out, pretty);

    }

    @Override
    public void println(NutsDescriptor descriptor, Writer out) throws UncheckedIOException {
        ws.io().writeJson(descriptor, out, pretty);
        try {
            out.write("\n");
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
        print(descriptor, ws.getTerminal());
    }

    @Override
    public void println(NutsDescriptor descriptor) {
        println(descriptor, ws.getTerminal());
    }

    @Override
    public void print(NutsDescriptor descriptor, NutsTerminal terminal) {
        print(descriptor, terminal.getOut());
    }

    @Override
    public void println(NutsDescriptor descriptor, NutsTerminal terminal) {
        println(descriptor, terminal.getOut());
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

}
