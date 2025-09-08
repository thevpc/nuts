package net.thevpc.nuts.runtime.standalone.format.yaml;

class YamlToken {
    String image;
    Object value;
    Type type;
    int indentation;

    public YamlToken(String image, Object value,Type type, int indentation) {
        this.image = image;
        this.value = value;
        this.type = type;
        this.indentation = indentation;
    }

    public boolean hasIndentation() {
        return indentation>=0;
    }

    enum Type{
        DOUBLE_STRING,
        SINGLE_STRING,
        OPEN_STRING,
        NAME,
        NULL,
        TRUE,
        FALSE,
        DECIMAL,
        INTEGER,
        DASH,
        COLON,
        COMMA,
        CLOSE_BRACKET,
        CLOSE_BRACE,
        OPEN_BRACKET,
        OPEN_BRACE,
    }

    @Override
    public String toString() {
        return "YamlToken{" +
                "image='" + image + '\'' +
                ", value=" + value +
                ", type=" + type +
                ", indentation=" + indentation +
                '}';
    }
}
