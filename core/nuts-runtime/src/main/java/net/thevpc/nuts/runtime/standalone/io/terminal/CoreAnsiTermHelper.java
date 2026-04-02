package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.spi.NSystemTerminalBase;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;

import java.util.Arrays;

public class CoreAnsiTermHelper {
//    public static String tput(String str, long timeout) {
//        return NExec.of()
//                .system()
//                .addCommand("tput", str)
//                .failFast()
//                .getGrabbedOutOnlyString()
//                .trim()
//                ;
//    }

//    public static String stty(String str, long timeout) {
//        return NExec.of()
//                .system()
//                .addCommand("stty", str)
//                .failFast()
//                .getGrabbedOutOnlyString()
//                .trim()
//                ;
//    }

//    public static boolean isXTerm() {
//        try {
//            tput("cols", 0);
//            return true;
//        } catch (Exception ex) {
//            return false;
//        }
//    }

    public static NSystemTerminalBase.Size evalSize() {
        {
            String size = evalCommand("bash", "-c", "echo $(tput lines) $(tput cols)");
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
        String c = evalCommand("tput", "u7");
        if (c != null) {
            return null;
        }
        return null;
    }

    public static String evalCommand(String... cmd) {
        NChronometer chronometer = NChronometer.of();
        try {
            try {
                String s = NExec.of()
                        .system()
                        .setIn(NExecInput.ofNull())
                        .addCommand(cmd)
                        .failFast()
                        .getGrabbedOutOnlyString()
                        .trim();
                if (!s.trim().isEmpty()) {
                    return s.trim();
                }
                NLog.of(CoreAnsiTermHelper.class)
                        .log(NMsg.ofC("command (%s) returned nothing, repeat with delay").asFinest());
                //add 500 of sleep time!
                s = NExec.of()
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
        } finally {
            NLog.of(CoreAnsiTermHelper.class)
                    .log(NMsg.ofC("command (%s) took %s", Arrays.asList(cmd), chronometer.stop()).asFinest());
        }
    }
}
