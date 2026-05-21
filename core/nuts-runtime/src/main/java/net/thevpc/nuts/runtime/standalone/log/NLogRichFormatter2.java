package net.thevpc.nuts.runtime.standalone.log;


import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;

public class NLogRichFormatter2 {
    private long lastMillis = -1;

    public String format(NMsg msg, long timestamp, String sourceClassName, boolean filtered) {
        NTexts tf = NTexts.of();

        NTextBuilder sb = tf.ofBuilder();
        String date = CoreNUtils.DEFAULT_DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(timestamp));

        sb.append(tf.ofStyled(date, NTextStyle.pale()));
        boolean verboseLog = false;//read from session or workspace;
        if (verboseLog) {
            sb.append(" ");
            int len = date.length() + 5;
            StringBuilder sb2 = new StringBuilder(5);
            if (lastMillis > 0) {
                sb2.append(timestamp - lastMillis);
            }
            while (sb2.length() < 5) {
                sb2.append(' ');
            }
            sb.append(sb.toString());
        }
        sb.append(" ");
        switch (msg.level().intValue()) {
            case 1000: {//Level.SEVERE
                sb.append(NLogUtils.logLevel(msg.level()), NTextStyle.error());
                break;
            }
            case 900: {//Level.WARNING
                sb.append(NLogUtils.logLevel(msg.level()), NTextStyle.warn());
                break;
            }
            case 800: {//Level.INFO
                sb.append(NLogUtils.logLevel(msg.level()), NTextStyle.info());
                break;
            }
            case 700: {//Level.CONFIG
                sb.append(NLogUtils.logLevel(msg.level()), NTextStyle.config());
                break;
            }
            case 500: {//Level.FINE
                sb.append(NLogUtils.logLevel(msg.level()), NTextStyle.primary4());
                break;
            }
            case 400: {//Level.FINER
//                    sb.append("[[");
                sb.append(NLogUtils.logLevel(msg.level()), NTextStyle.pale());
//                    sb.append("]]");
                break;
            }
            case 300: {//Level.FINEST
                sb.append(NLogUtils.logLevel(msg.level()), NTextStyle.pale());
                break;
            }
            default: {
                sb.append(NLogUtils.logLevel(msg.level()));
                break;
            }
        }

        sb.append(" ");
        switch (msg.intent() == null ? "" : msg.intent().name()) {
            case "FAIL": {//Level.SEVERE
                sb.append(NLogUtils.logVerb(msg.intent().name()), NTextStyle.error());
                break;
            }
            case "WARNING": {//Level.WARNING
                sb.append(NLogUtils.logVerb(msg.intent().name()), NTextStyle.warn());
                break;
            }
            case "UPDATE":
            case "START": {//Level.WARNING
                sb.append(NLogUtils.logVerb(msg.intent().name()), NTextStyle.info());
                break;
            }
            case "SUCCESS": {//Level.INFO
                sb.append(NLogUtils.logVerb(msg.intent().name()), NTextStyle.success());
                break;
            }
//                case NutsLogVerb.BIND:
//                    {//Level.CONFIG
//                    sb.append("^^");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(msg.getIntent())));
//                    sb.append("^^");
//                    break;
//                }
            case "INFO":
            case "READ": {//Level.FINE
                sb.append(NLogUtils.logVerb(msg.intent().name()), NTextStyle.option());
                break;
            }
            case "CACHE":
            case "DEBUG": {//Level.FINE
                sb.append(NLogUtils.logVerb(msg.intent().name()), NTextStyle.pale());
                break;
            }
//                case NutsLogVerb.INIT: {//Level.FINER
//                    sb.append("[[");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logVerb(msg.getIntent())));
//                    sb.append("]]");
//                    break;
//                }
//                case NutsLogVerb.START: {//Level.FINEST
//                    sb.append("<<");
//                    sb.append(tf.escapeText(NutsLogFormatHelper.logLevel(wRecord.getLevel())));
//                    sb.append(">>");
//                    break;
//                }
            default: {
                sb.append(NLogUtils.logVerb(msg.intent() == null ? null : msg.intent().name()));
                break;
            }
        }

        sb.append(" "
                + NLogUtils.formatClassName(sourceClassName)
                + ": ");

        NDuration duration = msg.duration();
        if (duration != null && !duration.isZero()) {
            sb.append("(");
            sb.append(duration);
            sb.append(") ");
        }
        NText msgStr =
                NTexts.of()
                        .of(msg);
        sb.append(msgStr);
        sb.append(NNewLineMode.system().value());
        lastMillis = timestamp;
        if (msg.throwable() != null) {
            sb.append(
                    NText.ofPlain(
                            NStringUtils.stacktrace(msg.throwable())
                    ).toString()
            );
        }
        if (filtered) {
            return sb.filteredText();
        }
        return sb.toString();

    }
}
