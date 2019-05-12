/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts;

/**
 * This is a minimal implementation of NutsArgument and hence should not be
 * used. Instead an instance of NutsArgument can be retrieved using
 * {@link NutsCommandLine#newArg(java.lang.String)}
 *
 * @author vpc
 * @since 0.5.5
 */
public class NutsDefaultArgument implements NutsArgument {

    private final String expression;

    public NutsDefaultArgument(String line) {
        this.expression = line;
    }

    @Override
    public boolean isOption() {
        return expression != null && expression.startsWith("-");
    }

    @Override
    public boolean isNonOption() {
        return expression == null || !expression.startsWith("-");
    }

    @Override
    public boolean isKeyValue() {
        return expression != null && expression.indexOf('=') >= 0;
    }

    @Override
    public boolean hasValue() {
        return expression != null && expression.indexOf('=') >= 0;
    }

    @Override
    public String strKey() {
        return getStrKey();
    }

    @Override
    public String getStrKey() {
        return getKey().getString();
    }

    @Override
    public NutsDefaultArgument getKey() {
        if (expression == null) {
            return this;
        }
        int x = expression.indexOf('=');
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
                    return new NutsDefaultArgument(sb.toString());
                }
                case '/': {
                    if (sb.length() > 0 && i + 1 < p.length() && p.charAt(i + 1) == '/') {
                        sb.append(p.substring(i + 2));
                        return new NutsDefaultArgument(sb.toString());
                    }
                }
                default: {
                    return new NutsDefaultArgument(p);
                }
            }
            i++;
        }
        return new NutsDefaultArgument(p);
    }

    @Override
    public NutsDefaultArgument getName() {
        if (expression == null) {
            return this;
        }
        int x = expression.indexOf('=');
        if (x >= 0) {
            return new NutsDefaultArgument(expression.substring(0, x));
        }
        return new NutsDefaultArgument(null);
    }

    @Override
    public NutsDefaultArgument getValue() {
        if (expression == null) {
            return this;
        }
        int x = expression.indexOf('=');
        if (x >= 0) {
            return new NutsDefaultArgument(expression.substring(x + 1));
        }
        return new NutsDefaultArgument(null);
    }

    @Override
    public String getString() {
        return expression;
    }

    @Override
    public String getString(String defaultValue) {
        return expression == null ? defaultValue : expression;
    }

    @Override
    public boolean isNull() {
        return expression == null;
    }

    @Override
    public boolean isBlank() {
        return expression == null || expression.trim().isEmpty();
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isEmpty() {
        return expression == null || expression.isEmpty();
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
    public boolean isComment() {
        if (expression == null) {
            return false;
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
                case '/': {
                    if (!opt) {
                        return false;
                    }
                    if (slash) {
                        return true;
                    }
                    slash = true;
                    break;
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
    public boolean isInt() {
        try {
            if (expression != null) {
                Integer.parseInt(expression);
                return true;
            }
        } catch (NumberFormatException ex) {
        }
        return false;
    }

    @Override
    public int getInt() {
        return getInt(0);
    }

    @Override
    public int getInt(int defaultValue) {
        if (NutsUtils.isBlank(expression)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(expression);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean isLong() {
        try {
            if (expression != null) {
                Long.parseLong(expression);
                return true;
            }
        } catch (NumberFormatException ex) {
        }
        return false;
    }

    @Override
    public long getLong() {
        return getLong(0);
    }

    @Override
    public long getLong(long defaultValue) {
        if (NutsUtils.isBlank(expression)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(expression);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean() {
        Boolean bb = NutsUtils.parseBoolean(expression, null);
        boolean b = NutsUtils.isBlank(expression)?false:bb==null?false:bb.booleanValue();
        if (isNegated()) {
            return !b;
        }
        return b;
    }

    @Override
    public boolean isBoolean() {
        if (expression != null) {
            return NutsUtils.parseBoolean(expression, null) != null;
        }
        return false;
    }

    @Override
    public Boolean getBoolean(Boolean defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        return NutsUtils.parseBoolean(expression, defaultValue);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }

    @Override
    public boolean getBooleanValue() {
        return getValue().getBoolean(!isNegated());
    }

    public NutsArgument required() {
        if (expression == null) {
            throw new IllegalArgumentException("Missing value");
        }
        return this;
    }
}
