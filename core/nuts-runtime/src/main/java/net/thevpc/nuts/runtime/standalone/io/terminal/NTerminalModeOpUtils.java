package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.runtime.standalone.io.printstream.NFormattedPrintStream;
import net.thevpc.nuts.runtime.standalone.text.ExtendedFormatAware;

import java.io.OutputStream;

public class NTerminalModeOpUtils {
    public static NTerminalModeOp resolveNutsTerminalModeOp(OutputStream out) {
        if (out == null) {
            return NTerminalModeOp.NOP;
        }
        if (out instanceof ExtendedFormatAware) {
            ExtendedFormatAware a = (ExtendedFormatAware) out;
            return a.getModeOp();
        }
        if (out instanceof NFormattedPrintStream) {
            return NTerminalModeOp.FORMAT;
        }
        return NTerminalModeOp.NOP;
    }
}
