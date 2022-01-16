package net.thevpc.nuts.runtime.standalone.id;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NutsIdListHelper {
    public static String formatIdList(NutsId[] s, NutsSession session) {
        return String.join(",");
    }

    public static String formatIdList(String[] s, NutsSession session) {
        return String.join(",");
    }

    public static String[] parseIdListStrings(String s, NutsSession session) {
        List<String> boots = new ArrayList<>();
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
                        boots.add(q.toString());
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
            boots.add(q.toString());
        }
        return boots.toArray(new String[0]);
    }

    public static NutsId[] parseIdList(String s, NutsSession session) {
        return Arrays.stream(parseIdListStrings(s, session)).map(x -> NutsId.of(x, session)).toArray(NutsId[]::new);
    }
}
