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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

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
public class DefaultNutsCommand implements NutsCommandLine {

    public static final NutsArgumentName VALUE = new DefaultNonOption("value");

    protected LinkedList<String> args = new LinkedList<>();
    protected List<NutsArgument> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = false;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    protected NutsWorkspace ws;
    private int wordIndex = 0;
    private NutsCommandAutoComplete autoComplete;
    private char eq = '=';

    //Constructors
    public DefaultNutsCommand(NutsWorkspace ws, NutsCommandLineContext context) {
        this.ws = ws;
        setArgs(context.getArguments());
        setAutoComplete(context.getAutoComplete());
    }

    public DefaultNutsCommand(NutsWorkspace ws, String[] args, NutsCommandAutoComplete autoComplete) {
        this.ws = ws;
        setArgs(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNutsCommand(NutsWorkspace ws, String[] args) {
        this.ws = ws;
        setArgs(args);
    }

    public DefaultNutsCommand(NutsWorkspace ws, List<String> args, NutsCommandAutoComplete autoComplete) {
        this.ws = ws;
        setArgs(args);
        setAutoComplete(autoComplete);
    }

    public DefaultNutsCommand(List<String> args) {
        setArgs(args);
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

    public void setArgs(List<String> args) {
        setArgs(args.toArray(new String[0]));
    }

    public void setArgs(String[] args) {
        this.lookahead.clear();
        this.args.clear();
        Collections.addAll(this.args, args);
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
                        autoComplete.addCandidate(new NutsDefaultArgumentCandidate(nameSeqArray[i]));
                    }
                }
            }
            for (int i = 0; i < nameSeqArray.length - 1; i++) {
                NutsArgument x = get(i);
                if (x == null || !x.getString().equals(nameSeqArray[i])) {
                    return null;
                }
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
            String r = args.get(0);
            skip();
            return newArgument(r);
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
        return new DefaultNutsArgument(s, eq);
    }

    @Override
    public NutsArgumentName createName(String type, String label) {
        if (type == null) {
            type = "";
        }
        if (label == null) {
            label = type;
        }
        switch (type) {
            case "arch": {
                return new ArchitectureNonOption(label, ws);
            }
            case "packaging": {
                return new PackagingNonOption(label, ws);
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
            case "repository": {
                return new RepositoryNonOption(label, ws);
            }
            case "repository-type": {
                return new RepositoryTypeNonOption(label, ws);
            }
            case "right": {
                return new RightNonOption(label, ws, null, null, false);
            }
            case "user": {
                return new UserNonOption(label, ws);
            }
            case "group": {
                return new GroupNonOption(label, ws);
            }
            default: {
                return new DefaultNonOption(label);
            }
        }
    }

    public NutsCommandLine copy() {
        DefaultNutsCommand c = new DefaultNutsCommand(ws, toArray(), autoComplete);
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
