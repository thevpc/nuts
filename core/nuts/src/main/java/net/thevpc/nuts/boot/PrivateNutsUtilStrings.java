package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.util.*;
import java.util.stream.Collectors;

public class PrivateNutsUtilStrings {
    public static List<String> split(String value, String chars, boolean trim, boolean ignoreEmpty) {
        if (value == null) {
            value = "";
        }
        StringTokenizer st = new StringTokenizer(value, chars,true);
        List<String> all = new ArrayList<>();
        boolean wasSep=true;
        while (st.hasMoreElements()) {
            String s = st.nextToken();
            if(chars.indexOf(s.charAt(0))>=0){
                if(wasSep){
                    s="";
                    if (!ignoreEmpty) {
                        all.add(s);
                    }
                }
                wasSep=true;
            }else {
                wasSep=false;
                if (trim) {
                    s = s.trim();
                }
                if (!ignoreEmpty || !s.isEmpty()) {
                    all.add(s);
                }
            }
        }
        if(wasSep){
            if (!ignoreEmpty) {
                all.add("");
            }
        }
        return all;
    }

    public static List<String> splitDefault(String str) {
        return split(str, " ;,\n\r\t|",true,true);
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


    public static Integer parseInt(String value, Integer emptyValue, Integer errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        value = value.trim();
        try {
            return Integer.parseInt(value);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    public static Integer parseInt16(String value, Integer emptyValue, Integer errorValue) {
        if (NutsBlankable.isBlank(value)) {
            return emptyValue;
        }
        value = value.trim();
        try {
            return Integer.parseInt(value, 16);
        } catch (Exception ex) {
            return errorValue;
        }
    }

    public static NutsOptional<Integer> parseFileSizeInBytes(String value, Integer defaultMultiplier) {
        if (NutsBlankable.isBlank(value)) {
            return NutsOptional.ofEmpty(session -> NutsMessage.cstyle("empty size"));
        }
        value = value.trim();
        Integer i = parseInt(value, null, null);
        if (i != null) {
            if (defaultMultiplier != null) {
                return NutsOptional.of(i * defaultMultiplier);
            } else {
                return NutsOptional.of(i);
            }
        }
        for (String s : new String[]{"kb", "mb", "gb", "k", "m", "g"}) {
            if (value.toLowerCase().endsWith(s)) {
                String v = value.substring(0, value.length() - s.length()).trim();
                i = parseInt(v, null, null);
                if (i != null) {
                    switch (s) {
                        case "k":
                        case "kb":
                            return NutsOptional.of(i * 1024);
                        case "m":
                        case "mb":
                            return NutsOptional.of(i * 1024 * 1024);
                        case "g":
                        case "gb":
                            return NutsOptional.of(i * 1024 * 1024 * 1024);
                    }
                }
            }
        }
        String finalValue = value;
        return NutsOptional.ofError(session->NutsMessage.cstyle("invalid size :%s", finalValue));
    }

    public static int firstIndexOf(String string, char[] chars) {
        char[] value = string.toCharArray();
        for (int i = 0; i < value.length; i++) {
            for (char aChar : chars) {
                if (value[i] == aChar) {
                    return i;
                }
            }
        }
        return -1;
    }
}
