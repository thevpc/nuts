package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.util.SecurityUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Base64;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreSecurityUtils {
    public static byte[] httpDecrypt(String data, String passphrase) throws IOException {
        try {
            byte[] key = SecurityUtils.evalMD5(passphrase);
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] decoded = Base64.getDecoder().decode(data);

            return c.doFinal(decoded);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }
}
