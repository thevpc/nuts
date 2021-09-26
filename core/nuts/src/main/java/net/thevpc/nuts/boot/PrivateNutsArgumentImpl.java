package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandLineManager;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.NutsVal;

import java.util.NoSuchElementException;

/**
 * This is a minimal implementation of NutsArgument and hence should not be
 * used. Instead an instance of NutsArgument can be retrieved using
 * {@link NutsCommandLineManager#createArgument(String)}
 *
 * @author thevpc
 * @app.category Format
 * @since 0.5.5
 */
final class PrivateNutsArgumentImpl /*extends PrivateNutsTokenFilter*/ implements NutsArgument {

    /**
     * equal character
     */
    private final char eq;
    protected String expression;

    /**
     * Constructor
     *
     * @param expression expression
     * @param eq         equals
     */
    public PrivateNutsArgumentImpl(String expression, char eq) {
        this.expression = expression;
        this.eq = eq;
    }

    /**
     * true if the expression is a an option (starts with '-' or '+') but
     * cannot not be evaluated.
     *
     * @return true if option is not evaluable argument.
     */
    public boolean isUnsupported() {
        return expression != null
                && (expression.startsWith("-!!")
                || expression.startsWith("--!!")
                || expression.startsWith("---")
                || expression.startsWith("++")
                || expression.startsWith("!!"));
    }

    /**
     * true if expression starts with '-' or '+'
     *
     * @return true if expression starts with '-' or '+'
     */
    @Override
    public boolean isOption() {
        return expression != null
                && expression.length() > 0
                && (expression.charAt(0) == '-' || expression.charAt(0) == '+');
    }

    @Override
    public boolean isNonOption() {
        return !isOption();
    }

    @Override
    public String getString() {
        return expression;
    }

    @Override
    public boolean isNegated() {
        if (expression == null) {
            return false;
        }
        int i = 0;
        while (i < expression.length()) {
            switch (expression.charAt(i)) {
                case '-': {
                    //ignore leading dashes
                    break;
                }
                case '+': {
                    //ignore leading dashes
                    break;
                }
                case '!': {
                    return true;
                }
                default: {
                    return false;
                }
            }
            i++;
        }
        return false;
    }

    @Override
    public boolean isEnabled() {
        if (expression == null) {
            return true;
        }
        int i = 0;
        boolean opt = false;
        boolean slash = false;
        while (i < expression.length()) {
            switch (expression.charAt(i)) {
                case '-': {
                    opt = true;
                    break;
                }
                case '+': {
                    opt = true;
                    break;
                }
                case '/': {
                    if (!opt) {
                        return false;
                    }
                    if (slash) {
                        return false;
                    }
                    slash = true;
                    break;
                }
                default: {
                    return true;
                }
            }
            i++;
        }
        return true;
    }

    @Override
    public NutsArgument required() {
        if (expression == null) {
            throw new NoSuchElementException("missing value");
        }
        return this;
    }

    @Override
    public boolean isKeyValue() {
        return expression != null && expression.indexOf(eq) >= 0;
    }

    @Override
    public String getOptionPrefix() {
        String k = getArgumentKey();
        if (k != null) {
            if (k.startsWith("---")) {
                return "---";
            }
            if (k.startsWith("--")) {
                return "--";
            }
            if (k.startsWith("-")) {
                return "-";
            }
            if (k.startsWith("+++")) {
                return "+++";
            }
            if (k.startsWith("++")) {
                return "++";
            }
            if (k.startsWith("+")) {
                return "+";
            }
        }
        return null;
    }

    @Override
    public String getSeparator() {
        return String.valueOf(eq);
    }

    @Override
    public NutsVal getOptionName() {
        String k = getArgumentKey();
        if (k != null) {
            String p = getOptionPrefix();
            if (p != null) {
                return new NutsBootStrValImpl(k.substring(p.length()));
            }
        }
        return new NutsBootStrValImpl(null);
    }

    @Override
    public NutsVal getValue() {
        String vv = null;
        if (expression != null) {
            int x = expression.indexOf(eq);
            if (x >= 0) {
                vv = expression.substring(x + 1);
            }
        }
        return new NutsBootStrValImpl(vv) {

            @Override
            public boolean getBoolean() {
                return getBoolean(true, false);
            }

            @Override
            public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
                Boolean b = NutsUtilStrings.parseBoolean(this.getString(), emptyValue, errorValue);
                if (b != null && isNegated()) {
                    return !b;
                }
                return b;
            }
        };
    }

    @Override
    public NutsVal getKey() {
        return new NutsBootStrValImpl(getArgumentKey());
    }

    @Override
    public NutsVal getAll() {
        return new NutsBootStrValImpl(expression);
    }

    public String getArgumentKey() {
        if (expression == null) {
            return null;
        }
        int x = expression.indexOf(eq);
        String p = expression;
        if (x >= 0) {
            p = expression.substring(0, x);
        }
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < p.length()) {
            switch (p.charAt(i)) {
                case '-': {
                    sb.append(p.charAt(i));
                    break;
                }
                case '!': {
                    sb.append(p.substring(i + 1));
                    return sb.toString();
                }
                case '/': {
                    if (sb.length() > 0 && i + 1 < p.length() && p.charAt(i + 1) == '/') {
                        sb.append(p.substring(i + 2));
                        return sb.toString();
                    }
                }
                default: {
                    return p;
                }
            }
            i++;
        }
        return p;
    }

    public boolean isNull() {
        return expression == null;
    }

    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }
}
