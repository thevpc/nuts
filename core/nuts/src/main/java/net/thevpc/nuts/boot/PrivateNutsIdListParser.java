package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsOptional;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.util.*;
import java.util.stream.Collectors;

public class PrivateNutsIdListParser {
    public static String formatIdList(List<NutsId> s) {
        return s.stream().map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatIdArray(NutsId[] s) {
        return Arrays.stream(s).map(Object::toString).collect(Collectors.joining(","));
    }

    public static String formatStringIdList(List<String> s) {
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        if (s != null) {
            for (String s1 : s) {
                s1 = NutsUtilStrings.trim(s1);
                if (s1.length() > 0) {
                    allIds.add(s1);
                }
            }
        }
        return String.join(",", allIds);
    }

    public static String formatStringIdArray(String[] s) {
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        if (s != null) {
            for (String s1 : s) {
                s1 = NutsUtilStrings.trim(s1);
                if (s1.length() > 0) {
                    allIds.add(s1);
                }
            }
        }
        return String.join(",", allIds);
    }

    public static NutsOptional<List<String>> parseStringIdList(String s) {
        if (s == null) {
            return NutsOptional.of(Collections.emptyList());
        }
        LinkedHashSet<String> allIds = new LinkedHashSet<>();
        StringBuilder q = null;
        boolean inBrackets = false;
        for (char c : s.toCharArray()) {
            if (q == null) {
                q = new StringBuilder();
                if (c == '[' || c == ']') {
                    inBrackets = true;
                    q.append(c);
                } else if (c == ',' || Character.isWhitespace(c) || c == ';') {
                    //ignore
                } else {
                    q.append(c);
                }
            } else {
                if (c == ',' || Character.isWhitespace(c) || c == ';') {
                    if (inBrackets) {
                        q.append(c);
                    } else {
                        if (q.length() > 0) {
                            allIds.add(q.toString());
                        }
                        q = null;
                        inBrackets = false;
                    }
                } else if (c == '[' || c == ']') {
                    if (inBrackets) {
                        inBrackets = false;
                        q.append(c);
                    } else {
                        inBrackets = true;
                        q.append(c);
                    }
                } else {
                    q.append(c);
                }
            }
        }
        if (q != null) {
            if (q.length() > 0) {
                allIds.add(q.toString());
            }
        }
        return NutsOptional.of(new ArrayList<>(allIds));
    }

    public static NutsOptional<List<NutsId>> parseIdList(String s) {
        List<NutsId> list = new ArrayList<>();
        NutsOptional<List<String>> o = parseStringIdList(s);
        if(o.isPresent()) {
            for (String x : o.get()) {
                NutsOptional<NutsId> y = NutsId.of(x).nonBlank();
                if (y.isError()) {
                    return NutsOptional.ofError(y.getMessage());
                }
                if (y.isPresent()) {
                    list.add(y.get());
                }
            }
            return NutsOptional.of(list);
        }
        if (o.isError()) {
            return NutsOptional.ofError(o.getMessage());
        }
        return NutsOptional.ofEmpty(o.getMessage());
    }
}
