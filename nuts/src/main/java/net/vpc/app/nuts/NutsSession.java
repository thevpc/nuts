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

import java.io.PrintStream;
import java.util.Map;

/**
 * session is context defining common command options and parameters.
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsSession extends NutsConfigurable {

    /**
     * When true, operations are invited to print to output stream extra
     * information about processing. Output may be in different formats
     * according to {@link #getOutputFormat()} and {@link #getIterableFormat()}
     *
     * @return true if trace flag is armed
     */
    boolean isTrace();

    /**
     * true if non iterable and plain formats along with trace flag are armed.
     * equivalent to {@code isTrace()
     * && !isIterableOut()
     * && getOutputFormat() == NutsOutputFormat.PLAIN}
     *
     * @return true plain non iterable format AND trace are armed
     */
    boolean isPlainTrace();

    /**
     * true if iterable format and trace flag are armed. equivalent to {@code isTrace()
     * && isIterableOut()}
     *
     * @return true plain non iterable format AND trace are armed
     */
    boolean isIterableTrace();

    /**
     * true if NON iterable and NON plain formats along with trace flag are
     * armed. equivalent to {@code isTrace()
     * && !isIterableOut()
     * && getOutputFormat() == NutsOutputFormat.PLAIN}
     *
     * @return true if NON iterable and NON plain formats along with trace flag
     * are armed.
     */
    boolean isStructuredTrace();

    /**
     * true if iterable format is armed. equivalent to
     * {@code  getIterableFormat()!=null}
     *
     * @return true if iterable format is armed.
     */
    boolean isIterableOut();

    /**
     * true if NON iterable and NON plain formats are armed. equivalent to {@code !isIterableOut()
     * && getOutputFormat() != NutsOutputFormat.PLAIN}
     *
     * @return true if non iterable format AND structured outpt format are
     * armed.
     */
    boolean isStructuredOut();

    /**
     * true if NON iterable and plain format are armed.
     *
     * @return true if non iterable format AND structured outpt format are
     * armed.
     */
    boolean isPlainOut();

    /**
     * change trace flag value. When true, operations are invited to print to
     * output stream information about processing. Output may be in different
     * formats according to {@link #getOutputFormat()} and
     * {@link #getIterableFormat()}
     *
     * @param trace new value
     * @return {@code this} instance
     */
    NutsSession setTrace(boolean trace);

    /**
     * equivalent to {@code setConfirm(true)}
     *
     * @return {@code this} instance
     */
    NutsSession trace();

    /**
     * equivalent to {@code setTrace(trace)}
     *
     * @param trace new value
     * @return {@code this} instance
     */
    NutsSession trace(boolean trace);

    /**
     * true if force flag is armed. some operations may require user
     * confirmation before performing critical operations such as overriding
     * existing values, deleting sensitive informations ; in such cases, arming
     * force flag will provide an implicit confirmation.
     *
     * @return true if force flag is armed.
     */
    boolean isForce();

    /**
     * /**
     * change force flag value. some operations may require user confirmation
     * before performing critical operations such as overriding existing values,
     * deleting sensitive informations ; in such cases, arming force flag will
     * provide an implicit confirmation.
     *
     * @param enable if true force flag is armed
     * @return {@code this} instance
     */
    NutsSession setForce(boolean enable);

    /**
     * equivalent to {@code setForce(true)}
     *
     * @return {@code this} instance
     */
    NutsSession force();

    /**
     * equivalent to {@code setForce(force)}
     *
     * @param enable new value
     * @return {@code this} instance
     */
    NutsSession force(boolean enable);

    /**
     * equivalent to {@code setAsk(true)}
     *
     * @return {@code this} instance
     */
    NutsSession ask();

    /**
     * equivalent to {@code setAsk(enable)}
     *
     * @param enable new value
     * @return {@code this} instance
     */
    NutsSession ask(boolean enable);

    /**
     * equivalent to {@code setConfirm(enable?ASK:null)}
     *
     * @param enable new value
     * @return {@code this} instance
     */
    NutsSession setAsk(boolean enable);

    /**
     * true if ASK is armed.
     *
     * @return true if ASK is armed.
     */
    boolean isAsk();

    /**
     * return current Output Format or {@code defaultValue} if null
     *
     * @param defaultValue value when Output Format is not set
     * @return current Output Format or {@code defaultValue} if null
     */
    NutsOutputFormat getOutputFormat(NutsOutputFormat defaultValue);

    /**
     * return effective trace output format. The effective trace output format
     * is the value of {@code getIterableFormat().getOutputFormat()} whenever {@code getIterableFormat()!=null
     * } otherwise it returns simply the value defined by calling
     * {@link #setOutputFormat(net.vpc.app.nuts.NutsOutputFormat)}. If none of      {@link #setIterableFormat(net.vpc.app.nuts.NutsIterableFormat)
     * } or {@link #setOutputFormat(net.vpc.app.nuts.NutsOutputFormat)} has been
     * called (or called with null values) {@link NutsOutputFormat#PLAIN} should
     * be returned.
     *
     * @return effective trace output format
     */
    NutsOutputFormat getOutputFormat();

    /**
     * set output format
     *
     * @param outputFormat output format
     * @return {@code this} instance
     */
    NutsSession outputFormat(NutsOutputFormat outputFormat);

    /**
     * set output format
     *
     * @param outputFormat output format
     * @return {@code this} instance
     */
    NutsSession setOutputFormat(NutsOutputFormat outputFormat);

    /**
     * set json output format
     *
     * @return {@code this} instance
     */
    NutsSession json();

    /**
     * set plain text (default) output format
     *
     * @return {@code this} instance
     */
    NutsSession plain();

    /**
     * set properties output format
     *
     * @return {@code this} instance
     */
    NutsSession props();

    /**
     * set tree output format
     *
     * @return {@code this} instance
     */
    NutsSession tree();

    /**
     * set table output format
     *
     * @return {@code this} instance
     */
    NutsSession table();

    /**
     * set xml output format
     *
     * @return {@code this} instance
     */
    NutsSession xml();

    /**
     * return new instance copy of {@code this} session
     *
     * @return new instance copy of {@code this} session
     */
    NutsSession copy();

    /**
     * add session listener. supported listeners are instances of:
     * <ul>
     * <li>{@link NutsWorkspaceListener}</li>
     * <li>{@link NutsInstallListener}</li>
     * <li>{@link NutsMapListener}</li>
     * <li>{@link NutsRepositoryListener}</li>
     * </ul>
     *
     * @param listener listener
     * @return {@code this} instance
     */
    NutsSession addListener(NutsListener listener);

    /**
     * remove session listener. supported listeners are instances of:
     * <ul>
     * <li>{@link NutsWorkspaceListener}</li>
     * <li>{@link NutsInstallListener}</li>
     * <li>{@link NutsMapListener}</li>
     * <li>{@link NutsRepositoryListener}</li>
     * </ul>
     *
     * @param listener listener
     * @return {@code this} instance
     */
    NutsSession removeListener(NutsListener listener);

    /**
     * return registered listeners for the given type.
     * Supported types are :
     * <ul>
     * <li>{@link NutsWorkspaceListener}</li>
     * <li>{@link NutsInstallListener}</li>
     * <li>{@link NutsMapListener}</li>
     * <li>{@link NutsRepositoryListener}</li>
     * </ul>
     *
     * @param <T> listener type
     * @param type listener type class
     * @return registered listeners
     */
    <T extends NutsListener> T[] getListeners(Class<T> type);

    /**
     * return all registered listeners.
     * @return all registered listeners.
     */
    NutsListener[] getListeners();

    /**
     * set session terminal
     * @param terminal session terminal
     * @return {@code this} instance
     */
    NutsSession setTerminal(NutsSessionTerminal terminal);

    /**
     * set session property
     * @param key property key
     * @param value property value
     * @return {@code this} instance
     */
    NutsSession setProperty(String key, Object value);

    /**
     * add session properties
     * @param properties properties
     * @return {@code this} instance
     */
    NutsSession setProperties(Map<String, Object> properties);

    /**
     * return defined properties
     * @return defined properties
     */
    Map<String, Object> getProperties();

    /**
     * return property value or null
     * @param key property key
     * @return return property value or null
     */
    Object getProperty(String key);

    /**
     * return confirmation mode or {@link NutsConfirmationMode#ASK}
     *
     * @return confirmation mode
     */
    NutsConfirmationMode getConfirm();

    /**
     * set confirm mode.
     * @param confirm confirm type.
     * @return {@code this} instance
     */
    NutsSession confirm(NutsConfirmationMode confirm);

    /**
     * set confirm mode.
     * @param confirm confirm type.
     * @return {@code this} instance
     */
    NutsSession setConfirm(NutsConfirmationMode confirm);

    /**
     * set output format options (clear and add)
     * @param options output format options.
     * @return {@code this} instance
     */
    NutsSession setOutputFormatOptions(String... options);

    /**
     * add output format options
     * @param options output format options.
     * @return {@code this} instance
     */
    NutsSession addOutputFormatOptions(String... options);

    /**
     * output format options
     * @return output format options
     */
    String[] getOutputFormatOptions();

    /**
     * change terminal mode
     *
     * @param mode mode
     */
    void setTerminalMode(NutsTerminalMode mode);

    /**
     * current output stream
     *
     * @return current output stream
     */
    PrintStream out();

    /**
     * update output stream
     *
     * @param out new value
     * @return {@code this} instance
     */
    NutsSession setOut(PrintStream out);

    /**
     * current error stream
     *
     * @return current error stream
     */
    PrintStream err();

    /**
     * update error stream
     *
     * @param err new value
     * @return {@code this} instance
     */
    NutsSession setErr(PrintStream err);

    /**
     * terminal mode
     *
     * @return terminal mode
     */
    NutsTerminalMode getTerminalMode();

    /**
     * Object Print Stream associated to out()
     *
     * @return Object Print Stream
     */
    NutsObjectPrintStream oout();

    /**
     * Object Print Stream associated to out()
     *
     * @return Object Print Stream
     */
    NutsObjectPrintStream oerr();

    /**
     * return iterable output
     * @return iterable output
     */
    NutsIterableOutput getIterableOutput();

    /**
     * return iterable output format
     * @return iterable output format
     */
    NutsIterableFormat getIterableFormat();

    /**
     * set iterable output format
     * @param value iterable output format
     * @return {@code this} instance
     */
    NutsSession iterableFormat(NutsIterableFormat value);

    /**
     * set iterable output format
     * @param value iterable output format
     * @return {@code this} instance
     */
    NutsSession setIterableFormat(NutsIterableFormat value);

    /**
     * current terminal
     *
     * @return current terminal
     */
    NutsSessionTerminal terminal();

    /**
     * current terminal
     *
     * @return current terminal
     */
    NutsSessionTerminal getTerminal();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NutsWorkspace workspace();

}
