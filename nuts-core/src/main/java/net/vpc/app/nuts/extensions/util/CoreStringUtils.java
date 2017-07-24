package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.util.StringUtils;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreStringUtils {
    //    public static void main(String[] args) {
//        try {
//            String encrypted = SecurityUtils.httpEncrypt("hello hello hello hello hello hello hello hello".getBytes(), "mypwd");
//            System.out.println(encrypted);
//            byte[] decrypted = SecurityUtils.httpDecrypt(encrypted, "mypwd");
//            System.out.println(new String(decrypted ));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public static String[] concat(String[] array1, String[] array2) {
        String[] r = new String[array1.length + array2.length];
        System.arraycopy(array1, 0, r, 0, array1.length);
        System.arraycopy(array2, 0, r, array1.length, array2.length);
        return r;
    }

    public static String[] removeFirst(String[] array) {
        String[] r = new String[array.length - 1];
        System.arraycopy(array, 1, r, 0, r.length);
        return r;
    }

    public static int parseInt(String v1, int defaultValue) {
        try {
            if (StringUtils.isEmpty(v1)) {
                return defaultValue;
            }
            return Integer.parseInt(StringUtils.trim(v1));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String checkNotEmpty(String str, String name) {
        str = StringUtils.trim(str);
        if (StringUtils.isEmpty(str)) {
            throw new RuntimeException("Empty string not allowed for " + name);
        }
        return str.trim();
    }
}
