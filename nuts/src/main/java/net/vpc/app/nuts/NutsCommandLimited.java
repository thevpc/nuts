/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.*;

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
class NutsCommandLimited implements NutsCommandLine {

    private static final String NOT_SUPPORTED = "This a minimal implementation of NutsCommand used to bootstrap. This Method is not supported.";
    private LinkedList<String> args = new LinkedList<>();
    private List<NutsArgument> lookahead = new ArrayList<>();
    private boolean expandSimpleOptions = false;
    private Set<String> specialSimpleOptions = new HashSet<>();
    private String commandName;
    private int wordIndex = 0;
    private NutsCommandAutoComplete autoComplete;
    private char eq = '=';
    //Constructors

    NutsCommandLimited(String[] args) {
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
    }

    //End Constructors
    @Override
    public NutsCommandLine autoComplete(NutsCommandAutoComplete autoComplete) {
        return setAutoComplete(autoComplete);
    }

    @Override
    public NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    @Override
    public NutsCommandLine removeSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }

    @Override
    public NutsCommandLine addSpecialSimpleOption(String option) {
        specialSimpleOptions.add(option);
        return this;
    }

    @Override
    public NutsCommandLine expandSimpleOptions() {
        return expandSimpleOptions(true);
    }

    @Override
    public NutsCommandLine expandSimpleOptions(boolean expand) {
        return setExpandSimpleOptions(expand);
    }

    @Override
    public NutsCommandLine requireNonOption() {
        if (!hasNext() || !peek().isNonOption()) {
            throwError("Expected value");
        }
        return this;
    }

    @Override
    public NutsCommandLine unexpectedArgument(String errorMessage) {
        if (!isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            String m = "Unexpected Argument " + highlightText(String.valueOf(peek()));
            if (errorMessage != null && errorMessage.trim().length() > 0) {
                m += " , " + errorMessage;
            }
            throwError(m);
        }
        return this;
    }

    @Override
    public NutsCommandLine unexpectedArgument() {
        return unexpectedArgument(null);
    }

    @Override
    public NutsCommandLine required(String errorMessage) {
        if (isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            throwError((errorMessage == null || errorMessage.trim().isEmpty()) ? "Missing Arguments" : errorMessage);
        }
        return this;
    }

    @Override
    public NutsCommandLine required() {
        return required(null);
    }

    @Override
    public NutsCommandLine pushBack(NutsArgument a) {
        if (a == null) {
            throwError("Null Argument");
        }
        lookahead.add(0, a);
        return this;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public NutsCommandLine commandName(String commandName) {
        return setCommandName(commandName);
    }

    @Override
    public NutsCommandLine setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public String[] getSpecialSimpleOptions() {
        return specialSimpleOptions.toArray(new String[0]);
    }

    @Override
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    @Override
    public NutsCommandLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    @Override
    public String[] toArray() {
        List<String> all = new ArrayList<>(length());
        for (NutsArgument nutsArgument : lookahead) {
            all.add(nutsArgument.getString());
        }
        all.addAll(args);
        return all.toArray(new String[0]);
    }

    public NutsCommandLine setArgs(List<String> args) {
        return setArgs(args.toArray(new String[0]));
    }

    public NutsCommandLine setArgs(String... args) {
        this.lookahead.clear();
        this.args.clear();
        if (args != null) {
            Collections.addAll(this.args, args);
        }
        return this;
    }

    @Override
    public NutsCommandLine parseLine(String commandLine) {
        throw new NutsException(null, "Unsupported");
    }

    @Override
    public boolean isSpecialOneDashOption(String v) {
        for (String x : specialSimpleOptions) {
            if (v.equals("-" + x) || v.startsWith("-" + x + eq)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public NutsArgument next() {
        return next(false, expandSimpleOptions);
    }

    @Override
    public NutsArgument peek() {
        return get(0);
    }

    @Override
    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }

    @Override
    public NutsArgument nextBoolean(String... names) {
        return next(NutsArgumentType.BOOLEAN, names);
    }

    @Override
    public NutsArgument nextString(String... names) {
        return next(NutsArgumentType.STRING, names);
    }

    @Override
    public NutsArgument nextImmediate(String... names) {
        return next(NutsArgumentType.IMMEDIATE, names);
    }

    @Override
    public NutsArgument next(String... names) {
        return next(NutsArgumentType.ANY, names);
    }

    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            NutsArgument x = get(i);
            if (x == null || !x.getString().equals(nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsArgument next(NutsArgumentType expectValue, String... names) {
        if (expectValue == null) {
            expectValue = NutsArgumentType.ANY;
        }
        if (names.length == 0) {
            if (hasNext()) {
                NutsArgument peeked = peek();
                names = new String[]{
                    peeked.getStringKey()
                };
            }
        }
        for (String nameSeq : names) {
            String[] nameSeqArray = NutsUtilsLimited.split(nameSeq, " ").toArray(new String[0]);
            if (isAutoCompleteMode()) {
                for (int i = 0; i < nameSeqArray.length; i++) {
                    if (getWordIndex() == autoComplete.getCurrentWordIndex() + i) {
                        autoComplete.addCandidate(new NutsDefaultArgumentCandidate(nameSeqArray[i]));
                    }
                }
            }
            if (!isPrefixed(nameSeqArray)) {
                continue;
            }
            String name = nameSeqArray[nameSeqArray.length - 1];
            NutsArgument p = get(nameSeqArray.length - 1);
            if (p != null) {
                if (p.getStringKey().equals(name)) {
                    switch (expectValue) {
                        case ANY: {
                            skip(nameSeqArray.length);
                            return p;
                        }
                        case IMMEDIATE: {
                            if (p.isKeyValue()) {
                                skip(nameSeqArray.length);
                                return p;
                            }
                            break;
                        }
                        case STRING: {
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return p;
                            } else {
                                if (isAutoCompleteMode() && getWordIndex() + 1 == autoComplete.getCurrentWordIndex()) {
                                    autoComplete.addCandidate(new NutsDefaultArgumentCandidate("<StringValueFor" + p.getStringKey() + ">"));
                                }
                                NutsArgument r2 = peek();
                                if (r2 != null && !r2.isOption()) {
                                    skip();
                                    return newArgument(p.getString() + eq + r2.getString());
                                } else {
                                    return p;
                                }
                            }
                        }
                        case BOOLEAN: {
                            skip(nameSeqArray.length);
                            if (p.isNegated()) {
                                if (p.isKeyValue()) {
                                    //should not happen
                                    boolean x = p.getBoolean();
                                    return newArgument(p.getStringKey() + eq + (!x));
                                } else {
                                    return newArgument(p.getStringKey() + eq + (false));
                                }
                            } else if (p.isKeyValue()) {
                                return p;
                            } else {
                                return newArgument(p.getStringKey() + eq + (true));
                            }
                        }
                        default: {
                            throwError("Unsupported " + highlightText(String.valueOf(expectValue)));
                        }
                    }
                }
            }

        }
        return null;
    }

    @Override
    public NutsArgument nextRequiredNonOption(NutsArgumentName name) {
        return next(name, true, true);
    }

    @Override
    public NutsArgument nextNonOption() {
        if (hasNext() && !peek().isOption()) {
            return next();
        }
        return null;
    }

    @Override
    public NutsArgument nextNonOption(NutsArgumentName name) {
        return next(name, true, false);
    }

    @Override
    public NutsArgument next(NutsArgumentName name) {
        return next(name, false, false);
    }

    public NutsArgument next(NutsArgumentName name, boolean forceNonOption, boolean error) {
        if (hasNext() && (!forceNonOption || !peek().isOption())) {
            if (isAutoComplete()) {
                List<NutsArgumentCandidate> values = name == null ? null : name.getCandidates();
                if (values == null || values.isEmpty()) {
                    autoComplete.addExpectedTypedValue(null, name == null ? "value" : name.getName());
                } else {
                    for (NutsArgumentCandidate value : values) {
                        autoComplete.addCandidate(value);
                    }
                }
            }
            NutsArgument r = peek();
            skip();
            return r;
        } else {
            if (autoComplete != null) {
                if (isAutoComplete()) {
                    List<NutsArgumentCandidate> values = name == null ? null : name.getCandidates();
                    if (values == null || values.isEmpty()) {
                        autoComplete.addExpectedTypedValue(null, name == null ? "value" : name.getName());
                    } else {
                        for (NutsArgumentCandidate value : values) {
                            autoComplete.addCandidate(value);
                        }
                    }
                }
                return newArgument("");
            }
            if (!error) {
                return null;//return new Argument("");
            }
            if (hasNext() && (!forceNonOption || !peek().isOption())) {
                throwError("Unexpected option " + highlightText(String.valueOf(peek())));
            }
            throwError("Missing argument " + highlightText((name == null ? "value" : name.getName())));
        }
        //ignored
        return null;
    }

    @Override
    public boolean accept(String... values) {
        return accept(0, values);
    }

    @Override
    public boolean accept(int pos, String... values) {
        for (int i = 0; i < values.length; i++) {
            NutsArgument argument = get(pos + i);
            if (argument == null) {
                return false;
            }
            if (!argument.getStringKey().equals(values[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsArgument find(String name) {
        int index = indexOf(name);
        if (index >= 0) {
            return get(index + 1);
        }
        return null;
    }

    @Override
    public NutsArgument get(int i) {
        if (i < 0) {
            return null;
        }
        if (i < lookahead.size()) {
            return lookahead.get(i);
        }
        while (!args.isEmpty() && i >= lookahead.size()) {
            if (!ensureNext(isExpandSimpleOptions(), true)) {
                break;
            }
        }
        if (i < lookahead.size()) {
            return lookahead.get(i);
        }
        return null;
    }

    @Override
    public boolean contains(String name) {
        return indexOf(name) >= 0;
    }

    @Override
    public int indexOf(String name) {
        int i = 0;
        while (i < length()) {
            if (get(i).getStringKey().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    @Override
    public int length() {
        return lookahead.size() + args.size();
    }

    @Override
    public boolean isEmpty() {
        return !hasNext();
    }

    @Override
    public int getWordIndex() {
        return wordIndex;
    }

    @Override
    public boolean isExecMode() {
        return autoComplete == null;
    }

    @Override
    public boolean isAutoCompleteMode() {
        return autoComplete != null;
    }

    @Override
    public boolean isOption(int index) {
        NutsArgument x = get(index);
        return x != null && x.isOption();
    }

    @Override
    public boolean isNonOption(int index) {
        NutsArgument x = get(index);
        return x != null && x.isNonOption();
    }

    @Override
    public int skipAll() {
        int count = 0;
        while (hasNext()) {
            count += skip(1);
        }
        return count;
    }

    @Override
    public int skip() {
        return skip(1);
    }

    @Override
    public int skip(int count) {
        if (count < 0) {
            count = 0;
        }
        int initialCount = count;
        while (initialCount > 0 && hasNext()) {
            if (next() != null) {
                wordIndex++;
                initialCount--;
            } else {
                break;
            }
        }
        return count;
    }

    @Override
    public NutsArgumentName createName(String type) {
        return createName(type, type);
    }

    public NutsArgument next(boolean required, boolean expandSimpleOptions) {
        if (ensureNext(expandSimpleOptions, false)) {
            if (!lookahead.isEmpty()) {
                return lookahead.remove(0);
            }
            String v = args.removeFirst();
            return newArgument(v);
        } else {
            if (required) {
                throwError("Missing argument");
            }
            return null;
        }
    }

    @Override
    public String toString() {
        return NutsUtilsLimited.escapeArguments(toArray());
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    private boolean isExpandableOption(String v, boolean expandSimpleOptions) {
        if (!expandSimpleOptions || v.length() <= 2) {
            return false;
        }
        if (isSpecialOneDashOption(v)) {
            return false;
        }
        if (v.charAt(0) == '-') {
            if (v.charAt(1) == '-') {
                return false;
            }
            return true;
        }
        if (v.charAt(0) == '+') {
            if (v.charAt(1) == '+') {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean ensureNext(boolean expandSimpleOptions, boolean ignoreExistingExpanded) {
        if (!ignoreExistingExpanded) {
            if (!lookahead.isEmpty()) {
                return true;
            }
        }
        if (!args.isEmpty()) {
            // -!abc=true
            String v = args.removeFirst();
            if (isExpandableOption(v, expandSimpleOptions)) {
                char[] chars = v.toCharArray();
                boolean negate = false;
                Character last = null;
                String prefix = Character.toString(v.charAt(0)) + (negate ? "!" : "");
                for (int i = 1; i < chars.length; i++) {
                    char c = chars[i];
                    if (c == '!') {
                        if (last != null) {
                            lookahead.add(newArgument(prefix + last));
                            last = null;
                        }
                        negate = true;
                    } else if (chars[i] == eq) {
                        String nextArg = new String(chars, i, chars.length - i);
                        if (last != null) {
                            nextArg = last + nextArg;
                            last = null;
                        }
                        lookahead.add(newArgument(prefix + nextArg));
                        i = chars.length;
                    } else {
                        if (last != null) {
                            lookahead.add(newArgument(prefix + last));
                        }
                        last = chars[i];
                    }
                }
                if (last != null) {
                    lookahead.add(newArgument(prefix + last));
                }
            } else {
                lookahead.add(newArgument(v));
            }
            return true;
        }
        return false;
    }

    private boolean isAutoComplete() {
        return autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex();
    }

    @Override
    public NutsArgument newArgument(String s) {
        return new NutsArgumentLimited(s, eq);
    }

    @Override
    public NutsArgumentName createName(String type, String label) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
    }

    protected void throwError(String message) {
        StringBuilder m = new StringBuilder();
        if (!NutsUtilsLimited.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw new NutsIllegalArgumentException(null, m.toString());
    }

    private String highlightText(String text) {
        return text;
    }
}
