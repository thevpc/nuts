package net.thevpc.nuts.runtime.standalone.io.printstream;

import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamExt extends OutputStream implements NContentMetadataProvider {

    private OutputStream base;
    private NMsg sourceName;
    private NContentMetadata md;
    private boolean closeBase;
    private Runnable onClose;

    public OutputStreamExt(OutputStream base, NContentMetadata md0,
                           boolean closeBase,
                           Runnable onClose) {
        this.base = base;
        this.closeBase = closeBase;
        this.onClose = onClose;
        this.md = CoreIOUtils.createContentMetadata(md0, base);
    }

    @Override
    public NContentMetadata getMetaData() {
        return md;
    }

    @Override
    public void write(int b) throws IOException {
        base.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        base.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        base.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        base.flush();
    }

    @Override
    public void close() throws IOException {
        if (closeBase) {
            base.close();
        }
        if (onClose != null) {
            onClose.run();
        }
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
