package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NObjectWriterSPI;
import net.thevpc.nuts.util.NMemorySize;
import net.thevpc.nuts.util.NMemorySizeFormat;

public class NMemorySizeWriterSPI implements NObjectWriterSPI {
    private Boolean iec;
    private boolean fixed;
    private NMemorySize value;

    public NMemorySizeWriterSPI(NMemorySize value) {
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
                if (a.isUncommented()) {
                    iec = a.getBooleanValue().get();
                }
                return true;
            }
            case "--fixed": {
                a = cmdLine.nextFlag().get();
                if (a.isUncommented()) {
                    fixed = a.getBooleanValue().get();
                }
                return true;
            }
        }
        return false;
    }
}
