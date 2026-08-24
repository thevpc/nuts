package net.thevpc.nuts.net;

import net.thevpc.nuts.util.NStringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * NHttpUrlEncoder class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NHttpUrlEncoder {
    /**
     * Encode object.
     *
     * @param any any
     * @return encode object result
     */
    public static String encodeObject(Object any) {
        if (any == null) {
            return "";
        }
        if (any instanceof String) {
            /**
             * Encode.
             *
             * @param any) any)
             * @return encode result
             */
            return encode(NStringUtils.strip((String) any));
        }
        if (any instanceof Number) {
            /**
             * Encode.
             *
             * @param any)) any))
             * @return encode result
             */
            return encode(String.valueOf(((Number) any)));
        }
        if (any instanceof Boolean) {
            /**
             * Encode.
             *
             * @param any)) any))
             * @return encode result
             */
            return encode(String.valueOf(((Boolean) any)));
        }
        if (any instanceof LocalDate) {
            /**
             * Encode.
             *
             * @param any).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) any).format( date time formatter.of pattern("yyyy-mm-dd"))
             * @return encode result
             */
            return encode(((LocalDate) any).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (any instanceof LocalDateTime) {
            /**
             * Encode.
             *
             * @param HH:mm:ss.SSS")) hh:mm:ss.sss"))
             * @return encode result
             */
            return encode(((LocalDateTime) any).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        }
        if (any instanceof java.sql.Date) {
            /**
             * Encode.
             *
             * @param any)) any))
             * @return encode result
             */
            return encode(new SimpleDateFormat("yyyy-MM-dd").format(((Date) any)));
        }
        if (any instanceof Time) {
            /**
             * Encode.
             *
             * @param any)) any))
             * @return encode result
             */
            return encode(new SimpleDateFormat("HH:mm:ss.SSS").format(((Time) any)));
        }
        if (any instanceof Date) {
            /**
             * Encode.
             *
             * @param any)) any))
             * @return encode result
             */
            return encode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(((Date) any)));
        }
        if (any instanceof Enum) {
            /**
             * Encode.
             *
             * @param String.valueOf(any) string.value of(any)
             * @return encode result
             */
            return encode(String.valueOf(any));
        }
        /**
         * Illegal argument exception.
         *
         * @param any any
         * @return illegal argument exception result
         */
        throw new IllegalArgumentException("unsupported object format of type " + any.getClass().getName() + " : " + any);
    }

    /**
     * Decode.
     *
     * @param any any
     * @return decode result
     */
    public static String decode(String any) {
        try {
            return URLDecoder.decode(any, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new RuntimeException(e);
        }
    }

    /**
     * Encode.
     *
     * @param any any
     * @return encode result
     */
    public static String encode(String any) {
        try {
            return URLEncoder.encode(any, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            /**
             * Runtime exception.
             *
             * @param e e
             * @return runtime exception result
             */
            throw new RuntimeException(e);
        }
    }
}
