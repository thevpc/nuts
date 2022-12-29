package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.NFormat;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NStream;
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
    public void print(NStream out) {
        spi.print(out);
    }

    @Override
    public boolean configureFirst(NCommandLine commandLine) {
        return spi.configureFirst(commandLine);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
