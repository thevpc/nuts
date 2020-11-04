package net.thevpc.nuts.runtime.format;

import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsDescriptor;
import net.thevpc.nuts.NutsDescriptorFormat;
import net.thevpc.nuts.NutsWorkspace;

import java.io.*;

public class DefaultNutsDescriptorFormat extends DefaultFormatBase<NutsDescriptorFormat> implements NutsDescriptorFormat {

    private boolean compact;
    private NutsDescriptor desc;

    public DefaultNutsDescriptorFormat(NutsWorkspace ws) {
        super(ws, "descriptor-format");
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

    public NutsDescriptor getDescriptor() {
        return desc;
    }

    public NutsDescriptorFormat setDescriptor(NutsDescriptor desc) {
        this.desc = desc;
        return this;
    }

    public NutsDescriptorFormat value(NutsDescriptor desc) {
        return setDescriptor(desc);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        return false;
    }

    @Override
    public void print(PrintStream out) {
        getWorkspace().formats().json().compact(isCompact()).value(desc).print(out);
    }

}
