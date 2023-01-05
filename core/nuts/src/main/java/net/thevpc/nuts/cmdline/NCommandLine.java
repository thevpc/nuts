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
public interface NCommandLine extends Iterable<NArg>, NFormattable, NBlankable {

    static NCommandLine of(String[] args) {
        return new DefaultNCommandLine(args);
    }

    static NCommandLine of(List<String> args) {
        return new DefaultNCommandLine(args);
    }

    /**
     * parses the line into a command line using system shell family
     *
     * @param line line to parse
     * @return new command line instance
     */
    static NOptional<NCommandLine> parseDefault(String line) {
        return DefaultNCommandLine.parseDefaultList(line)
                .map(DefaultNCommandLine::new);
    }

    static NOptional<NCommandLine> parseSystem(String line, NSession session) {
        return NOptional.of(NCommandLines.of(session).parseCommandline(line));
    }

    static NOptional<NCommandLine> parseSystem(String line, NShellFamily shellFamily, NSession session) {
        return NOptional.of(NCommandLines.of(session)
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
    static NCommandLine of(String line, NShellFamily shellFamily, NSession session) {
        return NCommandLines.of(session).setShellFamily(shellFamily).parseCommandline(line);
    }

    /**
     * autocomplete instance
     *
     * @return autocomplete instance
     */
    NCommandAutoComplete getAutoComplete();

    /**
     * set autocomplete instance
     *
     * @param autoComplete autocomplete instance
     * @return {@code this} instance
     */
    NCommandLine setAutoComplete(NCommandAutoComplete autoComplete);

    /**
     * unregister {@code options} as simple (with simple '-') option. This
     * method helps considering '-version' as a single simple options when
     * {@code isExpandSimpleOptions()==true}
     *
     * @param option option
     * @return {@code this} instance
     */
    NCommandLine unregisterSpecialSimpleOption(String option);

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
     * @param option option
     * @return {@code this} instance
     */
    NCommandLine registerSpecialSimpleOption(String option);


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
    NCommandLine setCommandName(String commandName);

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
    NCommandLine setExpandSimpleOptions(boolean expand);

    /**
     * throw exception if command line is not empty
     *
     * @param errorMessage message to throw
     * @return {@code this} instance
     */
    NCommandLine throwUnexpectedArgument(NString errorMessage);

    /**
     * throw exception if command line is not empty
     *
     * @param errorMessage message to throw
     * @return {@code this} instance
     */
    NCommandLine throwUnexpectedArgument(NMsg errorMessage);

    NCommandLine throwMissingArgument();

    NCommandLine throwMissingArgument(NMsg errorMessage);

    NCommandLine throwMissingArgumentByName(String argumentName);


        /**
         * throw exception if command line is not empty
         *
         * @return {@code this} instance
         */
    NCommandLine throwUnexpectedArgument();

    /**
     * push back argument so that it will be first to be retrieved (using next
     * methods)
     *
     * @param arg argument
     * @return {@code this} instance
     */
    NCommandLine pushBack(NArg arg);

    /**
     * consume (remove) the first argument and return it return null if not
     * argument is left
     *
     * @return next argument
     */
    NOptional<NArg> next();

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
    NOptional<NArg> nextBoolean(String... names);

    /**
     * next argument with boolean value equivalent to
     * next(NutsArgumentType.STRING,{})
     *
     * @return next argument
     */
    NOptional<NArg> nextBoolean();

    /**
     * consume next argument with boolean value and run {@code consumer}
     *
     * @return true if active
     */
    boolean withNextBoolean(NArgProcessor<Boolean> consumer);

    boolean withNextOptionalBoolean(NArgProcessor<NOptional<Boolean>> consumer);

    boolean withNextOptionalBoolean(NArgProcessor<NOptional<Boolean>> consumer, String... names);

    boolean withNextTrue(NArgProcessor<Boolean> consumer);

    /**
     * consume next argument with boolean value and run {@code consumer}
     *
     * @param names names
     * @return true if active
     */
    boolean withNextBoolean(NArgProcessor<Boolean> consumer, String... names);

    boolean withNextTrue(NArgProcessor<Boolean> consumer, String... names);


    boolean withNextOptionalString(NArgProcessor<NOptional<String>> consumer);

    boolean withNextOptionalString(NArgProcessor<NOptional<String>> consumer, String... names);

    /**
     * next argument with string value. equivalent to
     * next(NutsArgumentType.STRING,names)
     *
     * @param names names
     * @return next argument
     */
    NOptional<NArg> nextString(String... names);

    /**
     * consume next argument with string value and run {@code consumer}
     *
     * @return true if active
     */
    boolean withNextString(NArgProcessor<String> consumer);

    /**
     * consume next argument with string value and run {@code consumer}
     *
     * @param names names
     * @return true if active
     */
    boolean withNextString(NArgProcessor<String> consumer, String... names);


    /**
     * consume next argument and run {@code consumer}
     *
     * @return true if active
     */
    boolean withNextStringLiteral(NArgProcessor<NLiteral> consumer);

    /**
     * consume next argument and run {@code consumer}
     *
     * @param names names
     * @return true if active
     */
    boolean withNextStringLiteral(NArgProcessor<NLiteral> consumer, String... names);

    boolean withNextLiteral(NArgProcessor<NLiteral> consumer);

        /**
         * next argument with string value. equivalent to
         * next(NutsArgumentType.STRING,{})
         *
         * @return next argument
         */
    NOptional<NArg> nextString();

    NOptional<NLiteral> nextStringLiteral(String... names);

    NOptional<NLiteral> nextBooleanLiteral(String... names);


    NOptional<NLiteral> nextStringLiteral();

    NOptional<NLiteral> nextBooleanLiteral();

    /**
     * next argument with any value type (may have not a value). equivalent to
     * {@code next(NutsArgumentType.ANY,names)}
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
    NOptional<NArg> next(NArgumentType expectValue, String... names);

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @return next argument if it exists and it is a non option
     */
    NOptional<NArg> nextNonOption();

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
     */
    NOptional<NArg> nextNonOption(NArgName name);

    /**
     * next argument if it exists and it is a non option. Return null in all
     * other cases.
     *
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
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
    NCommandLine setArguments(List<String> arguments);

    /**
     * reset this instance with the given arguments
     *
     * @param arguments to parse
     * @return reset this instance with the given arguments
     */
    NCommandLine setArguments(String[] arguments);

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
    void throwError(NString message);

    NCommandLineFormat formatter(NSession session);

    /**
     * add new argument (ignoring null values)
     * since 0.8.4
     *
     * @param argument new argument
     * @return reset this instance
     */
    NCommandLine add(String argument);

    NCommandLine addAll(List<String> arguments);
    NSession getSession();

    NCommandLine setSession(NSession session);

    void process(NCommandLineProcessor processor, NCommandLineContext context);

}
