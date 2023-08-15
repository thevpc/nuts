package net.thevpc.nuts.io;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.text.NTextStyle;

public class NContentMetadataProviderFormatSPI implements NFormatSPI {
    private NContentMetadataProvider p;
    private NMsg defaultMsg;
    private String defaultName;

    public NContentMetadataProviderFormatSPI(NContentMetadataProvider p, NMsg defaultMsg,String defaultName) {
        this.p = p;
        this.defaultName = defaultName;
        this.defaultMsg = defaultMsg;
    }

    @Override
    public String getName() {
        String name = p.getMetaData().getName().orNull();
        if (!NBlankable.isBlank(defaultMsg)) {
            name = defaultMsg.toString();
        }
        if (NBlankable.isBlank(name)) {
            name = defaultName;
        }
        if (NBlankable.isBlank(name)) {
            name = "no-name";
        }
        return name;
    }

    @Override
    public void print(NPrintStream out) {
        NOptional<NMsg> m = p.getMetaData().getMessage();
        if (m.isPresent()) {
            out.print(m.get());
        } else {
            if (!NBlankable.isBlank(defaultMsg)) {
                out.print(defaultMsg, NTextStyle.path());
            }else {
                out.print(getClass().getSimpleName(), NTextStyle.path());
            }
        }
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        return false;
    }


}
