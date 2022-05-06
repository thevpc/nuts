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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.*;

import java.util.List;

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
public interface NutsCommandLine extends Iterable<NutsArgument>, NutsFormattable,NutsBlankable {

    static NutsCommandLine of(String[] args) {
        return new DefaultNutsCommandLine(args);
    }

    static NutsCommandLine of(List<String> args) {
        return new DefaultNutsCommandLine(args);
    }

    /**
     * parses the line into a command line using system shell family
     *
     * @param line line to parse
     * @return new command line instance
     */
    static NutsOptional<NutsCommandLine> parseDefault(String line) {
        return DefaultNutsCommandLine.parseDefaultList(line)
                .map(DefaultNutsCommandLine::new);
    }

    static NutsOptional<NutsCommandLine> parseSystem(String line, NutsSession session) {
        return NutsOptional.of(NutsCommandLines.of(session).parseCommandline(line));
    }

    static NutsOptional<NutsCommandLine> parseSystem(String line, NutsShellFamily shellFamily,NutsSession session) {
        return NutsOptional.of(NutsCommandLines.of(session)
                        .setShellFamily(shellFamily)
                .parseCommandline(line));
    }

    /**
     * parses the line into a command line using the provided shell family
     *
     * @param line        line to parse
     * @param shellFamily shell family
     * @param session     session
     * @return new command line instance
     */
    static NutsCommandLine of(String line, NutsShellFamily shellFamily, NutsSession session) {
        return NutsCommandLines.of(session).setShellFamily(shellFamily).parseCommandline(line);
    }

    /**
     * autocomplete instance
     *
     * @return autocomplete instance
     */
    NutsCommandAutoComplete getAutoComplete();

    /**
     * set autocomplete instance
     *
     * @param autoComplete autocomplete instance
     * @return {@code this} instance
     */
    NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete);

    /**
     * unregister {@code options} as simple (with simple '-') option. This
     * method helps considering '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    NutsCommandLine unregisterSpecialSimpleOption(String option);

    /**
     * list of registered simple options
     *
     * @return list of registered simple options
     */
    String[] getSpecialSimpleOptions();

    /**
     * register {@code options} as simple (with simple '-') option. This method
     * helps considering '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option  option
     * @param session session
     * @return {@code this} instance
     */
    NutsCommandLine registerSpecialSimpleOption(String option, NutsSession session);
    NutsCommandLine registerSpecialSimpleOption(String option);

        /**
         * test if the option is a registered simple option This method helps
         * considering '-version' as a single simple options when
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
    NutsCommandLine setCommandName(String commandName);

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
    NutsCommandLine setExpandSimpleOptions(boolean expand);

    /**
     * throw exception if command line is not empty
     *
     * @param errorMessage message to throw
     * @param session
     * @return {@code this} instance
     */
    NutsCommandLine throwUnexpectedArgument(NutsString errorMessage, NutsSession session);

    /**
     * throw exception if command line is not empty
     *
     * @param errorMessage message to throw
     * @param session
     * @return {@code this} instance
     */
    NutsCommandLine throwUnexpectedArgument(NutsMessage errorMessage, NutsSession session);

    NutsCommandLine throwMissingArgument(NutsSession session);

    NutsCommandLine throwMissingArgument(NutsMessage errorMessage, NutsSession session);

    /**
     * throw exception if command line is not empty
     *
     * @return {@code this} instance
     */
    NutsCommandLine throwUnexpectedArgument(NutsSession session);

    /**
     * push back argument so that it will be first to be retrieved (using next
     * methods)
     *
     * @param arg     argument
     * @param session
     * @return {@code this} instance
     */
    NutsCommandLine pushBack(NutsArgument arg, NutsSession session);

    /**
     * consume (remove) the first argument and return it return null if not
     * argument is left
     *
     * @return next argument
     */
    NutsOptional<NutsArgument> next();

    /**
     * consume (remove) the first argument and return it while adding a hint to
     * Auto Complete about expected argument candidates return null if not
     * argument is left
     *
     * @param name expected argument name
     * @return next argument
     */
    NutsOptional<NutsArgument> next(NutsArgumentName name);

    /**
     * consume (remove) the first option and return it while adding a hint to
     * Auto Complete about expected argument candidates return null if not
     * argument is left
     *
     * @param option expected option name
     * @return next argument
     */
    NutsOptional<NutsArgument> nextOption(String option);

    /**
     * the first argument to consume without removing/consuming it or null if
     * not argument is left
     *
     * @return the first argument to consume without removing/consuming it
     */
    NutsOptional<NutsArgument> peek();

    boolean isNextOption();

    boolean isNextNonOption();

    /**
     * true if there still at least one argument to consume
     *
     * @return true if there still at least one argument to consume
     */
    boolean hasNext();

    /**
     * next argument with boolean value equivalent to
     * next(NutsArgumentType.STRING,names)
     *
     * @param names names
     * @return next argument
     */
    NutsOptional<NutsArgument> nextBoolean(String... names);

    /**
     * next argument with string value. equivalent to
     * next(NutsArgumentType.STRING,names)
     *
     * @param names names
     * @return next argument
     */
    NutsOptional<NutsArgument> nextString(String... names);

    NutsOptional<String> nextStringValueLiteral(String... names);

    NutsOptional<Boolean> nextBooleanValueLiteral(String... names);

    NutsOptional<NutsValue> nextStringValue(String... names);

    NutsOptional<NutsValue> nextBooleanValue(String... names);

    /**
     * next argument with any value type (may having not a value). equivalent to
     * {@code next(NutsArgumentType.ANY,names)}
     *
     * @param names names
     * @return next argument
     */
    NutsOptional<NutsArgument> next(String... names);

    /**
     * next argument with any value type (may having not a value).
     *
     * @param expectValue expected value type
     * @param names       names
     * @return next argument
     */
    NutsOptional<NutsArgument> next(NutsArgumentType expectValue, String... names);

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @return next argument if it exists and it is a non option
     */
    NutsOptional<NutsArgument> nextNonOption();

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
     */
    NutsOptional<NutsArgument> nextNonOption(NutsArgumentName name);

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
     */
    NutsOptional<NutsArgument> nextNonOption(String name);

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
    NutsOptional<NutsArgument> find(String name);

    /**
     * return argument at given index
     *
     * @param index argument index
     * @return argument at given index
     */
    NutsOptional<NutsArgument> get(int index);

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

    NutsArgument[] toArgumentArray();

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
    NutsCommandLine setArguments(List<String> arguments);

    /**
     * reset this instance with the given arguments
     *
     * @param arguments to parse
     * @return reset this instance with the given arguments
     */
    NutsCommandLine setArguments(String[] arguments);

    /**
     * throw a new command line error
     *
     * @param message message
     * @param session session
     */
    void throwError(NutsMessage message, NutsSession session);

    /**
     * throw a new command line error
     *
     * @param message message
     * @param session session
     */
    void throwError(NutsString message, NutsSession session);

    NutsCommandLineFormat formatter(NutsSession session);

    /**
     * add new argument (ignoring null values)
     * since 0.8.4
     * @param argument new argument
     * @return reset this instance
     */
    NutsCommandLine add(String argument);
    NutsCommandLine addAll(List<String> arguments);
}
