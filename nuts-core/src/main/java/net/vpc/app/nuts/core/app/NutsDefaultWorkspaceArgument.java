/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.app;

import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsDefaultArgument;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;

/**
 * @author vpc
 */
public class NutsDefaultWorkspaceArgument implements NutsArgument {

    private final String expression;

    public NutsDefaultWorkspaceArgument(String expression) {
        this.expression = expression;
    }

    public boolean isNegatedOption() {
        return expression != null
                && (expression.startsWith("-!") || expression.startsWith("--!"));
    }

    public boolean isUnsupportedOption() {
        return expression != null
                && (expression.startsWith("---") || expression.startsWith("-!!") || expression.startsWith("--!!"));
    }

    public boolean isUnsupported() {
        return expression != null
                && (expression.startsWith("---") || expression.startsWith("-!!") || expression.startsWith("--!!") || expression.startsWith("!!"));
    }

    @Override
    public NutsArgument getName() {
        return new NutsDefaultWorkspaceArgument(getName0());
    }

    public String getName0() {
        boolean k = isKeyVal();
        String s = (k) ? getKey().getString() : getString();
        if (isUnsupported()) {
            return s;
        }
        if (s != null) {
            if (s.startsWith("!")) {
                return s.substring(1);
            }
            if (s.startsWith("--!")) {
                return "--" + s.substring(3);
            }
            if (s.startsWith("-!")) {
                return "-" + s.substring(2);
            }
        }
        return s;
    }

    public boolean isKeyVal() {
        return expression != null
                && expression.indexOf('=') >= 0;
    }

    public String getOptionName() {
        if (expression != null) {
            if (expression.startsWith("--")) {
                return expression.substring(2);
            } else if (expression.startsWith("-")) {
                return expression.substring(1);
            }
        }
        throw new IllegalArgumentException("Not an option");
    }

    public String getStringValue() {
        if (isKeyVal()) {
            return getValue().getString();
        }
        return "";
    }

    public int getIntValue() {
        String value = getStringValue();
        if (value.isEmpty()) {
            return 0;
        }
        return Integer.parseInt(value);
    }

    public long getLongValue() {
        String value = getStringValue();
        if (value.isEmpty()) {
            return 0;
        }
        return Long.parseLong(value);
    }

    public double getDoubleValue() {
        String value = getStringValue();
        if (value.isEmpty()) {
            return 0;
        }
        return Double.parseDouble(value);
    }

    public boolean isKeyVal(String sep) {
        return expression != null && expression.contains(sep);
    }

    public String getKey(String sep) {
        if (isKeyVal()) {
            int x = expression.indexOf(sep);
            return expression.substring(0, x);
        }
        throw new IllegalArgumentException("Not a KeyVal");
    }

    public String getValue(String sep) {
        if (isKeyVal()) {
            int x = expression.indexOf(sep);
            return expression.substring(x + sep.length());
        }
        throw new IllegalArgumentException("Not a KeyVal");
    }

    public String getExpression(String s) {
        return expression == null ? s : expression;
    }

    public boolean isAny(String... any) {
        for (String s : any) {
            if (s == null) {
                if (expression == null) {
                    return true;
                }
            } else if (s.equals(expression)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsArgument required() {
        if (expression == null) {
            throw new IllegalArgumentException("Missing value");
        }
        return this;
    }

    @Override
    public boolean isOption() {
        return expression != null && expression.startsWith("-");
    }

    @Override
    public boolean isNonOption() {
        return !expression.startsWith("-");
    }

    @Override
    public boolean isKeyValue() {
        return expression.indexOf('=') >= 0;
    }

    @Override
    public boolean hasValue() {
        return expression.indexOf('=') >= 0;
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
    public NutsArgument getKey() {
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

//    public Argument getName() {
//        int x = expression.indexOf('=');
//        if (x >= 0) {
//            return new Argument(expression.substring(0, x));
//        }
//        return new Argument(null);
//    }
    @Override
    public NutsDefaultWorkspaceArgument getValue() {
        int x = expression.indexOf('=');
        if (x >= 0) {
            return new NutsDefaultWorkspaceArgument(expression.substring(x + 1));
        }
        return new NutsDefaultWorkspaceArgument(null);
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

    @Override
    public boolean isEmpty() {
        return expression == null || expression.isEmpty();
    }

    @Override
    public boolean isNegated() {
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
        if (expression == null) {
            throw new IllegalArgumentException("Missing value");
        }
        return getInt(0);
    }

    @Override
    public int getInt(int defaultValue) {
        if (CoreStringUtils.isBlank(expression)) {
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
        if (CoreStringUtils.isBlank(expression)) {
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
        Boolean bb = CoreCommonUtils.parseBoolean(expression, null);
        boolean b = CoreStringUtils.isBlank(expression)?false:bb==null?false:bb.booleanValue();
        if (isNegated()) {
            return !b;
        }
        return b;
    }

    @Override
    public boolean isBoolean() {
        if (expression != null) {
            return CoreCommonUtils.parseBoolean(expression, null) != null;
        }
        return false;
    }

    @Override
    public Boolean getBoolean(Boolean defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        return CoreCommonUtils.parseBoolean(expression, defaultValue);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }

    @Override
    public boolean getBooleanValue() {
        return getValue().getBoolean(!isNegated());
    }

}
