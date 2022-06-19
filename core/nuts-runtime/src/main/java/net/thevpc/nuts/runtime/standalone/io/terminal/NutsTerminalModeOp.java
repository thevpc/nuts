package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsTerminalMode;
import net.thevpc.nuts.util.NutsEnum;
import net.thevpc.nuts.util.NutsNameFormat;
import net.thevpc.nuts.util.NutsStringUtils;

public enum NutsTerminalModeOp  implements NutsEnum {
    NOP(NutsTerminalMode.INHERITED, NutsTerminalMode.INHERITED),
    FILTER(NutsTerminalMode.FORMATTED, NutsTerminalMode.INHERITED),
    FORMAT(NutsTerminalMode.FORMATTED, NutsTerminalMode.INHERITED),
    ESCAPE(NutsTerminalMode.FORMATTED, NutsTerminalMode.FORMATTED),
    UNESCAPE(NutsTerminalMode.FORMATTED, NutsTerminalMode.FORMATTED);
    private NutsTerminalMode in;
    private NutsTerminalMode out;
    private String id;

    NutsTerminalModeOp(NutsTerminalMode in, NutsTerminalMode out) {
        this.in = in;
        this.out = out;
        this.id = NutsNameFormat.ID_NAME.formatName(name());
    }

    @Override
    public String id() {
        return id;
    }

    public NutsTerminalMode in() {
        return in;
    }

    public NutsTerminalMode out() {
        return out;
    }

    public static NutsOptional<NutsTerminalMode> parse(String value) {
        return NutsStringUtils.parseEnum(value, NutsTerminalMode.class);
    }

}
