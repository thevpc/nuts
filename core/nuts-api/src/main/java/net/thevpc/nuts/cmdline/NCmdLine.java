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

import net.thevpc.nuts.internal.rpi.NCmdLineRPI;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

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

    /**
     * Creates a new instance of of args.
     *
     * @param args args
     * @return of args result
     */
    static NCmdLine ofArgs(String... args) {
        NShellFamily current = NShellFamily.current();
        return NCmdLineRPI.of().createCmdLineByArgs(args, current);
    }

    /**
     * Creates a new instance of of args.
     *
     * @param family family
     * @param args args
     * @return of args result
     */
    static NCmdLine ofArgs(NShellFamily family, String... args) {
        return NCmdLineRPI.of().createCmdLineByArgs(args, family);
    }

    /**
     * Creates a new instance of of.
     *
     * @param args args
     * @return of result
     */
    static NCmdLine of(String[] args) {
        /**
         * Creates a new instance of of args.
         *
         * @param args args
         * @return of args result
         */
        return ofArgs(args);
    }

    /**
     * Creates a new instance of of.
     *
     * @param args args
     * @return of result
     */
    static NCmdLine of(List<String> args) {
        /**
         * Creates a new instance of of args.
         *
         * @param String[0]) string[0])
         * @return of args result
         */
        return ofArgs(args == null ? null : args.toArray(new String[0]));
    }

    /**
     * parses the line into a command line using default shell family (always BASH)
     *
     * @param line line to parse
     * @return new command line instance
     */
    static NOptional<NCmdLine> parseDefault(String line) {
        return NCmdLineRPI.of().parseCmdLine(line, NShellFamily.BASH, false);
    }

    /**
     * Parse.
     *
     * @param line line
     * @return parse result
     */
    static NOptional<NCmdLine> parse(String line) {
        /**
         * Parse.
         *
         * @param line line
         * @param NShellFamily.BASH n shell family.bash
         * @param false false
         * @return parse result
         */
        return parse(line, NShellFamily.BASH, false);
    }

    /**
     * Parse.
     *
     * @param line line
     * @param shellFamily shell family
     * @return parse result
     */
    static NOptional<NCmdLine> parse(String line, NShellFamily shellFamily) {
        /**
         * Parse.
         *
         * @param line line
         * @param shellFamily shell family
         * @param false false
         * @return parse result
         */
        return parse(line, shellFamily, false);
    }

    /**
     * Parse.
     *
     * @param line line
     * @param shellFamily shell family
     * @param lenient lenient
     * @return parse result
     */
    static NOptional<NCmdLine> parse(String line, NShellFamily shellFamily, boolean lenient) {
        if (NWorkspace.get().isNotPresent()) {
            /**
             * Parse default.
             *
             * @param line line
             * @return parse default result
             */
            return parseDefault(line);
        }
        return NCmdLineRPI.of()
                .parseCmdLine(line, shellFamily, lenient);
    }

    /**
     * parses the line into a command line using the provided shell family
     *
     * @param line        line to parse
     * @param shellFamily shell family
     * @return new command line instance
     */
    static NCmdLine of(String line, NShellFamily shellFamily) {
        /**
         * Parse.
         *
         * @param line line
         * @param shellFamily).get( shell family).get(
         * @return parse result
         */
        return parse(line, shellFamily).get();
    }

    /**
     * parses the line into a command line using the current system's  shell family
     *
     * @param line line to parse
     * @return
     */
    static NCmdLine of(String line) {
        /**
         * Parse.
         *
         * @param line).get( line).get(
         * @return parse result
         */
        return parse(line).get();
    }

    /**
     * parses the line into a command line using the default shell family (always BASH)
     *
     * @param line
     * @return
     */
    static NCmdLine ofDefault(String line) {
        /**
         * Parse default.
         *
         * @param line).get( line).get(
         * @return parse default result
         */
        return parseDefault(line).get();
    }

    /**
     * Returns an optional user-defined object attached to this command line,
     * typically used to identify the origin of the arguments
     * (e.g. a config file path, a plugin descriptor, or a request context).
     * Not used internally by NCmdLine.
     *
     * @return source object or null
     */
    Object source();

    /**
     * Attaches a user-defined object to this command line to identify its origin.
     * Not used internally by NCmdLine.
     *
     * @param source any object representing the source of this command line
     * @return {@code this} instance
     */
    NCmdLine source(Object source);


    /**
     * Configurable.
     *
     * @return configurable result
     */
    @NGetter
    NCmdLineConfigurable configurable();

    /**
     * Configurable.
     *
     * @param configurable configurable
     * @return configurable result
     */
    @NSetter
    NCmdLine configurable(NCmdLineConfigurable configurable);

    /**
     * Checks if is expand arguments file.
     *
     * @return is expand arguments file result
     */
    boolean isExpandArgumentsFile();

    /**
     * Expand arguments file.
     *
     * @param expandArgumentsFile expand arguments file
     * @return expand arguments file result
     */
    @NSetter
    NCmdLine expandArgumentsFile(boolean expandArgumentsFile);

    /**
     * autocomplete instance
     *
     * @return complete instance
     */
    @NGetter
    NArgCompleteResult completeResult();

    /**
     * Print complete result.
     *
     * @return print complete result result
     */
    NArgCompleteResult printCompleteResult();

    /**
     * set complete instance
     *
     * @param completePosition autocomplete instance
     * @return {@code this} instance
     */
    @NSetter
    NCmdLine completePosition(NArgCompletePosition completePosition);

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
    @NGetter
    String[] specialSimpleOptions();

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
    @NGetter
    int wordIndex();

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
    boolean isCompleteMode();

    /**
     * @return command name that will be used as an extra info in thrown
     * exceptions
     */
    @NGetter
    String commandName();

    /**
     * set command name that will be used as an extra info in thrown exceptions
     *
     * @param commandName commandName
     * @return {@code this} instance
     */
    @NSetter
    NCmdLine commandName(String commandName);

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
    @NSetter
    NCmdLine expandSimpleOptions(boolean expand);

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

    /**
     * Throw missing argument.
     *
     * @return throw missing argument result
     */
    NCmdLine throwMissingArgument();

    /**
     * Throw missing argument.
     *
     * @param errorMessage error message
     * @return throw missing argument result
     */
    NCmdLine throwMissingArgument(NMsg errorMessage);

    /**
     * Throw missing argument.
     *
     * @param argumentName argument name
     * @return throw missing argument result
     */
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

    /**
     * the first argument to consume without removing/consuming it or null if
     * not argument is left
     *
     * @return the first argument to consume without removing/consuming it
     */
    NOptional<NArg> peek();

    /**
     * Peek non option.
     *
     * @return peek non option result
     */
    NOptional<NArg> peekNonOption();

    /**
     * Peek option.
     *
     * @return peek option result
     */
    NOptional<NArg> peekOption();

    /**
     * Checks if is next option.
     *
     * @return is next option result
     */
    boolean isNextOption();

    /**
     * Checks if is next non option.
     *
     * @return is next non option result
     */
    boolean isNextNonOption();

    /**
     * true if at least one argument remains to be consumed
     *
     * @return true if at least one argument remains to be consumed
     */
    boolean hasNext();

    /**
     * Checks if has next option.
     *
     * @return has next option result
     */
    boolean hasNextOption();

    /**
     * Checks if has next non option.
     *
     * @return has next non option result
     */
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

    /**
     * Matcher.
     *
     * @return matcher result
     */
    NCmdLineMatcher matcher();

    /**
     * Next attached entry.
     *
     * @param names names
     * @return next attached entry result
     */
    NOptional<NArg> nextAttachedEntry(String... names);

    /**
     * Next required entry.
     *
     * @param names names
     * @return next required entry result
     */
    NOptional<NArg> nextRequiredEntry(String... names);

    /**
     * Next attached entry.
     *
     * @return next attached entry result
     */
    NOptional<NArg> nextAttachedEntry();

    /**
     * Next required entry.
     *
     * @return next required entry result
     */
    NOptional<NArg> nextRequiredEntry();

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
     * Complete position.
     *
     * @return complete position result
     */
    NArgCompletePosition completePosition();

    /**
     * next argument if it exists and It's a non option. Return null in all
     * other cases.
     *
     * @return next argument if it exists and It's a non option
     */
    NOptional<NArg> nextNonOption();

    /**
     * Next.
     *
     * @param expectedArgType expected arg type
     * @param argDisplay arg display
     * @param valueComplete value complete
     * @param names names
     * @return next result
     */
    NOptional<NArg> next(NArgType expectedArgType, String argDisplay, NArgValueComplete valueComplete, String... names);

    /**
     * next non-option argument if it exists. Return null in all other cases.
     *
     * @param name argument display name hint (shown in completion)
     * @return next argument if it exists and it's a non option
     */
    NOptional<NArg> nextNonOption(String name);

    /**
     * next non-option argument if it exists, providing a display label and
     * a completion value finder for auto-complete mode. Return empty in all
     * other cases.
     *
     * @param display  display hint shown in completion suggestions
     * @param complete supplier of completion candidates for the value
     * @return next argument if it exists and it's a non option
     */
    NOptional<NArg> nextNonOption(String display, NArgValueComplete complete);

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

    /**
     * Converts to string list.
     *
     * @return to string list result
     */
    List<String> toStringList();

    /**
     * Converts to argument array.
     *
     * @return to argument array result
     */
    NArg[] toArgumentArray();

    /**
     * Next all as string array.
     *
     * @return next all as string array result
     */
    String[] nextAllAsStringArray();

    /**
     * Next all as string list.
     *
     * @return next all as string list result
     */
    List<String> nextAllAsStringList();

    /**
     * Next all as argument array.
     *
     * @return next all as argument array result
     */
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

    /**
     * Adds the specified all.
     *
     * @param arguments arguments
     * @return add all result
     */
    NCmdLine addAll(List<String> arguments);

    /**
     * Push back.
     *
     * @param args args
     * @return push back result
     */
    NCmdLine pushBack(NArg... args);

    /**
     * Push back.
     *
     * @param args args
     * @return push back result
     */
    NCmdLine pushBack(String... args);

    /**
     * Append.
     *
     * @param args args
     * @return append result
     */
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

    /**
     * Copy.
     *
     * @return copy result
     */
    NCmdLine copy();

    /**
     * Shell family.
     *
     * @return shell family result
     */
    @NGetter
    NShellFamily shellFamily();

    /**
     * Shell family.
     *
     * @param shellFamily shell family
     * @return shell family result
     */
    @NSetter
    NCmdLine shellFamily(NShellFamily shellFamily);

}
