package net.thevpc.nuts.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class NHttpUrlEncoder {
    public static String encodeObject(Object any) {
        if (any == null) {
            return "";
        }
        if (any instanceof String) {
            return encode(((String) any).trim());
        }
        if (any instanceof Number) {
            return encode(String.valueOf(((Number) any)));
        }
        if (any instanceof Boolean) {
            return encode(String.valueOf(((Boolean) any)));
        }
        if (any instanceof LocalDate) {
            return encode(((LocalDate) any).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        }
        if (any instanceof LocalDateTime) {
            return encode(((LocalDateTime) any).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        }
        if (any instanceof java.sql.Date) {
            return encode(new SimpleDateFormat("yyyy-MM-dd").format(((Date) any)));
        }
        if (any instanceof Time) {
            return encode(new SimpleDateFormat("HH:mm:ss.SSS").format(((Time) any)));
        }
        if (any instanceof Date) {
            return encode(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(((Date) any)));
        }
        if (any instanceof Enum) {
            return encode(String.valueOf(any));
        }
        throw new IllegalArgumentException("unsupported object format of type " + any.getClass().getName() + " : " + any);
    }

    public static String encode(String any) {
        try {
            return URLEncoder.encode(any, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
