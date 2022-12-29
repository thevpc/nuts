package net.thevpc.nuts.toolbox.nutsserver.util;

import net.thevpc.nuts.NId;

import java.util.*;

public class NServerUtils {
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

    public static String toMvnScope(String s) {
        if (s == null) {
            s = "";
        }
        switch (s) {
            case "":
            case "api":
            case "implementation":
            case "compile": {
                return "compile";
            }
            case "testCompile":
            case "testProvided":
            case "testRuntime": {
                return "test";
            }
            default: {
                return s;
            }
        }
    }

    public static String[] extractFirstToken(String requestURI) {
        int s1 = requestURI.indexOf('/');
        String firstToken = "";
        String theRest = "";
        if (s1 == 0) {
            s1 = requestURI.indexOf('/', 1);
            if (s1 < 0) {
                firstToken = requestURI.substring(1);
                theRest = "";
            } else {
                firstToken = requestURI.substring(1, s1);
                theRest = requestURI.substring(s1);
            }
        } else if (s1 < 0) {
            firstToken = requestURI;
            theRest = "";
        } else {
            firstToken = requestURI.substring(0, s1);
            theRest = requestURI.substring(s1);
        }
        return new String[]{firstToken, theRest};
    }

    public static String iteratorNutsIdToString(Iterator<NId> it) {
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) {
            NId next = it.next();
            //System.out.println(next.getId().toString());
            sb.append(next.toString());
            sb.append("\n");
        }
        return sb.toString();
    }
}
