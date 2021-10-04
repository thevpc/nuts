package net.thevpc.nuts;

import java.text.MessageFormat;
import java.util.Formatter;

public class NutsMessage {

    private final String message;
    private final NutsTextFormatStyle style;
    private final Object[] params;

    public NutsMessage(NutsTextFormatStyle style, String message, Object... params) {
        if (message == null || params == null || style == null) {
            throw new NullPointerException();
        }
        this.style = style;
        if (style == NutsTextFormatStyle.PLAIN || style == NutsTextFormatStyle.FORMATTED) {
            if (params != null && params.length > 0) {
                throw new IllegalArgumentException("arguments are not supported for " + style);
            }
        }
        this.message = message;
        this.params = params;
    }

    public static NutsMessage formatted(String message) {
        return new NutsMessage(NutsTextFormatStyle.FORMATTED, message);
    }

    public static NutsMessage plain(String message) {
        return new NutsMessage(NutsTextFormatStyle.PLAIN, message);
    }

    public static NutsMessage cstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.CSTYLE, message, params);
    }

    public static NutsMessage jstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.JSTYLE, message, params);
    }

    public NutsTextFormatStyle getStyle() {
        return style;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getParams() {
        return params;
    }

    public NutsString toNutsString(NutsSession session) {
        return session.text().toText(this);
    }

    @Override
    public String toString() {
        switch (style) {
            case CSTYLE: {
                StringBuilder sb = new StringBuilder();
                new Formatter(sb).format(message, params);
                return sb.toString();
            }
            case JSTYLE: {
                return MessageFormat.format(message, params);
            }
            case FORMATTED: {
                return message;
            }
            case PLAIN: {
                return message;
            }
        }
        return "NutsMessage{" + "message=" + message + ", style=" + style + ", params=" + params + '}';
    }

}
