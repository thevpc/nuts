package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class NutsFormatFromSPI extends DefaultFormatBase<NutsFormat> {
    private final NutsFormatSPI spi;

    public NutsFormatFromSPI(NutsFormatSPI spi, NutsSession session) {
        super(session, spi.getName());
        this.spi = spi;
    }

    @Override
    public void print(NutsPrintStream out) {
        spi.print(out);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return spi.configureFirst(commandLine);
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
