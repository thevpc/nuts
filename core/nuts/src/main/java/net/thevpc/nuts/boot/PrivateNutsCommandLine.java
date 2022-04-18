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
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

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
 * <li>--!enable=yes : invalid option (no error will be thrown but the result is
 * undefined)</li>
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
 * <li>--!enable=yes : invalid option (no error will be thrown but the result is
 * undefined)</li>
 * </ul>
 *
 * </li>
 *
 * <li>long option arguments : any argument that starts with a '--' </li>
 * </ul>
 * option may start with '!' to switch armed flags expandSimpleOptions : when
 * activated
 *
 * @author thevpc
 * @app.category Internal
 * @since 0.5.5
 */
final class PrivateNutsCommandLine  {

    private static final String NOT_SUPPORTED = "this a minimal implementation of NutsCommandLine used to bootstrap; this method is not supported.";
    private final LinkedList<String> args = new LinkedList<>();
    private final List<PrivateNutsArgument> lookahead = new ArrayList<>();
    private final Set<String> specialSimpleOptions = new HashSet<>();
    //    private NutsCommandAutoComplete autoComplete;
    private final char eq = '=';
    private boolean expandSimpleOptions = false;
    private String commandName;
    private int wordIndex = 0;
    //Constructors

    PrivateNutsCommandLine() {

    }

    PrivateNutsCommandLine(String[] args) {
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
    }

    //End Constructors
    static PrivateNutsArgument createArgument0(String argument, char eq) {
        return new PrivateNutsArgument(argument, eq);
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
                        case '\'':
                        case '"': {
                            throw new NutsBootException(NutsMessage.cstyle("illegal char %s", c));
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
                throw new NutsBootException(NutsMessage.cstyle("expected %s", "'"));
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

    
    public NutsCommandAutoComplete getAutoComplete() {
        return null;
        //AUTOCOMPLETE
//        return autoComplete;
    }

    
    public PrivateNutsCommandLine registerSpecialSimpleOption(String option) {
        int len = option.length();
        switch (len) {
            case 0: {
                break;
            }
            case 1: {
                if (option.equals("-") || option.equals("+")) {
                    specialSimpleOptions.add(option);
                    return this;
                }
                break;
            }
            default: {
                if (
                        (option.charAt(0) == '-' && option.charAt(1) != '-')
                                || (option.charAt(0) == '+' && option.charAt(1) != '+')
                ) {
                    specialSimpleOptions.add(option);
                    return this;
                }
                break;
            }
        }
        throw new NutsBootException(NutsMessage.cstyle("invalid special option %s", option));
    }

    
    public boolean isSpecialSimpleOption(String option) {
        for (String x : specialSimpleOptions) {
            if (option.equals(x) || option.startsWith(x + eq)) {
                return true;
            }
            if (option.startsWith("-//" + x.substring(1))) {
                //disabled option
                return true;
            }
            if (option.startsWith("-!" + x.substring(1))) {
                //unarmed
                return true;
            }
            if (option.startsWith("-~" + x.substring(1))) {
                //unarmed
                return true;
            }
        }
        return false;
    }

    
    public int getWordIndex() {
        return wordIndex;
    }

    
    public boolean isExecMode() {
        return false;
        //AUTOCOMPLETE
//        return autoComplete == null;
    }

    
    public boolean isAutoCompleteMode() {
        return false;
        //AUTOCOMPLETE
//        return autoComplete != null;
    }

    
    public String getCommandName() {
        return commandName;
    }

    
    public PrivateNutsCommandLine setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    
    public PrivateNutsCommandLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    
    public PrivateNutsCommandLine requireNonOption() {
        if (!hasNext() || peek().isOption()) {
            throwError(NutsMessage.formatted("expected value"));
        }
        return this;
    }

    
    public PrivateNutsCommandLine unexpectedArgument(NutsString errorMessage) {
        return unexpectedArgument(NutsMessage.formatted(errorMessage == null ? "" : errorMessage.toString()));
    }

    
    public PrivateNutsCommandLine unexpectedArgument(NutsMessage errorMessage) {
        if (!isEmpty()) {
            List<Object> args = new ArrayList<>();
            String m = "unexpected argument %s";
            args.add(peek());
            if (errorMessage != null && errorMessage.getMessage().trim().length() > 0) {
                m += " , %s";
                args.add(errorMessage);
            }
            throwError(NutsMessage.cstyle(m, args.toArray()));
        }
        return this;
    }

    
    public PrivateNutsCommandLine unexpectedArgument() {
        return unexpectedArgument((NutsMessage) null);
    }

    
    public PrivateNutsCommandLine required() {
        return required((NutsMessage) null);
    }

    
    public PrivateNutsCommandLine required(NutsString errorMessage) {
        return required(NutsMessage.formatted(errorMessage == null ? "" : errorMessage.toString()));
    }

    
    public PrivateNutsCommandLine required(NutsMessage errorMessage) {
        if (isEmpty()) {
            //AUTOCOMPLETE
//            if (autoComplete != null) {
//                skipAll();
//                return this;
//            }
            throwError((errorMessage == null || errorMessage.getMessage().isEmpty()) ? NutsMessage.cstyle("missing arguments") : errorMessage);
        }
        return this;
    }

    
    public PrivateNutsCommandLine pushBack(PrivateNutsArgument arg) {
        if (arg == null) {
            throwError(NutsMessage.cstyle("null argument"));
        }
        lookahead.add(0, arg);
        return this;
    }

    
    public PrivateNutsArgument next() {
        return next(false, expandSimpleOptions);
    }

    
    public PrivateNutsArgument next(NutsArgumentName name) {
        return next(name, false, false);
    }

    
    public PrivateNutsArgument peek() {
        return get(0);
    }

    
    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }

    
    public PrivateNutsArgument nextBoolean(String... names) {
        return next(NutsArgumentType.BOOLEAN, names);
    }

    
    public PrivateNutsArgument nextString(String... names) {
        return next(NutsArgumentType.STRING, names);
    }

    
    public PrivateNutsArgument next(String... names) {
        return next(NutsArgumentType.ANY, names);
    }

    
    public PrivateNutsArgument next(NutsArgumentType expectValue, String... names) {
        if (expectValue == null) {
            expectValue = NutsArgumentType.ANY;
        }
        if (names.length == 0) {
            if (hasNext()) {
                PrivateNutsArgument peeked = peek();
                names = new String[]{
                        peeked.getKey().getString()
                };
            }
        }
        for (String nameSeq : names) {
            String[] nameSeqArray = NutsUtilStrings.split(nameSeq, " ").toArray(new String[0]);
            if (!isPrefixed(nameSeqArray)) {
                continue;
            }
            String name = nameSeqArray[nameSeqArray.length - 1];
            PrivateNutsArgument p = get(nameSeqArray.length - 1);
            if (p != null) {
                if (p.getKey().getString().equals(name)) {
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
//                                        autoComplete.addCandidate(createCandidate("<StringValueFor" + p.getKey().getString() + ">"));
//                                    }
                                }
                                PrivateNutsArgument r2 = peek();
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
                                    boolean x = p.getValue().getBoolean();
                                    return createArgument(p.getKey().getString() + eq + (!x));
                                } else {
                                    return createArgument(p.getKey().getString() + eq + (false));
                                }
                            } else if (p.isKeyValue()) {
                                return p;
                            } else {
                                return createArgument(p.getKey().getString() + eq + (true));
                            }
                        }
                        default: {
                            throwError(NutsMessage.cstyle("unsupported %s", expectValue));
                        }
                    }
                }
            }

        }
        return null;
    }

    
    public PrivateNutsArgument nextRequiredNonOption(NutsArgumentName name) {
        return next(name, true, true);
    }

    
    public PrivateNutsArgument nextNonOption() {
        if (hasNext() && !peek().isOption()) {
            return next();
        }
        return null;
    }

    
    public PrivateNutsArgument nextNonOption(NutsArgumentName name) {
        return next(name, true, false);
    }

    
    public int skipAll() {
        int count = 0;
        while (hasNext()) {
            count += skip(1);
        }
        return count;
    }

    
    public int skip() {
        return skip(1);
    }

    
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

    
    public boolean accept(String... values) {
        return accept(0, values);
    }

    
    public boolean accept(int index, String... values) {
        for (int i = 0; i < values.length; i++) {
            PrivateNutsArgument argument = get(index + i);
            if (argument == null) {
                return false;
            }
            if (!argument.getKey().getString().equals(values[i])) {
                return false;
            }
        }
        return true;
    }

    
    public PrivateNutsArgument find(String name) {
        int index = indexOf(name);
        if (index >= 0) {
            return get(index);
        }
        return null;
    }

    
    public PrivateNutsArgument get(int index) {
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

    
    public boolean contains(String name) {
        return indexOf(name) >= 0;
    }

    
    public int indexOf(String name) {
        int i = 0;
        while (i < length()) {
            if (get(i).getKey().getString().equals(name)) {
                return i;
            }
            i++;
        }
        return -1;
    }

    
    public int length() {
        return lookahead.size() + args.size();
    }

    
    public boolean isEmpty() {
        return !hasNext();
    }

    
    public String[] toStringArray() {
        List<String> all = new ArrayList<>(length());
        for (PrivateNutsArgument nutsArgument : lookahead) {
            all.add(nutsArgument.getString());
        }
        all.addAll(args);
        return all.toArray(new String[0]);
    }

    
    public PrivateNutsArgument[] toArgumentArray() {
        List<PrivateNutsArgument> aa = new ArrayList<>();
        while (hasNext()) {
            aa.add(next());
        }
        lookahead.addAll(aa);
        return aa.toArray(new PrivateNutsArgument[0]);
    }

    
    public boolean isOption(int index) {
        PrivateNutsArgument x = get(index);
        return x != null && x.isOption();
    }

    //    
//    public NutsArgumentName createName(String type) {
//        return createName(type, type);
//    }
    
    public boolean isNonOption(int index) {
        PrivateNutsArgument x = get(index);
        return x != null && !x.isOption();
    }

    
    public PrivateNutsCommandLine setArguments(List<String> arguments) {
        return setArguments(arguments.toArray(new String[0]));
    }

    public PrivateNutsCommandLine setArguments(String... arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            Collections.addAll(this.args, arguments);
        }
        return this;
    }

    public void throwError(NutsMessage message) {
        StringBuilder m = new StringBuilder();
        if (!NutsBlankable.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw new NutsBootException(NutsMessage.plain(m.toString()));
    }

    
    public void throwError(NutsString message) {
        throwError(NutsMessage.formatted(message == null ? "" : message.toString()));
    }

    
    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            PrivateNutsArgument x = get(i);
            if (x == null || !x.getString().equals(nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

    public PrivateNutsArgument next(NutsArgumentName name, boolean forceNonOption, boolean error) {
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
            PrivateNutsArgument r = peek();
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
                throwError(NutsMessage.cstyle("unexpected option %s", peek()));
            }
            throwError(NutsMessage.cstyle("missing argument %s", (name == null ? "value" : name.getName())));
        }
        //ignored
        return null;
    }

    public PrivateNutsArgument next(boolean required, boolean expandSimpleOptions) {
        if (ensureNext(expandSimpleOptions, false)) {
            if (!lookahead.isEmpty()) {
                return lookahead.remove(0);
            }
            String v = args.removeFirst();
            return createArgument(v);
        } else {
            if (required) {
                throwError(NutsMessage.cstyle("missing argument"));
            }
            return null;
        }
    }

    
    public String toString() {
        return escapeArguments(toStringArray());
    }

    private boolean isExpandableOption(String v, boolean expandSimpleOptions) {
        if (!expandSimpleOptions || v.length() <= 2) {
            return false;
        }
        if (isSpecialSimpleOption(v)) {
            return false;
        }
        if (v.charAt(0) == '-') {
            return v.charAt(1) != '-';
        }
        if (v.charAt(0) == '+') {
            return v.charAt(1) != '+';
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
                            lookahead.add(createArgument(createExpandedSimpleOption(start, negate, last)));
                            last = null;
                        }
                        negate = true;
                    } else if (chars[i] == eq) {
                        String nextArg = new String(chars, i, chars.length - i);
                        if (last != null) {
                            nextArg = last + nextArg;
                            last = null;
                        }
                        lookahead.add(createArgument(createExpandedSimpleOption(start, negate, nextArg)));
                        i = chars.length;
                    } else if (isPunctuation(chars[i])) {
                        StringBuilder sb = new StringBuilder();
                        if (last != null) {
                            sb.append(last);
                        }
                        sb.append(chars[i]);
                        while (i + 1 < chars.length) {
                            i++;
                            sb.append(chars[i]);
                        }
                        lookahead.add(createArgument(createExpandedSimpleOption(start, negate, sb.toString())));
                        last = null;
                    } else {
                        if (last != null) {
                            lookahead.add(createArgument(createExpandedSimpleOption(start, negate, last)));
                        }
                        last = chars[i];
                    }
                }
                if (last != null) {
                    lookahead.add(createArgument(createExpandedSimpleOption(start, negate, last)));
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

    //    
    public PrivateNutsArgument createArgument(String argument) {
        return createArgument0(argument, eq);
    }

    private String highlightText(String text) {
        return text;
    }

    private boolean isPunctuation(char c) {
        int t = Character.getType(c);
        return t != Character.LOWERCASE_LETTER
                && t != Character.UPPERCASE_LETTER
                && t != Character.TITLECASE_LETTER;
    }

    public Iterator<PrivateNutsArgument> iterator() {
        return Arrays.asList(toArgumentArray()).iterator();
    }

}
