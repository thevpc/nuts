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
     * When isTrace() is true and isVerbose() is true, operations are invited to
     * print to output stream even more extra information about processing.
     * Output may be in different formats according to
     * {@link #getOutputFormat()} and {@link #getIterableFormat()}
     *
     * @return true if trace flag is armed
     */
    boolean isVerbose();

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
     * When isTrace() is true and verbose is true, operations are invited to
     * print to output stream even more extra information about processing.
     * Output may be in different formats according to
     * {@link #getOutputFormat()} and {@link #getIterableFormat()}
     *
     * @param verbose verbose mode
     * @return true if trace flag is armed
     */
    NutsSession setVerbose(boolean verbose);

    /**
     * equivalent to {
     *
     * @ setConfirm(true)}
     * @return {@code this} instance
     */
    NutsSession trace();

    /**
     * equivalent to {
     *
     * @ setTrace(trace)}
     * @param trace new value
     * @return {@code this} instance
     */
    NutsSession trace(boolean trace);

    /**
     * equivalent to {
     *
     * @ setConfirm(true)}
     * @return {@code this} instance
     */
    NutsSession verbose();

    /**
     * equivalent to {
     *
     * @ setTrace(trace)}
     * @param verbose new value
     * @return {@code this} instance
     */
    NutsSession verbose(boolean verbose);

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
     * @param force new value
     * @return {@code this} instance
     */
    NutsSession force(boolean force);

    NutsSession ask();

    NutsSession ask(boolean enable);

    NutsSession setAsk(boolean enable);

    boolean isAsk();

    NutsOutputFormat getOutputFormat(NutsOutputFormat defaultValue);

    /**
     * return effective trace output format. The effective trace output format
     * is the value of {@code getIterableFormat().getOutputFormat()} whenever {@code getIterableFormat()!=null
     * } otherwise it returns simply the value defined by calling
     * {@link #setOutputFormat(net.vpc.app.nuts.NutsOutputFormat)}. If none of {@link #setIterableFormatHandler(net.vpc.app.nuts.NutsIterableFormatHandler)
     * } or {@link #setOutputFormat(net.vpc.app.nuts.NutsOutputFormat) } has
     * been called (or called with null values) {@link NutsOutputFormat#PLAIN}
     * should be returned.
     *
     * @return effective trace output format
     */
    NutsOutputFormat getOutputFormat();

    NutsSession outputFormat(NutsOutputFormat outputFormat);

    NutsSession setOutputFormat(NutsOutputFormat outputFormat);

    NutsSession json();

    NutsSession plain();

    NutsSession props();

    NutsSession tree();

    NutsSession table();

    NutsSession xml();

    NutsSession copy();

    NutsSession addListeners(NutsListener listener);

    NutsSession removeListeners(NutsListener listener);

    <T extends NutsListener> T[] getListeners(Class<T> type);

    NutsListener[] getListeners();

    NutsSession setTerminal(NutsSessionTerminal terminal);

    NutsSession setProperty(String key, Object value);

    NutsSession setProperties(Map<String, Object> properties);

    Map<String, Object> getProperties();

    Object getProperty(String key);

    /**
     * return confirmation mode or {@link NutsConfirmationMode#CANCEL}
     *
     * @return confirmation mode
     */
    NutsConfirmationMode getConfirm();

    /**
     *
     * @param confirm
     * @return
     */
    NutsSession confirm(NutsConfirmationMode confirm);

    NutsSession setConfirm(NutsConfirmationMode confirm);

    NutsSession setOutputFormatOptions(String... options);

    NutsSession addOutputFormatOptions(String... options);

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

    NutsIterableOutput getIterableOutput();

    NutsIterableFormat getIterableFormat();

    NutsSession iterableFormat(NutsIterableFormat value);

    NutsSession setIterableFormat(NutsIterableFormat value);

    NutsSessionTerminal terminal();

    NutsSessionTerminal getTerminal();
    
    NutsWorkspace getWorkspace();
    
    NutsWorkspace workspace();

}
