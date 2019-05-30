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
public interface NutsCommand {

    ////////////////////////////////////////////////
    NutsCommand autoComplete(NutsCommandAutoComplete autoComplete);

    NutsCommand setAutoComplete(NutsCommandAutoComplete autoComplete);

    NutsCommand removeSpecialSimpleOption(String option);

    NutsCommand addSpecialSimpleOption(String option);

    NutsCommand expandSimpleOptions();

    NutsCommand expandSimpleOptions(boolean expand);

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
    NutsCommand setCommandName(String commandName);

    NutsCommand commandName(String commandName);

    String[] getSpecialSimpleOptions();

    boolean isExpandSimpleOptions();

    NutsCommand setExpandSimpleOptions(boolean expand);

    boolean isSpecialOneDashOption(String v);

    ////////////////////////////////////////////////
    NutsCommand requireNonOption();

    NutsCommand unexpectedArgument();

    NutsCommand required();

    NutsCommand pushBack(NutsArgument a);

    NutsArgument next();

    NutsArgument peek();

    boolean hasNext();

    /**
     * next argument with boolean value
     *
     * @param names
     * @return
     */
    NutsArgument nextBoolean(String... names);

    /**
     * next argument with string value
     *
     * @param names
     * @return
     */
    NutsArgument nextString(String... names);

    NutsArgument nextImmediate(String... names);

    NutsArgument next(String... names);

    NutsArgument next(NutsArgumentType expectValue, String... names);

    NutsArgument nextRequiredNonOption(NutsArgumentNonOption name);

    NutsArgument nextNonOption();

    NutsArgument nextNonOption(NutsArgumentNonOption name);

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

    default NutsArgumentNonOption createNonOption(String type) {
        return createNonOption(type, type);
    }

    NutsArgumentNonOption createNonOption(String type, String label);

    NutsCommandAutoComplete getAutoComplete();

    String getCommandLine();

}
