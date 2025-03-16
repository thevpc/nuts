/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.boot.reserved.cmdline;

import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.reserved.util.NBootMsg;
import net.thevpc.nuts.boot.reserved.util.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * NCmdLine args=new DefaultNCmdLine(Arrays.asList("--!deleteLog","--deploy","/deploy/path","--deploy=/other-deploy/path","some-param"));
 * Argument a;
 * while (args.hasNext()) {
 * if ((a = args.nextBoolean("--deleteLog").orNull()) != null) {
 * deleteLog = a.getBooleanValue().get(session);
 * } else if ((a = args.nextString("--deploy").orNull()) != null) {
 * apps.add(a.getStringValue().get(session));
 * } else if ((a = args.next()) != null) {
 * name = a.getString();
 * } else {
 * args.throwUnexpectedArgument();
 * }
 * }
 * </pre>
 */
public class NBootCmdLine {
    /**
     * argument that may or may not accept value.
     */
    private static final int ARG_TYPE_DEFAULT=0;
    /**
     * argument that accepts a string as value. Either the string is included in
     * the argument itself (--option=value) or succeeds it (--option value).
     */
    private static final int ARG_TYPE_ENTRY=1;
    /**
     * argument that accepts a boolean as value. Either the boolean is not
     * defined (--option), is included in the argument itself (--option=true) or
     * succeeds it (--option true). Parsing boolean is also aware of negated
     * options (--!option) that will be interpreted as (--option=false).
     */
    private static final int ARG_TYPE_FLAG=2;

    protected LinkedList<String> args = new LinkedList<>();
    protected List<NBootArg> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    private int wordIndex = 0;
    private char eq = '=';

    //Constructors
    public NBootCmdLine() {

    }

    public NBootCmdLine(String[] args) {
        setArguments(args);
    }

    public NBootCmdLine(List<String> args) {
        setArguments(args);
    }


    public NBootCmdLine unregisterSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }


    public String[] getSpecialSimpleOptions() {
        return specialSimpleOptions.toArray(new String[0]);
    }


    public NBootCmdLine registerSpecialSimpleOption(String option) {
        if (option.length() > 2) {
            char c0 = option.charAt(0);
            char c1 = option.charAt(1);
            char c2 = option.charAt(2);
            if ((c0 == '-' || c0 == '+') && NBootArg.isSimpleKey(c1) && NBootArg.isSimpleKey(c2)) {
                specialSimpleOptions.add(option);
                return this;
            }
        }
        throwError(NBootMsg.ofC("invalid special option %s", option));
        return this;
    }


    public boolean isSpecialSimpleOption(String option) {
        if (option == null) {
            return false;
        }
        NBootArg a = new NBootArg(option);
        String p = a.getOptionPrefix();
        if (p == null || p.length() != 1) {
            return false;
        }
        String o = a.getKey();
        if (o == null) {
            return false;
        }
        for (String registered : specialSimpleOptions) {
            if (registered.equals(o)) {
                return true;
            }
        }
        return false;
    }


    public int getWordIndex() {
        return wordIndex;
    }


    public String getCommandName() {
        return commandName;
    }


    public NBootCmdLine setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }


    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }


    public NBootCmdLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }


    public NBootCmdLine throwUnexpectedArgument(NBootMsg errorMessage) {
        if (!isEmpty()) {
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("unexpected argument %s");
            ep.add(highlightText(String.valueOf(peek())));
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NBootMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }


    public NBootCmdLine throwMissingArgument() {
        if (isEmpty()) {
            throwError(NBootMsg.ofPlain("missing argument"));
        }
        return this;
    }


    public NBootCmdLine throwMissingArgument(String argumentName) {
        if (NBootUtils.isBlank(argumentName)) {
            throwMissingArgument();
        } else {
            if (isEmpty()) {
                throwError(NBootMsg.ofC("missing argument %s", argumentName));
            }
            return this;
        }
        return this;
    }


    public NBootCmdLine throwMissingArgument(NBootMsg errorMessage) {
        if (isEmpty()) {
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("missing argument");
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NBootMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }


    public NBootCmdLine throwUnexpectedArgument() {
        return throwUnexpectedArgument((NBootMsg) null);
    }


    public NBootCmdLine pushBack(NBootArg arg) {
        NBootUtils.requireNonNull(arg, "argument");
        lookahead.add(0, arg);
        return this;
    }


    public NBootArg next() {
        return next(expandSimpleOptions);
    }


    public String nextString() {
        NBootArg a = next();
        return a == null ? null : a.toString();
    }


    public NBootArg nextOption(String option) {
        if (!new NBootArg(option).isOption()) {
            return errorOptionalCformat("%s is not an option", option);
        }
        return next(option, true);
    }


    public boolean isNextOption() {
        NBootArg a = peek();
        if (a != null) {
            return a.isOption();
        }
        return false;
    }


    public boolean isNextNonOption() {
        NBootArg a = peek();
        if (a != null) {
            return a.isNonOption();
        }
        return false;
    }


    public NBootArg peek() {
        return get(0);
    }


    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }


    public boolean hasNextOption() {
        return hasNext() && peek().isOption();
    }


    public boolean hasNextNonOption() {
        return hasNext() && peek().isNonOption();
    }


    public NBootArg nextFlag(String... names) {
        return next(ARG_TYPE_FLAG, names);
    }


    public NBootArg nextEntry(String... names) {
        return next(ARG_TYPE_ENTRY, names);
    }


    public NBootArg nextEntry() {
        return nextEntry(new String[0]);
    }


    public NBootArg nextFlag() {
        return nextFlag(new String[0]);
    }

    public NBootArg next(String... names) {
        return next(ARG_TYPE_DEFAULT, names);
    }


    private NBootArg next(int expectedValue, String... names) {
        if (names.length == 0) {
            if (hasNext()) {
                NBootArg peeked = peek();
                String string = peeked.getKey();
                if (string != null) {
                    names = new String[]{string};
                } else {
                    names = new String[0];
                }
            }
        }

        for (String nameSeq : names) {
            String[] nameSeqArray = NBootUtils.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length == 0) {
                continue;
            }
            if (!isPrefixed(nameSeqArray)) {
                continue;
            }
            String name = nameSeqArray[nameSeqArray.length - 1];
            NBootArg p = get(nameSeqArray.length - 1);
            if (p != null) {
                String pks = p.getKey();
                if (pks != null && pks.equals(name)) {
                    switch (expectedValue) {
                        case ARG_TYPE_DEFAULT: {
                            skip(nameSeqArray.length);
                            return (p);
                        }
                        case ARG_TYPE_ENTRY: {
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return (p);
                            } else {
                                NBootArg r2 = peek();
                                if (r2 != null && !r2.isOption()) {
                                    skip();
                                    return (createArgument(NBootUtils.<String>firstNonNull(p==null?null:p.toString(),"") + eq + NBootUtils.firstNonNull(r2,"")));
                                } else {
                                    return (p);
                                }
                            }
                        }
                        case ARG_TYPE_FLAG: {
                            skip(nameSeqArray.length);
                            if (p.isNegated()) {
                                if (p.isKeyValue()) {
                                    //should not happen
                                    boolean x = NBootUtils.firstNonNull(p.getBooleanValue(), false);
                                    if (pks != null) {
                                        return (createArgument(pks + eq + (!x)));
                                    }
                                } else {
                                    if (pks != null) {
                                        return (createArgument(pks + eq + (false)));
                                    }
                                }
                            } else if (p.isKeyValue()) {
                                return (p);
                            } else {
                                if (pks != null) {
                                    return (createArgument(pks + eq + (true)));
                                }
                            }
                            break;
                        }
                        default: {
                            return errorOptionalCformat("unsupported %s", highlightText(String.valueOf(expectedValue)));
                        }
                    }
                }
            }

        }
        return emptyOptionalCformat("missing argument");
    }

    private <T> T emptyOptionalCformat(String str, Object... args) {
        List<Object> a = new ArrayList<>();
        if (!NBootUtils.isBlank(getCommandName())) {
            a.add(getCommandName());
            a.addAll(Arrays.asList(args));
            throw new IllegalArgumentException(NBootMsg.ofC("%s : " + str, a.toArray()).toString());
        } else {
            a.addAll(Arrays.asList(args));
        }
        throw new NBootException(NBootMsg.ofC(str, a.toArray()));
    }

    private <T> T errorOptionalCformat(String str, Object... args) {
        if (!NBootUtils.isBlank(getCommandName())) {
            throw new NBootException(NBootMsg.ofC("%s : %s ", getCommandName(), NBootMsg.ofC(str, args)));
        }
        throw new NBootException(NBootMsg.ofC(str, args));
    }


    public NBootArg nextNonOption(String name) {
        return next(name, true);
    }


    public NBootArg nextNonOption() {
        if (hasNext() && !isNextOption()) {
            return next();
        }
        return null;
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
            NBootArg argument = get(index + i);
            if (argument == null) {
                return false;
            }
            if (!Objects.equals(argument.getKey(), values[i])) {
                return false;
            }
        }
        return true;
    }


    public NBootArg find(String name) {
        int index = indexOf(name);
        if (index >= 0) {
            return get(index);
        }
        return null;
    }


    public NBootArg get(int index) {
        if (index < 0) {
            return null;
        }
        if (index < lookahead.size()) {
            return (lookahead.get(index));
        }
        while (!args.isEmpty() && index >= lookahead.size()) {
            if (!ensureNext(isExpandSimpleOptions(), true)) {
                break;
            }
        }
        if (index < lookahead.size()) {
            return (lookahead.get(index));
        }
        return emptyOptionalCformat("missing argument");
    }


    public boolean contains(String name) {
        return indexOf(name) >= 0;
    }


    public int indexOf(String name) {
        int i = 0;
        while (i < length()) {
            NBootArg g = get(i);
            if (g != null && Objects.equals(g.getKey(), name)) {
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
        return toStringList().toArray(new String[0]);
    }


    public List<String> toStringList() {
        List<String> all = new ArrayList<>(length());
        for (NBootArg nutsArgument : lookahead) {
            all.add(nutsArgument.toString());
        }
        all.addAll(args);
        return all;
    }


    public NBootArg[] toArgumentArray() {
        List<NBootArg> aa = new ArrayList<>();
        while (hasNext()) {
            aa.add(next());
        }
        lookahead.addAll(aa);
        return aa.toArray(new NBootArg[0]);
    }


    public boolean isOption(int index) {
        NBootArg a = get(index);
        if (a != null) {
            return a.isOption();
        }
        return false;
    }


    public boolean isNonOption(int index) {
        NBootArg a = get(index);
        if (a != null) {
            return a.isNonOption();
        }
        return false;
    }

    public NBootCmdLine setArguments(List<String> arguments) {
        if (arguments == null) {
            return setArguments(new String[0]);
        }
        return setArguments(arguments.toArray(new String[0]));
    }

    public NBootCmdLine setArguments(String[] arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            Collections.addAll(this.args, arguments);
        }
        return this;
    }


    public void throwError(NBootMsg message) {
        if (NBootUtils.isBlank(commandName)) {
            throw new NBootException(message);
        }
        throw new NBootException(NBootMsg.ofC("%s : %s", commandName, message));
    }

    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            NBootArg x = get(i);
            if (x == null || !Objects.equals(x.toString(), nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

    public NBootArg next(String name, boolean forceNonOption) {
        if (hasNext() && (!forceNonOption || !isNextOption())) {
            NBootArg r = peek();
            skip();
            if (r == null) {
                return emptyOptionalCformat("expected argument");
            }
            return (r);
        } else {
            if (hasNext() && (!forceNonOption || !isNextOption())) {
                return emptyOptionalCformat("unexpected option %s", highlightText(String.valueOf(peek())));
            }
            return emptyOptionalCformat("missing argument %s", highlightText(String.valueOf(name == null ? "value" : name)));
        }
        //ignored
    }

    public NBootArg next(boolean expandSimpleOptions) {
        if (ensureNext(expandSimpleOptions, false)) {
            if (!lookahead.isEmpty()) {
                return (lookahead.remove(0));
            }
            String v = args.removeFirst();
            return (createArgument(v));
        } else {
            return null;
        }
    }


    public String toString() {
        return toStringList().stream().map(x -> NBootUtils.formatStringLiteral(x, NBootQuoteTypeBoot.DOUBLE, NBootSupportMode.PREFERRED)).collect(Collectors.joining(" "));
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
            if (expandSimpleOptions && v.length() > 2 && !isSpecialSimpleOption(v) && ((v.charAt(0) == '-' && v.charAt(1) != '-') || (v.charAt(0) == '+' && v.charAt(1) != '+')) && (v.charAt(1) != '/' || v.charAt(2) == '/')) {
                NBootSimpleCharQueue vv = new NBootSimpleCharQueue(v.toCharArray());
                char start = vv.read();
                char negChar = '\0';
                boolean negate = false;
                if (vv.peek() == '!' || vv.peek() == '~') {
                    negChar = vv.read();
                    negate = true;
                }
                while (vv.hasNext()) {
                    char c = vv.read();
                    StringBuilder cc = new StringBuilder();
                    cc.append(start);
                    if (negate) {
                        cc.append(negChar);
                    }
                    cc.append(c);
                    if (NBootArg.isSimpleKey(c)) {
                        while (vv.hasNext() && (vv.peek() != eq && !NBootArg.isSimpleKey(vv.peek()))) {
                            cc.append(vv.read());
                        }
                        if (vv.hasNext() && vv.peek() == eq) {
                            while (vv.hasNext()) {
                                cc.append(vv.read());
                            }
                            lookahead.add(createArgument(cc.toString()));
                        } else {
                            lookahead.add(createArgument(cc.toString()));
                        }
                    } else {
                        while (vv.hasNext()) {
                            cc.append(vv.read());
                        }
                        lookahead.add(createArgument(cc.toString()));
                    }
                }
            } else {
                lookahead.add(createArgument(v));
            }
            return true;
        }
        return false;
    }

    private NBootArg createArgument(String v) {
        return new NBootArg(v, eq);
    }


    public NBootCmdLine copy() {
        NBootCmdLine c = new NBootCmdLine(toStringArray());
        c.eq = this.eq;
        c.commandName = this.commandName;
        return c;
    }

    private NBootMsg highlightText(String text) {
        return NBootMsg.ofC(text);
    }

    private boolean isPunctuation(char c) {
        switch (Character.getType(c)) {
            case Character.DASH_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.SPACE_SEPARATOR:
            case Character.START_PUNCTUATION:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.MODIFIER_SYMBOL:
            case Character.CONTROL:
                return true;
        }
        return false;
    }


    public Iterator<NBootArg> iterator() {
        return Arrays.asList(toArgumentArray()).iterator();
    }


    public static String[] parseDefaultList(String commandLineString) {
        if (commandLineString == null) {
            return (new String[0]);
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
                        case ' ':
                        case '\t': {
                            //ignore
                            break;
                        }
                        case '\r':
                        case '\n': //support multiline commands
                        {
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
                            throw new NBootException(NBootMsg.ofC("illegal char %s", c));
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
                            i = readEscapedArg(charArray, i + 1, sb);
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
                throw new NBootException(NBootMsg.ofPlain("expected quote"));
            }
        }
        return (args.toArray(new String[0]));
    }

    private static int readEscapedArg(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case '\\':
            case ';':
            case '\"':
            case '\'':
            case '$':
            case ' ':
            case '<':
            case '>':
            case '(':
            case ')':
            case '~':
            case '&':
            case '|': {
                sb.append(c);
                break;
            }
            default: {
                sb.append('\\').append(c);
                break;
            }
        }
        return i;
    }


    public NBootCmdLine add(String argument) {
        if (argument != null) {
            args.add(argument);
        }
        return this;
    }


    public NBootCmdLine addAll(List<String> arguments) {
        if (arguments != null) {
            for (String argument : arguments) {
                add(argument);
            }
        }
        return this;
    }


    public boolean isBlank() {
        return isEmpty();
    }


    public NBootCmdLine pushBack(NBootArg... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    public NBootCmdLine pushBack(String... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).map(x -> new NBootArg(x == null ? "" : x)).collect(Collectors.toList()));
        }
        return this;
    }

    public NBootCmdLine append(String... args) {
        if (args != null) {
            this.args.addAll(Arrays.stream(args).map(x -> x == null ? "" : x).collect(Collectors.toList()));
        }
        return this;
    }


}
