package net.thevpc.nuts.runtime.standalone.xtra.digest;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NHex;
import net.thevpc.nuts.util.NIOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class NDigestUtils {
    public static byte[] evalMD5(String input) {
        byte[] bytesOfMessage = input.getBytes(StandardCharsets.UTF_8);
        return evalMD5(bytesOfMessage);
    }

    public static String evalMD5Hex(Path path) {
        return NHex.fromBytes(evalMD5(path));
    }

    public static byte[] evalMD5(Path path) {
        try (java.io.InputStream is = new BufferedInputStream(Files.newInputStream(path))) {
            return evalMD5(is);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public static String evalMD5Hex(InputStream input) {
        return NHex.fromBytes(evalMD5(input));
    }

    public static byte[] evalHash(InputStream input, String algo) {

        try {
            MessageDigest md;
            md = MessageDigest.getInstance(algo);
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = input.read(buffer);
                while (len != -1) {
                    md.update(buffer, 0, len);
                    len = input.read(buffer);
                }
            } catch (IOException e) {
                throw new NIOException(e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(new IOException(e));
        }
    }

    public static byte[] evalMD5(InputStream input) {

        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int len = 0;
            try {
                len = input.read(buffer);
                while (len != -1) {
                    md.update(buffer, 0, len);
                    len = input.read(buffer);
                }
            } catch (IOException e) {
                throw new NIOException(e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(e);
        }
    }

    public static byte[] evalMD5(byte[] bytesOfMessage) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(e);
        }
    }

    public static String evalSHA1Hex(NPath file) {
        try {
            try (InputStream is = file.getInputStream()) {
                return evalSHA1Hex(is, true);
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
    }

    public static String evalSHA1(File file) {
        try {
            return evalSHA1Hex(new FileInputStream(file), true);
        } catch (FileNotFoundException e) {
            throw new NIOException(e);
        }
    }

    public static char[] evalSHA1(char[] input) {
        byte[] bytes = NIOUtils.charsToBytes(input);
        char[] r = evalSHA1HexChars(new ByteArrayInputStream(bytes), true);
        Arrays.fill(bytes, (byte) 0);
        return r;
    }

    public static String evalSHA1(String input) {
        return evalSHA1Hex(new ByteArrayInputStream(input.getBytes()), true);
    }

    public static String evalSHA1Hex(InputStream input, boolean closeStream) {
        return NHex.fromBytes(evalSHA1(input, closeStream));
    }

    public static char[] evalSHA1HexChars(InputStream input, boolean closeStream) {
        return NHex.fromBytes(evalSHA1(input, closeStream)).toCharArray();
    }

    public static byte[] evalSHA1(InputStream input, boolean closeStream) {
        try {
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new NIOException(ex);
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
                throw new NIOException(e);
            }
            return sha1.digest();
        } finally {
            if (closeStream) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ex) {
                        throw new NIOException(ex);
                    }
                }
            }
        }
    }
}
