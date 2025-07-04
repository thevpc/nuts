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

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
 * <ul>
 * <li>-!enable (with expandSimpleOptions=false) : option 'enable' with 'false'
 * value</li>
 * <li>--enable=yes : option 'enable' with 'yes' value</li>
 * <li>--!enable=yes : invalid option (no error will be thrown but the result is
 * undefined)</li>
 * </ul>
 *
 * </li>
 * <li>
 * condensed simple option arguments : any argument that starts with a single
 * '-' in the form of
 * <pre>-[//]([!]?[a-z])+[=.*]</pre> This is actually very similar to long
 * options and is parsable when expandSimpleOptions=true. When activating
 * expandSimpleOptions, multi characters key will be expanded as multiple
 * separate simple options Examples :
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
 * @app.category Command Line
 * @since 0.5.5
 */
public interface NCmdLine extends Iterable<NArg>, NBlankable {

    static NCmdLine ofArgs(String... args) {
        if (NWorkspace.get().isNotPresent()) {
            return new DefaultNCmdLine(args, NShellFamily.getCurrent());
        }
        return NCmdLines.of().of(args);
    }

    static NCmdLine of(String[] args) {
        return ofArgs(args);
    }

    static NCmdLine of(List<String> args) {
        return ofArgs(args == null ? null : args.toArray(new String[0]));
    }

    /**
     * parses the line into a command line using default shell family (always BASH)
     *
     * @param line line to parse
     * @return new command line instance
     */
    static NOptional<NCmdLine> parseDefault(String line) {
        if (NWorkspace.get().isNotPresent()) {
            return DefaultNCmdLine.parseDefaultList(line)
                    .map(args -> new DefaultNCmdLine(args, NShellFamily.BASH));
        }
        return NCmdLines.of().setShellFamily(NShellFamily.BASH).parseCmdLine(line);
    }

    static NOptional<NCmdLine> parse(String line) {
        if (NWorkspace.get().isNotPresent()) {
            return parseDefault(line);
        }
        return NCmdLines.of().parseCmdLine(line);
    }

    static NOptional<NCmdLine> parse(String line, NShellFamily shellFamily) {
        if (NWorkspace.get().isNotPresent()) {
            return parseDefault(line);
        }
        return NCmdLines.of()
                .setShellFamily(shellFamily)
                .parseCmdLine(line);
    }

    /**
     * parses the line into a command line using the provided shell family
     *
     * @param line        line to parse
     * @param shellFamily shell family
     * @return new command line instance
     */
    static NCmdLine of(String line, NShellFamily shellFamily) {
        return parse(line, shellFamily).get();
    }

    /**
     * parses the line into a command line using the current system's  shell family
     *
     * @param line line to parse
     * @return
     */
    static NCmdLine of(String line) {
        return parse(line).get();
    }

    /**
     * parses the line into a command line using the default shell family (always BASH)
     *
     * @param line
     * @return
     */
    static NCmdLine ofDefault(String line) {
        return parseDefault(line).get();
    }

    Object getSource();

    NCmdLine setSource(Object source);

    boolean isUnsafe();

    NCmdLine setUnsafe(boolean safe);

    NCmdLineConfigurable getConfigurable();

    NCmdLine setConfigurable(NCmdLineConfigurable configurable);

    boolean isExpandArgumentsFile();

    NCmdLine setExpandArgumentsFile(boolean expandArgumentsFile);

    /**
     * autocomplete instance
     *
     * @return autocomplete instance
     */
    NCmdLineAutoComplete getAutoComplete();

    /**
     * set autocomplete instance
     *
     * @param autoComplete autocomplete instance
     * @return {@code this} instance
     */
    NCmdLine setAutoComplete(NCmdLineAutoComplete autoComplete);

    /**
     * unregister {@code options} as simple (with simple '-') option. This
     * method helps considering '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    NCmdLine unregisterSpecialSimpleOption(String option);

    /**
     * list of registered simple options
     *
     * @return list of registered simple options
     */
    String[] getSpecialSimpleOptions();

    /**
     * register {@code options} as simple (with simple '-') option. This method
     * helps consider '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    NCmdLine registerSpecialSimpleOption(String option);

    /**
     * test if the option is a registered simple option This method helps
     * consider '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    boolean isSpecialSimpleOption(String option);

    /**
     * current word index
     *
     * @return current word index
     */
    int getWordIndex();

    /**
     * true if auto complete instance is not registered (is null)
     *
     * @return true if auto complete instance is not registered (is null)
     */
    boolean isExecMode();

    /**
     * true if auto complete instance is registered (is not null)
     *
     * @return true if auto complete instance is registered (is not null)
     */
    boolean isAutoCompleteMode();

    /**
     * @return command name that will be used as an extra info in thrown
     * exceptions
     */
    String getCommandName();

    /**
     * set command name that will be used as an extra info in thrown exceptions
     *
     * @param commandName commandName
     * @return {@code this} instance
     */
    NCmdLine setCommandName(String commandName);

    /**
     * true if simple option expansion is enabled
     *
     * @return true if simple option expansion is enabled
     */
    boolean isExpandSimpleOptions();

    /**
     * enable or disable simple option expansion
     *
     * @param expand expand
     * @return {@code this} instance
     */
    NCmdLine setExpandSimpleOptions(boolean expand);

    /**
     * throw exception if command line is not empty
     *
     * @param errorMessage message to throw
     * @return {@code this} instance
     */
    NCmdLine throwUnexpectedArgument(NText errorMessage);

    /**
     * throw exception if command line is not empty
     *
     * @param errorMessage message to throw
     * @return {@code this} instance
     */
    NCmdLine throwUnexpectedArgument(NMsg errorMessage);

    NCmdLine throwMissingArgument();

    NCmdLine throwMissingArgument(NMsg errorMessage);

    NCmdLine throwMissingArgument(String argumentName);

    /**
     * throw exception if command line is not empty
     *
     * @return {@code this} instance
     */
    NCmdLine throwUnexpectedArgument();

    /**
     * push back argument so that it will be first to be retrieved (using next
     * methods)
     *
     * @param arg argument
     * @return {@code this} instance
     */
    NCmdLine pushBack(NArg arg);

    /**
     * consume (remove) the first argument and return it return null if not
     * argument is left
     *
     * @return next argument
     */
    NOptional<NArg> next();

    NOptional<String> nextString();

    /**
     * consume (remove) the first argument and return it while adding a hint to
     * Auto Complete about expected argument candidates return null if not
     * argument is left
     *
     * @param name expected argument name
     * @return next argument
     */
    NOptional<NArg> next(NArgName name);

    /**
     * consume (remove) the first option and return it while adding a hint to
     * Auto Complete about expected argument candidates return null if not
     * argument is left
     *
     * @param option expected option name
     * @return next argument
     */
    NOptional<NArg> nextOption(String option);

    /**
     * the first argument to consume without removing/consuming it or null if
     * not argument is left
     *
     * @return the first argument to consume without removing/consuming it
     */
    NOptional<NArg> peek();

    NOptional<NArg> peekNonOption();

    NOptional<NArg> peekOption();

    boolean isNextOption();

    boolean isNextNonOption();

    /**
     * true if there still at least one argument to consume
     *
     * @return true if there still at least one argument to consume
     */
    boolean hasNext();

    boolean hasNextOption();

    boolean hasNextNonOption();

    /**
     * next argument with boolean value equivalent to
     * next(NArgType.STRING,names)
     *
     * @param names names
     * @return next argument
     */
    NOptional<NArg> nextFlag(String... names);

    /**
     * next argument with boolean value equivalent to next(NArgType.ENTRY,{})
     *
     * @return next argument
     */
    NOptional<NArg> nextFlag();

    /**
     * next argument with string value. equivalent to next(NArgType.ENTRY,names)
     *
     * @param names names
     * @return next argument
     */
    NOptional<NArg> nextEntry(String... names);

    Matcher matcher();

    /**
     * next argument as entry (key=value). equivalent to next(NArgType.ENTRY,{})
     *
     * @return next argument
     */
    NOptional<NArg> nextEntry();

    /**
     * next argument with any value type (may have not a value). equivalent to
     * {@code next(NArgType.ANY,names)}
     *
     * @param names names
     * @return next argument
     */
    NOptional<NArg> next(String... names);

    /**
     * next argument with any value type (may having not a value).
     *
     * @param expectValue expected value type
     * @param names       names
     * @return next argument
     */
    NOptional<NArg> next(NArgType expectValue, String... names);

    /**
     * next argument if it exists and It's a non option. Return null in all
     * other cases.
     *
     * @return next argument if it exists and It's a non option
     */
    NOptional<NArg> nextNonOption();

    /**
     * next argument if it exists and It's a non option. Return null in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and It's a non option
     */
    NOptional<NArg> nextNonOption(NArgName name);

    /**
     * next argument if it exists and It's a non option. Return null in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and It's a non option
     */
    NOptional<NArg> nextNonOption(String name);

    /**
     * consume all words and return consumed count
     *
     * @return consumed count
     */
    int skipAll();

    /**
     * skip next argument
     *
     * @return words count
     */
    int skip();

    /**
     * consume {@code count} words and return how much it was able to consume
     *
     * @param count count
     * @return consumed count
     */
    int skip(int count);

    /**
     * true if arguments start with the given suite.
     *
     * @param values arguments suite
     * @return true if arguments start with the given suite.
     */
    boolean accept(String... values);

    /**
     * true if arguments start at index {@code index} with the given suite.
     *
     * @param index  starting index
     * @param values arguments suite
     * @return true if arguments start with the given suite.
     */
    boolean accept(int index, String... values);

    /**
     * find first argument with argument key name
     *
     * @param name argument key name
     * @return find first argument with argument key name
     */
    NOptional<NArg> find(String name);

    /**
     * return argument at given index
     *
     * @param index argument index
     * @return argument at given index
     */
    NOptional<NArg> get(int index);

    /**
     * return true if any argument is equal to the given name
     *
     * @param name argument name
     * @return true if any argument is equal to the given name
     */
    boolean contains(String name);

    /**
     * first argument index (or -1 if not found) with value {@code name}
     *
     * @param name argument key name
     * @return first argument index (or -1 if not found) with value {@code name}
     */
    int indexOf(String name);

    /**
     * number of arguments available to retrieve
     *
     * @return number of arguments available to retrieve
     */
    int length();

    /**
     * true if no more arguments are available
     *
     * @return true if no more arguments are available
     */
    boolean isEmpty();

    /**
     * returns un-parsed (or partially parsed) available arguments
     *
     * @return returns un-parsed (or partially parsed) available arguments
     */
    String[] toStringArray();

    List<String> toStringList();

    NArg[] toArgumentArray();

    String[] nextAllAsStringArray();

    List<String> nextAllAsStringList();

    NArg[] nextAllAsArgumentArray();

    /**
     * true if the argument and index exists and is option
     *
     * @param index index
     * @return true if the argument and index exists and is option
     */
    boolean isOption(int index);

    /**
     * true if the argument and index exists and is non option
     *
     * @param index index
     * @return true if the argument and index exists and is non option
     */
    boolean isNonOption(int index);

    /**
     * reset this instance with the given arguments
     *
     * @param arguments to parse
     * @return reset this instance with the given arguments
     */
    NCmdLine setArguments(List<String> arguments);

    /**
     * reset this instance with the given arguments
     *
     * @param arguments to parse
     * @return reset this instance with the given arguments
     */
    NCmdLine setArguments(String[] arguments);

    /**
     * throw a new command line error
     *
     * @param message message
     */
    void throwError(NMsg message);

    /**
     * throw a new command line error
     *
     * @param message message
     */
    void throwError(NText message);

    /**
     * add new argument (ignoring null values) since 0.8.4
     *
     * @param argument new argument
     * @return reset this instance
     */
    NCmdLine add(String argument);

    NCmdLine addAll(List<String> arguments);

    void run(NCmdLineRunner processor);

    NCmdLine pushBack(NArg... args);

    NCmdLine pushBack(String... args);

    NCmdLine append(String... args);

    /**
     * creates an iterator from a snapshot of the current CmdLine. Will not
     * consume any argument in the current NCmdLine instance. use forEachPeek
     * instead.
     *
     * @return Iterator<NArg>
     */
    @Override
    Iterator<NArg> iterator();

    /**
     * Performs the given action for each element of the Iterable until all
     * elements have been processed or the action throws an exception. Will not
     * consume any argument in the current NCmdLine instance. use forEachPeek
     * instead.
     *
     * @param action The action to be performed for each element
     */
    @Override
    default void forEach(Consumer<? super NArg> action) {
        Iterable.super.forEach(action);
    }

    NCmdLine forEachPeek(NCmdLineProcessor action);

    NCmdLine forEachPeek(NCmdLineProcessor... actions);

    NCmdLine copy();

    NShellFamily getShellFamily();

    NCmdLine setShellFamily(NShellFamily shellFamily);

    interface Matcher {
        Matcher matchProcessor(NCmdLineProcessor processor);

        Matcher matchFlag(Consumer<NArg> consumer);

        Matcher matchEntry(Consumer<NArg> consumer);

        Matcher matchAny(Consumer<NArg> consumer);

        Matcher matchTrueFlag(Consumer<NArg> consumer);

        MatcherCondition withAny();

        MatcherCondition with(String... names);

        MatcherCondition withCondition(Predicate<NCmdLine> condition);

        MatcherCondition withNonOption();

        MatcherCondition withOption();

        boolean anyMatch();

        boolean noMatch();

        void requireWithDefault();

        void require();

        Matcher withDefaultLast();

        Matcher withDefaultFirst();
    }

    public interface MatcherCondition {
        /**
         * consume next argument with boolean value and run {@code consumer}
         *
         * @return true if active
         */
        Matcher matchFlag(Consumer<NArg> consumer);

        MatcherCondition and(Predicate<NCmdLine> condition);

        /**
         * consume next argument with string value and run {@code consumer}
         *
         * @return true if active
         */
        Matcher matchEntry(Consumer<NArg> consumer);

        Matcher matchAny(Consumer<NArg> consumer);

        Matcher matchAnyMultiple(Consumer<NCmdLine> consumer);

        Matcher matchTrueFlag(Consumer<NArg> consumer);
    }

}
