/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting
 * a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.NExceptions;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.reserved.util.NReservedSimpleCharQueue;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
public class DefaultNCmdLine implements NCmdLine {

    protected LinkedList<String> args = new LinkedList<>();
    protected List<NArg> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = true;
    protected boolean expandArgumentsFile = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    private int wordIndex = 0;
    private NCmdLineAutoComplete autoComplete;
    private char eq = '=';
    private NShellFamily shellFamily = NShellFamily.BASH;

    private Object source;

    private boolean unsafe;

    /**
     * configurable or null
     *
     * @return configurable or null
     */
    private NCmdLineConfigurable configurable;

    //Constructors
    public DefaultNCmdLine() {

    }

    public DefaultNCmdLine(String[] args, NShellFamily shellFamily) {
        this.shellFamily = shellFamily == null ? NShellFamily.getCurrent() : NShellFamily.BASH;
        setArguments(args);
    }

    public DefaultNCmdLine(List<String> args) {
        setArguments(args);
    }

    public NShellFamily getShellFamily() {
        return shellFamily;
    }

    public NCmdLine setShellFamily(NShellFamily shellFamily) {
        this.shellFamily = shellFamily;
        return this;
    }


    @Override
    public Object getSource() {
        return source;
    }

    @Override
    public NCmdLine setSource(Object source) {
        this.source = source;
        return this;
    }

    @Override
    public boolean isUnsafe() {
        return unsafe;
    }

    @Override
    public NCmdLine setUnsafe(boolean safe) {
        this.unsafe = safe;
        return this;
    }

    @Override
    public NCmdLineConfigurable getConfigurable() {
        return configurable;
    }

    public NCmdLine setConfigurable(NCmdLineConfigurable configurable) {
        this.configurable = configurable;
        return this;
    }

    @Override
    public boolean isExpandArgumentsFile() {
        return expandArgumentsFile;
    }

    @Override
    public NCmdLine setExpandArgumentsFile(boolean expandArgumentsFile) {
        this.expandArgumentsFile = expandArgumentsFile;
        return this;
    }

    //End Constructors
    @Override
    public NCmdLineAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public NCmdLine setAutoComplete(NCmdLineAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    @Override
    public NCmdLine unregisterSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }

    @Override
    public String[] getSpecialSimpleOptions() {
        return specialSimpleOptions.toArray(new String[0]);
    }

    @Override
    public NCmdLine registerSpecialSimpleOption(String option) {
        if (option.length() > 2) {
            char c0 = option.charAt(0);
            char c1 = option.charAt(1);
            char c2 = option.charAt(2);
            if ((c0 == '-' || c0 == '+') && DefaultNArg.isSimpleKey(c1) && DefaultNArg.isSimpleKey(c2)) {
                specialSimpleOptions.add(option);
                return this;
            }
        }
        throwError(NMsg.ofC("invalid special option %s", option));
        return this;
    }

    @Override
    public boolean isSpecialSimpleOption(String option) {
        if (option == null) {
            return false;
        }
        DefaultNArg a = new DefaultNArg(option, this);
        String p = a.getOptionPrefix().asString().orNull();
        if (p == null || p.length() != 1) {
            return false;
        }
        String o = a.getKey().asString().orNull();
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
    public String getCommandName() {
        return commandName;
    }

    @Override
    public NCmdLine setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    @Override
    public NCmdLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    @Override
    public NCmdLine throwUnexpectedArgument(NText errorMessage) {
        return throwUnexpectedArgument(NMsg.ofC("%s", errorMessage));
    }

    @Override
    public NCmdLine throwUnexpectedArgument(NMsg errorMessage) {
        if (!isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("unexpected argument %s");
            ep.add(highlightText(String.valueOf(peek().orNull())));
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }

    @Override
    public NCmdLine throwMissingArgument() {
        if (isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            throwError(NMsg.ofPlain("missing argument"));
        }
        return this;
    }

    @Override
    public NCmdLine throwMissingArgument(String argumentName) {
        if (NBlankable.isBlank(argumentName)) {
            throwMissingArgument();
        } else {
            if (isEmpty()) {
                if (autoComplete != null) {
                    skipAll();
                    return this;
                }
                throwError(NMsg.ofC("missing argument %s", NMsg.ofStyledKeyword(argumentName)));
            }
            return this;
        }
        return this;
    }

    @Override
    public NCmdLine throwMissingArgument(NMsg errorMessage) {
        if (isEmpty()) {
            if (autoComplete != null) {
                skipAll();
                return this;
            }
            StringBuilder sb = new StringBuilder();
            List<Object> ep = new ArrayList<>();
            sb.append("missing argument");
            if (errorMessage != null) {
                sb.append(", %s");
                ep.add(errorMessage);
            }
            throwError(NMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }

    @Override
    public NCmdLine throwUnexpectedArgument() {
        return throwUnexpectedArgument((NMsg) null);
    }

    @Override
    public NCmdLine pushBack(NArg arg) {
        NAssert.requireNonNull(arg, "argument");
        lookahead.add(0, arg);
        return this;
    }

    @Override
    public NOptional<NArg> next() {
        return next(expandSimpleOptions, expandArgumentsFile);
    }

    @Override
    public NOptional<String> nextString() {
        return next().map(Object::toString);
    }

    @Override
    public NOptional<NArg> next(NArgName name) {
        return next(name, false);
    }

    @Override
    public NOptional<NArg> nextOption(String option) {
        if (!new DefaultNArg(option, this).isOption()) {
            return errorOptionalCformat("%s is not an option", option);
        }
        return next(new DefaultNArgName(option), true);
    }

    @Override
    public boolean isNextOption() {
        return peek().map(NArg::isOption).orElse(false);
    }

    @Override
    public boolean isNextNonOption() {
        return peek().map(NArg::isNonOption).orElse(false);
    }

    @Override
    public NOptional<NArg> peek() {
        return get(0);
    }

    @Override
    public NOptional<NArg> peekNonOption() {
        return get(0).filter(x -> x.isNonOption());
    }

    @Override
    public NOptional<NArg> peekOption() {
        return get(0).filter(x -> x.isOption());
    }

    @Override
    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }

    @Override
    public boolean hasNextOption() {
        return hasNext() && peek().get().isOption();
    }

    @Override
    public boolean hasNextNonOption() {
        return hasNext() && peek().get().isNonOption();
    }

    @Override
    public NOptional<NArg> nextFlag(String... names) {
        return next(NArgType.FLAG, names);
    }

    @Override
    public NOptional<NArg> nextEntry(String... names) {
        return next(NArgType.ENTRY, names);
    }

    @Override
    public NOptional<NArg> nextEntry() {
        return nextEntry(new String[0]);
    }

    @Override
    public NOptional<NArg> nextFlag() {
        return nextFlag(new String[0]);
    }

    public static class MatcherImpl implements Matcher {
        private NCmdLine cmdLine;
        List<NCmdLineProcessor> processors = new ArrayList<>();

        public MatcherImpl(NCmdLine cmdLine) {
            this.cmdLine = cmdLine;
        }

        @Override
        public Matcher matchAll(NCmdLineProcessor processor) {
            if (processor != null) {
                processors.add(processor);
            }
            return this;
        }

        @Override
        public boolean noMatch() {
            return !anyMatch();
        }

        @Override
        public boolean anyMatch() {
            NArg a = cmdLine.peek().orNull();
            if (a == null) {
                return false;
            }
            for (NCmdLineProcessor consumer : processors) {
                if (consumer.process(cmdLine)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public MatcherCondition withAny() {
            return new MyMatcherConditionImpl(this, c -> true, new String[0]);
        }

        @Override
        public Matcher matchTrueFlag(Consumer<NArg> consumer) {
            return withAny().matchTrueFlag(consumer);
        }

        @Override
        public Matcher matchFlag(Consumer<NArg> consumer) {
            return withAny().matchFlag(consumer);
        }

        @Override
        public Matcher matchEntry(Consumer<NArg> consumer) {
            return withAny().matchEntry(consumer);
        }

        @Override
        public Matcher matchAny(Consumer<NArg> consumer) {
            return withAny().matchAny(consumer);
        }

        @Override
        public MatcherCondition with(String... names) {
            return new MyMatcherConditionImpl(this, cml -> {
                boolean acceptable0 = false;
                for (String name : names) {
                    String[] nameSeqArray = NStringUtils.split(name, " ").toArray(new String[0]);
                    boolean acceptable = true;
                    for (int i = 0; i < nameSeqArray.length; i++) {
                        NOptional<NArg> c = cml.get(i);
                        if (!c.isPresent() || !c.get().key().equals(nameSeqArray[i])) {
                            acceptable = false;
                        }
                    }
                    if (acceptable) {
                        acceptable0 = true;
                        break;
                    }
                }
                return acceptable0;
            }, names);
        }

        @Override
        public MatcherCondition withCondition(Predicate<NCmdLine> condition) {
            return new MyMatcherConditionImpl(this, condition, new String[0]);
        }

        @Override
        public MatcherCondition withNonOption() {
            return withCondition((c) -> c.isNextNonOption());
        }

        @Override
        public MatcherCondition withOption() {
            return withCondition((c) -> c.isNextOption());
        }

        @Override
        public Matcher withDefaultLast() {
            matchAll(new NCmdLineProcessor() {
                @Override
                public boolean process(NCmdLine cmdLine) {
                    NSession.of().configureLast(cmdLine);
                    return true;
                }
            });
            return this;
        }

        @Override
        public Matcher withDefaultFirst() {
            matchAll(new NCmdLineProcessor() {
                @Override
                public boolean process(NCmdLine cmdLine) {
                    return NSession.of().configureFirst(cmdLine);
                }
            });
            return this;
        }

        @Override
        public void requireWithDefault() {
            withDefaultLast();
            require();
        }

        @Override
        public void require() {
            if (noMatch()) {
                if (cmdLine.isEmpty()) {
                    cmdLine.throwMissingArgument();
                }
                cmdLine.throwUnexpectedArgument();
            }
        }
    }

    public Matcher matcher() {
        return new MatcherImpl(this);
    }

    @Override
    public NOptional<NArg> next(String... names) {
        return next(NArgType.DEFAULT, names);
    }

    @Override
    public NOptional<NArg> next(NArgType expectedValue, String... names) {
        if (expectedValue == null) {
            expectedValue = NArgType.DEFAULT;
        }
        if (names.length == 0) {
            if (hasNext()) {
                NArg peeked = peek().orNull();
                NOptional<String> string = peeked.getKey().asString();
                if (string.isError()) {
                    return NOptional.ofError(string.getMessage());
                }
                if (string.isPresent()) {
                    names = new String[]{string.get()};
                } else {
                    names = new String[0];
                }
            }
        } else {
            if (isAutoCompleteMode()) {
                NArgCandidate[] candidates = resolveRecommendations(expectedValue, names, autoComplete.getCurrentWordIndex());
                for (NArgCandidate c : candidates) {
                    autoComplete.addCandidate(c);
                }
            }
        }

        for (String nameSeq : names) {
            String[] nameSeqArray = NStringUtils.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length == 0) {
                continue;
            }
            if (!isPrefixed(nameSeqArray)) {
                continue;
            }
            String name = nameSeqArray[nameSeqArray.length - 1];
            NArg p = get(nameSeqArray.length - 1).orNull();
            if (p != null) {
                NOptional<String> pks = p.getKey().asString();
                if (pks.isPresent() && pks.get().equals(name)) {
                    switch (expectedValue) {
                        case DEFAULT: {
                            skip(nameSeqArray.length);
                            return NOptional.of(p);
                        }
                        case ENTRY: {
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return NOptional.of(p);
                            } else {
                                NArg r2 = peek().orNull();
                                if (r2 != null && !r2.isOption()) {
                                    skip();
                                    return NOptional.of(createArgument(p.asString().orElse("") + eq + r2.asString().orElse("")));
                                } else {
                                    return NOptional.of(p);
                                }
                            }
                        }
                        case FLAG: {
                            skip(nameSeqArray.length);
                            if (p.isNegated()) {
                                if (p.isKeyValue()) {
                                    //should not happen
                                    boolean x = p.getBooleanValue().orElse(false);
                                    if (pks.isPresent()) {
                                        return NOptional.of(createArgument(pks.get() + eq + (!x)));
                                    }
                                } else {
                                    if (pks.isPresent()) {
                                        return NOptional.of(createArgument(pks.get() + eq + (false)));
                                    }
                                }
                            } else if (p.isKeyValue()) {
                                return NOptional.of(p);
                            } else {
                                if (pks.isPresent()) {
                                    return NOptional.of(createArgument(pks.get() + eq + (true)));
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

    private <T> NOptional<T> emptyOptionalCformat(String str, Object... args) {
        List<Object> a = new ArrayList<>();
        if (!NBlankable.isBlank(getCommandName())) {
            a.add(getCommandName());
            a.addAll(Arrays.asList(args));
            return NOptional.ofEmpty(() -> NMsg.ofC("%s : " + str, a.toArray()));
        } else {
            a.addAll(Arrays.asList(args));
        }
        return NOptional.ofEmpty(() -> NMsg.ofC(str, a.toArray()));
    }

    private <T> NOptional<T> errorOptionalCformat(String str, Object... args) {
        return NOptional.ofError(() -> {
            if (!NBlankable.isBlank(getCommandName())) {
                return NMsg.ofC("%s : %s ", getCommandName(), NMsg.ofC(str, args));
            }
            return NMsg.ofC(str, args);
        });
    }

    @Override
    public NOptional<NArg> nextNonOption(NArgName name) {
        return next(name, true);
    }

    @Override
    public NOptional<NArg> nextNonOption(String name) {
        return nextNonOption(new DefaultNArgName(name));
    }

    @Override
    public NOptional<NArg> nextNonOption() {
        if (hasNext() && !isNextOption()) {
            return next();
        }
        return emptyOptionalCformat("missing non-option");
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
    public boolean accept(String... values) {
        return accept(0, values);
    }

    @Override
    public boolean accept(int index, String... values) {
        for (int i = 0; i < values.length; i++) {
            NArg argument = get(index + i).orNull();
            if (argument == null) {
                return false;
            }
            if (!argument.getKey().asString().orElse("").equals(values[i])) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NOptional<NArg> find(String name) {
        int index = indexOf(name);
        if (index >= 0) {
            return get(index);
        }
        return emptyOptionalCformat("missing argument");
    }

    @Override
    public NOptional<NArg> get(int index) {
        if (index < 0) {
            return emptyOptionalCformat("missing argument");
        }
        if (index < lookahead.size()) {
            return NOptional.of(lookahead.get(index));
        }
        while (!args.isEmpty() && index >= lookahead.size()) {
            if (!ensureNext(isExpandSimpleOptions(), true, expandArgumentsFile)) {
                break;
            }
        }
        if (index < lookahead.size()) {
            return NOptional.of(lookahead.get(index));
        }
        return emptyOptionalCformat("missing argument");
    }

    @Override
    public boolean contains(String name) {
        return indexOf(name) >= 0;
    }

    @Override
    public int indexOf(String name) {
        int i = 0;
        while (i < length()) {
            NOptional<NArg> g = get(i);
            if (g.isPresent() && g.get().getKey().asString().orElse("").equals(name)) {
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
    public String[] toStringArray() {
        return toStringList().toArray(new String[0]);
    }

    @Override
    public String[] nextAllAsStringArray() {
        String[] a = toStringArray();
        skipAll();
        return a;
    }

    @Override
    public List<String> nextAllAsStringList() {
        List<String> a = toStringList();
        skipAll();
        return a;
    }

    @Override
    public NArg[] nextAllAsArgumentArray() {
        NArg[] a = toArgumentArray();
        skipAll();
        return a;
    }

    @Override
    public List<String> toStringList() {
        List<String> all = new ArrayList<>(length());
        for (NArg nutsArgument : lookahead) {
            all.add(nutsArgument.asString().orElse(""));
        }
        all.addAll(args);
        return all;
    }

    @Override
    public NArg[] toArgumentArray() {
        List<NArg> aa = new ArrayList<>();
        while (hasNext()) {
            aa.add(next().get());
        }
        lookahead.addAll(aa);
        return aa.toArray(new NArg[0]);
    }

    @Override
    public boolean isOption(int index) {
        return get(index).map(NArg::isOption).orElse(false);
    }

    @Override
    public boolean isNonOption(int index) {
        return get(index).map(NArg::isNonOption).orElse(false);
    }

    public NCmdLine setArguments(List<String> arguments) {
        if (arguments == null) {
            return setArguments(new String[0]);
        }
        return setArguments(arguments.toArray(new String[0]));
    }

    public NCmdLine setArguments(String[] arguments) {
        this.lookahead.clear();
        this.args.clear();
        if (arguments != null) {
            for (String a : arguments) {
                if (a != null) {
                    this.args.add(a);
                }
            }
        }
        return this;
    }

    @Override
    public void throwError(NMsg message) {
        throw NExceptions.ofSafeCmdLineException(NMsg.ofC("%s : %s", NStringUtils.firstNonBlank(commandName, "command"), message));
    }

    @Override
    public void throwError(NText message) {
        NTextBuilder m = NTextBuilder.of();
        if (!NBlankable.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw NExceptions.ofSafeCmdLineException(NMsg.ofNtf(m.build().toString()));
    }

    private NArgCandidate[] resolveRecommendations(NArgType expectValue, String[] names, int autoCompleteCurrentWordIndex) {
        //nameSeqArray
        List<NArgCandidate> candidates = new ArrayList<>();
        for (String nameSeq : names) {
            String[] nameSeqArray = NStringUtils.split(nameSeq, " ").toArray(new String[0]);
            if (nameSeqArray.length > 0) {
                int i = autoCompleteCurrentWordIndex < nameSeqArray.length ? autoCompleteCurrentWordIndex : nameSeqArray.length - 1;
//                String rec = null;
                boolean skipToNext = false;
                for (int j = 0; j < i; j++) {
                    String a = nameSeqArray[j];
                    NArg x = get(j).orNull();
                    if (x != null) {
                        String xs = x.asString().orElse("");
                        if (xs.length() > 0 && !xs.equals(a)) {
                            skipToNext = true;
                            break;
                        }
                    }
                }
                if (skipToNext) {
                    continue;
                }
                skipToNext = false;
                if (i < nameSeqArray.length - 1) {
                    String a = nameSeqArray[i];
                    NArg x = get(i).orNull();
                    if (x != null) {
                        String xs = x.asString().orElse("");
                        if (xs.length() > 0 && xs.equals(a)) {
//                            switch (expectValue) {
//                                case ANY: {
//                                    candidates.add(createCandidate("<AnyValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case STRING: {
//                                    candidates.add(createCandidate("<StringValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case BOOLEAN: {
//                                    candidates.add(createCandidate("<BooleanValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                default: {
//                                    candidates.add(createCandidate("<OtherValueFor" + pgetKey().getString() + ">"));
//                                }
//                            }
                            skipToNext = true;
                        } else if (xs.length() > 0 && a.startsWith(xs) && !xs.equals(a)) {
                            candidates.add(new DefaultNArgCandidate(a));
                            skipToNext = true;
                        } else {
                            skipToNext = true;
                        }
                    }
                }
                if (skipToNext) {
                    continue;
                }
                if (getWordIndex() + nameSeqArray.length - 1 == autoCompleteCurrentWordIndex) {
                    String name = nameSeqArray[nameSeqArray.length - 1];
                    NArg p = get(nameSeqArray.length - 1).orNull();
                    if (p != null) {
                        if (name.startsWith(p.getKey().asString().orElse(""))) {
                            candidates.add(new DefaultNArgCandidate(name));
//                            switch (expectValue) {
//                                case ANY: {
//                                    candidates.add(createCandidate("<AnyValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case STRING: {
//                                    candidates.add(createCandidate("<StringValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                case BOOLEAN: {
//                                    candidates.add(createCandidate("<BooleanValueFor" + pgetKey().getString() + ">"));
//                                    break;
//                                }
//                                default: {
//                                    candidates.add(createCandidate("<OtherValueFor" + p.getStringKey() + ">"));
//                                }
//                            }
                        }
                    } else {
                        candidates.add(new DefaultNArgCandidate(name));
                    }
                }
            }
        }
        return candidates.toArray(new NArgCandidate[0]);
    }

    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            NArg x = get(i).orNull();
            if (x == null || !x.asString().orElse("").equals(nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

    public NOptional<NArg> next(NArgName name, boolean forceNonOption) {
        if (hasNext() && (!forceNonOption || !isNextOption())) {
            if (isAutoComplete()) {
                List<NArgCandidate> values = name == null ? null : name.getCandidates(getAutoComplete());
                if (values == null || values.isEmpty()) {
                    autoComplete.addCandidate(new DefaultNArgCandidate(name == null ? "<value>" : name.getName()));
                } else {
                    for (NArgCandidate value : values) {
                        autoComplete.addCandidate(value);
                    }
                }
            }
            NArg r = peek().orNull();
            skip();
            if (r == null) {
                return emptyOptionalCformat("expected argument");
            }
            return NOptional.of(r);
        } else {
            if (autoComplete != null) {
                if (isAutoComplete()) {
                    List<NArgCandidate> values = name == null ? null : name.getCandidates(getAutoComplete());
                    if (values == null || values.isEmpty()) {
                        autoComplete.addCandidate(new DefaultNArgCandidate(name == null ? "<value>" : name.getName()));
                    } else {
                        for (NArgCandidate value : values) {
                            autoComplete.addCandidate(value);
                        }
                    }
                }
                return NOptional.of(createArgument(""));
            }
            if (hasNext() && (!forceNonOption || !isNextOption())) {
                return emptyOptionalCformat("unexpected option %s", highlightText(String.valueOf(peek().get().image())));
            }
            return emptyOptionalCformat("missing argument %s", highlightText(String.valueOf(name == null ? "value" : name.getName())));
        }
        //ignored
    }

    public NOptional<NArg> next(boolean expandSimpleOptions, boolean expandArgumentsFile) {
        if (ensureNext(expandSimpleOptions, false, expandArgumentsFile)) {
            if (!lookahead.isEmpty()) {
                return NOptional.of(lookahead.remove(0));
            }
            String v = args.removeFirst();
            return NOptional.of(createArgument(v));
        } else {
            return emptyOptionalCformat("missing argument");
        }
    }

    @Override
    public String toString() {
        return toStringList().stream().map(x -> NStringUtils.formatStringLiteral(x, NElementType.DOUBLE_QUOTED_STRING, NSupportMode.PREFERRED)).collect(Collectors.joining(" "));
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

    private List<String> loadArgs(NPath path, NPath currentDir, Set<String> visited) {
        path = path.toAbsolute(currentDir).normalize();
        if (path.isRegularFile()) {
            if (visited.contains(path.toString())) {
                return Collections.emptyList();
            }
            visited.add(path.toString());
            List<String> all = new ArrayList<>();
            NShellFamily s = shellFamily;
            if (s == null) {
                s = NShellFamily.getCurrent();
//                s = NShellFamily.BASH;
            }
            String fileContent = path.readString();
            List<String> parsed = new ArrayList<>();
            for (String line : new NStringBuilder(fileContent).lines().toList()) {
                if (!NBlankable.isBlank(line) && !line.trim().startsWith("#")) {
                    NCmdLine subCmd = NCmdLines.of().setShellFamily(s).parseCmdLine(line).get();
                    subCmd.setExpandArgumentsFile(false);
                    subCmd.setExpandArgumentsFile(false);
                    parsed.addAll(subCmd.toStringList());
                }
            }
            for (String arg : parsed) {
                if (arg.length() > 3 && arg.startsWith("--@")) {
                    NPath nPath = NPath.of(arg.substring(3));
                    NPath parent = path.getParent();
                    all.addAll(loadArgs(nPath, parent == null ? currentDir : parent, visited));
                } else {
                    all.add(arg);
                }
            }
            return all;
        } else {
            if (path.exists()) {
                throw new NIllegalArgumentException(NMsg.ofC("argument file does not exist %s", path));
            } else {
                throw new NIllegalArgumentException(NMsg.ofC("argument file is not a valid regular file %s", path));
            }
        }
    }

    private boolean ensureNext(boolean expandSimpleOptions, boolean ignoreExistingExpanded, boolean expandArgumentsFile) {
        if (!ignoreExistingExpanded) {
            if (!lookahead.isEmpty()) {
                return true;
            }
        }
        if (!args.isEmpty()) {
            // -!abc=true
            String arg = args.removeFirst();
            if (arg.length() > 3 && expandArgumentsFile) {
                if (arg.startsWith("--@")) {
                    NPath nPath = NPath.of(arg.substring(3));
                    args.addAll(0, loadArgs(nPath, NPath.ofUserDirectory(), new HashSet<>()));
                    if (args.isEmpty()) {
                        return false;
                    }
                    arg = args.removeFirst();
                }
            }
            if (expandSimpleOptions && arg.length() > 2 && !isSpecialSimpleOption(arg) && ((arg.charAt(0) == '-' && arg.charAt(1) != '-') || (arg.charAt(0) == '+' && arg.charAt(1) != '+')) && (arg.charAt(1) != '/' || arg.charAt(2) == '/')) {
                NReservedSimpleCharQueue vv = new NReservedSimpleCharQueue(arg.toCharArray());
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
                    if (DefaultNArg.isSimpleKey(c)) {
                        while (vv.hasNext() && (vv.peek() != eq && !DefaultNArg.isSimpleKey(vv.peek()))) {
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
                lookahead.add(createArgument(arg));
            }
            return true;
        }
        return false;
    }

    private NArg createArgument(String v) {
        return new DefaultNArg(v, eq, this);
    }

    private boolean isAutoComplete() {
        return autoComplete != null && getWordIndex() == autoComplete.getCurrentWordIndex();
    }

    public NCmdLine copy() {
        DefaultNCmdLine c = new DefaultNCmdLine();
        c.setArguments(toStringArray());
        c.autoComplete = autoComplete;
        c.setShellFamily(shellFamily);
        c.setExpandArgumentsFile(expandArgumentsFile);
        c.setExpandSimpleOptions(expandSimpleOptions);
        c.eq = this.eq;
        c.specialSimpleOptions = new HashSet<>(specialSimpleOptions);
        c.commandName = this.commandName;
        c.configurable = this.configurable;
        c.source = this.source;
        c.unsafe = this.unsafe;
        return c;
    }

    private NMsg highlightText(String text) {
        return NMsg.ofStyledPrimary3(String.valueOf(text));
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

    @Override
    public Iterator<NArg> iterator() {
        return Arrays.asList(toArgumentArray()).iterator();
    }

    public static NOptional<String[]> parseDefaultList(String commandLineString) {
        return parseDefaultList(commandLineString, null, new HashSet<>());
    }

    private static NOptional<String[]> parseDefaultList(String commandLineString, String currentFolder, Set<String> loaded) {
        if (commandLineString == null) {
            return NOptional.of(new String[0]);
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
                            return NOptional.ofError(() -> NMsg.ofC("illegal char %s", c));
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
                return NOptional.ofError(() -> NMsg.ofPlain("expected quote"));
            }
        }
        return NOptional.of(args.toArray(new String[0]));
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

    @Override
    public NCmdLine add(String argument) {
        if (argument != null) {
            args.add(argument);
        }
        return this;
    }

    @Override
    public NCmdLine addAll(List<String> arguments) {
        if (arguments != null) {
            for (String argument : arguments) {
                add(argument);
            }
        }
        return this;
    }

    @Override
    public boolean isBlank() {
        return isEmpty();
    }

    @Override
    public void run(NCmdLineRunner processor) {
        NCmdLineConfigurable configurable = getConfigurable();
        NCmdLine cmd = this;
        NArg a;
        processor.init(cmd);
        if (isUnsafe()) {
            while ((a = peek().orNull()) != null) {
                if (processor.next(a, cmd)) {
                    // safe
                } else if (configurable != null && configurable.configureFirst(this)) {
                    // safe
                } else {
                    this.throwUnexpectedArgument();
                }
            }
        } else {
            while (cmd.hasNext()) {
                a = cmd.peek().get();
                if (processor.next(a, cmd)) {
                    NArg next = cmd.peek().orNull();
                    //reference equality!
                    if (next == a) {
                        //was not consumed!
                        throwError(NMsg.ofC("next must consume the argument: %s",
                                a));
                    }
                } else if (configurable != null && configurable.configureFirst(cmd)) {
                    NArg next = cmd.peek().orNull();
                    //reference equality!
                    if (next == a) {
                        //was not consumed!
                        throwError(NMsg.ofC("%s must consume the option: %s",
                                ("configurable.configureFirst(...)"),
                                a));
                    }
                } else {
                    cmd.throwUnexpectedArgument();
                }
            }
        }
        processor.validate(cmd);

        // test if application is running in exec mode
        // (and not in autoComplete mode)
        if (this.isExecMode()) {
            //do the good staff here
            processor.run(cmd);
        } else if (this.getAutoComplete() != null) {
            processor.autoComplete(this);
        }
    }

    public NCmdLine pushBack(NArg... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    public NCmdLine pushBack(String... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).map(x -> new DefaultNArg(x == null ? "" : x, this)).collect(Collectors.toList()));
        }
        return this;
    }

    public NCmdLine append(String... args) {
        if (args != null) {
            this.args.addAll(Arrays.stream(args).map(x -> x == null ? "" : x).collect(Collectors.toList()));
        }
        return this;
    }

    @Override
    public NCmdLine forEachPeek(NCmdLineProcessor... actions) {
        NAssert.requireNonNull(actions, () -> NMsg.ofC("missing processors"));
        NAssert.requireTrue(actions.length > 0, () -> NMsg.ofC("missing processors"));
        while (hasNext()) {
            boolean some = false;
            NArg a = peek().orNull();
            for (NCmdLineProcessor action : actions) {
                if (action != null) {
                    if (action.process(this)) {
                        some = true;
                        break;
                    } else {
                        if (isUnsafe()) {
                            NArg b = peek().orNull();
                            if (b != a) {
                                throwError(NMsg.ofC("process(...) must not consume the argument if not relevant"));
                            }
                        }
                    }
                }
            }
            if (!some) {
                if (configurable != null && configurable.configureFirst(this)) {
                    if (isUnsafe()) {
                        NArg b = peek().orNull();
                        if (b == a) {
                            throwError(NMsg.ofC("process(...) must consume the argument if relevant"));
                        }
                    }
                } else {
                    this.throwUnexpectedArgument();
                }
            } else {
                if (isUnsafe()) {
                    NArg b = peek().orNull();
                    if (b == a) {
                        throwError(NMsg.ofC("process(...) must consume the argument if relevant"));
                    }
                }
            }
        }
        return this;
    }

    @Override
    public NCmdLine forEachPeek(NCmdLineProcessor processor) {
        NAssert.requireNonNull(processor, () -> NMsg.ofC("processor"));
        return forEachPeek(new NCmdLineProcessor[]{processor});
    }

    private class MyNCmdLineArgProcessor implements NCmdLineArgProcessor {
        private final boolean finalAcceptable;
        private final String[] names;

        public MyNCmdLineArgProcessor(boolean finalAcceptable, String... names) {
            this.finalAcceptable = finalAcceptable;
            this.names = names;
        }

        public boolean isAcceptable() {
            return finalAcceptable;
        }

        @Override
        public boolean nextFlag(Consumer<NArg> consumer) {
            if (!finalAcceptable) {
                return false;
            }
            NOptional<NArg> v = next(NArgType.FLAG, names);
            if (v.isPresent()) {
                NArg a = v.get();
                if (a.isUncommented()) {
                    consumer.accept(a);
                    return true;
                }
                return true;
            }
            return false;
        }

        @Override
        public boolean nextEntry(Consumer<NArg> consumer) {
            if (!finalAcceptable) {
                return false;
            }
            NOptional<NArg> v = next(NArgType.ENTRY, names);
            if (v.isPresent()) {
                NArg a = v.get();
                if (a.isUncommented()) {
                    consumer.accept(a);
                    return true;
                }
                return true;
            }
            return false;
        }


        @Override
        public boolean nextTrueFlag(Consumer<NArg> consumer) {
            if (!finalAcceptable) {
                return false;
            }
            return nextFlag((value) -> {
                if (value.isBoolean() && value.booleanValue()) {
                    consumer.accept(value);
                }
            });
        }
    }

    private static class MyMatcherConditionImpl implements MatcherCondition {
        private final Predicate<NCmdLine> baseCondition;
        private final String[] names;
        private MatcherImpl selector;
        private List<Predicate<NCmdLine>> otherConditions = new ArrayList<>();

        public MyMatcherConditionImpl(MatcherImpl selector, Predicate<NCmdLine> baseCondition, String... names) {
            this.baseCondition = baseCondition;
            this.names = names;
            this.selector = selector;
        }

        @Override
        public MatcherCondition and(Predicate<NCmdLine> condition) {
            if (condition != null) {
                otherConditions.add(condition);
            }
            return this;
        }

        private boolean checkCondition(NCmdLine cmdLine) {
            if (!baseCondition.test(cmdLine)) {
                return false;
            }
            for (Predicate<NCmdLine> otherCondition : otherConditions) {
                if (!otherCondition.test(cmdLine)) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public Matcher matchFlag(Consumer<NArg> consumer) {
            selector.matchAll(
                    new NCmdLineProcessor() {
                        @Override
                        public boolean process(NCmdLine cmdLine) {
                            if (!checkCondition(cmdLine)) {
                                return false;
                            }
                            NOptional<NArg> v = selector.cmdLine.next(NArgType.FLAG, names);
                            if (v.isPresent()) {
                                NArg a = v.get();
                                if (a.isUncommented()) {
                                    consumer.accept(a);
                                    return true;
                                }
                                return true;
                            }
                            return false;
                        }
                    }
            );
            return selector;
        }

        @Override
        public Matcher matchEntry(Consumer<NArg> consumer) {
            selector.matchAll(new NCmdLineProcessor() {
                @Override
                public boolean process(NCmdLine cmdLine) {
                    if (!checkCondition(cmdLine)) {
                        return false;
                    }
                    NOptional<NArg> v = selector.cmdLine.next(NArgType.ENTRY, names);
                    if (v.isPresent()) {
                        NArg a = v.get();
                        if (a.isUncommented()) {
                            consumer.accept(a);
                            return true;
                        }
                        return true;
                    }
                    return false;

                }
            });
            return selector;
        }

        @Override
        public Matcher matchAnyMultiple(Consumer<NCmdLine> consumer) {
            selector.matchAll(new NCmdLineProcessor() {
                @Override
                public boolean process(NCmdLine cmdLine) {
                    if (!checkCondition(cmdLine)) {
                        return false;
                    }
                    NOptional<NArg> v = selector.cmdLine.peek();
                    if (v.isPresent()) {
                        consumer.accept(selector.cmdLine);
                        return true;
                    }
                    return false;

                }
            });
            return selector;
        }

        @Override
        public Matcher matchAny(Consumer<NArg> consumer) {
            selector.matchAll(new NCmdLineProcessor() {
                @Override
                public boolean process(NCmdLine cmdLine) {
                    if (!checkCondition(cmdLine)) {
                        return false;
                    }
                    NOptional<NArg> v = selector.cmdLine.next();
                    if (v.isPresent()) {
                        NArg a = v.get();
                        //if (a.isNonCommented()) {
                        consumer.accept(a);
                        return true;
                        //}
                        //return true;
                    }
                    return false;

                }
            });
            return selector;
        }

        @Override
        public Matcher matchTrueFlag(Consumer<NArg> consumer) {
            return matchFlag((value) -> {
                if (value.booleanValue()) {
                    consumer.accept(value);
                }
            });
        }
    }
}
