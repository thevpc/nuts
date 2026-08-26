package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.io.NContentMetadataProvider;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.spi.NObjectWriterSPI;
import net.thevpc.nuts.text.NTextStyle;

/**
 * NContentMetadataProviderWriterSPI class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NContentMetadataProviderWriterSPI implements NObjectWriterSPI {
    private NContentMetadataProvider p;
    private NMsg defaultMsg;
    private String defaultName;

    /**
     * N content metadata provider writer spi.
     *
     * @param p p
     * @param defaultMsg default msg
     * @param defaultName default name
     * @return n content metadata provider writer spi result
     */
    public NContentMetadataProviderWriterSPI(NContentMetadataProvider p, NMsg defaultMsg, String defaultName) {
        this.p = p;
        this.defaultName = defaultName;
        this.defaultMsg = defaultMsg;
    }

    @Override
    public String name() {
        String name = p.metaData().name().orNull();
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
        NOptional<NMsg> m = p.metaData().message();
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
