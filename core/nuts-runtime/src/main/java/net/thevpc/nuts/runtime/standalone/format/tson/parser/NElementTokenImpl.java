package net.thevpc.nuts.runtime.standalone.format.tson.parser;

import net.thevpc.nuts.elem.NElementToken;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;

public class NElementTokenImpl implements NElementToken {
    private NElementTokenType type;
    private String variant;
    private int level;
    private String image;
    private int line;
    private int col;
    private long pos;
    private Object value;
    private NMsg errorMessage;

    public NElementTokenImpl(String image, NElementTokenType type, String variant, int level, int line, int col, long pos, Object value, NMsg errorMessage) {
        this.type = type;
        this.image = image;
        this.variant = variant;
        this.level = level;
        this.line = line;
        this.col = col;
        this.pos = pos;
        this.value = value;
        this.errorMessage = errorMessage;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public NMsg errorMessage() {
        return errorMessage;
    }

    public NElementTokenType type() {
        return type;
    }

    public String variant() {
        return variant;
    }

    public int level() {
        return level;
    }

    public String image() {
        return image;
    }

    public int line() {
        return line;
    }

    public int col() {
        return col;
    }

    public long pos() {
        return pos;
    }

    public Object value() {
        return value;
    }

    @Override
    public String toString() {
        if(NBlankable.isBlank(variant) && level==0){
            return type.name()+"{" +
                    "image='" + image + '\'' +
                    ", line=" + line +
                    ", col=" + col +
                    ", pos=" + pos +
                    ", value=" + value +
                    (errorMessage != null ? ", errorMessage='" + errorMessage + '\'' : "") +
                    '}';
        }
        return type.name()+"{" +
                "image='" + image + '\'' +
                ", variant='" + variant + '\'' +
                ", level=" + level +
                ", line=" + line +
                ", col=" + col +
                ", pos=" + pos +
                ", value=" + value +
                (errorMessage != null ? ", errorMessage='" + errorMessage + '\'' : "") +
                '}';
    }
}
