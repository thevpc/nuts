package net.thevpc.nuts.runtime.standalone.format.text.util;

import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.util.Arrays;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.regex.Pattern;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.text.parser.DefaultNutsTextNodeParser;

public class FormattedPrintStreamUtils {

    // %[argument_index$][flags][width][.precision][t]conversion
    private static final Pattern PRINTF_PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");

    /**
     * transform plain text to formatted text so that the result is rendered as
     * is
     *
     * @param text text
     * @return escaped text
     */
    public static String escapeText(String text) {
        return DefaultNutsTextNodeParser.escapeText0(text);
    }

//    static int countRepeatable(char c,char[] cc,int offset){
//        int count=0;
//        while((offset+count)<cc.length && cc[offset+count]==c){
//            count++;
//        }
//        return count;
//    }

    public static boolean isSpecialFormattedObject(Object a) {
        return (a instanceof NutsFormattable) || (a instanceof NutsStringBase) ;
    }

    public static Object formatArgument(Object a, NutsSession session) {
        if(a instanceof Number && a instanceof Number && a instanceof Date  && a instanceof Temporal) {
            //do nothing
            return a;
        }else if(a instanceof NutsStringBase){
            return String.valueOf(a);
        }else if(a instanceof NutsFormattable){
            if(session==null){
                return escapeText(String.valueOf(a));
            }else{
                try {
                    return  session.getWorkspace().formats().of((NutsFormattable) a).setSession(session).format();
                }catch (Exception ex){
                    return escapeText(String.valueOf(a));
                }
            }
        }else {
            return escapeText(String.valueOf(a));
        }
    }

    public static String formatPositionalStyle(NutsSession session,Locale locale, String format, Object... args) {
        if(session==null){
            throw new RuntimeException("missing session");
        }
        Object[] args2=Arrays.copyOf(args,args.length);
        for (int i = 0; i < args2.length; i++) {
            args2[i]= formatArgument(args2[i],session);
        }
//        char[] m = format.toCharArray();
//        StringBuilder sb=new StringBuilder();
//        for (int i = 0; i < m.length; i++) {
//            if(m[i]=='{' || m[i]=='}') {
//                int r = countRepeatable(m[i], m, i);
//                if (r > 1) {
//                    sb.append('\'');
//                    sb.append(m, i, r);
//                    sb.append('\'');
//                } else {
//                    sb.append(m[i]);
//                }
//                i += r - 1;
//            }else if(m[i]=='\\' && i+1<m.length && m[i+1]=='\''){
//                sb.append("''");
//                i++;
//            }else if(m[i]=='\\' && i+1<m.length){
//                sb.append('\'');
//                sb.append(m[i+1]);
//                sb.append('\'');
//                i++;
//            }else{
//                sb.append(m[i]);
//            }
//        }
//        return MessageFormat.format(sb.toString(), args2);
        return MessageFormat.format(format, args2);
    }
    public static String formatCStyle(NutsSession session,Locale locale, String format, Object... args) {
        return format0(session,locale,format,args);
//        StringBuilder sb = new StringBuilder();
//        Matcher m = PRINTF_PATTERN.matcher(format);
//        int x = 0;
//        for (int i = 0, len = format.length(); i < len;) {
//            if (m.find(i)) {
//                if (m.start() != i) {
//                    sb.append(format, i, m.start());
//                }
//                String g = m.group();
//                switch (g.charAt(g.length()-1)){
//                    case 'n':{
//                        sb.append("\n");
//                        break;
//                    }
//                    case '%':{
//                        sb.append("%");
//                        break;
//                    }
//                    default:{
//                        //escape %
//                        Object arg = x < args.length ? args[x] : "MISSING_ARG_" + x;
//                        x++;
//                        sb.append(format0(locale, g, arg));
//                        break;
//                    }
//                }
//                i = m.end();
//            } else {
//                sb.append(format.substring(i));
//                break;
//            }
//        }
//        return sb.toString();
    }

    public static String format0(NutsSession session,Locale locale, String format, Object ...args) {
        if(session==null){
            throw new RuntimeException("missing session");
        }
        Object[] args2=Arrays.copyOf(args,args.length);
        for (int i = 0; i < args2.length; i++) {
            args2[i]= formatArgument(args2[i],session);
        }
        StringBuilder sb = new StringBuilder();
        new Formatter(sb, locale).format(format, args2);
        return sb.toString();
    }

//    public static String fillString(char x, int width) {
//        char[] cc = new char[width];
//        Arrays.fill(cc, x);
//        return new String(cc);
//    }

    public static boolean isEmpty(String string) {
        return string == null || string.trim().isEmpty();
    }
}
