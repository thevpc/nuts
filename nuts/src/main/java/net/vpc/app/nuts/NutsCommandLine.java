/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

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
 * <li>--!enable=yes : invalid option (no error will be thrown buts the result
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
 * <li>--!enable=yes : invalid option (no error will be thrown buts the result
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
 */
public interface NutsCommandLine {

    ////////////////////////////////////////////////
    NutsCommandLine autoComplete(NutsCommandAutoComplete autoComplete);

    NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete);

    NutsCommandLine removeSpecialSimpleOption(String option);

    NutsCommandLine addSpecialSimpleOption(String option);

    NutsCommandLine expandSimpleOptions();

    NutsCommandLine expandSimpleOptions(boolean expand);

    int getWordIndex();

    boolean isExecMode();

    boolean isAutoCompleteMode();

    /**
     *
     * @return command name that will be used as an extra info in thrown
     * exceptions
     */
    String getCommandName();

    /**
     * set command name that will be used as an extra info in thrown exceptions
     *
     * @param commandName
     * @return
     */
    NutsCommandLine setCommandName(String commandName);

    NutsCommandLine commandName(String commandName);

    String[] getSpecialSimpleOptions();

    boolean isExpandSimpleOptions();

    NutsCommandLine setExpandSimpleOptions(boolean expand);

    boolean isSpecialOneDashOption(String v);

    ////////////////////////////////////////////////
    NutsCommandLine requireNonOption();

    NutsCommandLine unexpectedArgument(String errorMessage);
    
    NutsCommandLine unexpectedArgument();

    NutsCommandLine required();
    
    NutsCommandLine required(String errorMessage);

    NutsCommandLine pushBack(NutsArgument a);

    /**
     * consume (remove) the first argument and return it
     * return null if not argument is left
     * @return next argument
     */
    NutsArgument next();

    /**
     * consume (remove) the first argument and return it while
     * adding a hint to Auto Complete about expected argument candidates
     * return null if not argument is left
     * @param name expected argument name
     * @return next argument
     */
    NutsArgument next(NutsArgumentName name);

    /**
     * the first argument to consume without removing/consuming it
     * or null if not argument is left
     * @return the first argument to consume without removing/consuming it
     */
    NutsArgument peek();

    /**
     * true if there still at least one argument to consume
     * @return true if there still at least one argument to consume
     */
    boolean hasNext();

    /**
     * next argument with boolean value
     * equivalent to next(NutsArgumentType.STRING,names)
     * @param names names
     * @return next argument
     */
    NutsArgument nextBoolean(String... names);

    /**
     * next argument with string value.
     * equivalent to next(NutsArgumentType.STRING,names)
     * @param names names
     * @return next argument
     */
    NutsArgument nextString(String... names);

    /**
     * next argument with immediate string value.
     * equivalent to next(NutsArgumentType.IMMEDIATE,names)
     * @param names names
     * @return next argument
     */
    NutsArgument nextImmediate(String... names);

    /**
     * next argument with any value type (may having not a value).
     * equivalent to next(NutsArgumentType.VOID,names)
     * @param names names
     * @return next argument
     */
    NutsArgument next(String... names);

    NutsArgument next(NutsArgumentType expectValue, String... names);

    /**
     * next argument if it exists and it is a non option. 
     * Throw an error in all other cases.
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
     */
    NutsArgument nextRequiredNonOption(NutsArgumentName name);

    /**
     * next argument if it exists and it is a non option. Return null in all other cases.
     * @return next argument if it exists and it is a non option
     */
    NutsArgument nextNonOption();

    /**
     * next argument if it exists and it is a non option. Return null in all other cases.
     * @param name argument specification (may be null)
     * @return next argument if it exists and it is a non option
     */
    NutsArgument nextNonOption(NutsArgumentName name);

    int skipAll();

    int skip();

    int skip(int count);

    boolean accept(String... values);

    boolean accept(int pos, String... values);

    NutsArgument find(String name);

    NutsArgument get(int i);

    boolean contains(String name);

    int indexOf(String name);

    int length();

    boolean isEmpty();

    /**
     * returns un-parsed arguments
     *
     * @return un-parsed arguments
     */
    String[] toArray();

    NutsArgument newArgument(String val);

    boolean isOption(int index);

    boolean isNonOption(int index);

    default NutsArgumentName createName(String type) {
        return createName(type, type);
    }

    NutsArgumentName createName(String type, String label);

    NutsCommandAutoComplete getAutoComplete();

//    String getCommandLine();

}
