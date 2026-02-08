//package net.thevpc.nuts.runtime.standalone.security;
//
//import net.thevpc.nuts.security.NSecureString;
//
//import java.security.NoSuchAlgorithmException;
//import java.security.SecureRandom;
//
//public class NSecureGenerator {
//    private static final SecureRandom RANDOM;
//
//    static {
//        SecureRandom temp;
//        try {
//            // SHA1PRNG is generally the most portable/strongest on most JVMs,
//            // but letting the OS choose (NativePRNG) is often better.
//            temp = SecureRandom.getInstanceStrong();
//        } catch (NoSuchAlgorithmException e) {
//            // Fallback to default if "Strong" isn't available
//            temp = new SecureRandom();
//        }
//        RANDOM = temp;
//    }
//
//    /**
//     * Generates a random byte array salt.
//     * 16 bytes (128 bits) is the standard for most salts.
//     */
//    public static byte[] generateByteSalt(int length) {
//        byte[] salt = new byte[length];
//        RANDOM.nextBytes(salt);
//        return salt;
//    }
//
//    /**
//     * Generates a random alphanumeric salt as an NSecureString.
//     */
//    public static NSecureString generateSecureStringSalt(int length) {
//        char[] alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
//        char[] salt = new char[length];
//        for (int i = 0; i < length; i++) {
//            salt[i] = alphabet[RANDOM.nextInt(alphabet.length)];
//        }
//        // Use your secure factory that wipes the input 'salt' array
//        return NSecureString.ofSecure(salt);
//    }
//}
