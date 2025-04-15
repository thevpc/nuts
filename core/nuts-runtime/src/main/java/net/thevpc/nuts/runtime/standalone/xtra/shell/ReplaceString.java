package net.thevpc.nuts.runtime.standalone.xtra.shell;

public class ReplaceString {
    private String expression;
    private String replacement;

    public ReplaceString(String replacement,String expression) {
        this.replacement = replacement;
        this.expression = expression;
    }

    public String getExpression() {
        return expression;
    }

    public String getReplacement() {
        return replacement;
    }

    public boolean matches(String str) {
        return str.matches(expression);
    }
}
