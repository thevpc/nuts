package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NObjectWriter;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultObjectWriterBase;
import net.thevpc.nuts.spi.NObjectWriterSPI;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NObjectWriterFromSPI extends DefaultObjectWriterBase<NObjectWriter> {
    private final NObjectWriterSPI spi;

    public NObjectWriterFromSPI(NObjectWriterSPI spi) {
        super(spi.getName());
        this.spi = spi;
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        spi.print(out);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return spi.configureFirst(cmdLine);
    }

}
