package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.format.NFormat;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class NFormatFromSPI extends DefaultFormatBase<NFormat> {
    private final NFormatSPI spi;

    public NFormatFromSPI(NFormatSPI spi, NSession session) {
        super(session, spi.getName());
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
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
