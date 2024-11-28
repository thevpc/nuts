package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.xtra.digest.DefaultNDigest;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public class NDescriptorInputSourceFormatSPI implements NFormatSPI {
    private DefaultNDigest.NDescriptorInputSource value;

    public NDescriptorInputSourceFormatSPI(DefaultNDigest.NDescriptorInputSource value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "input-stream";
    }

    @Override
    public void print(NPrintStream out) {
        NOptional<NMsg> m = value.getMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else {
            out.print(getClass().getSimpleName(), NTextStyle.path());
        }
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }
}
