package net.vpc.app.nuts;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivateNutsLog {
    public static final DateTimeFormatter DEFAULT_DATE_TIME_FORMATTER
            = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
            .withZone(ZoneId.systemDefault());
    private static final Pattern LOG_PARAM_PATTERN = Pattern.compile("\\{(?<v>[0-9]+)}");
    private NutsWorkspaceOptions options;

    public void log(Level lvl, String s) {
        log(lvl, s, new Object[0]);
    }

    public void log(Level lvl, String s, Object object) {
        log(lvl, s, new Object[]{object});
    }

    public void log(Level lvl, String s, Object[] objects) {
        if (isLoggable(lvl)) {
            Matcher m = LOG_PARAM_PATTERN.matcher(s);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String v = m.group("v");
                m.appendReplacement(sb, Matcher.quoteReplacement(String.valueOf(objects[Integer.parseInt(v)])));
            }
            m.appendTail(sb);
            doLog(lvl, sb.toString());
        }
//        LOG.log(lvl, s, objects);
    }

    public void log(Level lvl, String s, Throwable err) {
        if (isLoggable(lvl)) {
            doLog(lvl, s);
            err.printStackTrace(System.err);
        }
        //LOG.log(lvl, s, err);
    }

    private void doLog(Level lvl, String s) {
        System.err.printf("%s %-6s : [%-7s] %s%n", DEFAULT_DATE_TIME_FORMATTER.format(Instant.now()), lvl, "BOOT", s);
    }

    public boolean isLoggable(Level lvl) {
        if (/*options.isDebug() && */options.getLogConfig() != null && lvl.intValue() >= options.getLogConfig().getLogTermLevel().intValue()) {
            return true;
        }
        return false;
    }

    public void setOptions(NutsWorkspaceOptions options) {
        this.options = options;
    }
}
