package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFormat;
import net.vpc.app.nuts.NutsWorkspace;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.NutsTerminal;

public class DefaultNutsDescriptorFormat implements NutsDescriptorFormat {

    private NutsWorkspace ws;
    private boolean pretty;

    public DefaultNutsDescriptorFormat(NutsWorkspace ws) {
        this.ws = ws;
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
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b);
        format(descriptor, w);
        try {
            w.flush();
        } catch (IOException e) {
            //
        }
        return new String(b.toByteArray());
    }

    @Override
    public void format(NutsDescriptor descriptor, OutputStream os) throws UncheckedIOException {
        OutputStreamWriter o = new OutputStreamWriter(os);
        format(descriptor, o);
        try {
            o.flush();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void format(NutsDescriptor descriptor, Writer out) throws UncheckedIOException {
        ws.io().writeJson(descriptor, out, pretty);
    }

    @Override
    public void format(NutsDescriptor descriptor, PrintStream out) throws UncheckedIOException {
        PrintWriter out1 = new PrintWriter(out);
        format(descriptor, out1);
        out1.flush();
    }

    @Override
    public void format(NutsDescriptor descriptor, File file) throws UncheckedIOException {
        format(descriptor, file.toPath());
    }

    @Override
    public void format(NutsDescriptor descriptor) {
        format(descriptor, ws.getTerminal());
    }

    @Override
    public void format(NutsDescriptor descriptor, NutsTerminal terminal) {
        format(descriptor, terminal.getOut());
    }

    @Override
    public void format(NutsDescriptor descriptor, Path file) throws UncheckedIOException {
        try {
            Files.createDirectories(file.getParent());
            try (Writer os = Files.newBufferedWriter(file)) {
                format(descriptor, os);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
