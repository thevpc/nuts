package net.thevpc.nuts.runtime.core.terminals;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsTerminalMode;

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
        this.id = name().toLowerCase().replace('_', '-');
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

    public static NutsTerminalModeOp parseLenient(String value) {
        return parseLenient(value, null);
    }

    public static NutsTerminalModeOp parseLenient(String value, NutsTerminalModeOp emptyOrErrorValue) {
        return parseLenient(value, emptyOrErrorValue, emptyOrErrorValue);
    }

    public static NutsTerminalModeOp parseLenient(String value, NutsTerminalModeOp emptyValue, NutsTerminalModeOp errorValue) {
        if (value == null) {
            value = "";
        } else {
            value = value.toUpperCase().trim().replace('-', '_');
        }
        if (value.isEmpty()) {
            return emptyValue;
        }
        try {
            return NutsTerminalModeOp.valueOf(value.toUpperCase());
        } catch (Exception notFound) {
            return errorValue;
        }
    }
}
