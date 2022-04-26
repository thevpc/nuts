package net.thevpc.nuts.runtime.standalone.log;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;

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

public class NutsLogUtils {
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
//                            new NutsMessage(
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
    public static NutsLogRecord toNutsLogRecord(LogRecord record, NutsSession session){
        if(record instanceof NutsLogRecord){
            return (NutsLogRecord) record;
        }
        Level lvl = record.getLevel();
        NutsLogRecord h = new NutsLogRecord(
                session, lvl,
                lvl.intValue() <= Level.SEVERE.intValue() ? NutsLogVerb.FAIL :
                        lvl.intValue() <= Level.WARNING.intValue() ? NutsLogVerb.WARNING :
                                lvl.intValue() <= Level.INFO.intValue() ? NutsLogVerb.INFO :
                                        lvl.intValue() <= Level.FINE.intValue() ? NutsLogVerb.DEBUG :
                                                NutsLogVerb.DEBUG,
                NutsMessage.jstyle(record.getMessage(),
                        record.getParameters()),
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
        if(l==null){
            l="";
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

    public static String[] stacktraceToArray(Throwable th) {
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            BufferedReader br = new BufferedReader(new StringReader(sw.toString()));
            List<String> s = new ArrayList<>();
            String line = null;
            while ((line = br.readLine()) != null) {
                s.add(line);
            }
            return s.toArray(new String[0]);
        } catch (Exception ex) {
            // ignore
        }
        return new String[0];
    }

    public static String stacktrace(Throwable th){
        try {
            StringWriter sw = new StringWriter();
            try (PrintWriter pw = new PrintWriter(sw)) {
                th.printStackTrace(pw);
            }
            return sw.toString();
        } catch (Exception ex) {
            // ignore
        }
        return "";
    }

    public static final NutsSession resolveSession(LogRecord record,NutsWorkspace ws) {
        NutsSession session = null;
        if (record instanceof NutsLogRecord){
            session=((NutsLogRecord) record).getSession();
        }
        if(session==null){
            session=NutsWorkspaceExt.of(ws).defaultSession();
        }
        return session;
    }

    public static final NutsSession resolveSession(LogRecord record,NutsSession defSession) {
        NutsSession session = null;
        if (record instanceof NutsLogRecord){
            session=((NutsLogRecord) record).getSession();
        }
        if(session==null){
            session=defSession;
        }
        return session;
    }

    public static void traceMessage(NutsLogger log, Level lvl, String name, NutsSession session, NutsFetchMode fetchMode, NutsId id, NutsLogVerb tracePhase, String title, long startTime, NutsMessage extraMsg) {
        if (!log.isLoggable(lvl)) {
            return;
        }
        String sep;
        if (extraMsg == null) {
            sep = "";
            extraMsg = NutsMessage.formatted("");
        } else {
            sep = " : ";
        }
        long time = (startTime != 0) ? (System.currentTimeMillis() - startTime) : 0;
        String modeString = NutsUtilStrings.formatAlign(fetchMode.id(), 7,NutsPositionType.FIRST);
        log.with().session(session).level(lvl).verb(tracePhase).time(time)
                .log(NutsMessage.jstyle("[{0}] {1} {2} {3} {4}",
                        modeString,
                        NutsUtilStrings.formatAlign(name, 20,NutsPositionType.FIRST),
                        NutsUtilStrings.formatAlign(title, 18,NutsPositionType.FIRST),
                        (id == null ? "" : id),
                        extraMsg));
    }
    public static void traceMessage(NutsLogger log,NutsFetchStrategy fetchMode, NutsId id, NutsLogVerb tracePhase, String message, long startTime) {
        if (log.isLoggable(Level.FINEST)) {

            long time = (startTime != 0) ? (System.currentTimeMillis() - startTime) : 0;
            String fetchString = "[" + NutsUtilStrings.formatAlign(fetchMode.id(), 7,NutsPositionType.FIRST) + "] ";
            log.with().level(Level.FINEST)
                    .verb(tracePhase).time(time)
                    .log(NutsMessage.jstyle("{0}{1} {2}",
                            fetchString,
                            id,
                            NutsUtilStrings.formatAlign(message, 18,NutsPositionType.FIRST)
                    ));
        }
    }
}
