package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.text.NFormat;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NFormatSPI;

public class NFormatFromSPI extends DefaultFormatBase<NFormat> {
    private final NFormatSPI spi;

    public NFormatFromSPI(NFormatSPI spi) {
        super(spi.getName());
        this.spi = spi;
    }

    @Override
    public void print(NPrintStream out) {
        spi.print(out);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return spi.configureFirst(cmdLine);
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }
}
