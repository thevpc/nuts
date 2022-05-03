/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.xtra.digest.NutsDigestUtils;
import net.thevpc.nuts.util.NutsStringUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreSecurityUtils {

    public static final String ENV_KEY_PASSPHRASE = "passphrase";
    public static final String DEFAULT_PASSPHRASE = NutsStringUtils.toHexString("It's completely nuts!!".getBytes());

    public static char[] defaultDecryptChars(char[] data, String passphrase,NutsSession session) {
        return decryptString(new String(data), passphrase,session).toCharArray();
//        return CoreIOUtils.bytesToChars(CoreSecurityUtils.httpDecrypt(CoreIOUtils.charsToBytes(data), passphrase));
    }

    public static char[] defaultEncryptChars(char[] data, String passphrase,NutsSession session) {
        return encryptString(new String(data), passphrase,session).toCharArray();
//        byte[] bytes = httpEncrypt(CoreIOUtils.charsToBytes(data), passphrase);
//        return CoreIOUtils.bytesToChars(bytes);
    }

    public static char[] defaultHashChars(char[] data, String passphrase, NutsSession session) {
        return defaultEncryptChars(NutsDigestUtils.evalSHA1(data,session), passphrase,session);
    }

//    public static byte[] httpDecrypt(byte[] data, String passphrase) {
//        try {
//            byte[] key = CoreIOUtils.evalMD5(passphrase);
//            Cipher c = Cipher.getInstance("AES");
//            SecretKeySpec k = new SecretKeySpec(key, "AES");
//            c.init(Cipher.DECRYPT_MODE, k);
//            byte[] decoded = Base64.getDecoder().decode(data);
//
//            return c.doFinal(decoded);
//        } catch (GeneralSecurityException e) {
//            throw NutsIOException(session,e);
//        }
//    }
//
//    public static byte[] httpEncrypt(byte[] data, String passphrase) {
//        try {
//            byte[] key = CoreIOUtils.evalMD5(passphrase);
//            Cipher c = null;
//
//            c = Cipher.getInstance("AES");
//            SecretKeySpec k = new SecretKeySpec(key, "AES");
//            c.init(Cipher.ENCRYPT_MODE, k);
//            byte[] encryptedData = c.doFinal(data);
//            return (Base64.getEncoder().encode(encryptedData));
//        } catch (GeneralSecurityException e) {
//            throw new NutsIOException(session,e);
//        }
//    }

    private static String encryptString(String strToEncrypt, String secret,NutsSession session) {
        try {
            //strToEncrypt must be multiple of 16 (bug in jdk11)
            byte[] bytes = strToEncrypt.getBytes(StandardCharsets.UTF_8);
            int v=bytes.length;
            ByteArrayOutputStream out=new ByteArrayOutputStream();
            out.write((v >>> 24) & 0xFF);
            out.write((v >>> 16) & 0xFF);
            out.write((v >>>  8) & 0xFF);
            out.write((v >>>  0) & 0xFF);
            out.write(bytes);
            int s=v+4;
            while(s%16!=0){
                out.write(0);
                s++;
            }
            bytes=out.toByteArray();

            KeyInfo k = createKeyInfo(secret,session);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, k.secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(bytes));
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("encryption failed"),ex);
        }
    }

    private static String decryptString(String strToDecrypt, String secret,NutsSession session) {
        try {
            KeyInfo k = createKeyInfo(secret,session);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, k.secretKey);
            byte[] bytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));

            //bytes is padded to be multiple of 16 (bug in jdk11)
            int ch1 = bytes[0] & 0xff;
            int ch2 = bytes[1] & 0xff;
            int ch3 = bytes[2] & 0xff;
            int ch4 = bytes[3] & 0xff;
            if ((ch1 | ch2 | ch3 | ch4) < 0)
                throw new EOFException();
            int v= ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
            bytes=Arrays.copyOfRange(bytes,4,4+v);
            return new String(bytes);
        } catch (NutsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("decryption failed"),ex);
        }
    }

    private static class KeyInfo {

        SecretKeySpec secretKey;
        byte[] key;
    }

    private static KeyInfo createKeyInfo(String password,NutsSession session) {
        if (password == null || password.length() == 0) {
            password = "password";
        }
        MessageDigest sha = null;
        KeyInfo k = new KeyInfo();
        try {
            k.key = password.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-256");
            k.key = sha.digest(k.key);
            k.secretKey = new SecretKeySpec(k.key, "AES");
        } catch (NoSuchAlgorithmException ex) {
            throw new NutsIllegalArgumentException(session, NutsMessage.ofPlain("encryption key building failed"),ex);
        }
        return k;
    }
}
