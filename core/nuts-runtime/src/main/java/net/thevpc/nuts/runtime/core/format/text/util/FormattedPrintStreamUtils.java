//package net.thevpc.nuts.runtime.core.format.text.util;
//
//import java.text.MessageFormat;
//import java.time.temporal.Temporal;
//import java.util.Arrays;
//import java.util.Date;
//import java.util.Formatter;
//import java.util.Locale;
//import java.util.regex.Pattern;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
//import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
//
//public class FormattedPrintStreamUtils {
//
//    // %[argument_index$][flags][width][.precision][t]conversion
//    private static final Pattern PRINTF_PATTERN = Pattern.compile("%(\\d+\\$)?([-#+ 0,(<]*)?(\\d+)?(\\.\\d+)?([tT])?([a-zA-Z%])");
//
//    /**
//     * transform plain text to formatted text so that the result is rendered as
//     * is
//     *
//     * @param text text
//     * @return escaped text
//     */
//    public static String escapeText(String text) {
//        return DefaultNutsTextNodeParser.escapeText0(text);
//    }
//
////    static int countRepeatable(char c,char[] cc,int offset){
////        int count=0;
////        while((offset+count)<cc.length && cc[offset+count]==c){
////            count++;
////        }
////        return count;
////    }
//
//    public static boolean isSpecialFormattedObject(Object a) {
//        return (a instanceof NutsFormattable) || (a instanceof NutsString)  || (a instanceof NutsTextNode) ;
//    }
//
//    public static String formatCStyle(NutsSession session,Locale locale, NutsTextFormatStyle style,String format, Object... args) {
//        if(session==null){
//            throw new RuntimeException("missing session");
//        }
//        if(style==null){
//            style=NutsTextFormatStyle.POSITIONAL;
//        }
//        Object[] args2=new Object[args.length];
//        NutsTextFormatManager text = session.getWorkspace().formats().text();
//        for (int i = 0; i < args2.length; i++) {
//            Object a=args[i];
//            if(a instanceof Number || a instanceof Date  || a instanceof Temporal) {
//                //do nothing
//                args2[i]=a;
//            }else {
//                args2[i]= text.toString(a,session).toString();
//            }
//        }
//        switch (style){
//            case CSTYLE:{
//                StringBuilder sb = new StringBuilder();
//                new Formatter(sb, locale).format(format, args2);
//                return sb.toString();
//            }
//            case POSITIONAL:{
//                return MessageFormat.format(format, args2);
//            }
//        }
//        throw new NutsUnsupportedEnumException(session.getWorkspace(),style);
//    }
//
//}
