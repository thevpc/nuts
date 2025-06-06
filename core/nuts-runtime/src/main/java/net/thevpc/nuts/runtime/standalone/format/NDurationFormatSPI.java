package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.time.DefaultNDurationFormat;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.time.NDurationFormatMode;

public class NDurationFormatSPI implements NFormatSPI {
    private NDurationFormatMode formatMode;
    private NDuration value;

    public NDurationFormatSPI(NDuration value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "duration";
    }

    @Override
    public void print(NPrintStream out) {
        DefaultNDurationFormat.of(formatMode).print(value, out);
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        switch (a.key()) {
            case "--mode": {
                a = cmdLine.nextEntry().get();
                if (a.isNonCommented()) {
                    formatMode = NDurationFormatMode.parse(a.getStringValue().get()).get();
                }
                return true;
            }
        }
        return false;
    }
}
