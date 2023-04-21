package net.thevpc.nuts.toolbox.nsh.cmds.posix.grep;

public class ExpressionInfo {
    boolean invertMatch = false;
    boolean word = false;
    boolean ignoreCase = false;
    String pattern;

    public String getPattern() {
        return pattern;
    }

    public ExpressionInfo setPattern(String pattern) {
        this.pattern = pattern;
        return this;
    }

    public boolean isInvertMatch() {
        return invertMatch;
    }

    public ExpressionInfo setInvertMatch(boolean invertMatch) {
        this.invertMatch = invertMatch;
        return this;
    }

    public boolean isWord() {
        return word;
    }

    public ExpressionInfo setWord(boolean word) {
        this.word = word;
        return this;
    }

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public ExpressionInfo setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }
}
