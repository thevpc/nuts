package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.NExecCommand;
import net.thevpc.nuts.NExecutionType;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NLiteral;
import net.thevpc.nuts.spi.NSystemTerminalBase;

public class CoreAnsiTermHelper {
    public static boolean isXTerm(NSession session) {
        try {
            String str = "cols";
            NExecCommand.of(session).setExecutionType(NExecutionType.SYSTEM)
                    .grabOutputString()
                    .addCommand("tput", str)
                    .setFailFast(true)
                    .getOutputString();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static NSystemTerminalBase.Size evalSize(NSession session) {
        Integer c = NLiteral.of(evalCapability("cols", session)).asInt().orNull();
        Integer l = NLiteral.of(evalCapability("lines", session)).asInt().orNull();
        if (c != null && l != null) {
            return new NSystemTerminalBase.Size(c, l);
        }
        return null;
    }

    public static NSystemTerminalBase.Cursor evalCursor(NSession session) {
        String c = evalCapability("u7", session);
        if (c != null) {
            return null;
        }
        return null;
    }

    public static String evalCapability(String str, NSession session) {
        try {
            String d = NExecCommand.of(session).setExecutionType(NExecutionType.SYSTEM)
                    .grabOutputString()
                    .addCommand("tput", str)
                    .getOutputString();
            String s = d.trim();
            if (s.isEmpty()) {
                return null;
            }
            //add 500 of sleep time!
            d = NExecCommand.of(session).setExecutionType(NExecutionType.SYSTEM)
                    .grabOutputString()
                    .addCommand("tput", str)
                    .setSleepMillis(500)
                    .getOutputString();
            s = d.trim();
            if (s.isEmpty()) {
                return null;
            }
            return s;
        } catch (Exception ex) {
            return null;
        }
    }
}
