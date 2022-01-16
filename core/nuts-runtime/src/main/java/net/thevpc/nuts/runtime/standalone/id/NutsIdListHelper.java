package net.thevpc.nuts.runtime.standalone.id;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsUtilStrings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

public class NutsIdListHelper {
    public static String formatIdList(NutsId[] s, NutsSession session) {
        return String.join(",");
    }

    public static String formatIdList(String[] s, NutsSession session) {
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

    public static String[] parseIdListStrings(String s, NutsSession session) {
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
        return allIds.toArray(new String[0]);
    }

    public static NutsId[] parseIdList(String s, NutsSession session) {
        return Arrays.stream(parseIdListStrings(s, session)).map(x -> NutsId.of(x, session)).toArray(NutsId[]::new);
    }
}
