package net.thevpc.nuts;

import java.text.MessageFormat;
import java.util.Formatter;

public class NutsMessage {

    private String message;
    private NutsTextFormatStyle style;
    private Object[] params;

    public static NutsMessage cstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.CSTYLE, message, params);
    }

    public static NutsMessage jstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.JSTYLE, message, params);
    }

    public NutsMessage(NutsTextFormatStyle style, String message, Object... params) {
        if (message == null || params == null || style == null) {
            throw new NullPointerException();
        }
        this.style = style;
        this.message = message;
        this.params = params;
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

        }
        return "NutsMessage{" + "message=" + message + ", style=" + style + ", params=" + params + '}';
    }

}
