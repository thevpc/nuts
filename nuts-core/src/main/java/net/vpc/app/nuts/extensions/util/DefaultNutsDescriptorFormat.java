package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsDescriptorFormat;
import net.vpc.app.nuts.NutsIOException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.common.io.FileUtils;

import java.io.*;

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
    public String format(NutsDescriptor descriptor) {
        return null;
    }

    @Override
    public void format(NutsDescriptor descriptor, OutputStream os) throws NutsIOException {
        OutputStreamWriter o = new OutputStreamWriter(os);
        format(descriptor, o);
        try {
            o.flush();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void format(NutsDescriptor descriptor, Writer out) throws NutsIOException {
        ws.getIOManager().writeJson(descriptor,out,pretty);
    }

    @Override
    public void format(NutsDescriptor descriptor, PrintStream out) throws NutsIOException {
        PrintWriter out1 = new PrintWriter(out);
        format(descriptor, out1);
        out1.flush();
    }


    @Override
    public void format(NutsDescriptor descriptor, File file) throws NutsIOException {
        FileUtils.createParents(file);
        FileWriter os = null;
        try {
            try {
                os = new FileWriter(file);
            } catch (IOException e) {
                throw new NutsIOException(e);
            }
            format(descriptor, os);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    throw new NutsIOException(e);
                }
            }
        }
    }

    public String toString(NutsDescriptor descriptor) {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(b);
        format(descriptor,w);
        try {
            w.flush();
        } catch (IOException e) {
            //
        }
        return new String(b.toByteArray());
    }

}
