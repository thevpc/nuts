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
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.util.io;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreSecurityUtils {

    public static final String ENV_KEY_PASSPHRASE = "passphrase";
    public static final String DEFAULT_PASSPHRASE = CoreIOUtils.bytesToHex("It's completely nuts!!".getBytes());

    public static char[] defaultDecryptChars(char[] data, String passphrase) {
        return CoreIOUtils.bytesToChars(CoreSecurityUtils.httpDecrypt(CoreIOUtils.charsToBytes(data), passphrase));
    }

    public static char[] defaultEncryptChars(char[] data, String passphrase) {
        byte[] bytes = httpEncrypt(CoreIOUtils.charsToBytes(data), passphrase);
        return CoreIOUtils.bytesToChars(bytes);
    }

    public static char[] defaultHashChars(char[] data, String passphrase) {
        return defaultEncryptChars(CoreIOUtils.evalSHA1(data), passphrase);
    }

    public static byte[] httpDecrypt(byte[] data, String passphrase) {
        try {
            byte[] key = CoreIOUtils.evalMD5(passphrase);
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] decoded = Base64.getDecoder().decode(data);

            return c.doFinal(decoded);
        } catch (GeneralSecurityException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static byte[] httpEncrypt(byte[] data, String passphrase) {
        try {
            byte[] key = CoreIOUtils.evalMD5(passphrase);
            Cipher c = null;

            c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.ENCRYPT_MODE, k);
            byte[] encryptedData = c.doFinal(data);
            return (Base64.getEncoder().encode(encryptedData));
        } catch (GeneralSecurityException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

}
