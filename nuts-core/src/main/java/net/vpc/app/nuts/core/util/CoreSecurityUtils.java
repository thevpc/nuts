/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.util;


import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * Created by vpc on 5/16/17.
 */
public class CoreSecurityUtils {

    private final static char[] HEX_ARR = "0123456789ABCDEF".toCharArray();

    public static byte[] httpDecrypt(byte[] data, String passphrase) {
        try {
            byte[] key = evalMD5(passphrase);
            Cipher c = Cipher.getInstance("AES");
            SecretKeySpec k = new SecretKeySpec(key, "AES");
            c.init(Cipher.DECRYPT_MODE, k);
            byte[] decoded = Base64.getDecoder().decode(data);

            return c.doFinal(decoded);
        } catch (GeneralSecurityException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARR[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARR[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] httpEncrypt(byte[] data, String passphrase) {
        try {
            byte[] key = evalMD5(passphrase);
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

    public static String evalSHA1(File file) {
        try {
            return evalSHA1(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static String evalSHA1(String input) {
        return evalSHA1(new ByteArrayInputStream(input.getBytes()), true);
    }

    public static byte[] evalMD5(String input) {
        try {
            byte[] bytesOfMessage = input.getBytes("UTF-8");
            return evalMD5(bytesOfMessage);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static byte[] evalMD5(byte[] bytesOfMessage) {
        try {

            MessageDigest md;

            md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            throw new UncheckedIOException(new IOException(e));
        }
    }

    public static String evalSHA1(InputStream input, boolean closeStream) {

        MessageDigest sha1 = null;

        try {
            sha1 = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException ex) {
            throw new UncheckedIOException(new IOException(ex));
        }

        byte[] buffer = new byte[8192];
        int len = 0;
        try {
            len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return toHexString(sha1.digest());

    }

    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            sb.append(toHex(aByte >> 4));
            sb.append(toHex(aByte));
        }

        return sb.toString();
    }

    private static char toHex(int nibble) {
        return HEX_ARR[nibble & 0xF];
    }
}
