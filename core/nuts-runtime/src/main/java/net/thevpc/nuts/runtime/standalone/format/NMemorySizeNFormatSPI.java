package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.util.NMemorySize;
import net.thevpc.nuts.util.NMemorySizeFormat;

public class NMemorySizeNFormatSPI implements NFormatSPI {
    private Boolean iec;
    private boolean fixed;
    private NMemorySize value;

    public NMemorySizeNFormatSPI(NMemorySize value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "memory-size";
    }

    @Override
    public void print(NPrintStream out) {
        NMemorySizeFormat.of(fixed, iec).print(value, out);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        switch (a.key()) {
            case "--iec": {
                a = cmdLine.nextFlag().get();
                if (a.isNonCommented()) {
                    iec = a.getBooleanValue().get();
                }
                return true;
            }
            case "--fixed": {
                a = cmdLine.nextFlag().get();
                if (a.isNonCommented()) {
                    fixed = a.getBooleanValue().get();
                }
                return true;
            }
        }
        return false;
    }
}
