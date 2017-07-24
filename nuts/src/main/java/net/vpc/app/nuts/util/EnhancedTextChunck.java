package net.vpc.app.nuts.util;

/**
 * Created by vpc on 5/23/17.
 */
public class EnhancedTextChunck {
    public static final EnhancedTextChunck NULL=new EnhancedTextChunck(null,null);
    private String pattern;
    private String value;

    public EnhancedTextChunck(String pattern, String value) {
        this.pattern = pattern;
        this.value = value;
    }

    public String getPattern() {
        return pattern;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return pattern == null ? ("\"" +value +'\"'):
                (pattern + "\"" +value +'\"');
    }
}
