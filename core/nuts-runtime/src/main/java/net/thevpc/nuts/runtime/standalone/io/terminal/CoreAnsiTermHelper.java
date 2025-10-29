package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.command.NExecCmd;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.util.NBlankable;
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
    public static String stty(String str,long timeout) {
        return NExecCmd.of()
                .system()
                .addCommand("stty", str)
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
        {
            String size = evalCommand("bash", "-c","echo $(tput lines) $(tput cols)");
            if (!NBlankable.isBlank(size)) {
                String[] u = size.split(" ");
                if (u.length >= 2) {
                    Integer l = NLiteral.of(u[0]).asInt().orNull();
                    Integer c = NLiteral.of(u[1]).asInt().orNull();
                    if (c != null && l != null) {
                        return new NSystemTerminalBase.Size(c, l);
                    }
                }
            }
        }
        return null;
    }

    public static NSystemTerminalBase.Cursor evalCursor() {
        String c = evalCommand("tput","u7");
        if (c != null) {
            return null;
        }
        return null;
    }

    public static String evalCommand(String ...cmd) {
        try {
            String s= NExecCmd.of()
                    .system()
                    .addCommand(cmd)
                    .failFast()
                    .getGrabbedOutOnlyString()
                    .trim()
                    ;
            if (!s.trim().isEmpty()) {
                return s.trim();
            }
            //add 500 of sleep time!
            s= NExecCmd.of()
                    .system()
                    .addCommand(cmd)
                    .failFast()
                    .setSleepMillis(500)
                    .getGrabbedOutOnlyString()
                    .trim()
                    ;
            if (!s.trim().isEmpty()) {
                return s.trim();
            }
            return null;
        } catch (Exception ex) {
            return null;
        }
    }
}
