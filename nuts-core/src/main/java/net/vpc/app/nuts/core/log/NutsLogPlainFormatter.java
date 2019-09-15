package net.vpc.app.nuts.core.log;


import net.vpc.app.nuts.NutsTerminalFormat;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class NutsLogPlainFormatter extends Formatter {
    private long lastMillis = -1;
    public static final NutsLogPlainFormatter PLAIN = new NutsLogPlainFormatter();

    public NutsLogPlainFormatter() {

    }

    @Override
    public String format(LogRecord record) {
        if (record instanceof NutsLogRecord) {
            NutsLogRecord wRecord = (NutsLogRecord) record;
            StringBuilder sb = new StringBuilder();
            String date = CoreNutsUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(wRecord.getMillis()));
            sb.append(date);
            boolean verboseLog = false;//read from session or workspace;
            if (verboseLog) {
                sb.append(" ");
                int len = sb.length() + 4;
                if (lastMillis > 0) {
                    sb.append(wRecord.getMillis() - lastMillis);
                }
                NutsLogFormatHelper.ensureSize(sb, len);
            }
            sb
                    .append(" ")
                    .append(NutsLogFormatHelper.logLevel(wRecord.getLevel()))
                    .append(" ")
                    .append(NutsLogFormatHelper.logVerb(wRecord.getVerb()))
                    .append(" ")
                    .append(NutsLogFormatHelper.formatClassName(wRecord.getSourceClassName()))
                    .append(": ");
            String msgStr = formatMessage(wRecord);
            if(((NutsLogRecord) record).isFormatted()) {
                sb.append(wRecord.getWorkspace().io().terminalFormat().filterText(msgStr));
            }else{
                sb.append(msgStr);
            }
            if(wRecord.getTime()>0){
                sb.append(" (").append(CoreCommonUtils.formatPeriodMilli(wRecord.getTime())).append(")");
            }
            sb.append(NutsLogFormatHelper.LINE_SEPARATOR);
            lastMillis = wRecord.getMillis();
            if (wRecord.getThrown() != null) {
                sb.append(NutsLogFormatHelper.stacktrace(wRecord.getThrown()));
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
                NutsLogFormatHelper.ensureSize(sb, len);
            }
            sb
                    .append(" ")
                    .append(NutsLogFormatHelper.logLevel(record.getLevel()))
                    .append(" ")
                    .append(NutsLogFormatHelper.logVerb(""))
                    .append(" ")
                    .append(NutsLogFormatHelper.formatClassName(record.getSourceClassName()))
                    .append(": ")
                    .append(formatMessage(record))
                    .append(NutsLogFormatHelper.LINE_SEPARATOR);
            lastMillis = record.getMillis();
            if (record.getThrown() != null) {
                sb.append(NutsLogFormatHelper.stacktrace(record.getThrown()));
            }
            return sb.toString();
        }
    }

}
