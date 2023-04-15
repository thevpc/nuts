package net.thevpc.nuts.runtime.standalone.descriptor.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextCode;
import net.thevpc.nuts.text.NTexts;

import java.io.*;

public class DefaultNDescriptorFormat extends DefaultFormatBase<NDescriptorFormat> implements NDescriptorFormat {

    private boolean compact;
    private NDescriptor desc;

    public DefaultNDescriptorFormat(NSession session) {
        super(session, "descriptor-format");
    }

    public NDescriptorFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NDescriptorFormat compact(boolean compact) {
        return setCompact(compact);
    }

    @Override
    public NDescriptorFormat compact() {
        return compact(true);
    }

    @Override
    public boolean isCompact() {
        return compact;
    }

    @Override
    public NDescriptorFormat setCompact(boolean compact) {
        this.compact = compact;
        return this;
    }

    public NDescriptor getDescriptor() {
        return desc;
    }

    public NDescriptorFormat setDescriptor(NDescriptor desc) {
        this.desc = desc;
        return this;
    }

    public NDescriptorFormat setValue(NDescriptor desc) {
        return setDescriptor(desc);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }

    @Override
    public void print(NPrintStream out) {
        checkSession();
        if (isNtf()) {
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            NElements.of(getSession())
                    .setNtf(true).json()
                    .setValue(desc).setCompact(isCompact())
                    .print(os);
            NTextCode r = NTexts.of(getSession()).ofCode("json", os.toString());
            out.print(r);
        } else {
            NElements.of(getSession()).setNtf(false).json()
                    .setValue(desc).setCompact(isCompact())
                    .print(out);
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
