package net.thevpc.nuts.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NHttpCode {
    private static final Map<Integer, NHttpCode> cache = new HashMap<>();
    public static final NHttpCode OK = of(200);
    public static final NHttpCode CREATED = of(201);
    public static final NHttpCode ACCEPTED = of(202);
    public static final NHttpCode NON_AUTHORITATIVE_INFORMATION = of(203);
    public static final NHttpCode NO_CONTENT = of(204);
    public static final NHttpCode RESET_CONTENT = of(205);
    public static final NHttpCode BAD_REQUEST = of(400);
    public static final NHttpCode UNAUTHORIZED = of(401);
    public static final NHttpCode PAYMENT_REQUIRED = of(402);
    public static final NHttpCode FORBIDDEN = of(403);
    public static final NHttpCode NOT_FOUND = of(404);
    public static final NHttpCode METHOD_NOT_ALLOWED = of(405);
    public static final NHttpCode METHOD_NOT_ACCEPTABLE = of(406);
    public static final NHttpCode PROXY_AUTHENTICATION_REQUIRED = of(407);
    public static final NHttpCode REQUEST_TIMEOUT = of(408);
    public static final NHttpCode CONFLICT = of(409);
    public static final NHttpCode GONE = of(410);
    public static final NHttpCode LENGTH_REQUIRED = of(411);
    public static final NHttpCode PRECONDITION_FAILED = of(412);
    public static final NHttpCode CONTENT_TOO_LARGE = of(413);
    public static final NHttpCode URI_TOO_LONG = of(414);
    public static final NHttpCode UNSUPPORTED_MEDIA_TYPE = of(415);
    public static final NHttpCode INTERNAL_SERVER_ERROR = of(500);
    private int code;

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
