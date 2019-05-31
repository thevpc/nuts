/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

/**
 * This is a minimal implementation of NutsArgument and hence should not be
 * used. Instead an instance of NutsArgument can be retrieved using
 * {@link NutsCommand#newArgument(String)}
 *
 * @author vpc
 * @since 0.5.5
 */
class NutsArgumentLimited extends NutsTokenFilterLimited implements NutsArgument {
    /**
     * equal character
     */
    private final char eq;

    /**
     * Constructor
     * @param expression expression
     * @param eq equals
     */
    public NutsArgumentLimited(String expression, char eq) {
        super(expression);
        this.eq = eq;
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
        return expression != null && expression.indexOf(eq) >= 0;
    }

    @Override
    public NutsArgument getKey() {
        if (expression == null) {
            return this;
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
                    return new NutsArgumentLimited(sb.toString(),eq);
                }
                case '/': {
                    if (sb.length() > 0 && i + 1 < p.length() && p.charAt(i + 1) == '/') {
                        sb.append(p.substring(i + 2));
                        return new NutsArgumentLimited(sb.toString(),eq);
                    }
                }
                default: {
                    return new NutsArgumentLimited(p,eq);
                }
            }
            i++;
        }
        return new NutsArgumentLimited(p,eq);
    }

    @Override
    public NutsArgument getValue() {
        if (expression == null) {
            return this;
        }
        int x = expression.indexOf(eq);
        if (x >= 0) {
            return new NutsArgumentLimited(expression.substring(x + 1),eq);
        }
        return new NutsArgumentLimited(null,eq);
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
    public boolean isInt() {
        try {
            if (expression != null) {
                Integer.parseInt(expression);
                return true;
            }
        } catch (NumberFormatException ex) {
            //ignore
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
        if (NutsUtilsLimited.isBlank(expression)) {
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
            //ignore
        }
        return false;
    }

    @Override
    public long getLong() {
        return getLong(0);
    }

    @Override
    public long getLong(long defaultValue) {
        if (NutsUtilsLimited.isBlank(expression)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(expression);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean isDouble() {
        try {
            if (expression != null) {
                Double.parseDouble(expression);
                return true;
            }
        } catch (NumberFormatException ex) {
            //ignore
        }
        return false;
    }

    @Override
    public double getDouble() {
        return getDouble(0);
    }

    @Override
    public double getDouble(double defaultValue) {
        if (NutsUtilsLimited.isBlank(expression)) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(expression);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    @Override
    public boolean getBoolean() {
        Boolean bb = NutsUtilsLimited.parseBoolean(expression, null);
        boolean b = NutsUtilsLimited.isBlank(expression)?false:bb==null?false:bb.booleanValue();
        if (isNegated()) {
            return !b;
        }
        return b;
    }

    @Override
    public boolean isBoolean() {
        if (expression != null) {
            return NutsUtilsLimited.parseBoolean(expression, null) != null;
        }
        return false;
    }

    @Override
    public Boolean getBoolean(Boolean defaultValue) {
        if (expression == null) {
            return defaultValue;
        }
        return NutsUtilsLimited.parseBoolean(expression, defaultValue);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }

    @Override
    public NutsArgument required() {
        if (expression == null) {
            throw new NutsIllegalArgumentException(null,"Missing value");
        }
        return this;
    }
}
