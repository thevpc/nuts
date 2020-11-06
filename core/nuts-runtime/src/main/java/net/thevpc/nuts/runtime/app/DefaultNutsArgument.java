/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts.runtime.app;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;

import net.thevpc.nuts.runtime.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.DefaultNutsTokenFilter;
import net.thevpc.nuts.NutsArgument;

/**
 * @author vpc
 */
public class DefaultNutsArgument extends DefaultNutsTokenFilter implements NutsArgument {
    boolean enabled = true;
    boolean negated = false;
    String optionPrefix = null;
    String optionName = null;
    String keyPart = null;
    String valuePart = null;
    /**
     * equal character
     */
    private final char eq;

    public DefaultNutsArgument(String expression) {
        this(expression,'=');
    }


    /**
     * Constructor
     *
     * @param expression expression
     * @param eq         equals
     */
    public DefaultNutsArgument(String expression, char eq) {
        super(expression);
        this.eq = eq;

        if (expression == null) {
            //
        } else if (expression.length() == 0) {
            optionPrefix = "";
            optionName = "";
            keyPart = "";
        } else if (expression.length() == 1) {
            switch (expression.charAt(0)) {
                case '-':
                case '+': {
                    optionPrefix = expression;
                    optionName = "";
                    keyPart = expression;
                    break;
                }
                default: {
                    optionPrefix = "";
                    optionName = "";
                    keyPart = expression;
                }
            }
        } else {
            StringReader reader = new StringReader(expression);
            int r = -1;
            final int EXPECT_OPTION = 1;
            final int EXPECT_COMMENT = 2;
            final int EXPECT_NEG = 3;
            final int EXPECT_NAME = 4;
            final int EXPECT_VAL = 5;
            int status = EXPECT_OPTION;
            StringBuilder b_option_prefix = new StringBuilder();
            StringBuilder b_option_name = new StringBuilder();
            StringBuilder b_key = new StringBuilder();
            StringBuilder b_val = null;
            try {
                while ((r = reader.read()) != -1) {
                    char c = (char) r;
                    switch (status) {
                        case EXPECT_OPTION: {
                            if (c == '-' || c == '+') {
                                b_option_prefix.append(c);
                                b_key.append(c);
                                if (b_option_prefix.length() >= 2) {
                                    status = EXPECT_COMMENT;
                                }
                            } else if (c == '/') {
                                reader.mark(1);
                                r = reader.read();
                                if (r == '/') {
                                    enabled = false;
                                    status = EXPECT_NEG;
                                } else {
                                    if (b_option_prefix.length() > 0) {
                                        b_option_name.append('/');
                                    }
                                    b_key.append('/');
                                    if (r != -1) {
                                        reader.reset();
                                    }
                                    status = EXPECT_NAME;
                                }
                            } else if (c == '!') {
                                negated = true;
                                status = EXPECT_NAME;
                            } else if (c == eq) {
                                status = EXPECT_VAL;
                                b_val = new StringBuilder();
                            } else {
                                if (b_option_prefix.length() > 0) {
                                    b_option_name.append(c);
                                }
                                b_key.append(c);
                                status = EXPECT_NAME;
                            }
                            break;
                        }
                        case EXPECT_COMMENT: {
                            if (c == '/') {
                                reader.mark(1);
                                r = reader.read();
                                if (r == '/') {
                                    enabled = false;
                                    status = EXPECT_NEG;
                                } else {
                                    b_option_name.append('/');
                                    if (r != -1) {
                                        reader.reset();
                                    }
                                    status = EXPECT_NAME;
                                }
                            } else if (c == '!') {
                                negated = true;
                                status = EXPECT_NAME;
                            } else if (c == eq) {
                                status = EXPECT_VAL;
                                b_val = new StringBuilder();
                            } else {
                                if (b_option_prefix.length() > 0) {
                                    b_option_name.append(c);
                                }
                                b_key.append(c);
                                status = EXPECT_NAME;
                            }
                            break;
                        }
                        case EXPECT_NEG: {
                            if (c == '!') {
                                negated = true;
                                status = EXPECT_NAME;
                            } else if (c == eq) {
                                status = EXPECT_VAL;
                                b_val = new StringBuilder();
                            } else {
                                if (b_option_prefix.length() > 0) {
                                    b_option_name.append(c);
                                }
                                b_key.append(c);
                                status = EXPECT_NAME;
                            }
                            break;
                        }
                        case EXPECT_NAME: {
                            if (c == eq) {
                                status = EXPECT_VAL;
                                b_val = new StringBuilder();
                            } else {
                                if (b_option_prefix.length() > 0) {
                                    b_option_name.append(c);
                                }
                                b_key.append(c);
                                status = EXPECT_NAME;
                            }
                            break;
                        }
                        case EXPECT_VAL: {
                            b_val.append(c);
                            break;
                        }
                        default:{
                            throw new IllegalStateException("Unsupported state");
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
            optionPrefix = b_option_prefix.toString();
            optionName = b_option_name.toString();
            keyPart = b_key.toString();
            valuePart = b_val == null ? null : b_val.toString();
        }
    }

    public boolean isUnsupported() {
        return expression != null
                && (expression.startsWith("-!!")
                || expression.startsWith("--!!")
                || expression.startsWith("---")
                || expression.startsWith("++")
                || expression.startsWith("!!"));
    }

    @Override
    public boolean isOption() {
        return optionPrefix!=null && optionPrefix.length() > 0;
    }

    @Override
    public boolean isNonOption() {
        return !isOption();
    }

    @Override
    public boolean isKeyValue() {
        return valuePart != null;
    }

    public String getStringOptionPrefix() {
        return optionPrefix;
    }

    @Override
    public String getKeyValueSeparator() {
        return String.valueOf(eq);
    }

    @Override
    public NutsArgument getArgumentKey() {
        if (expression == null) {
            return this;
        }
        return new DefaultNutsArgument(keyPart, eq);
    }

    @Override
    public NutsArgument getArgumentOptionName() {
        if (expression == null) {
            return this;
        }
        return new DefaultNutsArgument(optionName, eq);
    }

    @Override
    public String getStringOptionName() {
        return optionName;
    }

    @Override
    public NutsArgument getArgumentValue() {
        if (expression == null) {
            return this;
        }
        return new DefaultNutsArgument(valuePart, eq);
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
        return negated;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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
        if (CoreStringUtils.isBlank(expression)) {
            throw new NumberFormatException("missing value");
        }
        return Integer.parseInt(expression);
    }

    @Override
    public int getIntValue(int defaultValue) {
        return getArgumentValue().getInt(defaultValue);
    }

    @Override
    public int getIntValue() {
        try {
            return getArgumentValue().getInt();
        }catch (NumberFormatException e){
            throw new NumberFormatException("invalid int value for "+getString()+": "+ CoreStringUtils.exceptionToString(e));
        }
    }

    @Override
    public long getLongValue() {
        try {
            return getArgumentValue().getLong();
        }catch (NumberFormatException e){
            throw new NumberFormatException("invalid long value for "+getString()+": "+CoreStringUtils.exceptionToString(e));
        }
    }

    @Override
    public double getDoubleValue() {
        try {
            return getArgumentValue().getDouble();
        }catch (NumberFormatException e){
            throw new NumberFormatException("invalid double value for "+getString()+": "+CoreStringUtils.exceptionToString(e));
        }
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
            //ignore
        }
        return false;
    }

    @Override
    public long getLong() {
        if (CoreStringUtils.isBlank(expression)) {
            throw new NumberFormatException("missing value");
        }
        return Long.parseLong(expression);
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
        if (CoreStringUtils.isBlank(expression)) {
            throw new NumberFormatException("missing value");
        }
        return Double.parseDouble(expression);
    }

    @Override
    public double getDouble(double defaultValue) {
        if (CoreStringUtils.isBlank(expression)) {
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
        boolean b = CoreCommonUtils.parseBoolean(expression, false);
        if (isNegated()) {
            return !b;
        }
        return b;
    }

    @Override
    public boolean isBoolean() {
        return CoreCommonUtils.parseBoolean(expression, null) != null;
    }

    @Override
    public Boolean getBoolean(Boolean defaultValue) {
        return CoreCommonUtils.parseBoolean(expression, defaultValue);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }

    @Override
    public NutsArgument required() {
        if (expression == null) {
            throw new NoSuchElementException("missing value");
        }
        return this;
    }

    @Override
    public String getStringKey() {
        return getArgumentKey().getString();
    }

    @Override
    public String getStringValue() {
        return getArgumentValue().getString();
    }

    @Override
    public boolean getBooleanValue() {
        return getArgumentValue().getBoolean();
    }

    @Override
    public Boolean getBooleanValue(Boolean defaultValue) {
        return getArgumentValue().getBoolean(defaultValue);
    }

    @Override
    public String getStringValue(String defaultValue) {
        return getArgumentValue().getString(defaultValue);
    }
}
