package net.thevpc.nuts.runtime.standalone.io.terminal;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.spi.base.NSystemTerminalBase;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.mon.NChronometer;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NStringUtils;

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

    /**
     * Resolve the terminal geometry.<p>
     * Only live sources are trusted: a terminal can be resized while the
     * process is running, and environment variables ({@code COLUMNS} /
     * {@code LINES}) are a snapshot taken when the process started, so they
     * must never win over a query of the attached tty.
     *
     * @return the current size or {@code null} when it cannot be established
     */
    public static NSystemTerminalBase.Size evalSize() {
        //1. ask the attached tty directly: live, one process, no shell, so it
        //   also works when bash/tput are missing. 'stty size' prints 'rows cols'.
        NSystemTerminalBase.Size s = parseSttySize(evalSizeCommand("stty", "size"));
        if (s != null) {
            return s;
        }
        //2. terminfo. Only as good as the tty it can see, but still live when
        //   stdout is the terminal (tput cols / tput lines).
        Integer columns = evalSizeCommandInt("tput", "cols");
        if (columns != null && columns > 0) {
            Integer rows = evalSizeCommandInt("tput", "lines");
            return new NSystemTerminalBase.Size(columns, rows == null ? -1 : rows);
        }
        //3. last resort only (pipes, CI, minimal images): COLUMNS/LINES do not
        //   follow a resize, they are frozen at process start.
        columns = evalEnvInt("COLUMNS");
        if (columns != null && columns > 0) {
            Integer rows = evalEnvInt("LINES");
            return new NSystemTerminalBase.Size(columns, rows == null ? -1 : rows);
        }
        return null;
    }

    private static NSystemTerminalBase.Size parseSttySize(String value) {
        if (NBlankable.isBlank(value)) {
            return null;
        }
        String[] u = value.trim().split("\\s+");
        if (u.length < 2) {
            return null;
        }
        Integer rows = NLiteral.of(u[0]).asInt().orNull();
        Integer columns = NLiteral.of(u[1]).asInt().orNull();
        if (columns != null && columns > 0) {
            return new NSystemTerminalBase.Size(columns, rows == null ? -1 : rows);
        }
        return null;
    }

    private static Integer evalEnvInt(String name) {
        String s = System.getenv(name);
        return NBlankable.isBlank(s) ? null : NLiteral.of(s).asInt().orNull();
    }

    private static Integer evalSizeCommandInt(String... cmd) {
        String s = evalSizeCommand(cmd);
        return NBlankable.isBlank(s) ? null : NLiteral.of(s).asInt().orNull();
    }

    /**
     * single shot variant used for size queries: reads the terminal directly
     * from /dev/tty, so it neither needs a shell nor inherits a possibly
     * redirected stdin, and it never sleeps/retries (size is polled often).
     */
    private static String evalSizeCommand(String... command) {
        try {
            String s = NExec.of()
                    .system()
                    .in(NExecInput.ofPath(NPath.of("/dev/tty")))
                    .command(command)
                    .failFast(true)
                    .grabbedOutOnly();
            return s == null ? null : NStringUtils.strip(s);
        } catch (Exception ex) {
            return null;
        }
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
                String s = NStringUtils.strip(NExec.of()
                        .system()
                        .in(NExecInput.ofNull())
                        .command(cmd)
                        .failFast(true)
                        .grabbedOutOnly()
                );
                if (!s.isEmpty()) {
                    return s;
                }
                NLog.of(CoreAnsiTermHelper.class)
                        .log(NMsg.ofC("command (%s) returned nothing, repeat with delay").asFinest());
                //add 500 of sleep time!
                s = NStringUtils.strip(NExec.of()
                        .system()
                        .command(cmd)
                        .failFast(true)
                        .sleepDuration(NDuration.ofMillis(500))
                        .grabbedOutOnly()
                        )
                ;
                if (!NStringUtils.isBlank(s)) {
                    return NStringUtils.strip(s);
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
