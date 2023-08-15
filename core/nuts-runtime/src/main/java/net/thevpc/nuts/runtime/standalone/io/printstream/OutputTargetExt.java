package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.NFormat;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.io.NContentMetadataProviderFormatSPI;
import net.thevpc.nuts.text.NTextStyle;

import java.io.OutputStream;

public class OutputTargetExt implements NOutputTarget {

    private OutputStream base;
    private NMsg sourceName;
    private NContentMetadata md;
    private NSession session;

    public OutputTargetExt(OutputStream base, NContentMetadata md0,NSession session) {
        this.base = base;
        this.md = CoreIOUtils.createContentMetadata(md0,base);
        this.session = session;
    }

    @Override
    public OutputStream getOutputStream() {
        return base;
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    @Override
    public NFormat formatter(NSession session) {
        return NFormat.of(session, new NContentMetadataProviderFormatSPI(this, null, "output-stream"));
    }

    @Override
    public String toString() {
        NPlainPrintStream out = new NPlainPrintStream();
        NOptional<NMsg> m = getMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else if (sourceName != null) {
            out.print(sourceName, NTextStyle.path());
        } else {
            out.print(getClass().getSimpleName(), NTextStyle.path());
        }
        return out.toString();
    }
}
