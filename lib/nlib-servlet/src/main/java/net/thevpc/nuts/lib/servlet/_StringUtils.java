package net.thevpc.nuts.lib.servlet;

class _StringUtils {


    public static boolean isBlank(String line) {
        return line==null || line.trim().length()==0;
    }

    public static String trim(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
