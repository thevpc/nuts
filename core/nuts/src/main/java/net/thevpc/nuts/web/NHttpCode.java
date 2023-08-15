package net.thevpc.nuts.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NHttpCode {
    public static final NHttpCode OK = of(200);
    public static final NHttpCode BAD_REQUEST = of(400);
    public static final NHttpCode UNAUTHORIZED = of(401);
    public static final NHttpCode FORBIDDEN = of(403);
    public static final NHttpCode NOT_FOUND = of(404);
    public static final NHttpCode METHOD_NOT_ALLOWED = of(405);
    public static final NHttpCode INTERNAL_SERVER_ERROR = of(500);
    private int code;
    private static final Map<Integer, NHttpCode> cache = new HashMap<>();

    public static NHttpCode of(int code) {
        synchronized (NHttpCode.cache) {
            if (code >= 0 && code < 600) {
                return cache.computeIfAbsent(code, integer -> new NHttpCode(code));
            }
        }
        return new NHttpCode(code);
    }

    private NHttpCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NHttpCode httpCode = (NHttpCode) o;
        return code == httpCode.code;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }
}
