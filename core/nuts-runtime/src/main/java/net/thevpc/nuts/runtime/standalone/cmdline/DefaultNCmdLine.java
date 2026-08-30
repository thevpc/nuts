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
package net.thevpc.nuts.runtime.standalone.cmdline;

import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.runtime.standalone.util.NStringBuilderImpl;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.elem.NElementType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.internal.util.NReservedSimpleCharQueue;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.*;

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
public class DefaultNCmdLine implements NCmdLine {

    protected LinkedList<String> args = new LinkedList<>();
    protected List<NArg> lookahead = new ArrayList<>();
    protected boolean expandSimpleOptions = true;
    protected boolean expandArgumentsFile = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();
    protected String commandName;
    private int wordIndex = 0;
    private NArgCompletePosition completePosition;
    private List<NArgCompleteCandidate> completeCandidates;
    private Set<NArgCompleteFlag> completeFlags;
    private char eq = '=';
    private NShellFamily shellFamily = NShellFamily.BASH;

    private Object source;


    /**
     * configurable or null
     *
     * @return configurable or null
     */
    private NCmdLineConfigurable configurable;

    //Constructors
    /**
     * Default n cmd line.
     *
     * @return default n cmd line result
     */
    public DefaultNCmdLine() {

    }

    /**
     * Default n cmd line.
     *
     * @param args args
     * @param shellFamily shell family
     * @return default n cmd line result
     */
    public DefaultNCmdLine(String[] args, NShellFamily shellFamily) {
        this.shellFamily = shellFamily == null ? NShellFamily.current() : shellFamily;
      /**
       * Sets the arguments.
       *
       * @param args args
       */
        setArguments(args);
    }

    /**
     * Default n cmd line.
     *
     * @param args args
     * @return default n cmd line result
     */
    public DefaultNCmdLine(List<String> args) {
      /**
       * Sets the arguments.
       *
       * @param args args
       */
        setArguments(args);
    }

    /**
     * Shell family.
     *
     * @return shell family result
     */
    public NShellFamily shellFamily() {
        return shellFamily;
    }

    /**
     * Shell family.
     *
     * @param shellFamily shell family
     * @return shell family result
     */
    public NCmdLine shellFamily(NShellFamily shellFamily) {
        this.shellFamily = shellFamily;
        return this;
    }


    @Override
    public Object source() {
        return source;
    }

    @Override
    public NCmdLine source(Object source) {
        this.source = source;
        return this;
    }

    @Override
    public NCmdLineConfigurable configurable() {
        return configurable;
    }

    /**
     * Configurable.
     *
     * @param configurable configurable
     * @return configurable result
     */
    public NCmdLine configurable(NCmdLineConfigurable configurable) {
        this.configurable = configurable;
        return this;
    }

    @Override
    public boolean isExpandArgumentsFile() {
        return expandArgumentsFile;
    }

    @Override
    public NCmdLine expandArgumentsFile(boolean expandArgumentsFile) {
        this.expandArgumentsFile = expandArgumentsFile;
        return this;
    }

    //End Constructors
    @Override
    public NArgCompleteResult completeResult() {
        if (isCompleteMode()) {
            return NArgCompleteResult.of(
                    completeCandidates,
                    completeFlags
            );
        }
        return null;
    }

    @Override
    public NArgCompleteResult printCompleteResult() {
        NArgCompleteResult r = completeResult();
        if (r != null) {
            NIO.of().stdout().println(r.format());
        }
        return r;
    }

    @Override
    public NCmdLine completePosition(NArgCompletePosition completePosition) {
        this.completePosition = completePosition;
        return this;
    }

    @Override
    public NCmdLine unregisterSpecialSimpleOption(String option) {
        specialSimpleOptions.remove(option);
        return this;
    }

    @Override
    public String[] specialSimpleOptions() {
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
      /**
       * Throw error.
       *
       * @param option) option)
       */
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
    public int wordIndex() {
        return wordIndex;
    }

    @Override
    public boolean isExecMode() {
        return completePosition == null;
    }

    @Override
    public boolean isCompleteMode() {
        return completePosition != null;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public NCmdLine commandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    @Override
    public NCmdLine expandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
    }

    @Override
    public NCmdLine throwUnexpectedArgument(NText errorMessage) {
        /**
         * Throw unexpected argument.
         *
         * @param errorMessage) error message)
         * @return throw unexpected argument result
         */
        return throwUnexpectedArgument(NMsg.ofC("%s", errorMessage));
    }

    @Override
    public NCmdLine throwUnexpectedArgument(NMsg errorMessage) {
        if (!isEmpty()) {
            if (isCompleteMode()) {
              /**
               * Skip all.
               */
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
          /**
           * Throw error.
           *
           * @param ep.toArray()) ep.to array())
           */
            throwError(NMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }

    @Override
    public NCmdLine throwMissingArgument() {
        if (isEmpty()) {
            if (isCompleteMode()) {
              /**
               * Skip all.
               */
                skipAll();
                return this;
            }
          /**
           * Throw error.
           *
           * @param argument") argument")
           */
            throwError(NMsg.ofP("missing argument"));
        }
        return this;
    }

    @Override
    public NCmdLine throwMissingArgument(String argumentName) {
        if (NBlankable.isBlank(argumentName)) {
          /**
           * Throw missing argument.
           */
            throwMissingArgument();
        } else {
            if (isEmpty()) {
                if (isCompleteMode()) {
                  /**
                   * Skip all.
                   */
                    skipAll();
                    return this;
                }
              /**
               * Throw error.
               *
               * @param NMsg.ofStyledKeyword(argumentName)) n msg.of styled keyword(argument name))
               */
                throwError(NMsg.ofC("missing argument %s", NMsg.ofStyledKeyword(argumentName)));
            }
            return this;
        }
        return this;
    }

    @Override
    public NCmdLine throwMissingArgument(NMsg errorMessage) {
        if (isEmpty()) {
            if (isCompleteMode()) {
              /**
               * Skip all.
               */
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
          /**
           * Throw error.
           *
           * @param ep.toArray()) ep.to array())
           */
            throwError(NMsg.ofC(sb.toString(), ep.toArray()));
        }
        return this;
    }

    @Override
    public NCmdLine throwUnexpectedArgument() {
        /**
         * Throw unexpected argument.
         *
         * @param null null
         * @return throw unexpected argument result
         */
        return throwUnexpectedArgument((NMsg) null);
    }

    @Override
    public NCmdLine pushBack(NArg arg) {
        NAssert.requireNamedNonNull(arg, "argument");
        lookahead.add(0, arg);
        return this;
    }

    @Override
    public NOptional<NArg> next() {
        /**
         * Next.
         *
         * @param expandSimpleOptions expand simple options
         * @param expandArgumentsFile expand arguments file
         * @return next result
         */
        return next(expandSimpleOptions, expandArgumentsFile);
    }

    @Override
    public boolean isNextOption() {
        /**
         * Peek.
         *
         * @param ).map(NArg::isOption).orElse(false ).map(n arg::is option).or else(false
         * @return peek result
         */
        return peek().map(NArg::isOption).orElse(false);
    }

    @Override
    public boolean isNextNonOption() {
        /**
         * Peek.
         *
         * @param ).map(NArg::isNonOption).orElse(false ).map(n arg::is non option).or else(false
         * @return peek result
         */
        return peek().map(NArg::isNonOption).orElse(false);
    }

    @Override
    public NOptional<NArg> peek() {
        /**
         * Returns the get.
         *
         * @param 0 0
         * @return get result
         */
        return get(0);
    }

    @Override
    public NOptional<NArg> peekNonOption() {
        /**
         * Returns the get.
         *
         * @param x.isNonOption() x.is non option()
         * @return get result
         */
        return get(0).filter(x -> x.isNonOption());
    }

    @Override
    public NOptional<NArg> peekOption() {
        /**
         * Returns the get.
         *
         * @param x.isOption() x.is option()
         * @return get result
         */
        return get(0).filter(x -> x.isOption());
    }

    @Override
    public boolean hasNext() {
        return !lookahead.isEmpty() || !args.isEmpty();
    }

    @Override
    public boolean hasNextOption() {
        /**
         * Checks if has next.
         *
         * @param peek().get().isOption( peek().get().is option(
         * @return has next result
         */
        return hasNext() && peek().get().isOption();
    }

    @Override
    public boolean hasNextNonOption() {
        /**
         * Checks if has next.
         *
         * @param peek().get().isNonOption( peek().get().is non option(
         * @return has next result
         */
        return hasNext() && peek().get().isNonOption();
    }

    @Override
    public NOptional<NArg> nextFlag(String... names) {
        /**
         * Next.
         *
         * @param NArgType.FLAG n arg type.flag
         * @param names names
         * @return next result
         */
        return next(NArgType.FLAG, names);
    }

    @Override
    public NOptional<NArg> nextEntry(String... names) {
        /**
         * Next.
         *
         * @param NArgType.ENTRY n arg type.entry
         * @param names names
         * @return next result
         */
        return next(NArgType.ENTRY, names);
    }

    @Override
    public NOptional<NArg> nextAttachedEntry(String... names) {
        /**
         * Next.
         *
         * @param NArgType.ATTACHED_ENTRY n arg type.attached_entry
         * @param names names
         * @return next result
         */
        return next(NArgType.ATTACHED_ENTRY, names);
    }

    @Override
    public NOptional<NArg> nextRequiredEntry(String... names) {
        /**
         * Next.
         *
         * @param NArgType.REQUIRED_ENTRY n arg type.required_entry
         * @param names names
         * @return next result
         */
        return next(NArgType.REQUIRED_ENTRY, names);
    }

    @Override
    public NOptional<NArg> nextAttachedEntry() {
        /**
         * Next.
         *
         * @param NArgType.ATTACHED_ENTRY n arg type.attached_entry
         * @return next result
         */
        return next(NArgType.ATTACHED_ENTRY);
    }

    @Override
    public NOptional<NArg> nextRequiredEntry() {
        /**
         * Next.
         *
         * @param NArgType.REQUIRED_ENTRY n arg type.required_entry
         * @return next result
         */
        return next(NArgType.REQUIRED_ENTRY);
    }

    @Override
    public NOptional<NArg> nextEntry() {
        /**
         * Next entry.
         *
         * @param String[0] string[0]
         * @return next entry result
         */
        return nextEntry(new String[0]);
    }

    @Override
    public NOptional<NArg> nextFlag() {
        /**
         * Next flag.
         *
         * @param String[0] string[0]
         * @return next flag result
         */
        return nextFlag(new String[0]);
    }

    /**
     * Matcher.
     *
     * @return matcher result
     */
    public NCmdLineMatcher matcher() {
        return new NCmdLineMatcherImpl(this);
    }

    @Override
    public NOptional<NArg> next(String... names) {
        /**
         * Next.
         *
         * @param NArgType.DEFAULT n arg type.default
         * @param names names
         * @return next result
         */
        return next(NArgType.DEFAULT, names);
    }

    @Override
    public NOptional<NArg> next(NArgType expectedArgType, String... names) {
        /**
         * Next.
         *
         * @param expectedArgType expected arg type
         * @param null null
         * @param null null
         * @param names names
         * @return next result
         */
        return next(expectedArgType, null, null, names);
    }

    /**
     * Complete position.
     *
     * @return complete position result
     */
    public NArgCompletePosition completePosition() {
        return completePosition;
    }

    /**
     * Adds the specified candidate.
     *
     * @param candidate candidate
     * @return add candidate result
     */
    private void addCandidate(NArgCompleteCandidate candidate) {
        if (candidate != null) {

            if (completeCandidates == null) {
                completeCandidates = new ArrayList<>();
            }
            completeCandidates.add(candidate);
        }
    }

    /**
     * Adds the specified candidate flag.
     *
     * @param candidate candidate
     * @return add candidate flag result
     */
    private void addCandidateFlag(NArgCompleteFlag candidate) {
        if (candidate != null) {
            if (completeFlags == null) {
                completeFlags = new HashSet<>();
            }
            completeFlags.add(candidate);
        }
    }

    /**
     * Adds the specified value candidates.
     *
     * @param valueComplete value complete
     * @param argDisplay arg display
     * @return add value candidates result
     */
    private void addValueCandidates(NArgValueComplete valueComplete, String argDisplay) {
        if (valueComplete != null) {
            NArgCompleteResult rvalues = valueComplete.searchValue(createSearchContext());
            if (rvalues != null) {
                for (NArgCompleteCandidate c : rvalues.candidates()) {
                  /**
                   * Adds the specified candidate.
                   *
                   * @param c c
                   */
                    addCandidate(c);
                }
                for (NArgCompleteFlag f : rvalues.flags()) {
                  /**
                   * Adds the specified candidate flag.
                   *
                   * @param f f
                   */
                    addCandidateFlag(f);
                }
                return;
            }
        }
        // no finder or finder returned null — fall back to a display-hint placeholder
        if (argDisplay != null) {
          /**
           * Adds the specified candidate.
           *
           * @param NArgCompleteCandidate.of(argDisplay) n arg complete candidate.of(arg display)
           */
            addCandidate(NArgCompleteCandidate.of(argDisplay));
        }
    }

    /**
     * Creates a new instance of create search context.
     *
     * @return create search context result
     */
    private NArgValueComplete.Context createSearchContext() {
        NArg word = peek().orNull();
        String wordStr = word == null ? "" : word.asString().orElse("");
        return new MyContext(wordStr, completePosition);
    }

    /**
     * Next.
     *
     * @param expectedArgType expected arg type
     * @param argDisplay arg display
     * @param valueComplete value complete
     * @param names names
     * @return next result
     */
    public NOptional<NArg> next(NArgType expectedArgType, String argDisplay, NArgValueComplete valueComplete, String... names) {
        if (expectedArgType == null) {
            expectedArgType = NArgType.DEFAULT;
        }
        if (names.length == 0) {
            if (hasNext()) {
                NArg peeked = peek().orNull();
                NOptional<String> string = peeked.getKey().asString();
                if (string.isError()) {
                    return NOptional.ofError(string.message());
                }
                if (string.isPresent()) {
                    names = new String[]{string.get()};
                } else {
                    names = new String[0];
                }
            }
        } else {
            if (isCompleteMode()) {
                NArgCompleteCandidate[] candidates = resolveRecommendations(expectedArgType, argDisplay, valueComplete, names);
                for (NArgCompleteCandidate c : candidates) {
                  /**
                   * Adds the specified candidate.
                   *
                   * @param c c
                   */
                    addCandidate(c);
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
                    switch (expectedArgType) {
                        case DEFAULT: {
                          /**
                           * Skip.
                           *
                           * @param nameSeqArray.length name seq array.length
                           */
                            skip(nameSeqArray.length);
                            return NOptional.of(p);
                        }
                        case ENTRY: {
                          /**
                           * Skip.
                           *
                           * @param nameSeqArray.length name seq array.length
                           */
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return NOptional.of(p);
                            } else {
                                NArg r2 = peek().orNull();
                                if (r2 != null && !r2.isOption()) {
                                    if (isCompleteMode() && isAtCompletePosition()) {
                                        // cursor is at the value token — invoke valueComplete
                                      /**
                                       * Adds the specified value candidates.
                                       *
                                       * @param valueComplete value complete
                                       * @param argDisplay arg display
                                       */
                                        addValueCandidates(valueComplete, argDisplay);
                                    }
                                  /**
                                   * Skip.
                                   */
                                    skip();
                                    return NOptional.of(createArgument(p.asString().orElse("") + eq + r2.asString().orElse("")));
                                } else {
                                    return NOptional.of(p);
                                }
                            }
                        }
                        case REQUIRED_ENTRY: {
                          /**
                           * Skip.
                           *
                           * @param nameSeqArray.length name seq array.length
                           */
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return NOptional.of(p);
                            } else {
                                //get the next arg without expanding any simple option, just take it as is
                                NArg r2 = isEmpty() ? null : get(0, false, true, false).orNull();
                                if (r2 != null) {
                                    if (isCompleteMode() && isAtCompletePosition()) {
                                        // cursor is at the value token — invoke valueComplete
                                      /**
                                       * Adds the specified value candidates.
                                       *
                                       * @param valueComplete value complete
                                       * @param argDisplay arg display
                                       */
                                        addValueCandidates(valueComplete, argDisplay);
                                    }
                                  /**
                                   * Skip.
                                   */
                                    skip();
                                    return NOptional.of(createArgument(p.asString().orElse("") + eq + r2.asString().orElse("")));
                                } else {
                                    if (isCompleteMode()) {
                                        // no value token yet but complete mode — suggest values at end
                                      /**
                                       * Adds the specified value candidates.
                                       *
                                       * @param valueComplete value complete
                                       * @param argDisplay arg display
                                       */
                                        addValueCandidates(valueComplete, argDisplay);
                                        return NOptional.of(p);
                                    } else {
                                        // should i throw exception?
                                      /**
                                       * Throw missing argument.
                                       *
                                       * @param provided") provided")
                                       */
                                        throwMissingArgument(NMsg.ofC("option '%s' expects a value that was not provided"));
                                        return NOptional.of(p);
                                    }
                                }
                            }
                        }
                        case ATTACHED_ENTRY: {
                          /**
                           * Skip.
                           *
                           * @param nameSeqArray.length name seq array.length
                           */
                            skip(nameSeqArray.length);
                            if (p.isKeyValue()) {
                                return NOptional.of(p);
                            } else {
                                if (isCompleteMode()) {
                                    // advertise "--k=" as the only valid continuation — no bare-token grab
                                  /**
                                   * Adds the specified candidate.
                                   *
                                   * @param eq) eq)
                                   */
                                    addCandidate(NArgCompleteCandidate.of(pks.orElse(name) + eq));
                                } else {
                                    // should i throw exception?
                                    return NOptional.of(p);
                                }
                            }
                        }
                        case FLAG: {
                          /**
                           * Skip.
                           *
                           * @param nameSeqArray.length name seq array.length
                           */
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
                            /**
                             * Error optional cformat.
                             *
                             * @param %s" %s"
                             * @param highlightText(String.valueOf(expectedArgType)) highlight text( string.value of(expected arg type))
                             * @return error optional cformat result
                             */
                            return errorOptionalCformat("unsupported %s", highlightText(String.valueOf(expectedArgType)));
                        }
                    }
                }
            }

        }
        /**
         * Empty optional cformat.
         *
         * @param argument" argument"
         * @return empty optional cformat result
         */
        return emptyOptionalCformat("missing argument");
    }


    /**
     * Matches candidate.
     *
     * @param currentArg current arg
     * @param expected expected
     * @return matches candidate result
     */
    private boolean matchesCandidate(NArg currentArg, String expected) {
        if (currentArg == null) {
            return true;
        }
        String token = currentArg.getKey().asString().orElse("");
        NArgValueComplete.Context searchContext = new MyContext(token, completePosition == null ? -1 : completePosition.wordOffset());
        return searchContext.matches(expected);
    }

    /**
     * Empty optional cformat.
     *
     * @param str str
     * @param args args
     * @return empty optional cformat result
     */
    private <T> NOptional<T> emptyOptionalCformat(String str, Object... args) {
        List<Object> a = new ArrayList<>();
        if (!NBlankable.isBlank(commandName())) {
            a.add(commandName());
            a.addAll(Arrays.asList(args));
            return NOptional.ofEmpty(() -> NMsg.ofC("%s : " + str, a.toArray()));
        } else {
            a.addAll(Arrays.asList(args));
        }
        return NOptional.ofEmpty(() -> NMsg.ofC(str, a.toArray()));
    }

    /**
     * Error optional cformat.
     *
     * @param str str
     * @param args args
     * @return error optional cformat result
     */
    private <T> NOptional<T> errorOptionalCformat(String str, Object... args) {
        return NOptional.ofError(() -> {
            if (!NBlankable.isBlank(commandName())) {
                return NMsg.ofC("%s : %s ", commandName(), NMsg.ofC(str, args));
            }
            return NMsg.ofC(str, args);
        });
    }


    @Override
    public NOptional<NArg> nextNonOption(String display) {
        /**
         * Next non option.
         *
         * @param display display
         * @param null null
         * @return next non option result
         */
        return nextNonOption(display, null);
    }

    @Override
    public NOptional<NArg> nextNonOption(String display, NArgValueComplete complete) {
        if (hasNext() && !isNextOption()) {
            if (isAtCompletePosition()) {
                NArgCompleteResult rvalues = complete == null ? null : complete.searchValue(createSearchContext());
                if (rvalues == null || (rvalues.candidates().isEmpty() && rvalues.flags().isEmpty())) {
                  /**
                   * Adds the specified candidate.
                   *
                   * @param display) display)
                   */
                    addCandidate(NArgCompleteCandidate.of(display == null ? "<value>" : display));
                } else {
                    for (NArgCompleteCandidate value : rvalues.candidates()) {
                      /**
                       * Adds the specified candidate.
                       *
                       * @param value value
                       */
                        addCandidate(value);
                    }
                    for (NArgCompleteFlag value : rvalues.flags()) {
                      /**
                       * Adds the specified candidate flag.
                       *
                       * @param value value
                       */
                        addCandidateFlag(value);
                    }
                }
            }
            NArg r = peek().orNull();
          /**
           * Skip.
           */
            skip();
            if (r == null) {
                /**
                 * Empty optional cformat.
                 *
                 * @param argument" argument"
                 * @return empty optional cformat result
                 */
                return emptyOptionalCformat("expected argument");
            }
            return NOptional.of(r);
        } else {
            if (isCompleteMode()) {
                if (isAtCompletePosition()) {
                    NArgCompleteResult rvalues = complete == null ? null : complete.searchValue(createSearchContext());
                    if (rvalues == null || (rvalues.candidates().isEmpty() && rvalues.flags().isEmpty())) {
                      /**
                       * Adds the specified candidate.
                       *
                       * @param display) display)
                       */
                        addCandidate(NArgCompleteCandidate.of(display == null ? "<value>" : display));
                    } else {
                        for (NArgCompleteCandidate value : rvalues.candidates()) {
                          /**
                           * Adds the specified candidate.
                           *
                           * @param value value
                           */
                            addCandidate(value);
                        }
                        for (NArgCompleteFlag value : rvalues.flags()) {
                          /**
                           * Adds the specified candidate flag.
                           *
                           * @param value value
                           */
                            addCandidateFlag(value);
                        }
                    }
                }
                return NOptional.of(createArgument(""));
            }
            return emptyOptionalCformat("missing non-option argument %s",
                  /**
                   * Highlight text.
                   *
                   * @param display) display)
                   */
                    highlightText(display == null ? "value" : display));
        }
    }

    @Override
    public NOptional<NArg> nextNonOption() {
        if (hasNext() && !isNextOption()) {
            /**
             * Next.
             *
             * @return next result
             */
            return next();
        }
        /**
         * Empty optional cformat.
         *
         * @param non-option" non-option"
         * @return empty optional cformat result
         */
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
        /**
         * Skip.
         *
         * @param 1 1
         * @return skip result
         */
        return skip(1);
    }

    @Override
    public int skip(int count) {
        if (count < 0) {
            count = 0;
        }
        int initialCount = count;
        while (initialCount > 0 && hasNext()) {
          /**
           * Next.
           */
            next();
            wordIndex++;
            initialCount--;
        }
        return count;
    }

    @Override
    public boolean accept(String... values) {
        /**
         * Accept.
         *
         * @param 0 0
         * @param values values
         * @return accept result
         */
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
            /**
             * Returns the get.
             *
             * @param index index
             * @return get result
             */
            return get(index);
        }
        /**
         * Empty optional cformat.
         *
         * @param argument" argument"
         * @return empty optional cformat result
         */
        return emptyOptionalCformat("missing argument");
    }

    @Override
    public NOptional<NArg> get(int index) {
        /**
         * Returns the get.
         *
         * @param index index
         * @param isExpandSimpleOptions() is expand simple options()
         * @param true true
         * @param isExpandArgumentsFile() is expand arguments file()
         * @return get result
         */
        return get(index, isExpandSimpleOptions(), true, isExpandArgumentsFile());
    }

    /**
     * Returns the get.
     *
     * @param index index
     * @param expandSimpleOptions expand simple options
     * @param ignoreExistingExpanded ignore existing expanded
     * @param expandArgumentsFile expand arguments file
     * @return get result
     */
    private NOptional<NArg> get(int index, boolean expandSimpleOptions, boolean ignoreExistingExpanded, boolean expandArgumentsFile) {
        if (index < 0) {
            /**
             * Empty optional cformat.
             *
             * @param argument" argument"
             * @return empty optional cformat result
             */
            return emptyOptionalCformat("missing argument");
        }
        if (index < lookahead.size()) {
            return NOptional.of(lookahead.get(index));
        }
        while (!args.isEmpty() && index >= lookahead.size()) {
            if (!ensureNext(expandSimpleOptions, ignoreExistingExpanded, expandArgumentsFile)) {
                break;
            }
        }
        if (index < lookahead.size()) {
            return NOptional.of(lookahead.get(index));
        }
        /**
         * Empty optional cformat.
         *
         * @param argument" argument"
         * @return empty optional cformat result
         */
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
        /**
         * Converts to string list.
         *
         * @param String[0] string[0]
         * @return to string list result
         */
        return toStringList().toArray(new String[0]);
    }

    @Override
    public String[] nextAllAsStringArray() {
        String[] a = toStringArray();
      /**
       * Skip all.
       */
        skipAll();
        return a;
    }

    @Override
    public List<String> nextAllAsStringList() {
        List<String> a = toStringList();
      /**
       * Skip all.
       */
        skipAll();
        return a;
    }

    @Override
    public NArg[] nextAllAsArgumentArray() {
        NArg[] a = toArgumentArray();
      /**
       * Skip all.
       */
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
        /**
         * Returns the get.
         *
         * @param index).map(NArg::isOption).orElse(false index).map(n arg::is option).or else(false
         * @return get result
         */
        return get(index).map(NArg::isOption).orElse(false);
    }

    @Override
    public boolean isNonOption(int index) {
        /**
         * Returns the get.
         *
         * @param index).map(NArg::isNonOption).orElse(false index).map(n arg::is non option).or else(false
         * @return get result
         */
        return get(index).map(NArg::isNonOption).orElse(false);
    }

    /**
     * Sets the arguments.
     *
     * @param arguments arguments
     * @return set arguments result
     */
    public NCmdLine setArguments(List<String> arguments) {
        if (arguments == null) {
            /**
             * Sets the arguments.
             *
             * @param String[0] string[0]
             * @return set arguments result
             */
            return setArguments(new String[0]);
        }
        /**
         * Sets the arguments.
         *
         * @param String[0]) string[0])
         * @return set arguments result
         */
        return setArguments(arguments.toArray(new String[0]));
    }

    /**
     * Sets the arguments.
     *
     * @param arguments arguments
     * @return set arguments result
     */
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
        throw NException.ofSafeCmdLineException(NMsg.ofC("%s : %s", NStringUtils.firstNonBlankStripped(commandName, "command"), message));
    }

    @Override
    public void throwError(NText message) {
        NTextBuilder m = NTextBuilder.of();
        if (!NBlankable.isBlank(commandName)) {
            m.append(commandName).append(" : ");
        }
        m.append(message);
        throw NException.ofSafeCmdLineException(NMsg.ofNtf(m.build().toString()));
    }

    /**
     * Resolve recommendations.
     *
     * @param expectedArgType expected arg type
     * @param argDisplay arg display
     * @param finder finder
     * @param names names
     * @return resolve recommendations result
     */
    private NArgCompleteCandidate[] resolveRecommendations(NArgType expectedArgType, String argDisplay, NArgValueComplete finder, String[] names) {
        int autoCompleteCurrentWordIndex = completePosition.wordIndex();
        //nameSeqArray
        List<NArgCompleteCandidate> candidates = new ArrayList<>();
        NArgValueComplete.Context searchContext = createSearchContext();
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
                            skipToNext = true;
                        } else if (xs.length() > 0 && a.startsWith(xs) && !xs.equals(a)) {
                            candidates.add(NArgCompleteCandidate.of(a));
                            skipToNext = true;
                        } else {
                            skipToNext = true;
                        }
                    }
                }
                if (skipToNext) {
                    continue;
                }
                if (wordIndex() + nameSeqArray.length - 1 == autoCompleteCurrentWordIndex) {
                    String name = nameSeqArray[nameSeqArray.length - 1];
                    NArg p = get(nameSeqArray.length - 1).orNull();
                    if (p != null) {
                        if (matchesCandidate(p, name)) {
                            candidates.add(NArgCompleteCandidate.of(name));
                        }
                    } else {
                        candidates.add(NArgCompleteCandidate.of(name));
                    }
                }
            }
        }
        return candidates.toArray(new NArgCompleteCandidate[0]);
    }

    /**
     * Checks if is prefixed.
     *
     * @param nameSeqArray name seq array
     * @return is prefixed result
     */
    private boolean isPrefixed(String[] nameSeqArray) {
        for (int i = 0; i < nameSeqArray.length - 1; i++) {
            NArg x = get(i).orNull();
            if (x == null || !x.asString().orElse("").equals(nameSeqArray[i])) {
                return false;
            }
        }
        return true;
    }

//    public NOptional<NArg> next(NArgName name, boolean forceNonOption) {
//        if (hasNext() && (!forceNonOption || !isNextOption())) {
//            if (isAtCompletePosition()) {
//                NArgCompleteResult rvalues = name == null ? null : name.resolveCandidates();
//                if (rvalues == null || (rvalues.candidates().isEmpty() && rvalues.flags().isEmpty())) {
//                    addCandidate(NArgCompleteCandidate.of(name == null ? "<value>" : name.name()));
//                } else {
//                    for (NArgCompleteCandidate value : rvalues.candidates()) {
//                        addCandidate(value);
//                    }
//                    for (NArgCompleteFlag value : rvalues.flags()) {
//                        addCandidateFlag(value);
//                    }
//                }
//            }
//            NArg r = peek().orNull();
//            skip();
//            if (r == null) {
//                return emptyOptionalCformat("expected argument");
//            }
//            return NOptional.of(r);
//        } else {
//            if (isCompleteMode()) {
//                if (isAtCompletePosition()) {
//                    NArgCompleteResult rvalues = name == null ? null : name.resolveCandidates();
//                    if (rvalues == null || (rvalues.candidates().isEmpty() && rvalues.flags().isEmpty())) {
//                        addCandidate(NArgCompleteCandidate.of(name == null ? "<value>" : name.name()));
//                    } else {
//                        for (NArgCompleteCandidate value : rvalues.candidates()) {
//                            addCandidate(value);
//                        }
//                        for (NArgCompleteFlag value : rvalues.flags()) {
//                            addCandidateFlag(value);
//                        }
//                    }
//                }
//                return NOptional.of(createArgument(""));
//            }
//            if (hasNext() && (!forceNonOption || !isNextOption())) {
//                return emptyOptionalCformat("unexpected option %s", highlightText(String.valueOf(peek().get().image())));
//            }
//            return emptyOptionalCformat("missing argument %s", highlightText(String.valueOf(name == null ? "value" : name.name())));
//        }
//        //ignored
//    }

    /**
     * Next.
     *
     * @param expandSimpleOptions expand simple options
     * @param expandArgumentsFile expand arguments file
     * @return next result
     */
    public NOptional<NArg> next(boolean expandSimpleOptions, boolean expandArgumentsFile) {
        if (ensureNext(expandSimpleOptions, false, expandArgumentsFile)) {
            if (!lookahead.isEmpty()) {
                return NOptional.of(lookahead.remove(0));
            }
            String v = args.removeFirst();
            return NOptional.of(createArgument(v));
        } else {
            /**
             * Empty optional cformat.
             *
             * @param argument" argument"
             * @return empty optional cformat result
             */
            return emptyOptionalCformat("missing argument");
        }
    }

    @Override
    public String toString() {
        /**
         * Converts to string list.
         *
         * @param NStringUtils.formatStringLiteral(x n string utils.format string literal(x
         * @param NElementType.DOUBLE_QUOTED_STRING n element type.double_quoted_string
         * @param ") ")
         * @return to string list result
         */
        return toStringList().stream().map(x -> NStringUtils.formatStringLiteral(x, NElementType.DOUBLE_QUOTED_STRING, NSupportMode.PREFERRED)).collect(Collectors.joining(" "));
    }

    /**
     * Creates a new instance of create expanded simple option.
     *
     * @param start start
     * @param negate negate
     * @param val val
     * @return create expanded simple option result
     */
    private String createExpandedSimpleOption(char start, boolean negate, char val) {
        return new String(negate ? new char[]{start, '!', val} : new char[]{start, val});
    }

    /**
     * Creates a new instance of create expanded simple option.
     *
     * @param start start
     * @param negate negate
     * @param val val
     * @return create expanded simple option result
     */
    private String createExpandedSimpleOption(char start, boolean negate, String val) {
        StringBuilder sb = new StringBuilder();
        sb.append(start);
        if (negate) {
            sb.append('!');
        }
        sb.append(val);
        return sb.toString();
    }

    /**
     * Load args.
     *
     * @param path path
     * @param currentDir current dir
     * @param visited visited
     * @return load args result
     */
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
                s = NShellFamily.current();
//                s = NShellFamily.BASH;
            }
            String fileContent = path.readString();
            List<String> parsed = new ArrayList<>();
            for (String line : new NStringBuilderImpl(fileContent).lines().toList()) {
                if (!NBlankable.isBlank(line) && !NStringUtils.strip(line).startsWith("#")) {
                    NCmdLine subCmd = NCmdLine.parse(line, s).get();
                    subCmd.expandArgumentsFile(false);
                    subCmd.expandArgumentsFile(false);
                    parsed.addAll(subCmd.toStringList());
                }
            }
            for (String arg : parsed) {
                if (arg.length() > 3 && arg.startsWith("--@")) {
                    NPath nPath = NPath.of(arg.substring(3));
                    NPath parent = path.parent();
                    all.addAll(loadArgs(nPath, parent == null ? currentDir : parent, visited));
                } else {
                    all.add(arg);
                }
            }
            return all;
        } else {
            if (path.exists()) {
                /**
                 * N illegal argument exception.
                 *
                 * @param path) path)
                 * @return n illegal argument exception result
                 */
                throw new NIllegalArgumentException(NMsg.ofC("argument file does not exist %s", path));
            } else {
                /**
                 * N illegal argument exception.
                 *
                 * @param path) path)
                 * @return n illegal argument exception result
                 */
                throw new NIllegalArgumentException(NMsg.ofC("argument file is not a valid regular file %s", path));
            }
        }
    }

    /**
     * Ensure next.
     *
     * @param expandSimpleOptions expand simple options
     * @param ignoreExistingExpanded ignore existing expanded
     * @param expandArgumentsFile expand arguments file
     * @return ensure next result
     */
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

    /**
     * Creates a new instance of create argument.
     *
     * @param v v
     * @return create argument result
     */
    private NArg createArgument(String v) {
        return new DefaultNArg(v, eq, this);
    }

    /**
     * Checks if is at complete position.
     *
     * @return is at complete position result
     */
    private boolean isAtCompletePosition() {
        /**
         * Checks if is complete mode.
         *
         * @param completePosition().wordIndex( complete position().word index(
         * @return is complete mode result
         */
        return isCompleteMode() && wordIndex() == completePosition().wordIndex();
    }

    /**
     * Copy.
     *
     * @return copy result
     */
    public NCmdLine copy() {
        DefaultNCmdLine c = new DefaultNCmdLine();
        c.setArguments(toStringArray());
        c.completePosition = completePosition;
        c.shellFamily(shellFamily);
        c.expandArgumentsFile(expandArgumentsFile);
        c.expandSimpleOptions(expandSimpleOptions);
        c.eq = this.eq;
        c.specialSimpleOptions = new HashSet<>(specialSimpleOptions);
        c.commandName = this.commandName;
        c.configurable = this.configurable;
        c.source = this.source;
        return c;
    }

    /**
     * Highlight text.
     *
     * @param text text
     * @return highlight text result
     */
    private NMsg highlightText(String text) {
        return NMsg.ofStyledPrimary3(String.valueOf(text));
    }

    /**
     * Checks if is punctuation.
     *
     * @param c c
     * @return is punctuation result
     */
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

    /**
     * Parse default list.
     *
     * @param commandLineString command line string
     * @return parse default list result
     */
    public static NOptional<String[]> parseDefaultList(String commandLineString) {
        /**
         * Parse default list.
         *
         * @param commandLineString command line string
         * @param null null
         * @param HashSet<>() hash set<>()
         * @return parse default list result
         */
        return parseDefaultList(commandLineString, null, new HashSet<>());
    }

    /**
     * Parse default list.
     *
     * @param commandLineString command line string
     * @param currentFolder current folder
     * @param loaded loaded
     * @return parse default list result
     */
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
                return NOptional.ofError(() -> NMsg.ofP("expected quote"));
            }
        }
        return NOptional.of(args.toArray(new String[0]));
    }

    /**
     * Read escaped arg.
     *
     * @param charArray char array
     * @param i i
     * @param sb sb
     * @return read escaped arg result
     */
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
              /**
               * Adds add.
               *
               * @param argument argument
               */
                add(argument);
            }
        }
        return this;
    }

    @Override
    public boolean isBlank() {
        /**
         * Checks if is empty.
         *
         * @return is empty result
         */
        return isEmpty();
    }



    /**
     * Push back.
     *
     * @param args args
     * @return push back result
     */
    public NCmdLine pushBack(NArg... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * Push back.
     *
     * @param args args
     * @return push back result
     */
    public NCmdLine pushBack(String... args) {
        if (args != null) {
            this.lookahead.addAll(0, Arrays.stream(args).map(x -> new DefaultNArg(x == null ? "" : x, this)).collect(Collectors.toList()));
        }
        return this;
    }

    /**
     * Append.
     *
     * @param args args
     * @return append result
     */
    public NCmdLine append(String... args) {
        if (args != null) {
            this.args.addAll(Arrays.stream(args).map(x -> x == null ? "" : x).collect(Collectors.toList()));
        }
        return this;
    }

    //    private class MyNCmdLineArgProcessor implements NCmdLineArgProcessor {
//        private final boolean finalAcceptable;
//        private final String[] names;
//
//        public MyNCmdLineArgProcessor(boolean finalAcceptable, String... names) {
//            this.finalAcceptable = finalAcceptable;
//            this.names = names;
//        }
//
//        public boolean isAcceptable() {
//            return finalAcceptable;
//        }
//
//        @Override
//        public boolean nextFlag(Consumer<NArg> consumer) {
//            if (!finalAcceptable) {
//                return false;
//            }
//            NOptional<NArg> v = next(NArgType.FLAG, names);
//            if (v.isPresent()) {
//                NArg a = v.get();
//                if (a.isUncommented()) {
//                    consumer.accept(a);
//                    return true;
//                }
//                return true;
//            }
//            return false;
//        }
//
//        @Override
//        public boolean nextEntry(Consumer<NArg> consumer) {
//            if (!finalAcceptable) {
//                return false;
//            }
//            NOptional<NArg> v = next(NArgType.ENTRY, names);
//            if (v.isPresent()) {
//                NArg a = v.get();
//                if (a.isUncommented()) {
//                    consumer.accept(a);
//                    return true;
//                }
//                return true;
//            }
//            return false;
//        }
//
//
//        @Override
//        public boolean nextTrueFlag(Consumer<NArg> consumer) {
//            if (!finalAcceptable) {
//                return false;
//            }
//            return nextFlag((value) -> {
//                if (value.getBooleanValue().isPresent() && value.booleanValue()) {
//                    consumer.accept(value);
//                }
//            });
//        }
//    }

}
