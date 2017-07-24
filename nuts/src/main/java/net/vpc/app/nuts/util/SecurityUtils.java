/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Created by vpc on 1/22/17.
 */
public class SecurityUtils {

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String httpEncrypt(byte[] data, String passphrase) throws IOException {
        byte[] key = evalMD5(passphrase);
        Cipher c = null;
        try {
            c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.ENCRYPT_MODE, k);
            byte[] encryptedData = c.doFinal(data);
            return new String(Base64.getEncoder().encode(encryptedData));
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public static byte[] httpDecrypt(String data, String passphrase) throws IOException {
        try {
            byte[] key = evalMD5(passphrase);
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] decoded = Base64.getDecoder().decode(data);

            return c.doFinal(decoded);
        } catch (GeneralSecurityException e) {
            throw new IOException(e);
        }
    }

    public static String evalSHA1(File file) throws IOException {
        InputStream input = null;
        try {
            input = new FileInputStream(file);
            return evalSHA1(input);
        } finally {
            if (input != null) {
                input.close();
            }
        }
    }

    public static String evalSHA1(String input) throws IOException {
        return evalSHA1(new ByteArrayInputStream(input.getBytes()));
    }

    public static byte[] evalMD5(String input) throws IOException {
        byte[] bytesOfMessage = input.getBytes("UTF-8");

        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            throw new IOException(e);
        }
    }

    public static String evalSHA1(InputStream input) throws IOException {

        MessageDigest sha1 = null;

        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new IOException(ex);
        }

        byte[] buffer = new byte[8192];
        int len = input.read(buffer);

        while (len != -1) {
            sha1.update(buffer, 0, len);
            len = input.read(buffer);
        }

        return new HexBinaryAdapter().marshal(sha1.digest());

    }
}
