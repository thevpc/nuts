package net.thevpc.nuts.runtime.core.log;


import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreCommonUtils;

import java.text.MessageFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class NutsLogPlainFormatter extends Formatter {
    public static final NutsLogPlainFormatter PLAIN = new NutsLogPlainFormatter();
    private long lastMillis = -1;

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
                    .append(NutsLogFormatHelper.logVerb(wRecord.getVerb().name()))
                    .append(" ")
                    .append(NutsLogFormatHelper.formatClassName(wRecord.getSourceClassName()))
                    .append(": ");
            Object[] parameters2 = wRecord.getParameters();
            if (parameters2 == null) {
                parameters2 = new Object[0];
            }
            String msgStr = null;
            NutsFormatManager formats = wRecord.getWorkspace().formats();
            NutsTextFormatManager text = formats.text();
            if (wRecord.isFormatted()) {
                msgStr = text.filterText(text.toString(
                        new NutsMessage(
                                wRecord.getFormatStyle(),
                                NutsString.of(wRecord.getMessage()),
                                parameters2
                        ), wRecord.getSession()
                ).toString());
            } else {
                parameters2 = Arrays.copyOf(parameters2, parameters2.length);
                for (int i = 0; i < parameters2.length; i++) {
                    if (parameters2[i] instanceof NutsString) {
                        parameters2[i] = text.filterText(parameters2[i].toString());
                    } else if (parameters2[i] instanceof NutsFormattable) {
                        parameters2[i] = text.filterText(
                                ((NutsFormattable) parameters2[i]).formatter().setSession(wRecord.getSession()).format()
                        );
                    }
                }
                switch (wRecord.getFormatStyle()) {
                    case JSTYLE: {
                        msgStr = MessageFormat.format(wRecord.getMessage(), parameters2);
                        break;
                    }
                    case CSTYLE: {
                        StringBuilder sb2 = new StringBuilder();
                        new java.util.Formatter(sb2, Locale.getDefault()).format(wRecord.getMessage(), parameters2);
                        msgStr = sb2.toString();
                        break;
                    }
                }
            }
            sb.append(msgStr);
            if (wRecord.getTime() > 0) {
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
