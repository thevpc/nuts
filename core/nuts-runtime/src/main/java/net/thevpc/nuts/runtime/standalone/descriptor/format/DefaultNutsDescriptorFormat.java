package net.thevpc.nuts.runtime.standalone.descriptor.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.*;

public class DefaultNutsDescriptorFormat extends DefaultFormatBase<NutsDescriptorFormat> implements NutsDescriptorFormat {

    private boolean compact;
    private NutsDescriptor desc;

    public DefaultNutsDescriptorFormat(NutsSession session) {
        super(session, "descriptor-format");
    }

    public NutsDescriptorFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
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

    public NutsDescriptorFormat setValue(NutsDescriptor desc) {
        return setDescriptor(desc);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        return false;
    }

    @Override
    public void print(NutsPrintStream out) {
        checkSession();
        if (isNtf()) {
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            NutsElements.of(getSession())
                    .setNtf(true).json()
                    .setValue(desc).setCompact(isCompact())
                    .print(os);
            NutsTextCode r = NutsTexts.of(getSession()).ofCode("json", os.toString());
            out.print(r);
        } else {
            NutsElements.of(getSession()).setNtf(false).json()
                    .setValue(desc).setCompact(isCompact())
                    .print(out);
        }
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
