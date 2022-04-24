package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsUtilStrings;

import java.util.*;
import java.util.stream.Collectors;

public class PrivateNutsUtilStrings {


    public static List<String> split(String str, String separators, boolean trim) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if (trim) {
                s = s.trim();
            }
            result.add(s);
        }
        return result;
    }

    public static List<String> splitDefault(String str) {
        return split(str, " ;,\n\r\t|");
    }

    public static List<String> splitSpace(String str) {
        return split(str, " ");
    }

    public static List<String> splitColon(String str) {
        return split(str, " ");
    }

    public static List<String> splitNewLine(String str) {
        return split(str, "\r\n");
    }

    public static List<String> split(String str, String separators) {
        if (str == null) {
            return Collections.EMPTY_LIST;
        }
        StringTokenizer st = new StringTokenizer(str, separators);
        List<String> result = new ArrayList<>();
        while (st.hasMoreElements()) {
            result.add(st.nextToken());
        }
        return result;
    }

    public static List<String> splitSemiColon(String str) {
        return split(str, ";");
    }

    public static List<String> splitFileSlash(String str) {
        return split(str, "/\\");
    }

    public static List<String> parseAndTrimToDistinctList(String s) {
        if (s == null) {
            return new ArrayList<>();
        }
        return splitDefault(s).stream().map(String::trim)
                .filter(x -> x.length() > 0)
                .distinct().collect(Collectors.toList());
    }

    public static String joinAndTrimToNull(List<String> args){
        return NutsUtilStrings.trimToNull(
                String.join(",",args)
        );
    }


}
