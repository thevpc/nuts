package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.util.NLiteral;

public class CoreAnsiTermHelper {
    public static String tput(String str,long timeout) {
        return NExecCmd.of()
                .system()
                .addCommand("tput", str)
                .failFast()
                .getGrabbedOutOnlyString()
                .trim()
        ;
    }
    public static boolean isXTerm() {
        try {
            tput("cols",0);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static NSystemTerminalBase.Size evalSize() {
        Integer c = NLiteral.of(evalCapability("cols")).asIntValue().orNull();
        Integer l = NLiteral.of(evalCapability("lines")).asIntValue().orNull();
        if (c != null && l != null) {
            return new NSystemTerminalBase.Size(c, l);
        }
        return null;
    }

    public static NSystemTerminalBase.Cursor evalCursor() {
        String c = evalCapability("u7");
        if (c != null) {
            return null;
        }
        return null;
    }

    public static String evalCapability(String str) {
        try {
            String s = tput(str,0);
            if (s.isEmpty()) {
                return null;
            }
            //add 500 of sleep time!
            s = tput(str,500);
            if (s.isEmpty()) {
                return null;
            }
            return s;
        } catch (Exception ex) {
            return null;
        }
    }
}
