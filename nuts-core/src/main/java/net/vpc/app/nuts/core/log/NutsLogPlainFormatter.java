package net.vpc.app.nuts.core.log;


import net.vpc.app.nuts.NutsTerminalFormat;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogPlainFormatter extends Formatter {
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final Map<Level, String> logLevelCache = new HashMap<>();
    private final Map<String, String> classNameCache = new HashMap<>();
    private long lastMillis = -1;
    public static final NutsLogPlainFormatter PLAIN = new NutsLogPlainFormatter();

    public NutsLogPlainFormatter() {

    }

    private String logLevel(Level l) {
        String v = logLevelCache.get(l);
        if (v == null) {
            StringBuilder sb = new StringBuilder(l.getName());
            ensureSize(sb, 6);
            v = sb.toString();
            logLevelCache.put(l, v);
        }
        return v;
    }

    private void ensureSize(StringBuilder sb, int size) {
        sb.ensureCapacity(size);
        int length = size - sb.length();
        while (length > 0) {
            if (length >= 16) {
                sb.append("                ");
                length -= 16;
            } else if (length >= 8) {
                sb.append("        ");
                length -= 8;
            } else if (length >= 4) {
                sb.append("    ");
                length -= 4;
            } else if (length >= 2) {
                sb.append("  ");
                length -= 2;
            } else {
                sb.append(' ');
                length--;
            }
        }
    }

    private String formatClassName(String className) {
        if (className == null) {
            return "";
        }
        String v = classNameCache.get(className);
        if (v == null) {
            int maxSize = 47;
            StringBuilder sb = new StringBuilder(maxSize);
            int pos = 0, end;
            while ((end = className.indexOf('.', pos)) >= 0) {
                sb.append(className.charAt(pos)).append('.');
                pos = end + 1;
            }
            sb.append(className.substring(pos));
            ensureSize(sb, maxSize);
            v = sb.toString();
            classNameCache.put(className, v);
        }
        return v;
    }

    @Override
    public String format(LogRecord record) {
        if (record instanceof NutsLogRecord) {
            NutsLogRecord wRecord = (NutsLogRecord) record;
            StringBuilder sb = new StringBuilder();
            String date = Instant.ofEpochMilli(wRecord.getMillis()).toString().replace(":", "");
            sb.append(date);
            for (int i = 22 - date.length() - 1; i >= 0; i--) {
                sb.append(' ');
            }
            boolean verboseLog = false;//read from session or workspace;
            if (verboseLog) {
                sb.append(" ");
                int len = sb.length() + 4;
                if (lastMillis > 0) {
                    sb.append(wRecord.getMillis() - lastMillis);
                }
                ensureSize(sb, len);
            }
            sb
                    .append(" ")
                    .append(logLevel(wRecord.getLevel()))
                    .append(" ")
                    .append(formatClassName(wRecord.getSourceClassName()))
                    .append(": ")
                    .append(formatMessage(wRecord))
                    .append(LINE_SEPARATOR);
            lastMillis = wRecord.getMillis();
            if (wRecord.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    try (PrintWriter pw = new PrintWriter(sw)) {
                        wRecord.getThrown().printStackTrace(pw);
                    }
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    // ignore
                }
            }
            return sb.toString();

        } else {

            StringBuilder sb = new StringBuilder();
            String date = Instant.ofEpochMilli(record.getMillis()).toString().replace(":", "");

            sb.append(date);
            for (int i = 22 - date.length() - 1; i >= 0; i--) {
                sb.append(' ');
            }
            boolean verboseLog = false;//read from session or workspace;
            if (verboseLog) {
                sb.append(" ");
                int len = sb.length() + 4;
                if (lastMillis > 0) {
                    sb.append(record.getMillis() - lastMillis);
                }
                ensureSize(sb, len);
            }
            sb
                    .append(" ")
                    .append(logLevel(record.getLevel()))
                    .append(" ")
                    .append(formatClassName(record.getSourceClassName()))
                    .append(": ")
                    .append(formatMessage(record))
                    .append(LINE_SEPARATOR);
            lastMillis = record.getMillis();
            if (record.getThrown() != null) {
                try {
                    StringWriter sw = new StringWriter();
                    try (PrintWriter pw = new PrintWriter(sw)) {
                        record.getThrown().printStackTrace(pw);
                    }
                    sb.append(sw.toString());
                } catch (Exception ex) {
                    // ignore
                }
            }
            return sb.toString();
        }
    }

}
