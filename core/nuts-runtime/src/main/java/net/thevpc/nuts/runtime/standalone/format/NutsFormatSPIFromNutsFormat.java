package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.NutsFormat;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.spi.NutsFormatSPI;

public class NutsFormatSPIFromNutsFormat implements NutsFormatSPI {
    private final NutsFormat formatter;

    public NutsFormatSPIFromNutsFormat(NutsFormat formatter) {
        this.formatter = formatter;
    }

    @Override
    public void print(NutsPrintStream out) {
        formatter.print(out);
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        return formatter.configureFirst(commandLine);
    }
}
