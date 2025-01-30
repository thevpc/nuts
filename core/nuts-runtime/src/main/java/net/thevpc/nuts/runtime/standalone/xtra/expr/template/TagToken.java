package net.thevpc.nuts.runtime.standalone.xtra.expr.template;

public class TagToken {
    TagTokenType type;
    String value;

    public TagToken(TagTokenType type, String value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return type+"{" +
                value+
                '}';
    }
}
