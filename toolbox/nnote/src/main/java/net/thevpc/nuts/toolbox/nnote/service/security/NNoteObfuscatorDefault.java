/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.service.security;

import net.thevpc.nuts.toolbox.nnote.model.CypherInfo;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import net.thevpc.common.swing.util.CancelException;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.toolbox.nnote.model.NNote;

/**
 *
 * @author vpc
 */
public class NNoteObfuscatorDefault implements NNoteObfuscator {
    public static final String ID="v1.0.0";
    private NutsApplicationContext context;

    public NNoteObfuscatorDefault(NutsApplicationContext context) {
        this.context = context;
    }

    @Override
    public CypherInfo encrypt(NNote a, PasswordHandler handler) {
        String password = handler.askForPassword(null);
        if (password == null || password.length() == 0) {
            throw new CancelException();
        }
        String s = context.getWorkspace().formats().element()
                .setContentType(NutsContentType.JSON)
                .setValue(a)
                .setCompact(true)
                .setSession(context.getSession())
                .format();
        return new CypherInfo(ID,
                encryptString(s, password)
        );
    }

    @Override
    public NNote decrypt(CypherInfo cypherInfo, NNote original, PasswordHandler handler) {
        if (!ID.equals(cypherInfo.getAlgo())) {
            throw new IllegalArgumentException("unsupported algo: " + cypherInfo.getAlgo());
        }
        String password = handler.askForPassword(null);
        if (password == null || password.length() == 0) {
            throw new CancelException();
        }
        String s = null;
        try {
            s = decryptString(cypherInfo.getValue(), password);
        } catch (Exception ex) {
            throw new InvalidSecretException();
        }
        return context.getWorkspace().formats().element()
                .setContentType(NutsContentType.JSON)
                .setValue(password)
                .setCompact(true)
                .setSession(context.getSession())
                .parse(s, NNote.class);
    }

    private class KeyInfo {

        SecretKeySpec secretKey;
        byte[] key;
    }

    private KeyInfo createKeyInfo(String password) {
        if (password == null || password.length() == 0) {
            password = "password";
        }
        MessageDigest sha = null;
        KeyInfo k = new KeyInfo();
        try {
            k.key = password.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            k.key = sha.digest(k.key);
            k.key = Arrays.copyOf(k.key, 16);
            k.secretKey = new SecretKeySpec(k.key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
        return k;
    }

    private String encryptString(String strToEncrypt, String secret) {
        try {
            KeyInfo k = createKeyInfo(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, k.secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String decryptString(String strToDecrypt, String secret) {
        try {
            KeyInfo k = createKeyInfo(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, k.secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
