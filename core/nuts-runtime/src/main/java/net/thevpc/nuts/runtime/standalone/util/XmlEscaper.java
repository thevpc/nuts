package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NutsLoggerVerb;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.PomXmlParser;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class XmlEscaper {
    private static final Pattern ENTITY_PATTERN = Pattern.compile("&[a-zA-Z]+;");
    private static final Map<String, String> atToSharp = new HashMap<>();
    private static final Map<String, String> atToUnicode = new HashMap<>();

    static {
        install("&Oslash;", "&#216;","Ø");
        install("&oslash;", "&#248;","ø");
        install("&AElig;", "&#198;","Æ");
        install("&aelig;", "&#230;","æ");
        install("&Auml;", "&#196;","Ä");
        install("&auml;", "&#228;","ä");
        install("&OElig;", "&#338;","Œ");
        install("&oelig;", "&#339;","œ");
        install("&lt;", "&#60;","<");
        install("&gt;", "&#62;",">");
        install("&amp;", "&#38;","&");
        install("&quot;", "&#34;","\"");
        install("&euro;", "&#8364;","€");
        install("&circ;", "&#710;","ˆ");
        install("&tilde;", "&#732;","∼");
        install("&ndash;", "&#45;","–");
        install("&copy;", "&#169;","©");
        install("&nbsp;", "&#32;"," ");
        install("&apos;", "&#39;","'");
    }

    private static void install(String at, String sharp,String unicode){
        atToSharp.put(at,sharp);
        atToUnicode.put(at,unicode);
    }


    public static String escapeToCode(String any, NutsSession session) {
        Matcher m = ENTITY_PATTERN.matcher(any);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String g = m.group();
            String z = atToSharp.get(g);
            if (z != null) {
                m.appendReplacement(sb, z);
            } else {
                NutsLoggerOp.of(PomXmlParser.class, session)
                        .verb(NutsLoggerVerb.WARNING)
                        .level(Level.FINEST)
                        .log(NutsMessage.ofCstyle("unsupported  xml entity declaration : %s", g));
                m.appendReplacement(sb, g);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }

    public static String escapeToUnicode(String any, NutsSession session) {
        Matcher m = ENTITY_PATTERN.matcher(any);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String g = m.group();
            String z = atToUnicode.get(g);
            if (z != null) {
                m.appendReplacement(sb, z);
            } else {
                NutsLoggerOp.of(PomXmlParser.class, session)
                        .verb(NutsLoggerVerb.WARNING)
                        .level(Level.FINEST)
                        .log(NutsMessage.ofCstyle("unsupported  xml entity declaration : %s", g));
                m.appendReplacement(sb, g);
            }
        }
        m.appendTail(sb);
        return sb.toString();
    }
}
