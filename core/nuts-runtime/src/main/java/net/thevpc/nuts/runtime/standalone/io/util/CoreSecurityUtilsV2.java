/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.runtime.standalone.xtra.digest.NDigestUtils;
import net.thevpc.nuts.security.NSecurityException;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NException;
import net.thevpc.nuts.util.NHex;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.util.NStringUtils;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreSecurityUtilsV2 {

//    public String cypherAlgo = "DES/CBC/PKCS5Padding";
//    public String digestAlgo = "SHA-256";

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_BITS = 128;
    private static final int IV_LENGTH = 12;  // 96 bits (NIST recommendation)
    private static final int SALT_LENGTH = 16;
    private static final int PBKDF2_ITERATIONS = 65536;

    public static final CoreSecurityUtilsV2 INSTANCE = new CoreSecurityUtilsV2();

    public CoreSecurityUtilsV2() {
        encryptChars(new char[]{'a' }, "a");
    }

    public char[] defaultDecryptChars(char[] data, String passphrase) {
        return decryptChars(data, passphrase);
    }

    public char[] defaultEncryptChars(char[] data, String passphrase) {
        return encryptChars(data, passphrase);
    }

    public char[] defaultHashChars(char[] data) {
        byte[] salt = new byte[SALT_LENGTH];
        new SecureRandom().nextBytes(salt);
        return BCrypt.hashpw(data, BCrypt.gensalt());
    }

    protected char[] encryptChars(char[] plaintext, String passphrase) {
        try {
            // 1. Generate fresh random salt + IV (critical for security)
            byte[] salt = new byte[SALT_LENGTH];
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(salt);
            new SecureRandom().nextBytes(iv);

            // 2. Derive strong key from passphrase + salt
            SecretKey key = deriveKey(passphrase.toCharArray(), salt);

            // 3. Encrypt with AES-GCM (authenticated encryption)
            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ciphertext = cipher.doFinal(NStringUtils.charsToUtf8Bytes(plaintext));

            // 4. Format: salt(16) + iv(12) + ciphertext
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(salt);
            out.write(iv);
            out.write(ciphertext);

            return Base64.getEncoder().encodeToString(out.toByteArray()).toCharArray();

        } catch (Exception e) {
            throw new NSecurityException(NMsg.ofC("AES-GCM encryption failed"), e);
        }
    }

    protected char[] decryptChars(char[] base64Data, String passphrase) {
        try {
            byte[] data = Base64.getDecoder().decode(NStringUtils.charsToUtf8Bytes(base64Data));
            if (data.length < SALT_LENGTH + IV_LENGTH + GCM_TAG_BITS / 8) {
                throw new NSecurityException(NMsg.ofC("Truncated ciphertext"));
            }

            // 1. Extract components
            byte[] salt = Arrays.copyOfRange(data, 0, SALT_LENGTH);
            byte[] iv = Arrays.copyOfRange(data, SALT_LENGTH, SALT_LENGTH + IV_LENGTH);
            byte[] ciphertext = Arrays.copyOfRange(data, SALT_LENGTH + IV_LENGTH, data.length);

            // 2. Derive key (same as encryption)
            SecretKey key = deriveKey(passphrase.toCharArray(), salt);

            // 3. Decrypt with authentication (throws on tampering)
            Cipher cipher = Cipher.getInstance(CIPHER);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            byte[] plaintext = cipher.doFinal(ciphertext); // ← AEADBadTagException on tampering

            return NStringUtils.utf8BytesToChars(plaintext);

        } catch (AEADBadTagException e) {
            throw new NSecurityException(NMsg.ofC("Ciphertext tampering detected!"), e);
        } catch (Exception e) {
            throw new NSecurityException(NMsg.ofC("AES-GCM decryption failed"), e);
        }
    }

    private SecretKey deriveKey(char[] passphrase, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        PBEKeySpec spec = new PBEKeySpec(passphrase, salt, PBKDF2_ITERATIONS, 256);
        try {
            SecretKey tmp = factory.generateSecret(spec);
            return new SecretKeySpec(tmp.getEncoded(), "AES");
        } finally {
            spec.clearPassword();
        }
    }


    public boolean verifyOneWay(char[] candidate, char[] storedHash) {
        return BCrypt.checkpw(candidate, storedHash);
    }
}
