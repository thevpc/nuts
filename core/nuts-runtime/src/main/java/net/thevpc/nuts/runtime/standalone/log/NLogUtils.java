package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NPositionType;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogRecord;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NLogUtils {
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private static final Map<Level, String> logLevelCache = new HashMap<>();
    private static final Map<String, String> logVerbCache = new HashMap<>();
    private static final Map<String, String> classNameCache = new HashMap<>();

    //    public static LogRecord toPlainRecord(LogRecord record) {
//        LogRecord h=null;
//        if (record instanceof NutsLogRecord) {
//            NutsLogRecord wRecord = (NutsLogRecord) record;
//            Object[] p = wRecord.getParameters();
//            NutsWorkspace ws = wRecord.getWorkspace();
//            h = new NutsLogRecord(
//                    wRecord.getSession(),
//                    wRecord.getLevel(),
//                    wRecord.getVerb(),
//                    ws.text().setSession(wRecord.getSession()).toText(
//                            new NMsg(
//                                    wRecord.getFormatStyle(),
//                                    wRecord.getMessage(),
//                                    p
//                            )
//
//                    ).toString()
//                    ,
//                    null,
//                    wRecord.isFormatted(),
//                    wRecord.getTime(),
//                    wRecord.getFormatStyle()
//            );
//        }else{
//            return record;
//        }
//        h.setResourceBundle(record.getResourceBundle());
//        h.setResourceBundleName(record.getResourceBundleName());
//        h.setSequenceNumber(record.getSequenceNumber());
//        h.setMillis(record.getMillis());
//        h.setSourceClassName(record.getSourceClassName());
//        h.setLoggerName(record.getLoggerName());
//        h.setSourceMethodName(record.getSourceMethodName());
//        h.setThreadID(record.getThreadID());
//        h.setThrown(record.getThrown());
//        return h;
//    }
    public static String filterLogText(NMsg msg) {
        if (msg == null) {
            return null;
        }
        try {
            return NText.of(msg).filteredText();
        } catch (Exception e) {
            return msg.toString();
        }
    }

    public static NLogRecord toNutsLogRecord(LogRecord record, NSession session) {
        if (record instanceof NLogRecord) {
            return (NLogRecord) record;
        }
        Level lvl = record.getLevel();
        NMsg jMsg = NMsg.ofJ(record.getMessage(),
                record.getParameters());
        NLogRecord h = new NLogRecord(
                session, lvl,
                lvl.intValue() <= Level.SEVERE.intValue() ? NLogVerb.FAIL :
                        lvl.intValue() <= Level.WARNING.intValue() ? NLogVerb.WARNING :
                                lvl.intValue() <= Level.INFO.intValue() ? NLogVerb.INFO :
                                        lvl.intValue() <= Level.FINE.intValue() ? NLogVerb.DEBUG :
                                                NLogVerb.DEBUG,
                jMsg,
                filterLogText(jMsg),
                record.getMillis(),
                record.getThrown()
        );
        h.setResourceBundle(record.getResourceBundle());
        h.setResourceBundleName(record.getResourceBundleName());
        h.setSequenceNumber(record.getSequenceNumber());
        h.setSourceClassName(record.getSourceClassName());
        h.setLoggerName(record.getLoggerName());
        h.setSourceMethodName(record.getSourceMethodName());
        h.setThreadID(record.getThreadID());
        return h;
    }

    public static String logLevel(Level l) {
        String v = logLevelCache.get(l);
        if (v == null) {
            StringBuilder sb = new StringBuilder(l.getName());
            ensureSize(sb, 6);
            v = sb.toString();
            logLevelCache.put(l, v);
        }
        return v;
    }

    public static String logVerb(String l) {
        if (l == null) {
            l = "";
        }
        String v = logVerbCache.get(l);
        if (v == null) {
            StringBuilder sb = new StringBuilder(7/*9*/);
//            sb.append("[");
            sb.append(l);
            ensureSize(sb, 7/*8*/);
//            sb.append("]");
            v = sb.toString();
            logVerbCache.put(l, v);
        }
        return v;
    }

    public static void ensureSize(StringBuilder sb, int size) {
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

    public static String formatClassName(String className) {
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

    public static final NSession resolveSession(LogRecord record, NWorkspace ws) {
        NSession session = null;
        if (record instanceof NLogRecord) {
            session = ((NLogRecord) record).getSession();
        }
        if (session == null && ws != null) {
            session = ws.currentSession();
        }
        if (session == null) {
            session = NSession.of();
        }
        return session;
    }

    public static void traceMessage(NLog log, Level lvl, String name, NFetchMode fetchMode, NId id, NLogVerb tracePhase, String title, long startTime, NMsg extraMsg) {
        if (!log.isLoggable(lvl)) {
            return;
        }
        long time = (startTime != 0) ? (System.currentTimeMillis() - startTime) : 0;
        String modeString = NStringUtils.formatAlign(fetchMode.id(), 7, NPositionType.FIRST);
        log.with().level(lvl).verb(tracePhase).time(time)
                .log(NMsg.ofJ("[{0}] {1} {2} {3} {4}",
                        modeString,
                        NStringUtils.formatAlign(name, 20, NPositionType.FIRST),
                        NStringUtils.formatAlign(title, 18, NPositionType.FIRST),
                        (id == null ? "" : id),
                        extraMsg));
    }

    public static void traceMessage(NLog log, NFetchStrategy fetchMode, NId id, NLogVerb tracePhase, String message, long startTime) {
        if (log.isLoggable(Level.FINEST)) {

            long time = (startTime != 0) ? (System.currentTimeMillis() - startTime) : 0;
            String fetchString = "[" + NStringUtils.formatAlign(fetchMode.id(), 7, NPositionType.FIRST) + "] ";
            log.with().level(Level.FINEST)
                    .verb(tracePhase).time(time)
                    .log(NMsg.ofJ("{0}{1} {2}",
                            fetchString,
                            id,
                            NStringUtils.formatAlign(message, 18, NPositionType.FIRST)
                    ));
        }
    }
}
