package net.thevpc.nuts.runtime.standalone.format.impl;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.time.NDurationFormatMode;

public class NChronometerNFormatSPI implements NFormatSPI {
    private NDurationFormatMode formatMode;
    private NChronometer value;

    public NChronometerNFormatSPI(NChronometer value) {
        this.value = value;
    }

    @Override
    public String getName() {
        return "chronometer";
    }

    @Override
    public void print(NPrintStream out) {
        if (value.getName() != null) {
            out.print(value.getName());
            out.print("=", NTextStyle.separator());
        }
        out.print(NFormats.of(value.getDuration()).get()
                .configure(true,
                        "--mode",
                        (formatMode == null ? NDurationFormatMode.DEFAULT : formatMode).id())
                .format());
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
