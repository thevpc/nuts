package net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser;

public class Token {
    public String type;
    public Object value;
    public String image;

    public Token(String type, Object value,String image) {
        this.type = type;
        this.value = value;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public boolean isWord(String n) {
        return "WORD".equals(type) && n.equals(value);
    }

    public boolean isWord() {
        return "WORD".equals(type);
    }
    public boolean isWhite() {
        return "WHITE".equals(type);
    }

    public boolean isNewline() {
        return "NEWLINE".equals(type);
    }
    public boolean isEndCommand() {
        return ";".equals(type);
    }

    public String toKeyStr(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(': {
                    sb.append("<OPAR>");
                    break;
                }
                case ')': {
                    sb.append("<CPAR>");
                    break;
                }
                case '[': {
                    sb.append("<OSBRACK>");
                    break;
                }
                case ']': {
                    sb.append("<CSBRACK>");
                    break;
                }
                case '{': {
                    sb.append("<OCBRACK>");
                    break;
                }
                case '}': {
                    sb.append("<CCBRACK>");
                    break;
                }
                case '\"': {
                    sb.append("<DQTE>");
                    break;
                }
                case '\'': {
                    sb.append("<SQTE>");
                    break;
                }
                case '`': {
                    sb.append("<AQTE>");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public String toStr(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\n': {
                    sb.append("\\n");
                    break;
                }
                case '\t': {
                    sb.append("\\t");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        String k=toKeyStr(type);
        String v=toStr(String.valueOf(value));
        if(k.equals(v)){
            return k;
        }
        return toKeyStr(type) + "(" +
                toStr(String.valueOf(value)) +
                ')';
    }

}
