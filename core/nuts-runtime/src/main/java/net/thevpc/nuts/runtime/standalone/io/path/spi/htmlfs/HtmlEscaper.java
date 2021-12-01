package net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlEscaper {
    public static String escape(String any){
        Matcher m = Pattern.compile("&[a-z]+;").matcher(any);
        StringBuffer sb=new StringBuffer();
        while(m.find()){
            switch (m.group()){
                case "&nbsp;":{
                    m.appendReplacement(sb," ");
                    break;
                }
                default:{
                    m.appendReplacement(sb,m.group());
                }
            }
        }
        return sb.toString();
    }
}
