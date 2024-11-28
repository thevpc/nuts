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
import net.thevpc.nuts.boot.reserved.NAssertBoot;
import net.thevpc.nuts.boot.reserved.NMsgBoot;
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
public class NCmdLineBoot {
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
    protected List<NArgBoot> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    private int wordIndex = 0;
    private char eq = '=';

    //Constructors
    public NCmdLineBoot() {

    }

    public NCmdLineBoot(String[] args) {
        setArguments(args);
    }

    public NCmdLineBoot(List<String> args) {
        setArguments(args);
    }


    public NCmdLineBoot unregisterSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }


    public String[] getSpecialSimpleOptions() {
        return specialSimpleOptions.toArray(new String[0]);
    }


    public NCmdLineBoot registerSpecialSimpleOption(String option) {
        if (option.length() > 2) {
            char c0 = option.charAt(0);
            char c1 = option.charAt(1);
            char c2 = option.charAt(2);
            if ((c0 == '-' || c0 == '+') && NArgBoot.isSimpleKey(c1) && NArgBoot.isSimpleKey(c2)) {
                specialSimpleOptions.add(option);
                return this;
            }
        }
        throwError(NMsgBoot.ofC("invalid special option %s", option));
        return this;
    }


    public boolean isSpecialSimpleOption(String option) {
        if (option == null) {
            return false;
        }
        NArgBoot a = new NArgBoot(option);
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


    public NCmdLineBoot setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }


    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }


    public NCmdLineBoot setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }


    public NCmdLineBoot throwUnexpectedArgument(NMsgBoot errorMessage) {
        if (!isEmpty()) {
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("unexpected argument %s");
            ep.add(highlightText(String.valueOf(peek())));
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NMsgBoot.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }


    public NCmdLineBoot throwMissingArgument() {
        if (isEmpty()) {
            throwError(NMsgBoot.ofPlain("missing argument"));
        }
        return this;
    }


    public NCmdLineBoot throwMissingArgument(String argumentName) {
        if (NStringUtilsBoot.isBlank(argumentName)) {
            throwMissingArgument();
        } else {
            if (isEmpty()) {
                throwError(NMsgBoot.ofC("missing argument %s", argumentName));
            }
            return this;
        }
        return this;
    }


    public NCmdLineBoot throwMissingArgument(NMsgBoot errorMessage) {
        if (isEmpty()) {
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("missing argument");
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NMsgBoot.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }


    public NCmdLineBoot throwUnexpectedArgument() {
        return throwUnexpectedArgument((NMsgBoot) null);
    }


    public NCmdLineBoot pushBack(NArgBoot arg) {
        NAssertBoot.requireNonNull(arg, "argument");
        lookahead.add(0, arg);
        return this;
    }


    public NArgBoot next() {
        return next(expandSimpleOptions);
    }


    public String nextString() {
        NArgBoot a = next();
        return a == null ? null : a.toString();
    }


    public NArgBoot nextOption(String option) {
        if (!new NArgBoot(option).isOption()) {
            return errorOptionalCformat("%s is not an option", option);
        }
        return next(option, true);
    }


    public boolean isNextOption() {
        NArgBoot a = peek();
        if (a != null) {
            return a.isOption();
        }
        return false;
    }


    public boolean isNextNonOption() {
        NArgBoot a = peek();
        if (a != null) {
            return a.isNonOption();
        }
        return false;
    }


    public NArgBoot peek() {
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


    public NArgBoot nextFlag(String... names) {
        return next(ARG_TYPE_FLAG, names);
    }


    public NArgBoot nextEntry(String... names) {
        return next(ARG_TYPE_ENTRY, names);
    }


    public NArgBoot nextEntry() {
        return nextEntry(new String[0]);
    }


    public NArgBoot nextFlag() {
        return nextFlag(new String[0]);
    }

    public NArgBoot next(String... names) {
        return next(ARG_TYPE_DEFAULT, names);
    }


    private NArgBoot next(int expectedValue, String... names) {
        if (names.length == 0) {
            if (hasNext()) {
                NArgBoot peeked = peek();
                String string = peeked.getKey();
                if (string != null) {
                    names = new String[]{string};
                } else {
                    names = new String[0];
                }
            }
        }

        for (String nameSeq : names) {
            String[] nameSeqArray = NStringUtilsBoot.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length == 0) {
                continue;
            }
            if (!isPrefixed(nameSeqArray)) {
                continue;
            }
            String name = nameSeqArray[nameSeqArray.length - 1];
            NArgBoot p = get(nameSeqArray.length - 1);
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
                                NArgBoot r2 = peek();
                                if (r2 != null && !r2.isOption()) {
                                    skip();
                                    return (createArgument(NUtilsBoot.<String>firstNonNull(p==null?null:p.toString(),"") + eq + NUtilsBoot.firstNonNull(r2,"")));
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
                                    boolean x = NUtilsBoot.firstNonNull(p.getBooleanValue(), false);
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
        if (!NStringUtilsBoot.isBlank(getCommandName())) {
            a.add(getCommandName());
            a.addAll(Arrays.asList(args));
            throw new IllegalArgumentException(NMsgBoot.ofC("%s : " + str, a.toArray()).toString());
        } else {
            a.addAll(Arrays.asList(args));
        }
        throw new NBootException(NMsgBoot.ofC(str, a.toArray()));
    }

    private <T> T errorOptionalCformat(String str, Object... args) {
        if (!NStringUtilsBoot.isBlank(getCommandName())) {
            throw new NBootException(NMsgBoot.ofC("%s : %s ", getCommandName(), NMsgBoot.ofC(str, args)));
        }
        throw new NBootException(NMsgBoot.ofC(str, args));
    }


    public NArgBoot nextNonOption(String name) {
        return next(name, true);
    }


    public NArgBoot nextNonOption() {
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
            NArgBoot argument = get(index + i);
            if (argument == null) {
                return false;
            }
            if (!Objects.equals(argument.getKey(), values[i])) {
                return false;
            }
        }
        return true;
    }


    public NArgBoot find(String name) {
        int index = indexOf(name);
        if (index >= 0) {
            return get(index);
        }
        return null;
    }


    public NArgBoot get(int index) {
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
            NArgBoot g = get(i);
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
        for (NArgBoot nutsArgument : lookahead) {
            all.add(nutsArgument.toString());
        }
        all.addAll(args);
        return all;
    }


    public NArgBoot[] toArgumentArray() {
        List<NArgBoot> aa = new ArrayList<>();
        while (hasNext()) {
            aa.add(next());
        }
        lookahead.addAll(aa);
        return aa.toArray(new NArgBoot[0]);
    }


    public boolean isOption(int index) {
        NArgBoot a = get(index);
        if (a != null) {
            return a.isOption();
        }
        return false;
    }


    public boolean isNonOption(int index) {
        NArgBoot a = get(index);
        if (a != null) {
            return a.isNonOption();
        }
        return false;
    }

    public NCmdLineBoot setArguments(List<String> arguments) {
        if (arguments == null) {
            return setArguments(new String[0]);
        }
        return setArguments(arguments.toArray(new String[0]));
    }

    public NCmdLineBoot setArguments(String[] arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            Collections.addAll(this.args, arguments);
        }
        return this;
    }


    public void throwError(NMsgBoot message) {
        if (NStringUtilsBoot.isBlank(commandName)) {
            throw new NBootException(message);
        }
        throw new NBootException(NMsgBoot.ofC("%s : %s", commandName, message));
    }

    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            NArgBoot x = get(i);
            if (x == null || !Objects.equals(x.toString(), nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

    public NArgBoot next(String name, boolean forceNonOption) {
        if (hasNext() && (!forceNonOption || !isNextOption())) {
            NArgBoot r = peek();
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

    public NArgBoot next(boolean expandSimpleOptions) {
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
        return toStringList().stream().map(x -> NStringUtilsBoot.formatStringLiteral(x, NQuoteTypeBoot.DOUBLE, NSupportModeBoot.PREFERRED)).collect(Collectors.joining(" "));
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
                NReservedSimpleCharQueueBoot vv = new NReservedSimpleCharQueueBoot(v.toCharArray());
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
                    if (NArgBoot.isSimpleKey(c)) {
                        while (vv.hasNext() && (vv.peek() != eq && !NArgBoot.isSimpleKey(vv.peek()))) {
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

    private NArgBoot createArgument(String v) {
        return new NArgBoot(v, eq);
    }


    public NCmdLineBoot copy() {
        NCmdLineBoot c = new NCmdLineBoot(toStringArray());
        c.eq = this.eq;
        c.commandName = this.commandName;
        return c;
    }

    private NMsgBoot highlightText(String text) {
        return NMsgBoot.ofC(text);
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


    public Iterator<NArgBoot> iterator() {
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
                            throw new NBootException(NMsgBoot.ofC("illegal char %s", c));
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
                throw new NBootException(NMsgBoot.ofPlain("expected quote"));
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


    public NCmdLineBoot add(String argument) {
        if (argument != null) {
            args.add(argument);
        }
        return this;
    }


    public NCmdLineBoot addAll(List<String> arguments) {
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


    public NCmdLineBoot pushBack(NArgBoot... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    public NCmdLineBoot pushBack(String... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).map(x -> new NArgBoot(x == null ? "" : x)).collect(Collectors.toList()));
        }
        return this;
    }

    public NCmdLineBoot append(String... args) {
        if (args != null) {
            this.args.addAll(Arrays.stream(args).map(x -> x == null ? "" : x).collect(Collectors.toList()));
        }
        return this;
    }


}
