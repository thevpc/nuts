/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
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
 * <li>--!enable=yes : invalid option (no error will be thrown but the result
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
 * <li>--!enable=yes : invalid option (no error will be thrown but the result
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
 * @category Internal
 */
final class PrivateNutsCommandLine implements NutsCommandLine {

    private static final String NOT_SUPPORTED = "This a minimal implementation of NutsCommand used to bootstrap. This Method is not supported.";
    private final LinkedList<String> args = new LinkedList<>();
    private final List<NutsArgument> lookahead = new ArrayList<>();
    private boolean expandSimpleOptions = false;
    private final Set<String> specialSimpleOptions = new HashSet<>();
    private String commandName;
    private int wordIndex = 0;
    //    private NutsCommandAutoComplete autoComplete;
    private final char eq = '=';
    //Constructors

    PrivateNutsCommandLine() {

    }

    PrivateNutsCommandLine(String[] args) {
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
    }

    //End Constructors

    @Override
    public NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete) {
        throw new UnsupportedOperationException(NOT_SUPPORTED);
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
    public NutsCommandLine requireNonOption() {
        if (!hasNext() || !peek().isNonOption()) {
            throwError("Expected value");
        }
        return this;
    }

    @Override
    public NutsCommandLine unexpectedArgument(String errorMessage) {
        if (!isEmpty()) {
            //AUTOCOMPLETE
//            if (autoComplete != null) {
//                skipAll();
//                return this;
//            }
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
            //AUTOCOMPLETE
//            if (autoComplete != null) {
//                skipAll();
//                return this;
//            }
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
        return setArguments(arguments.toArray(new String[0]));
    }

    public NutsCommandLine setArguments(String... arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            Collections.addAll(this.args, arguments);
        }
        return this;
    }

    @Override
    public NutsCommandLine parseLine(String commandLine) {
        throw new NutsException(null, "Unsupported");
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
            String[] nameSeqArray = PrivateNutsUtils.split(nameSeq, " ").toArray(new String[0]);
            if (isAutoCompleteMode()) {
                //AUTOCOMPLETE
//                for (int i = 0; i < nameSeqArray.length; i++) {
//                    if (getWordIndex() == autoComplete.getCurrentWordIndex() + i) {
//                        autoComplete.addCandidate(createCandidate(nameSeqArray[i]));
//                    }
//                }
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
                                if (isAutoCompleteMode()) {
                                    //AUTOCOMPLETE
//                                    if(getWordIndex() + 1 == autoComplete.getCurrentWordIndex()) {
//                                        autoComplete.addCandidate(createCandidate("<StringValueFor" + p.getStringKey() + ">"));
//                                    }
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
                //AUTOCOMPLETE
//                List<NutsArgumentCandidate> values = name == null ? null : name.getCandidates();
//                if (values == null || values.isEmpty()) {
//                    autoComplete.addCandidate(createCandidate(name == null ? "<value>" : name.getName()));
//                } else {
//                    for (NutsArgumentCandidate value : values) {
//                        autoComplete.addCandidate(value);
//                    }
//                }
            }
            NutsArgument r = peek();
            skip();
            return r;
        } else {
            if (isAutoComplete()) {
                if (isAutoComplete()) {
                    //AUTOCOMPLETE
//                    List<NutsArgumentCandidate> values = name == null ? null : name.getCandidates();
//                    if (values == null || values.isEmpty()) {
//                        autoComplete.addCandidate(createCandidate(name == null ? "<value>" : name.getName()));
//                    } else {
//                        for (NutsArgumentCandidate value : values) {
//                            autoComplete.addCandidate(value);
//                        }
//                    }
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
        return false;
        //AUTOCOMPLETE
//        return autoComplete == null;
    }

    @Override
    public boolean isAutoCompleteMode() {
        return false;
        //AUTOCOMPLETE
//        return autoComplete != null;
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

//    @Override
//    public NutsArgumentName createName(String type) {
//        return createName(type, type);
//    }

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
        return escapeArguments(toArray());
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return null;
        //AUTOCOMPLETE
//        return autoComplete;
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
                    if (c == '!' || c == '~') {
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

    private boolean isAutoComplete() {
        return false;
        //AUTOCOMPLETE
//        return autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex();
    }

    //    @Override
    public NutsArgument createArgument(String argument) {
        return createArgument0(argument, eq);
    }

    static NutsArgument createArgument0(String argument, char eq) {
        return new ArgumentImpl(argument, eq);
    }

//    @Override
//    public NutsArgumentName createName(String type, String label) {
//        throw new UnsupportedOperationException(NOT_SUPPORTED);
//    }

    protected void throwError(String message) {
        StringBuilder m = new StringBuilder();
        if (!PrivateNutsUtils.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw new NutsIllegalArgumentException(null, m.toString());
    }

    private String highlightText(String text) {
        return text;
    }

    public static String[] parseCommandLineArray(String commandLineString) {
        if (commandLineString == null) {
            return new String[0];
        }
        List<String> args = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        final int START = 0;
        final int IN_WORD = 1;
        final int IN_QUOTED_WORD = 2;
        final int IN_DBQUOTED_WORD = 3;
        int status = START;
        char[] charArray = commandLineString.toCharArray();
        for (int i = 0; i < charArray.length; i++) {
            char c = charArray[i];
            switch (status) {
                case START: {
                    switch (c) {
                        case ' ': {
                            //ignore
                            break;
                        }
                        case '\'': {
                            status = IN_QUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '"': {
                            status = IN_DBQUOTED_WORD;
                            //ignore
                            break;
                        }
                        case '\\': {
                            status = IN_WORD;
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            status = IN_WORD;
                            break;
                        }
                    }
                    break;
                }
                case IN_WORD: {
                    switch (c) {
                        case ' ': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            break;
                        }
                        case '\'': {
                            throw new NutsParseException(null, "Illegal char " + c);
                        }
                        case '"': {
                            throw new NutsParseException(null, "Illegal char " + c);
                        }
                        case '\\': {
                            i++;
                            sb.append(charArray[i]);
                            break;
                        }
                        default: {
                            sb.append(c);
                            break;
                        }
                    }
                    break;
                }
                case IN_QUOTED_WORD: {
                    switch (c) {
                        case '\'': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscapedArgument(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                    break;
                }
                case IN_DBQUOTED_WORD: {
                    switch (c) {
                        case '"': {
                            args.add(sb.toString());
                            sb.delete(0, sb.length());
                            status = START;
                            //ignore
                            break;
                        }
                        case '\\': {
                            i = readEscapedArgument(charArray, i + 1, sb);
                            //ignore
                            break;
                        }
                        default: {
                            sb.append(c);
                            //ignore
                            break;
                        }
                    }
                }
            }
        }
        switch (status) {
            case START: {
                break;
            }
            case IN_WORD: {
                args.add(sb.toString());
                sb.delete(0, sb.length());
                break;
            }
            case IN_QUOTED_WORD: {
                throw new NutsParseException(null, "Expected '");
            }
        }
        return args.toArray(new String[0]);
    }

    public static String escapeArguments(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(escapeArgument(arg));
        }
        return sb.toString();
    }

    public static String escapeArgument(String arg) {
        StringBuilder sb = new StringBuilder();
        if (arg != null) {
            for (char c : arg.toCharArray()) {
                switch (c) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case '\'':
                        sb.append("\\'");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\r':
                        sb.append("\\r");
                    case '\f':
                        sb.append("\\f");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }
        return sb.toString();
    }

    public static int readEscapedArgument(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case 'n': {
                sb.append('\n');
                break;
            }
            case 't': {
                sb.append('\t');
                break;
            }
            case 'r': {
                sb.append('\r');
                break;
            }
            case 'f': {
                sb.append('\f');
                break;
            }
            default: {
                sb.append(c);
            }
        }
        return i;
    }

    @Override
    public void process(NutsConfigurable defaultConfigurable, NutsCommandLineProcessor processor) {
        throw new UnsupportedOperationException("Not supported operation process(...)");
    }
    

//    @Override
//    public NutsArgumentCandidate createCandidate(String value, String label) {
//        throw new UnsupportedOperationException(NOT_SUPPORTED);
//        //AUTOCOMPLETE
////        return new CandidateImpl(value,label);
//    }

//    /**
//     * Default (simple) NutsArgumentCandidate implementation.
//     * @author vpc
//     * @since 0.5.5
//     */
//    private static final class CandidateImpl implements NutsArgumentCandidate {
//
//        private final String value;
//        private final String display;
//
//        public CandidateImpl(String value, String display) {
//            this.value = value;
//            this.display = display;
//        }
//
//        @Override
//        public String getDisplay() {
//            return display;
//        }
//
//        @Override
//        public String getValue() {
//            return value;
//        }
//    }

    /**
     * This is a minimal implementation of NutsArgument and hence should not be
     * used. Instead an instance of NutsArgument can be retrieved using
     * {@link NutsCommandLineFormat#createArgument(String)}
     *
     * @author vpc
     * @since 0.5.5
     */
    private final static class ArgumentImpl extends PrivateNutsTokenFilter implements NutsArgument {

        /**
         * equal character
         */
        private final char eq;

        /**
         * Constructor
         *
         * @param expression expression
         * @param eq         equals
         */
        public ArgumentImpl(String expression, char eq) {
            super(expression);
            this.eq = eq;
        }

        @Override
        public String getStringOptionPrefix() {
            throw new NutsException(null, "Unsupported");
        }

        @Override
        public String getKeyValueSeparator() {
            return String.valueOf(eq);
        }

        @Override
        public NutsArgument getArgumentOptionName() {
            throw new NutsException(null, "Unsupported");
        }

        @Override
        public String getStringOptionName() {
            throw new NutsException(null, "Unsupported");
        }

        /**
         * true if the expression is a an option (starts with '-' or '+')
         * but cannot not be evaluated.
         *
         * @return true if option is not evaluable argument.
         */
        public boolean isUnsupported() {
            return expression != null
                    && (expression.startsWith("-!!")
                    || expression.startsWith("--!!")
                    || expression.startsWith("---")
                    || expression.startsWith("++")
                    || expression.startsWith("!!"));
        }

        /**
         * true if expression starts with '-' or '+'
         *
         * @return true if expression starts with '-' or '+'
         */
        @Override
        public boolean isOption() {
            return expression != null
                    && expression.length() > 0
                    && (expression.charAt(0) == '-' || expression.charAt(0) == '+');
        }

        @Override
        public boolean isNonOption() {
            return !isOption();
        }

        @Override
        public boolean isKeyValue() {
            return expression != null && expression.indexOf(eq) >= 0;
        }

        @Override
        public NutsArgument getArgumentKey() {
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
                        return PrivateNutsCommandLine.createArgument0(sb.toString(), eq);
                    }
                    case '/': {
                        if (sb.length() > 0 && i + 1 < p.length() && p.charAt(i + 1) == '/') {
                            sb.append(p.substring(i + 2));
                            return PrivateNutsCommandLine.createArgument0(sb.toString(), eq);
                        }
                    }
                    default: {
                        return PrivateNutsCommandLine.createArgument0(p, eq);
                    }
                }
                i++;
            }
            return PrivateNutsCommandLine.createArgument0(p, eq);
        }

        @Override
        public NutsArgument getArgumentValue() {
            if (expression == null) {
                return this;
            }
            int x = expression.indexOf(eq);
            if (x >= 0) {
                return PrivateNutsCommandLine.createArgument0(expression.substring(x + 1), eq);
            }
            return PrivateNutsCommandLine.createArgument0(null, eq);
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
                    case '+': {
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
                    case '+': {
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
            if (PrivateNutsUtils.isBlank(expression)) {
                throw new NumberFormatException("Missing value");
            }
            return Integer.parseInt(expression);
        }

        @Override
        public int getInt(int defaultValue) {
            if (PrivateNutsUtils.isBlank(expression)) {
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
            if (PrivateNutsUtils.isBlank(expression)) {
                throw new NumberFormatException("Missing value");
            }
            return Long.parseLong(expression);
        }

        @Override
        public long getLong(long defaultValue) {
            if (PrivateNutsUtils.isBlank(expression)) {
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
            if (PrivateNutsUtils.isBlank(expression)) {
                throw new NumberFormatException("Missing value");
            }
            return Double.parseDouble(expression);
        }

        @Override
        public double getDouble(double defaultValue) {
            if (PrivateNutsUtils.isBlank(expression)) {
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
            Boolean b = PrivateNutsUtils.parseBoolean(expression, false);
            if (isNegated()) {
                return !b;
            }
            return b;
        }

        @Override
        public boolean isBoolean() {
            return PrivateNutsUtils.parseBoolean(expression, null) != null;
        }

        @Override
        public Boolean getBoolean(Boolean defaultValue) {
            return PrivateNutsUtils.parseBoolean(expression, defaultValue);
        }

        @Override
        public String toString() {
            return String.valueOf(expression);
        }

        @Override
        public NutsArgument required() {
            if (expression == null) {
                throw new NoSuchElementException("Missing value");
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
        public String getStringValue(String defaultValue) {
            return getArgumentValue().getString(defaultValue);
        }
    }

}
