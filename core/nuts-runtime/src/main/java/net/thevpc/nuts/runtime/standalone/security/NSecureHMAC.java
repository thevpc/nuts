//package net.thevpc.nuts.runtime.standalone.security;
//
//import javax.crypto.Mac;
//import javax.crypto.spec.SecretKeySpec;
//import java.nio.ByteBuffer;
//import java.nio.CharBuffer;
//import java.nio.charset.StandardCharsets;
//import java.security.GeneralSecurityException;
//import java.util.Arrays;
//
//public class NSecureHMAC {
//    public static byte[] calculate(NSecureString secret, byte[] data) {
//        // We use the Loan Pattern to keep the key safe
//        return secret.readContent(keyChars -> {
//            try {
//                // HMAC-SHA256 is the modern industry standard
//                Mac mac = Mac.getInstance("HmacSHA256");
//
//                // We must convert char[] to byte[] carefully.
//                // Using UTF-8 is standard, but we must wipe the byte array after!
//                byte[] keyBytes = charsToBytes(keyChars);
//                try {
//                    SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
//                    mac.init(keySpec);
//                    return mac.doFinal(data);
//                } finally {
//                    Arrays.fill(keyBytes, (byte) 0); // Wipe the transient bytes
//                }
//            } catch (GeneralSecurityException e) {
//                throw new RuntimeException("HMAC Calculation Failed", e);
//            }
//        });
//    }
//    public static byte[] calculate(char[] keyChars, byte[] data) {
//        try {
//            // HMAC-SHA256 is the modern industry standard
//            Mac mac = Mac.getInstance("HmacSHA256");
//
//            // We must convert char[] to byte[] carefully.
//            // Using UTF-8 is standard, but we must wipe the byte array after!
//            byte[] keyBytes = charsToBytes(keyChars);
//            try {
//                SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "HmacSHA256");
//                mac.init(keySpec);
//                return mac.doFinal(data);
//            } finally {
//                Arrays.fill(keyBytes, (byte) 0); // Wipe the transient bytes
//            }
//        } catch (GeneralSecurityException e) {
//            throw new RuntimeException("HMAC Calculation Failed", e);
//        }
//    }
//
//    private static byte[] charsToBytes(char[] chars) {
//        // Manual conversion to avoid String allocation
//        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(CharBuffer.wrap(chars));
//        byte[] bytes = new byte[byteBuffer.remaining()];
//        byteBuffer.get(bytes);
//        return bytes;
//    }
//}
