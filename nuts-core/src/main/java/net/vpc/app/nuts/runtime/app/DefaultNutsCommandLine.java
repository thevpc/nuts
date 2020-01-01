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
package net.vpc.app.nuts.runtime.app;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.util.*;

/**
 * <pre>
 * CommandLine args=new CommandLine(Arrays.asList("--!deleteLog","--deploy","/deploy/path","--deploy=/other-deploy/path","some-param"));
 * Argument a;
 * while (args.hasNext()) {
 * if ((a = args.nextBoolean("--deleteLog")) != null) {
 * deleteLog = a.getBooleanValue();
 * } else if ((a = args.nextString("--deploy")) != null) {
 * apps.add(a.getStringValue());
 * } else if ((a = args.next()) != null) {
 * name = a.getString();
 * } else {
 * args.unexpectedArgument();
 * }
 * }
 * </pre> Created by vpc on 12/7/16.
 */
public class DefaultNutsCommandLine implements NutsCommandLine {

    protected LinkedList<String> args = new LinkedList<>();
    protected List<NutsArgument> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    protected NutsWorkspace ws;
    private int wordIndex = 0;
    private NutsCommandAutoComplete autoComplete;
    private char eq = '=';

    //Constructors
    public DefaultNutsCommandLine(NutsWorkspace ws) {
        this.ws = ws;
    }

    public DefaultNutsCommandLine(NutsWorkspace ws, NutsApplicationContext context) {
        this.ws = ws;
        setArguments(context == null ? null : context.getArguments());
        setAutoComplete(context == null ? null : context.getAutoComplete());
    }

    public DefaultNutsCommandLine(NutsWorkspace ws, String[] args, NutsCommandAutoComplete autoComplete) {
        this.ws = ws;
        setArguments(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNutsCommandLine(NutsWorkspace ws, String[] args) {
        this.ws = ws;
        setArguments(args);
    }

    public DefaultNutsCommandLine(NutsWorkspace ws, List<String> args, NutsCommandAutoComplete autoComplete) {
        this.ws = ws;
        setArguments(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNutsCommandLine(List<String> args) {
        setArguments(args);
    }

    public NutsCommandLine parseLine(String commandLine) {
        setArguments(NutsCommandLineUtils.parseCommandLine(ws, commandLine));
        return this;
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
    public NutsCommandLine unregisterSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }

    @Override
    public NutsCommandLine registerSpecialSimpleOption(String option) {
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
    public NutsCommandLine pushBack(NutsArgument arg) {
        if (arg == null) {
            throwError("Null Argument");
        }
        lookahead.add(0, arg);
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

    public NutsCommandLine setArguments(List<String> arguments) {
        if(arguments==null){
            return setArguments(new String[0]);
        }
        return setArguments(arguments.toArray(new String[0]));
    }

    public NutsCommandLine setArguments(String[] arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            Collections.addAll(this.args, arguments);
        }
        return this;
    }

    @Override
    public boolean isSpecialSimpleOption(String option) {
        for (String x : specialSimpleOptions) {
            if (option.equals("-" + x) || option.startsWith("-" + x + eq)) {
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
            String[] nameSeqArray = CoreStringUtils.split(nameSeq, " ").toArray(new String[0]);
            if (isAutoCompleteMode()) {
                for (int i = 0; i < nameSeqArray.length; i++) {
                    if (getWordIndex() == autoComplete.getCurrentWordIndex() + i) {
                        autoComplete.addCandidate(createCandidate(nameSeqArray[i]));
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
                        case STRING: {
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return p;
                            } else {
                                if (isAutoCompleteMode() && getWordIndex() + 1 == autoComplete.getCurrentWordIndex()) {
                                    autoComplete.addCandidate(createCandidate("<StringValueFor" + p.getStringKey() + ">"));
                                }
                                NutsArgument r2 = peek();
                                if (r2 != null && !r2.isOption()) {
                                    skip();
                                    return createArgument(p.getString() + eq + r2.getString());
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
                                    return createArgument(p.getStringKey() + eq + (!x));
                                } else {
                                    return createArgument(p.getStringKey() + eq + (false));
                                }
                            } else if (p.isKeyValue()) {
                                return p;
                            } else {
                                return createArgument(p.getStringKey() + eq + (true));
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

    private NutsArgumentCandidate createCandidate(String s) {
        return DefaultNutsCommandLineFormat.Factory.createCandidate0(ws,s,null);
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
                    autoComplete.addCandidate(createCandidate(name == null ? "<value>" : name.getName()));
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
                        autoComplete.addCandidate(createCandidate(name == null ? "<value>" : name.getName()));
                    } else {
                        for (NutsArgumentCandidate value : values) {
                            autoComplete.addCandidate(value);
                        }
                    }
                }
                return createArgument("");
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
    public boolean accept(int index, String... values) {
        for (int i = 0; i < values.length; i++) {
            NutsArgument argument = get(index + i);
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
            return get(index);
        }
        return null;
    }

    @Override
    public NutsArgument get(int index) {
        if (index < 0) {
            return null;
        }
        if (index < lookahead.size()) {
            return lookahead.get(index);
        }
        while (!args.isEmpty() && index >= lookahead.size()) {
            if (!ensureNext(isExpandSimpleOptions(), true)) {
                break;
            }
        }
        if (index < lookahead.size()) {
            return lookahead.get(index);
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

    public NutsArgument next(boolean required, boolean expandSimpleOptions) {
        if (ensureNext(expandSimpleOptions, false)) {
            if (!lookahead.isEmpty()) {
                return lookahead.remove(0);
            }
            String v = args.removeFirst();
            return createArgument(v);
        } else {
            if (required) {
                throwError("Missing argument");
            }
            return null;
        }
    }

    @Override
    public String toString() {
        String[] args = toArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(NutsCommandLineUtils.escapeArgument(arg));
        }
        return sb.toString();
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    private boolean isExpandableOption(String v, boolean expandSimpleOptions) {
        if (!expandSimpleOptions || v.length() <= 2) {
            return false;
        }
        if (isSpecialSimpleOption(v)) {
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

    private String createExpandedSimpleOption(char start, boolean negate, char val) {
        return new String(negate ? new char[]{start, '!', val} : new char[]{start, val});
    }

    private String createExpandedSimpleOption(char start, boolean negate, String val) {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        if (negate) {
            sb.append('!');
        }
        sb.append(val);
        return sb.toString();
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
                char start = v.charAt(0);
                for (int i = 1; i < chars.length; i++) {
                    char c = chars[i];
                    if (c == '!') {
                        if (last != null) {
                            lookahead.add(createArgument(createExpandedSimpleOption(start,negate,last)));
                            last = null;
                        }
                        negate = true;
                    } else if (chars[i] == eq) {
                        String nextArg = new String(chars, i, chars.length - i);
                        if (last != null) {
                            nextArg = last + nextArg;
                            last = null;
                        }
                        lookahead.add(createArgument(createExpandedSimpleOption(start,negate,nextArg)));
                        i = chars.length;
                    } else {
                        if (last != null) {
                            lookahead.add(createArgument(createExpandedSimpleOption(start,negate,last)));
                        }
                        last = chars[i];
                    }
                }
                if (last != null) {
                    lookahead.add(createArgument(createExpandedSimpleOption(start,negate,last)));
                }
            } else {
                lookahead.add(createArgument(v));
            }
            return true;
        }
        return false;
    }

    private NutsArgument createArgument(String v) {
        return DefaultNutsCommandLineFormat.Factory.createArgument0(ws,v,eq);
    }

    private boolean isAutoComplete() {
        return autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex();
    }



    public NutsCommandLine copy() {
        DefaultNutsCommandLine c = new DefaultNutsCommandLine(ws, toArray(), autoComplete);
        c.eq = this.eq;
        c.commandName = this.commandName;
        return c;
    }

    protected void throwError(String message) {
        StringBuilder m = new StringBuilder();
        if (!CoreStringUtils.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw new NutsIllegalArgumentException(ws, m.toString());
    }

    private String highlightText(String text) {
        return "{{" + ws.io().getTerminalFormat().escapeText(text) + "}}";
    }


}
