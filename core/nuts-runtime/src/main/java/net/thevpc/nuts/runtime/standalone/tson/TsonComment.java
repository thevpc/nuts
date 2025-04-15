package net.thevpc.nuts.runtime.standalone.tson;

import java.util.Objects;

public class TsonComment implements Comparable<TsonComment>{
    private TsonCommentType type;
    private String text;

    public static TsonComment of(String text) {
        return ofMultiLine(text);
    }

    public static TsonComment ofMultiLine(String text) {
        return new TsonComment(TsonCommentType.MULTI_LINE, text);
    }
    public static TsonComment ofSingleLine(String text) {
        return new TsonComment(TsonCommentType.SINGLE_LINE, text);
    }
    public TsonComment(TsonCommentType type, String text) {
        this.type = type;
        this.text = text==null?"":text;
    }

    public TsonCommentType type() {
        return type;
    }

    public String text() {
        return text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TsonComment that = (TsonComment) o;
        return type == that.type && Objects.equals(text, that.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, text);
    }

    @Override
    public int compareTo(TsonComment o) {
        int i = text.compareTo(o.text);
        if(i!=0){
            return i;
        }
        return type.compareTo(o.type);
    }
}
