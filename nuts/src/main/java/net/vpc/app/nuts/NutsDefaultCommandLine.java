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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * Simple Command line parser implementation. The command line supports
 * arguments in the following forms :
 * <ul>
 * <li> non option arguments : any argument that does not start with '-'</li>
 *
 * <li>
 * long option arguments : any argument that starts with a single '--' in the
 * form of
 * <pre>--[//][!]?[^=]*[=.*]</pre>
 * <ul>
 * <li>// means disabling the option</li>
 * <li>! means switching (to 'false') the option's value</li>
 * <li>the string before the '=' is the option's key</li>
 * <li>the string after the '=' is the option's value</li>
 * </ul>
 * Examples :
 * <ul>
 * <li>--!enable : option 'enable' with 'false' value</li>
 * <li>--enable=yes : option 'enable' with 'yes' value</li>
 * <li>--!enable=yes : invalid option (no error will be thrown buts the result
 * is undefined)</li>
 * </ul>
 * </li>
 * <li>
 * simple option arguments : any argument that starts with a single '-' in the
 * form of
 * <pre>-[//][!]?[a-z][=.*]</pre> This is actually very similar to long options
 * if expandSimpleOptions=false. When activating expandSimpleOptions, multi
 * characters key will be expanded as multiple separate simple options Examples
 * :
 * <ul>
 * <li>-!enable (with expandSimpleOptions=false) : option 'enable' with 'false'
 * value</li>
 * <li>--enable=yes : option 'enable' with 'yes' value</li>
 * <li>--!enable=yes : invalid option (no error will be thrown buts the result
 * is undefined)</li>
 * </ul>
 *
 * </li>
 *
 * <li>long option arguments : any argument that starts with a '--' </li>
 * </ul>
 * option may start with '!' to switch armed flags expandSimpleOptions : when
 * activated
 *
 * @author vpc
 * @since 0.5.5
 */
public class NutsDefaultCommandLine implements NutsCommandLine {

    protected LinkedList<String> args = new LinkedList<>();
    protected LinkedList<NutsArgument> expanded = new LinkedList<>();
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    private static final String NOT_SUPPORTED = "This a minimal implementation of NutsCommandLine used to bootstrap. This Method is not supported.";

    public NutsDefaultCommandLine(String[] args) {
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
    }

    @Override
    public NutsDefaultCommandLine addSpecialSimpleOption(String option) {
        specialSimpleOptions.add(option);
        return this;
    }

    @Override
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    @Override
    public NutsDefaultCommandLine expandSimpleOptions() {
        return expandSimpleOptions(true);
    }

    @Override
    public NutsDefaultCommandLine expandSimpleOptions(boolean expand) {
        return setExpandSimpleOptions(expand);
    }

    @Override
    public NutsDefaultCommandLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    @Override
    public List<String> getArgs() {
        ArrayList<String> p = new ArrayList<>();
        for (NutsArgument a : expanded) {
            p.add(a.toString());
        }
        for (String a : args) {
            p.add(a);
        }
        return p;
    }

    /**
     * returns un-parsed arguments and then clears them
     *
     * @return
     */
    @Override
    public List<String> removeAll() {
        List<String> x = getArgs();
        args.clear();
        expanded.clear();
        return x;
    }

    /**
     * if the argument defines a values (with =) it will be returned : If not
     * consumes the next argument (without expanding simple options)
     *
     * @param cmdArg
     * @return
     */
    @Override
    public NutsArgument getValueFor(NutsArgument cmdArg) {
        NutsArgument v = cmdArg.getValue();
        if (v.isNull()) {
            return next(true, false);
        }
        return v;
    }

    @Override
    public NutsArgument next(boolean required) {
        return next(required, expandSimpleOptions);
    }

    @Override
    public boolean isSpecialOneDashOption(String v) {
        for (String x : specialSimpleOptions) {
            if (v.equals("-" + x) || v.startsWith("-" + x + "=")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsArgument next() {
        return next(false, expandSimpleOptions);
    }

    public NutsArgument newArgument(String s) {
        return new NutsDefaultArgument(s);
    }

    public NutsArgument next(boolean required, boolean expandSimpleOptions) {
        if (!expanded.isEmpty()) {
            return expanded.removeFirst();
        }
        if (!args.isEmpty()) {
            // -!abc=true
            String v = args.removeFirst();
            if (expandSimpleOptions && (v.length() > 2 && v.charAt(0) == '-' && v.charAt(1) != '-' && !isSpecialOneDashOption(v))) {
                char[] chars = v.toCharArray();
                boolean negate = false;
                Character c = null;
                for (int i = 1; i < chars.length; i++) {
                    switch (chars[i]) {
                        case '!': {
                            if (c != null) {
                                if (negate) {
                                    expanded.add(newArgument("-!" + c));
                                } else {
                                    expanded.add(newArgument("-" + c));
                                }
                                c = null;
                            }
                            negate = true;
                            break;
                        }
                        case '=': {
                            if (c == null) {
                                if (negate) {
                                    expanded.add(newArgument("-!" + new String(chars, i, chars.length - i)));
                                    negate = false;
                                } else {
                                    expanded.add(newArgument("-" + new String(chars, i, chars.length - i)));
                                }
                            } else {
                                if (negate) {
                                    negate = false;
                                    expanded.add(newArgument("-!" + c + new String(chars, i, chars.length - i)));
                                } else {
                                    expanded.add(newArgument("-" + c + new String(chars, i, chars.length - i)));
                                }
                            }
                            c = null;
                            i = chars.length;
                            break;
                        }
                        default: {
                            if (c != null) {
                                if (negate) {
                                    expanded.add(newArgument("-!" + c));
                                    negate = false;
                                } else {
                                    expanded.add(newArgument("-" + c));
                                }
                            }
                            c = chars[i];
                        }
                    }
                }
                if (c != null) {
                    if (negate) {
                        expanded.add(newArgument("!" + c));
                        negate = false;
                    } else {
                        expanded.add(newArgument("" + c));
                    }
                }
                return expanded.removeFirst();
            }
            return newArgument(v);
        }
        if (required) {
            throw new NoSuchElementException("Missing argument");
        }
        return null;
    }

    @Override
    public boolean hasNext() {
        return !expanded.isEmpty() || !args.isEmpty();
    }

    // NOT IMPLEMENTED !!!!
    @Override
    public NutsArgument readBooleanOption(String... names) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readStringOption(String... names) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readImmediateStringOption(String... names) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readOption(String... names) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readVoidOption(String... names) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readOption(OptionType expectValue, String... names) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine requiredNonOption() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int skipAll() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readNonOption(boolean expectValue, String name) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readRequiredOption(String name) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readRequiredNonOption() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readRequiredNonOption(NutsArgumentNonOption name) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readNonOption() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readNonOption(String... names) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readRequiredNonOption(String name) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readNonOption(NutsArgumentNonOption name) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument readNonOption(NutsArgumentNonOption name, boolean error) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument read() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean readAll(boolean acceptDuplicates, String... vals) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean readAll(String... vals) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean readAllOnce(String... vals) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean acceptSequence(int pos, String... vals) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument findOption(String option) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument get() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgument get(int i) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean containOption(String name) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int indexOfOption(String name) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int length() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine requireEmpty() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine unexpectedArgument() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine requireNonEmpty() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine removeSpecialSimpleOption(String option) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine pushBack(NutsArgument a) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine setCommandName(String commandName) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String getCommandName() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String[] getSpecialSimpleOptions() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public String[] toArray() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int getWordIndex() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isExecMode() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isAutoCompleteMode() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isOption(int index) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isNonOption(int index) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isOption(String... options) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public boolean isOption(int index, String... options) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int skip() {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public int skip(int count) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    @Override
    public NutsArgumentNonOption createNonOption(String type, String label) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

}
