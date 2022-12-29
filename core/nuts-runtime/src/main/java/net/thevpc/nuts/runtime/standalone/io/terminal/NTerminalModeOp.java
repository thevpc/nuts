package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NStringUtils;

public enum NTerminalModeOp implements NEnum {
    NOP(NTerminalMode.INHERITED, NTerminalMode.INHERITED),
    FILTER(NTerminalMode.FORMATTED, NTerminalMode.INHERITED),
    FORMAT(NTerminalMode.FORMATTED, NTerminalMode.INHERITED),
    ESCAPE(NTerminalMode.FORMATTED, NTerminalMode.FORMATTED),
    UNESCAPE(NTerminalMode.FORMATTED, NTerminalMode.FORMATTED);
    private NTerminalMode in;
    private NTerminalMode out;
    private String id;

    NTerminalModeOp(NTerminalMode in, NTerminalMode out) {
        this.in = in;
        this.out = out;
        this.id = NNameFormat.ID_NAME.format(name());
    }

    @Override
    public String id() {
        return id;
    }

    public NTerminalMode in() {
        return in;
    }

    public NTerminalMode out() {
        return out;
    }

    public static NOptional<NTerminalMode> parse(String value) {
        return NStringUtils.parseEnum(value, NTerminalMode.class);
    }

}
