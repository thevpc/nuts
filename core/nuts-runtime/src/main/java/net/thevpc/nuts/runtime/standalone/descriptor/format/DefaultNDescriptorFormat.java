package net.thevpc.nuts.runtime.standalone.descriptor.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextCode;

import java.io.*;

public class DefaultNDescriptorFormat extends DefaultFormatBase<NDescriptorFormat> implements NDescriptorFormat {

    private boolean compact;
    private NDescriptor desc;

    public DefaultNDescriptorFormat() {
        super("descriptor-format");
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
        if (isNtf()) {
            ByteArrayOutputStream os=new ByteArrayOutputStream();
            NElements.ofNtfJson(desc).setCompact(isCompact())
                    .print(os);
            NTextCode r = NText.ofCode("json", os.toString());
            out.print(r);
        } else {
            NElements.ofPlainJson(desc).setCompact(isCompact())
                    .print(out);
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
