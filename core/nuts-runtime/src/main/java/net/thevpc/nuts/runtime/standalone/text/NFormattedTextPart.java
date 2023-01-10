package net.thevpc.nuts.runtime.standalone.text;

public class NFormattedTextPart {

    private boolean format;
    private String value;

    public NFormattedTextPart(boolean format, String value) {
        this.format = format;
        this.value = value;
    }

    public boolean isFormat() {
        return format;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        if(format){
            return "FORMAT("+value+")";
        }
        return "PLAIN("+value+")";
    }
}
