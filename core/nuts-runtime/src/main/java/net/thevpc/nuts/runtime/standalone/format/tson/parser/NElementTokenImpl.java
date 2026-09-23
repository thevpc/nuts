package net.thevpc.nuts.runtime.standalone.format.tson.parser;

import net.thevpc.nuts.elem.NElementToken;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;

public class NElementTokenImpl implements NElementToken {
    private final NElementTokenType type;
    private final String variant;
    private final int level;
    private final String image;
    private final int line;
    private final int col;
    private final long pos;
    private final Object value;
    private final NMsg errorMessage;
    private final int fenceLength;

    public NElementTokenImpl(String image, NElementTokenType type, String variant, int level, int line, int col, long pos, Object value, NMsg errorMessage) {
        this(image, type, variant, level, line, col, pos, value, errorMessage, 0);
    }

    public NElementTokenImpl(String image, NElementTokenType type, String variant, int level, int line, int col, long pos, Object value, NMsg errorMessage, int fenceLength) {
        this.type = type;
        this.image = image;
        this.variant = variant;
        this.level = level;
        this.line = line;
        this.col = col;
        this.pos = pos;
        this.value = value;
        this.errorMessage = errorMessage;
        this.fenceLength = fenceLength;
    }

    public boolean isError() {
        return errorMessage != null;
    }

    public NMsg errorMessage() {
        return errorMessage;
    }

    /**
     * length of the opening/closing quote run (fence length N) of a fenced
     * (quoted) string token. 0 means "not a fenced string token / derive from
     * token type".
     *
     * @return fence length N (1 for simple quoted strings, &gt;=3 for
     * arbitrary fenced strings, 0 if undefined)
     */
    public int fenceLength() {
        return fenceLength;
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

    @Override
    public String typeName() {
        return type==null?"":type.id();
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
