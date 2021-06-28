/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * @author thevpc
 */
public class DiffUtils {
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static byte[] allBytes(InputStream stream) {
        ByteArrayOutputStream o=new ByteArrayOutputStream();
        byte[] b=new byte[8092];
        int len;
        while(true){
            try {
                if (!((len=stream.read(b))>0)) break;
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            o.write(b,0,len);
        }
        return o.toByteArray();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String hashStream(InputStream input) {
        try {
            byte[] buffer = new byte[8192];
            MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
            int len = input.read(buffer);

            while (len != -1) {
                sha1.update(buffer, 0, len);
                len = input.read(buffer);
            }

            return bytesToHex(sha1.digest());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        } catch (NoSuchAlgorithmException ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Manifest prepareManifest(Manifest m) {
        m.getMainAttributes().remove(new Attributes.Name("Created-By"));
        m.getMainAttributes().remove(new Attributes.Name("Built-By"));
        m.getMainAttributes().remove(new Attributes.Name("Ant-Version"));
        m.getMainAttributes().remove(new Attributes.Name("Archiver-Version"));
        String jdk = m.getMainAttributes().getValue("Build-Jdk");
        if (jdk != null) {
            String[] a = jdk.split("[.]");
            if (a.length >= 2) {
                jdk = a[0] + "." + a[1];
                m.getMainAttributes().put(new Attributes.Name("Build-Jdk"), jdk);//uniform jdk value
            }
        }
        return m;
    }

    public static boolean isFileName(String name, String path) {
        return path.equals(name) || path.endsWith("/" + name);
    }

    public static boolean isFileNameIgnoreCase(String name, String path) {
        return path.equalsIgnoreCase(name) || path.toLowerCase().endsWith("/" + name.toLowerCase());
    }
}
