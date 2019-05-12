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
package net.vpc.app.nuts.core.app;

import net.vpc.app.nuts.NutsDefaultArgumentCandidate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsArgumentCandidate;
import net.vpc.app.nuts.NutsCommandLineContext;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.NutsArgumentNonOption;
import net.vpc.app.nuts.NutsWorkspace;

/**
 * <pre>
 *         CommandLine args=new CommandLine(Arrays.asList("--!deleteLog","--deploy","/deploy/path","--deploy=/other-deploy/path","some-param"));
 *         Argument a;
 *         while (args.hasNext()) {
 *             if ((a = args.readBooleanOption("--deleteLog")) != null) {
 *                 deleteLog = a.getValue().getBoolean();
 *             } else if ((a = args.readStringOption("--deploy")) != null) {
 *                 apps.add(a.getValue().getString());
 *             } else if ((a = args.readNonOption()) != null) {
 *                 name = a.getString();
 *             } else {
 *                 args.unexpectedArgument();
 *             }
 *         }
 * </pre> Created by vpc on 12/7/16.
 */
public class DefaultWorkspaceCommandLine implements NutsCommandLine {

    public static final NutsArgumentNonOption VALUE = new DefaultNonOption("value");

    protected List<NutsArgument> lookahead = new ArrayList<>();
    protected LinkedList<String> args = new LinkedList<>();

    private int wordIndex = 0;
    private NutsCommandAutoComplete autoComplete;
    private HashSet<String> visitedSequences = new HashSet<>();
    private char eq = '=';
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected NutsWorkspace ws;
    protected String commandName;

    public DefaultWorkspaceCommandLine(NutsWorkspace ws, NutsCommandLineContext context) {
        this.ws = ws;
        setArgs(context.getArgs());
        setAutoComplete(context.getAutoComplete());
    }

    public DefaultWorkspaceCommandLine(NutsWorkspace ws, String[] args, NutsCommandAutoComplete autoComplete) {
        this.ws = ws;
        setArgs(args);
        setAutoComplete(autoComplete);
    }

    public DefaultWorkspaceCommandLine(NutsWorkspace ws, String[] args) {
        this.ws = ws;
        setArgs(args);
    }

    public DefaultWorkspaceCommandLine(NutsWorkspace ws, List<String> args, NutsCommandAutoComplete autoComplete) {
        this.ws = ws;
        setArgs(args);
        setAutoComplete(autoComplete);
    }

    public DefaultWorkspaceCommandLine(List<String> args) {
        setArgs(args);
    }

    public DefaultWorkspaceCommandLine copy() {
        DefaultWorkspaceCommandLine c = new DefaultWorkspaceCommandLine(ws, getArgs(), autoComplete);
        c.eq = this.eq;
        c.visitedSequences = new HashSet<>(this.visitedSequences);
        c.commandName = this.commandName;
        return c;
    }

    @Override
    public NutsCommandLine setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    public void setArgs(List<String> args) {
        setArgs(args.toArray(new String[0]));
    }

    public void setArgs(String[] args) {
        this.lookahead.clear();
        this.args.clear();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                this.args.add(arg);
            } else if (arg.startsWith("-!")) {
                char[] chars = arg.toCharArray();
                for (int i = 2; i < chars.length; i++) {
                    this.args.add("-!" + chars[i]);
                }
            } else if (arg.startsWith("-")) {
                char[] chars = arg.toCharArray();
                for (int i = 1; i < chars.length; i++) {
                    this.args.add("-" + chars[i]);
                }
            } else {
                this.args.add(arg);
            }
        }
    }

    @Override
    public NutsCommandLine removeSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }

    @Override
    public String[] getSpecialSimpleOptions() {
        return specialSimpleOptions.toArray(new String[0]);
    }

    @Override
    public NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
        return this;
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
    public int skip() {
        return skip(1);
    }

    @Override
    public int skip(int count) {
        if (count < 0) {
            count = 0;
        }
        while (count > 0 && hasNext()) {
            if (next() != null) {
                wordIndex++;
            }
        }
        return count;
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
    public boolean isOption(String... options) {
        return isOption(0, options);
    }

    @Override
    public boolean isOption(int index, String... options) {
        for (String s : options) {
            if (s != null) {
                checkOptionString(s);
            }
        }
        if (index >= 0 && index < length()) {
            NutsArgument v = get(index);
            if (v.isOption()) {
                if (v.isKeyValue()) {
                    for (String s : options) {
                        if (s != null) {
                            if (v.getKey().getString().equals(s)) {
                                return true;
                            }
                        }
                    }
                } else {
                    for (String s : options) {
                        if (s != null) {
                            if (v.getString().equals(s)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public NutsArgument readBooleanOption(String... names) {
        return readOption(OptionType.BOOLEAN, names);
    }

    @Override
    public NutsArgument readStringOption(String... names) {
        return readOption(OptionType.STRING, names);
    }

    @Override
    public NutsArgument readImmediateStringOption(String... names) {
        return readOption(OptionType.IMMEDIATE_STRING, names);
    }

    @Override
    public NutsArgument readOption(String... names) {
        return readVoidOption(names);
    }

    @Override
    public NutsArgument readVoidOption(String... names) {
        return readOption(OptionType.VOID, names);
    }

    @Override
    public NutsArgument readOption(OptionType expectValue, String... names) {
        boolean acceptAnyName = false;
        if (names.length == 0) {
            names = new String[]{null};
            acceptAnyName = true;
        }
        for (String name : names) {
            if (!acceptAnyName) {
                checkOptionString(name);
            }
            if (!acceptAnyName) {
                if (isAutoCompleteMode() && getWordIndex() == autoComplete.getCurrentWordIndex()) {
                    autoComplete.addCandidate(new NutsDefaultArgumentCandidate(name));
                }
            }
            if (hasNext() && get().isOption()) {
                NutsArgument p = get(0);
                switch (expectValue) {
                    case VOID: {
                        if (acceptAnyName || p.getString().equals(name)) {
                            skip();
                            return p;
                        }
                        break;
                    }
                    case STRING: {
                        if (acceptAnyName || p.getName().getString().equals(name)) {
                            if (p.isKeyValue()) {
                                skip();
                                return p;
                            } else {
                                if (isAutoCompleteMode() && getWordIndex() + 1 == autoComplete.getCurrentWordIndex()) {
                                    autoComplete.addCandidate(new NutsDefaultArgumentCandidate("<StringValueFor" + p.getName().getString() + ">"));
                                }
                                NutsArgument r2 = get(1);
                                if (r2 != null && !r2.isOption()) {
                                    skip(2);
                                    return newArgument(p.getString() + eq + r2.getString());
                                }
                            }
                        }
                        break;
                    }
                    case IMMEDIATE_STRING: {
                        if (acceptAnyName || p.getName().getString().equals(name)) {
                            if (p.isKeyValue()) {
                                skip();
                                return p;
                            }
                        }
                        break;
                    }
                    case BOOLEAN: {
                        if (acceptAnyName || p.getName().getString().equals(name)) {
                            if (p.isNegated()) {
                                if (p.isKeyValue()) {
                                    //should not happen
                                    boolean x = p.getBoolean();
                                    skip();
                                    return newArgument(p.getName().getString() + eq + (!x));
                                } else {
                                    skip();
                                    return newArgument(p.getName().getString() + eq + (false));
                                }
                            } else if (p.isKeyValue()) {
                                skip();
                                return p;
                            } else {
                                skip();
                                return newArgument(p.getName().getString() + eq + (true));
                            }
                        }
                        break;
                    }
                    default: {
                        throw new IllegalArgumentException("Unsupported " + expectValue);
                    }
                }
            }
        }
        return null;
    }

    @Override
    public DefaultWorkspaceCommandLine requiredNonOption() {
        if (!hasNext() || !get().isNonOption()) {
            throw new IllegalArgumentException("Expected value");
        }
        return this;
    }

    @Override
    public int skipAll() {
        int count = 0;
        while (hasNext()) {
            count += skip(length());
        }
        return count;
    }

    @Override
    public NutsArgument readNonOption(boolean expectValue, String name) {
        if (isAutoCompleteMode() && getWordIndex() == autoComplete.getCurrentWordIndex()) {
            autoComplete.addCandidate(new NutsDefaultArgumentCandidate(name));
        }
        checkNonOptionString(name);
        if (hasNext() && get().isNonOption()) {
            NutsArgument p = get(0);
            if (expectValue) {
                if (p.isKeyValue()) {
                    skip();
                    return p;
                } else {
                    NutsArgument r2 = get(1);
                    if (r2 != null && !r2.isOption()) {
                        skip(2);
                        return newArgument(p.getString() + eq + r2.getString());
                    }
                }
            } else {
                skip();
                return p;
            }
        }
        return null;
    }

    @Override
    public NutsArgument readRequiredOption(String name) {
        NutsArgument o = readVoidOption(name);
        if (o == null) {
            throw new IllegalArgumentException("Missing argument " + name);
        }
        return o;
    }

    @Override
    public NutsArgument readRequiredNonOption() {
        return readRequiredNonOption(VALUE);
    }

    @Override
    public NutsArgument readRequiredNonOption(NutsArgumentNonOption name) {
        return readNonOption(name, true);
    }

    @Override
    public NutsArgument readNonOption() {
        return readNonOption(VALUE);
    }

    @Override
    public NutsArgument readNonOption(String... names) {
        if (names.length == 0) {
            throw new IllegalArgumentException("Missing non Option Name");
        }
        NutsArgument a = get(0);
        if (a != null) {
            for (String name : names) {
                if (!a.isOption() && a.getString().equals(name)) {
                    skip();
                    return a;
                }
            }
        }
        if (isAutoComplete()) {
            for (String name : names) {
                autoComplete.addCandidate(new NutsDefaultArgumentCandidate(name));
            }
        }
        return null;
    }

    @Override
    public NutsArgument readRequiredNonOption(String name) {
        NutsArgument a = readNonOption(name);
        if (a != null) {
            return a;
        }
        throw new IllegalArgumentException("Expected " + name);
    }

    @Override
    public NutsArgument readNonOption(NutsArgumentNonOption name) {
        return readNonOption(name, false);
    }

    @Override
    public NutsArgument readNonOption(NutsArgumentNonOption name, boolean error) {
        if (hasNext() && !get().isOption()) {
            if (isAutoComplete()) {
                List<NutsArgumentCandidate> values = name.getValues();
                if (values == null || values.isEmpty()) {
                    autoComplete.addExpectedTypedValue(null, name.getName());
                } else {
                    for (NutsArgumentCandidate value : name.getValues()) {
                        autoComplete.addCandidate(value);
                    }
                }
            }
            String r = args.get(0);
            skip();
            return newArgument(r);
        } else {
            if (autoComplete != null) {
                if (isAutoComplete()) {
                    List<NutsArgumentCandidate> values = name.getValues();
                    if (values == null || values.isEmpty()) {
                        autoComplete.addExpectedTypedValue(null, name.getName());
                    } else {
                        for (NutsArgumentCandidate value : name.getValues()) {
                            autoComplete.addCandidate(value);
                        }
                    }
                }
                return newArgument("");
            }
            if (!error) {
                return null;//return new Argument("");
            }
            if (hasNext() && get().isOption()) {
                throw new IllegalArgumentException("Unexpected option " + get(0));
            }
            throw new IllegalArgumentException("Missing argument " + name);
        }
    }

    @Override
    public NutsArgument read() {
        NutsArgument val = get(0);
        skip();
        return val;
    }

    private boolean isAutoCompleteContext() {
        return autoComplete != null;
    }

    private boolean isAutoComplete() {
        if (autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex()) {
            return true;
        }
        return false;
    }

    @Override
    public boolean readAll(boolean acceptDuplicates, String... vals) {
        String[][] vals2 = new String[vals.length][];
        for (int i = 0; i < vals2.length; i++) {
            vals2[i] = _Utils.split(vals[i], " ");
        }
        return readAll(acceptDuplicates, vals2);
    }

    @Override
    public boolean readAll(String... vals) {
        return readAll(true, vals);
    }

    @Override
    public boolean readAllOnce(String... vals) {
        return readAll(false, vals);
    }

    private boolean readAll(boolean acceptDuplicates, String[]... vals) {
        if (autoComplete != null) {
            for (String[] val : vals) {
                if ((acceptDuplicates || !isVisitedSequence(val))) {
                    if (acceptSequence(0, val)) {
                        setVisitedSequence(val);
                        skip(val.length);
                        return true;
                    } else {
                        setVisitedSequence(val);
                        for (int i = 0; i < val.length; i++) {
                            String v = val[i];
                            if (getWordIndex() + i == autoComplete.getCurrentWordIndex()) {
                                autoComplete.addCandidate(new NutsDefaultArgumentCandidate(v));
                            } else if (get(i) == null || !get(i).getString("").equals(v)) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        for (String[] val : vals) {
            if ((acceptDuplicates || !isVisitedSequence(val)) && acceptSequence(0, val)) {
                setVisitedSequence(val);
                skip(val.length);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean acceptSequence(int pos, String... vals) {
        for (int i = 0; i < vals.length; i++) {
            NutsArgument argument = get(pos + i);
            if (argument == null) {
                return false;
            }
            if (!argument.getString("").equals(vals[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsArgument findOption(String option) {
        int index = indexOfOption(option);
        if (index >= 0) {
            return get(index + 1);
        }
        return null;
    }

    @Override
    public NutsArgument get() {
        return get(0);
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
            if (!ensureNext(isExpandSimpleOptions(), true, true)) {
                break;
            }
        }
        if (i < lookahead.size()) {
            return lookahead.get(i);
        }
        return null;
    }

    @Override
    public boolean containOption(String name) {
        return indexOfOption(name) >= 0;
    }

    @Override
    public int indexOfOption(String name) {
        if (name.startsWith("-") || name.startsWith("--")) {
            for (int i = 0; i < length(); i++) {
                if (get(i).getString().equals(name)) {
                    return i;
                }
            }
        } else {
            throw new IllegalArgumentException("Not an option " + name);
        }
        return -1;
    }

    @Override
    public int length() {
        return lookahead.size() + args.size();
    }

    @Override
    public NutsCommandLine requireEmpty() {
        if (!isEmpty()) {
            if (autoComplete != null) {
                removeAll();
                return this;
            }
            throw new IllegalArgumentException("Too Many arguments");
        }
        return this;
    }

    @Override
    public NutsCommandLine unexpectedArgument() {
        if (!isEmpty()) {
            if (autoComplete != null) {
                removeAll();
                return this;
            }
            if (commandName == null) {
                throw new IllegalArgumentException("Unexpected Argument " + get());
            } else {
                throw new IllegalArgumentException(commandName + ": Unexpected Argument " + get());
            }
        }
        return this;
    }

    @Override
    public NutsCommandLine requireNonEmpty() {
        if (isEmpty()) {
            if (autoComplete != null) {
                removeAll();
                return this;
            }
            throw new IllegalArgumentException("Missing Arguments");
        }
        return this;
    }

    @Override
    public boolean isEmpty() {
        return !hasNext();
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

    @Override
    public String toString() {
        return "CommmandLine{"
                + Arrays.asList(toArray())
                + '}';
    }

    @Override
    public int getWordIndex() {
        return wordIndex;
    }

    private boolean isVisitedSequence(String[] aaa) {
        return visitedSequences.contains(flattenSequence(aaa));
    }

    private boolean setVisitedSequence(String[] aaa) {
        return visitedSequences.add(flattenSequence(aaa));
    }

    private String flattenSequence(String[] aaa) {
        StringBuilder sb = new StringBuilder();
        sb.append(aaa[0]);
        for (int i = 1; i < aaa.length; i++) {
            sb.append("\n").append(aaa[i]);
        }
        return sb.toString();
    }

    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    private static void checkOptionString(String s) {
        if (!isOptionString(s)) {
            throw new IllegalArgumentException("Option must start with - but got " + s);
        }
    }

    private static void checkNonOptionString(String s) {
        if (isOptionString(s)) {
            throw new IllegalArgumentException("Option unexpected " + s);
        }
    }

    private static boolean isOptionString(String s) {
        return (s != null && s.startsWith("-"));
    }

    public void reset() {
        visitedSequences.clear();
    }

    public NutsArgument next(boolean required, boolean expandSimpleOptions) {
        if (ensureNext(expandSimpleOptions, false, false)) {
            if (!lookahead.isEmpty()) {
                return lookahead.remove(0);
            }
            String v = args.removeFirst();
            return newArgument(v);
        } else {
            if (required) {
                throw new NoSuchElementException("Missing argument");
            }
            return null;
        }
    }

    public boolean ensureNext(boolean expandSimpleOptions, boolean moveToExpanded, boolean ignoreExisitingExpanded) {
        if (!ignoreExisitingExpanded) {
            if (!lookahead.isEmpty()) {
                return true;
            }
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
                                    lookahead.add(newArgument("-!" + c));
                                } else {
                                    lookahead.add(newArgument("-" + c));
                                }
                                c = null;
                            }
                            negate = true;
                            break;
                        }
                        default: {
                            if (c == eq) {
                                if (c == null) {
                                    if (negate) {
                                        lookahead.add(newArgument("-!" + new String(chars, i, chars.length - i)));
                                        negate = false;
                                    } else {
                                        lookahead.add(newArgument("-" + new String(chars, i, chars.length - i)));
                                    }
                                } else {
                                    if (negate) {
                                        negate = false;
                                        lookahead.add(newArgument("-!" + c + new String(chars, i, chars.length - i)));
                                    } else {
                                        lookahead.add(newArgument("-" + c + new String(chars, i, chars.length - i)));
                                    }
                                }
                                c = null;
                                i = chars.length;
                            } else {
                                if (c != null) {
                                    if (negate) {
                                        lookahead.add(newArgument("-!" + c));
                                        negate = false;
                                    } else {
                                        lookahead.add(newArgument("-" + c));
                                    }
                                }
                                c = chars[i];
                            }
                        }
                    }
                }
                if (c != null) {
                    if (negate) {
                        lookahead.add(newArgument("!" + c));
                        negate = false;
                    } else {
                        lookahead.add(newArgument("" + c));
                    }
                }
                return true;
            }
            lookahead.add(newArgument(v));
            return true;
        }
        return false;
    }

    @Override
    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }

    @Override
    public NutsArgument next() {
        return next(false, expandSimpleOptions);
    }

    @Override
    public NutsArgument peek() {
        NutsArgument a = next();
        if (a != null) {
            pushBack(a);
        }
        return a;
    }

    @Override
    public NutsArgument newArgument(String s) {
        return new NutsDefaultWorkspaceArgument(s);
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
    public NutsCommandLine addSpecialSimpleOption(String option) {
        specialSimpleOptions.add(option);
        return this;
    }

    @Override
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
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
    public NutsCommandLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    @Override
    public String[] getArgs() {
        ArrayList<String> p = new ArrayList<>();
        for (NutsArgument a : lookahead) {
            p.add(a.toString());
        }
        for (String a : args) {
            p.add(a);
        }
        return p.toArray(new String[0]);
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

    /**
     * returns un-parsed arguments and then clears them
     *
     * @return
     */
    @Override
    public String[] removeAll() {
        String[] x = getArgs();
        args.clear();
        lookahead.clear();
        return x;
    }

    @Override
    public NutsArgument next(boolean required) {
        return next(required, expandSimpleOptions);
    }

    @Override
    public NutsArgumentNonOption createNonOption(String type) {
        return createNonOption(type, type);
    }

    @Override
    public NutsArgumentNonOption createNonOption(String type, String label) {
        if (type == null) {
            type = "";
        }
        if (label == null) {
            label = type;
        }
        switch (type) {
            case "packaging": {
                return new ValueNonOption(type, "jar");
            }
            case "extension": {
                return new ExtensionNonOption(type, null);
            }
            case "file": {
                return new FileNonOption(type);
            }
            case "boolean": {
                return new ValueNonOption(type, "true", "false");
            }
            default: {
                return new DefaultNonOption(label);
            }
        }
    }

    @Override
    public NutsCommandLine pushBack(NutsArgument a) {
        if (a == null) {
            throw new NullPointerException();
        }
        lookahead.add(0, a);
        return this;
    }

}
