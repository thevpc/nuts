package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.io.NContentMetadataProviderFormatSPI;
import net.thevpc.nuts.text.NTextStyle;

import java.io.OutputStream;

public class OutputTargetExt implements NOutputTarget {

    private OutputStream base;
    private NMsg sourceName;
    private NContentMetadata md;
    private NWorkspace workspace;

    public OutputTargetExt(OutputStream base, NContentMetadata md0,NWorkspace workspace) {
        this.base = base;
        this.md = CoreIOUtils.createContentMetadata(md0,base);
        this.workspace = workspace;
    }

    @Override
    public OutputStream getOutputStream() {
        return base;
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    public NMsg getSourceName() {
        return sourceName;
    }

    @Override
    public String toString() {
        NMemoryPrintStream out = NPrintStream.ofMem(NTerminalMode.FILTERED);
        NOptional<NMsg> m = getMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else if (sourceName != null) {
            out.print(NText.ofStyled(sourceName,NTextStyle.path()));
        } else {
            out.print(getClass().getSimpleName(), NTextStyle.path());
        }
        return out.toString();
    }
}
