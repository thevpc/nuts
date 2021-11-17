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
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.app.cmdline;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NutsBootStrValImpl;
import net.thevpc.nuts.runtime.standalone.xtra.vals.DefaultNutsVal;

import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.NoSuchElementException;

/**
 * @author thevpc
 */
public class DefaultNutsArgument /*extends DefaultNutsTokenFilter*/ implements NutsArgument {
    /**
     * equal character
     */
    private final char eq;
    private final boolean enabled;
    private final boolean negated;
    private final String optionPrefix;
    private final String optionName;
    private final String keyPart;
    private final String valuePart;
    private final String expression;

    public DefaultNutsArgument(String expression) {
        this(expression, '\0');
    }


    /**
     * Constructor
     *
     * @param expression expression
     * @param eq         equals
     */
    public DefaultNutsArgument(String expression, char eq) {
        boolean _enabled = true;
        boolean _negated = false;
        this.expression=expression;
        if (expression == null) {
            optionPrefix=null;
            optionName=null;
            keyPart=null;
            valuePart=null;
        } else if (expression.length() == 0) {
            optionPrefix = "";
            optionName = "";
            keyPart = "";
            valuePart=null;
        } else if (expression.length() == 1) {
            switch (expression.charAt(0)) {
                case '-':
                case '+': {
                    optionPrefix = expression;
                    optionName = "";
                    keyPart = expression;
                    valuePart=null;
                    break;
                }
                default: {
                    optionPrefix = "";
                    optionName = "";
                    keyPart = expression;
                    valuePart=null;
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
                                    _enabled = false;
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
                                _negated = true;
                                status = EXPECT_NAME;
                            } else if (isEq(c, eq)) {
                                eq = c;
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
                                    _enabled = false;
                                    status = EXPECT_NEG;
                                } else {
                                    b_option_name.append('/');
                                    if (r != -1) {
                                        reader.reset();
                                    }
                                    status = EXPECT_NAME;
                                }
                            } else if (c == '!') {
                                _negated = true;
                                status = EXPECT_NAME;
                            } else if (isEq(c, eq)) {
                                eq = c;
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
                                _negated = true;
                                status = EXPECT_NAME;
                            } else if ((isEq(c, eq))) {
                                eq = c;
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
                            if ((isEq(c, eq))) {
                                eq = c;
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
                        default: {
                            throw new IllegalStateException("unsupported state");
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
        this.enabled=_enabled;
        this.negated=_negated;
        this.eq = (eq == '\0' ? '=' : eq);
    }

    private static boolean isEq(char found, char expected) {
        if (expected == '\0') {
            if (found == '=' || found == ':') {
                return true;
            }
        } else {
            if (found == expected) {
                return true;
            }
        }
        return false;
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
        return optionPrefix != null && optionPrefix.length() > 0;
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
        return negated;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
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
        return valuePart != null;
    }

    @Override
    public NutsVal getValue() {
        return new DefaultNutsVal(valuePart) {
            @Override
            public boolean getBoolean() {
                return getBoolean(true,false);
            }
            @Override
            public Boolean getBoolean(Boolean emptyValue, Boolean errorValue) {
                Boolean b = NutsUtilStrings.parseBoolean(this.getString(), emptyValue, errorValue);
                if (b!=null && isNegated()) {
                    return !b;
                }
                return b;
            }
        };
    }

    @Override
    public NutsVal getKey() {
        return new DefaultNutsVal(keyPart);
    }

    @Override
    public NutsVal getAll() {
        return new DefaultNutsVal(expression);
    }

    public String getOptionPrefix() {
        return optionPrefix;
    }

    @Override
    public String getSeparator() {
        return String.valueOf(eq);
    }

    @Override
    public NutsVal getOptionName() {
        return new NutsBootStrValImpl(optionName);
    }

    @Override
    public String toString() {
        return String.valueOf(expression);
    }
}
