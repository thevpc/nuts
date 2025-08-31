package net.thevpc.nuts.runtime.standalone.descriptor.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NDependencyFormat;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextCode;
import net.thevpc.nuts.util.NMsg;

import java.io.*;

public class DefaultNDescriptorFormat extends DefaultFormatBase<NDescriptorFormat> implements NDescriptorFormat {

    private boolean compact;
    private NDescriptor desc;
    private NDescriptorStyle descriptorStyle;

    public DefaultNDescriptorFormat() {
        super("descriptor-format");
    }

    public NDescriptorFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public NDescriptorStyle getDescriptorStyle() {
        return descriptorStyle;
    }

    @Override
    public NDescriptorFormat setDescriptorStyle(NDescriptorStyle descriptorStyle) {
        this.descriptorStyle = descriptorStyle;
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
        NDescriptorStyle s = getDescriptorStyle();
        if (s == null) {
            s = NDescriptorStyle.NUTS;
        }
        switch (s) {
            case NUTS: {
                if (isNtf()) {
                    ByteArrayOutputStream os = new ByteArrayOutputStream();
                    NElementWriter.ofJson().setNtf(true).setCompact(isCompact())
                            .write(desc, os);
                    NTextCode r = NText.ofCode("json", os.toString());
                    out.print(r);
                } else {
                    NElementWriter.ofJson().setCompact(isCompact())
                            .write(desc, out);
                }
                break;
            }
            case MANIFEST: {
                throw new NUnsupportedOperationException(NMsg.ofC("formatting descriptor in %s format is not yet implemented yet, your help is more than welcome", s));
            }
            case MAVEN: {
                throw new NUnsupportedOperationException(NMsg.ofC("formatting descriptor in %s format is not yet implemented yet, your help is more than welcome", s));
            }
        }
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
