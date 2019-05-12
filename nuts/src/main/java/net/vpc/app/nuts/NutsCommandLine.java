/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.ArrayList;
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

    NutsCommandLine setAutoComplete(NutsCommandAutoComplete autoComplete);

    NutsCommandLine removeSpecialSimpleOption(String option);

    NutsCommandLine addSpecialSimpleOption(String option);

    NutsCommandLine expandSimpleOptions();

    NutsCommandLine expandSimpleOptions(boolean expand);

    NutsCommandLine setExpandSimpleOptions(boolean expand);

    NutsCommandLine requiredNonOption();

    NutsCommandLine requireEmpty();

    NutsCommandLine unexpectedArgument();

    NutsCommandLine requireNonEmpty();

    NutsCommandLine pushBack(NutsArgument a);

    /**
     * set command name that will be used as an extra info in thrown exceptions
     *
     * @param commandName
     * @return
     */
    NutsCommandLine setCommandName(String commandName);

    /**
     *
     * @return command name that will be used as an extra info in thrown
     * exceptions
     */
    String getCommandName();

    String[] getSpecialSimpleOptions();

    boolean isExpandSimpleOptions();

    String[] getArgs();

    /**
     * returns un-parsed arguments and then clears thems
     *
     * @return
     */
    String[] removeAll();

    /**
     * if the argument defines a values (with =) it will be returned : If not
     * consumes the next argument (without expanding simple options)
     *
     * @param cmdArg
     * @return
     */
    NutsArgument getValueFor(NutsArgument cmdArg);

    NutsArgument next(boolean required);

    boolean isSpecialOneDashOption(String v);

    NutsArgument next();

    NutsArgument peek();

    boolean hasNext();

    NutsArgument readBooleanOption(String... names);

    NutsArgument readStringOption(String... names);

    NutsArgument readImmediateStringOption(String... names);

    NutsArgument readOption(String... names);

    NutsArgument readVoidOption(String... names);

    NutsArgument readOption(OptionType expectValue, String... names);

    NutsArgument readNonOption(boolean expectValue, String name);

    NutsArgument readRequiredOption(String name);

    NutsArgument readRequiredNonOption();

    NutsArgument readRequiredNonOption(NutsArgumentNonOption name);

    NutsArgument readNonOption();

    NutsArgument readNonOption(String... names);

    NutsArgument readRequiredNonOption(String name);

    NutsArgument readNonOption(NutsArgumentNonOption name);

    NutsArgument readNonOption(NutsArgumentNonOption name, boolean error);

    NutsArgument read();

    boolean readAll(boolean acceptDuplicates, String... vals);

    boolean readAll(String... vals);

    boolean readAllOnce(String... vals);

    boolean acceptSequence(int pos, String... vals);

    NutsArgument findOption(String option);

    NutsArgument get();

    NutsArgument get(int i);

    boolean containOption(String name);

    int indexOfOption(String name);

    int length();

    boolean isEmpty();

    String[] toArray();

    int getWordIndex();

    boolean isExecMode();

    boolean isAutoCompleteMode();

    NutsArgument newArgument(String val);

    boolean isOption(int index);

    boolean isNonOption(int index);

    boolean isOption(String... options);

    boolean isOption(int index, String... options);

    int skipAll();

    int skip();

    int skip(int count);

    default NutsArgumentNonOption createNonOption(String type) {
        return createNonOption(type, type);
    }

    NutsArgumentNonOption createNonOption(String type, String label);

    public enum OptionType {
        VOID,
        STRING,
        IMMEDIATE_STRING,
        BOOLEAN,
    }

    public static String escapeArgument(String arg) {
        StringBuilder sb = new StringBuilder();
        if (arg != null) {
            for (char c : arg.toCharArray()) {
                switch (c) {
                    case '\\':
                        sb.append('\\');
                        break;
                    case '\'':
                        sb.append("\\'");
                        break;
                    case '"':
                        sb.append("\\\"");
                        break;
                    case '\n':
                        sb.append("\\n");
                        break;
                    case '\t':
                        sb.append("\\t");
                        break;
                    case '\r':
                        sb.append("\\r");
                    case '\f':
                        sb.append("\\f");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
        }
        return sb.toString();
    }

    public static String escapeArguments(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(escapeArgument(arg));
        }
        return sb.toString();
    }

    public static String[] parseCommandLine(String commandLineString) {
        if (commandLineString == null) {
            return new String[0];
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
                        case ' ': {
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
                        case '\'': {
                            throw new NutsParseException("Illegal char " + c);
                        }
                        case '"': {
                            throw new NutsParseException("Illegal char " + c);
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
                        case '\\': {
                            i = readEscaped(charArray, i + 1, sb);
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
                            i = readEscaped(charArray, i + 1, sb);
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
                throw new NutsParseException("Expected '");
            }
        }
        return args.toArray(new String[0]);
    }

    public static int readEscaped(char[] charArray, int i, StringBuilder sb) {
        char c = charArray[i];
        switch (c) {
            case 'n': {
                sb.append('\n');
                break;
            }
            case 't': {
                sb.append('\t');
                break;
            }
            case 'r': {
                sb.append('\r');
                break;
            }
            case 'f': {
                sb.append('\f');
                break;
            }
            default: {
                sb.append(c);
            }
        }
        return i;
    }
}
