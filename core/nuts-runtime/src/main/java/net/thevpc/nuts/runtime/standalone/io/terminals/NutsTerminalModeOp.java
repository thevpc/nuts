package net.thevpc.nuts.runtime.standalone.io.terminals;

import net.thevpc.nuts.*;

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

    public static NutsTerminalModeOp parse(String value, NutsSession session) {
        return parse(value, null,session);
    }

    public static NutsTerminalModeOp parse(String value, NutsTerminalModeOp emptyValue, NutsSession session) {
        NutsTerminalModeOp v = parseLenient(value, emptyValue, null);
        if(v==null){
            if(!NutsBlankable.isBlank(value)){
                throw new NutsParseEnumException(session,value,NutsTerminalModeOp.class);
            }
        }
        return v;
    }
}
