package net.thevpc.nuts;

public class NutsMessage {
    private NutsString message;
    private NutsTextFormatStyle style;
    private Object[] params;

    public static NutsMessage cstyle(NutsString message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.CSTYLE, message,params);
    }

    public static NutsMessage jstyle(NutsString message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.JSTYLE, message,params);
    }

    public static NutsMessage cstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.CSTYLE, NutsString.of(message),params);
    }

    public static NutsMessage jstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.JSTYLE, NutsString.of(message),params);
    }

    public NutsMessage(NutsTextFormatStyle style, NutsString message, Object... params) {
        if(message==null || params ==null || style==null){
            throw new NullPointerException();
        }
        this.style = style;
        this.message = message;
        this.params = params;
    }

    public NutsTextFormatStyle getStyle() {
        return style;
    }

    public NutsString getMessage() {
        return message;
    }

    public Object[] getParams() {
        return params;
    }
}
