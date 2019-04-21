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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

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
 * <pre>-[//][!]?[a-z][=.*]</pre>
 * This is actually very similar to long options if expandSimpleOptions=false.
 * When activating expandSimpleOptions, multi characters key will be expanded 
 * as multiple separate simple options
 * Examples :
 * <ul>
 * <li>-!enable (with expandSimpleOptions=false) : option 'enable' with 'false' value</li>
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
 * @since 0.5.4
 */
public class NutsCommandLine {

    protected LinkedList<String> args = new LinkedList<>();
    protected LinkedList<NutsCommandArg> expanded = new LinkedList<>();
    protected boolean expandSimpleOptions = true;
    protected Set<String> specialSimpleOptions = new HashSet<>();

    public NutsCommandLine(String[] args) {
        if (args != null) {
            this.args.addAll(Arrays.asList(args));
        }
    }

    public NutsCommandLine addSpecialSimpleOption(String option) {
        specialSimpleOptions.add(option);
        return this;
    }

    public boolean isExpandSimpleOptions() {
        return expandSimpleOptions;
    }

    public NutsCommandLine expandSimpleOptions() {
        return expandSimpleOptions(true);
    }

    public NutsCommandLine expandSimpleOptions(boolean expand) {
        return setExpandSimpleOptions(expand);
    }

    public NutsCommandLine setExpandSimpleOptions(boolean expand) {
        this.expandSimpleOptions = expand;
        return this;
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

    public void clear() {
        args.clear();
        expanded.clear();
    }

    public List<String> getArgs() {
        ArrayList<String> p = new ArrayList<>();
        for (NutsCommandArg a : expanded) {
            p.add(a.toString());
        }
        for (String a : args) {
            p.add(a);
        }
        return p;
    }

    /**
     * returns un-parsed arguments and then clears thems
     *
     * @return
     */
    public List<String> removeAll() {
        List<String> x = getArgs();
        clear();
        return x;
    }

    /**
     * if the argument defines a values (with =) it will be returned : If not
     * consumes the next argument (without expanding simple options)
     *
     * @param cmdArg
     * @return
     */
    public NutsCommandArg getValueFor(NutsCommandArg cmdArg) {
        NutsCommandArg v = cmdArg.getValue();
        if (v.isNull()) {
            return next(true, false);
        }
        return v;
    }

    public NutsCommandArg next(boolean required) {
        return next(required, expandSimpleOptions);
    }

    public boolean isSpecialOneDashOption(String v) {
        for (String x : specialSimpleOptions) {
            if (v.equals("-" + x) || v.startsWith("-" + x + "=")) {
                return true;
            }
        }
        return false;
    }

    public NutsCommandArg next() {
        return next(false, expandSimpleOptions);
    }

    private NutsCommandArg next(boolean required, boolean expandSimpleOptions) {
        if (!expanded.isEmpty()) {
            return expanded.removeFirst();
        }
        if (!args.isEmpty()) {
            // -!abc=true
            String v = args.removeFirst();
            if (expandSimpleOptions && (v.length() > 2 && v.charAt(0) == '-' && v.charAt(1) != '-' && !isSpecialOneDashOption(v))) {
                char[] chars = v.toCharArray();
                boolean negate = false;
                Character c = null;
                for (int i = 1; i < chars.length; i++) {
                    switch (chars[i]) {
                        case '!': {
                            if (c != null) {
                                if (negate) {
                                    expanded.add(new NutsCommandArg("-!" + c));
                                } else {
                                    expanded.add(new NutsCommandArg("-" + c));
                                }
                                c = null;
                            }
                            negate = true;
                            break;
                        }
                        case '=': {
                            if (c == null) {
                                if (negate) {
                                    expanded.add(new NutsCommandArg("-!" + new String(chars, i, chars.length - i)));
                                    negate = false;
                                } else {
                                    expanded.add(new NutsCommandArg("-" + new String(chars, i, chars.length - i)));
                                }
                            } else {
                                if (negate) {
                                    negate = false;
                                    expanded.add(new NutsCommandArg("-!" + c + new String(chars, i, chars.length - i)));
                                } else {
                                    expanded.add(new NutsCommandArg("-" + c + new String(chars, i, chars.length - i)));
                                }
                            }
                            c = null;
                            i = chars.length;
                            break;
                        }
                        default: {
                            if (c != null) {
                                if (negate) {
                                    expanded.add(new NutsCommandArg("-!" + c));
                                    negate = false;
                                } else {
                                    expanded.add(new NutsCommandArg("-" + c));
                                }
                            }
                            c = chars[i];
                        }
                    }
                }
                if (c != null) {
                    if (negate) {
                        expanded.add(new NutsCommandArg("!" + c));
                        negate = false;
                    } else {
                        expanded.add(new NutsCommandArg("" + c));
                    }
                }
                return expanded.removeFirst();
            }
            return new NutsCommandArg(v);
        }
        if (required) {
            throw new NoSuchElementException("Missing argument");
        }
        return null;
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

    private static int readEscaped(char[] charArray, int i, StringBuilder sb) {
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

    public boolean hasNext() {
        return !expanded.isEmpty() || !args.isEmpty();
    }

}
