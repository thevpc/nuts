package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.runtime.standalone.io.printstream.NutsFormattedPrintStream;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAware;

import java.io.OutputStream;

public class NutsTerminalModeOpUtils {
    public static NutsTerminalModeOp resolveNutsTerminalModeOp(OutputStream out) {
        if (out == null) {
            return NutsTerminalModeOp.NOP;
        }
        if (out instanceof ExtendedFormatAware) {
            ExtendedFormatAware a = (ExtendedFormatAware) out;
            return a.getModeOp();
        }
        if (out instanceof NutsFormattedPrintStream) {
            return NutsTerminalModeOp.FORMAT;
        }
        return NutsTerminalModeOp.NOP;
    }
}
