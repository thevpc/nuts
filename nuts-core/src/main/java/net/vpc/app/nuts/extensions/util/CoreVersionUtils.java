package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreVersionUtils {
    public static boolean versionMatches(String version, String pattern) {
        if (pattern == null || StringUtils.isEmpty(pattern) || pattern.equals("LAST")) {
            return true;
        }
        return pattern.equals(version);
    }

    public static String incVersion(String oldVersion){
        if(StringUtils.isEmpty(oldVersion)){
            return "1";
        }
        int t = oldVersion.lastIndexOf('.');
        if(t>=0){
            String a=oldVersion.substring(0,t);
            String b=oldVersion.substring(t+1);
            if(StringUtils.isLong(b)){
                return a+"."+(Long.parseLong(b)+1L);
            }else {
                Matcher m = Pattern.compile("^(<C>.+)(?<N>[0-9]+)$").matcher(b);
                if (m.find()) {
                    return a+"."+m.group("C")+String.valueOf(Long.parseLong(m.group("N")) + 1);
                }
                m = Pattern.compile("^(?<N>[0-9]+)(?<C>.+)$").matcher(b);
                if (m.find()) {
                    return a+"."+m.group("C")+String.valueOf(Long.parseLong(m.group("N")) + 1);
                }
                m = Pattern.compile("^(?<C1>.+)(?<N>[0-9]+)(?<C2>.+)$").matcher(b);
                if (m.find()) {
                    return a+"."+m.group("C1")+String.valueOf(Long.parseLong(m.group("N")) + 1)+m.group("C2");
                }
                return oldVersion+"."+1;
            }
        }else{
            return oldVersion+"."+1;
        }
    }
}
