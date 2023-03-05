package net.thevpc.nuts.toolbox.nsh.util.bundles;

public class _StringUtils {
    public static String formatLeft(Object number, int size) {
        String s = String.valueOf(number);
        int len = s.length();
        int bufferSize = Math.max(size, len);
        StringBuilder sb = new StringBuilder(bufferSize);
        sb.append(s);
        for (int i = bufferSize-len; i >0 ; i--) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String formatRight(Object number, int size) {
        String s = String.valueOf(number);
        int len = s.length();
        int bufferSize = Math.max(size, len);
        StringBuilder sb = new StringBuilder(bufferSize);
        for (int i = bufferSize - len; i > 0; i--) {
            sb.append(' ');
        }
        sb.append(s);
        return sb.toString();
    }

    public static String exceptionToString(Throwable ex) {
        for (Class aClass : new Class[]{
                NullPointerException.class,
                ArrayIndexOutOfBoundsException.class,
                ClassCastException.class,
                UnsupportedOperationException.class,
                ReflectiveOperationException.class,}) {
            if (aClass.isInstance(ex)) {
                return ex.toString();
            }
        }
        String message = ex.getMessage();
        if (message == null) {
            message = ex.toString();
        }
        return message;
    }

}
