package net.thevpc.nuts.runtime.standalone.format.impl;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NObjectWriterSPI;
import net.thevpc.nuts.text.NObjectWriter;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.time.NDurationFormatMode;

public class NChronometerWriterSPI implements NObjectWriterSPI {
    private NDurationFormatMode formatMode;
    private NChronometer value;

    public NChronometerWriterSPI(NChronometer value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "chronometer";
    }

    @Override
    public void print(NPrintStream out) {
        if (value.name() != null) {
            out.print(value.name());
            out.print("=", NTextStyle.separator());
        }
        out.print(NObjectWriter.of(value.duration())
                .configure(true,
                        "--mode",
                        (formatMode == null ? NDurationFormatMode.DEFAULT : formatMode).id())
                .format(value.duration()));
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        switch (a.key()) {
            case "--mode": {
                a = cmdLine.nextEntry().get();
                if (a.isUncommented()) {
                    formatMode = NDurationFormatMode.parse(a.getStringValue().get()).get();
                }
                return true;
            }
        }
        return false;
    }
}
