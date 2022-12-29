package net.thevpc.nuts.runtime.standalone.xtra.digest;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class NDigestUtils {
    public static byte[] evalMD5(String input, NSession session) {
        byte[] bytesOfMessage = input.getBytes(StandardCharsets.UTF_8);
        return evalMD5(bytesOfMessage, session);
    }

    public static String evalMD5Hex(Path path, NSession session) {
        return NStringUtils.toHexString(evalMD5(path, session));
    }

    public static byte[] evalMD5(Path path, NSession session) {
        try (java.io.InputStream is = new BufferedInputStream(Files.newInputStream(path))) {
            return evalMD5(is, session);
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }

    public static String evalMD5Hex(java.io.InputStream input, NSession session) {
        return NStringUtils.toHexString(evalMD5(input, session));
    }

    public static byte[] evalHash(java.io.InputStream input, String algo, NSession session) {

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
                throw new NIOException(session, e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(session, new IOException(e));
        }
    }

    public static byte[] evalMD5(java.io.InputStream input, NSession session) {

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
                throw new NIOException(session, e);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(session, e);
        }
    }

    public static byte[] evalMD5(byte[] bytesOfMessage, NSession session) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            return md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            throw new NIOException(session, e);
        }
    }

    public static String evalSHA1Hex(NPath file, NSession session) {
        try {
            try (InputStream is = file.getInputStream()) {
                return evalSHA1Hex(is, true, session);
            }
        } catch (IOException e) {
            throw new NIOException(session, e);
        }
    }

    public static String evalSHA1(File file, NSession session) {
        try {
            return evalSHA1Hex(new FileInputStream(file), true, session);
        } catch (FileNotFoundException e) {
            throw new NIOException(session, e);
        }
    }

    public static char[] evalSHA1(char[] input, NSession session) {
        byte[] bytes = CoreIOUtils.charsToBytes(input);
        char[] r = evalSHA1HexChars(new ByteArrayInputStream(bytes), true, session);
        Arrays.fill(bytes, (byte) 0);
        return r;
    }

    public static String evalSHA1(String input, NSession session) {
        return evalSHA1Hex(new ByteArrayInputStream(input.getBytes()), true, session);
    }

    public static String evalSHA1Hex(InputStream input, boolean closeStream, NSession session) {
        return NStringUtils.toHexString(evalSHA1(input, closeStream, session));
    }

    public static char[] evalSHA1HexChars(InputStream input, boolean closeStream, NSession session) {
        return NStringUtils.toHexString(evalSHA1(input, closeStream, session)).toCharArray();
    }

    public static byte[] evalSHA1(InputStream input, boolean closeStream, NSession session) {
        try {
            MessageDigest sha1 = null;
            try {
                sha1 = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new NIOException(session, ex);
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
                throw new NIOException(session, e);
            }
            return sha1.digest();
        } finally {
            if (closeStream) {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ex) {
                        throw new NIOException(session, ex);
                    }
                }
            }
        }
    }
}
