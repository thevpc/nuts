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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Map;

/**
 * session is context defining common command options and parameters.
 *
 * @author vpc
 * @since 0.5.4
 * @category Base
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
     * equivalent to {@code setTrace(false)}
     *
     * @return {@code this} instance
     */
    NutsSession setSilent();

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
     * change force flag value. some operations may require user confirmation
     * before performing critical operations such as overriding existing values,
     * deleting sensitive information ; in such cases, arming force flag will
     * provide an implicit confirmation.
     *
     * @param enable if true force flag is armed
     * @return {@code this} instance
     */
    NutsSession setForce(boolean enable);

    /**
     * equivalent to {@code setAsk(true)}
     *
     * @return {@code this} instance
     */
    NutsSession ask();

    /**
     * equivalent to {@code setConfirm(enable?ASK:null)}
     *
     * @param enable new value
     * @return {@code this} instance
     */
    NutsSession setAsk(boolean enable);

    /**
     * change YES flag value. some operations may require user confirmation
     * before performing critical operations such as overriding existing values,
     * deleting sensitive information ; in such cases, arming yes flag will
     * provide an implicit confirmation.
     *
     * @param enable if true yes flag is armed
     * @return {@code this} instance
     */
    NutsSession setYes(boolean enable);

    /**
     * equivalent to {@code setYes(true)}
     *
     * @return {@code this} instance
     */
    NutsSession yes();

    /**
     * equivalent to {@code setYes(enable)}
     *
     * @param enable new value
     * @return {@code this} instance
     */
    NutsSession yes(boolean enable);

    /**
     * true if YES is armed.
     *
     * @return true if YES is armed.
     * @since 0.5.7
     */
    boolean isYes();

    /**
     * change no flag value. some operations may require user confirmation
     * before performing critical operations such as overriding existing values,
     * deleting sensitive information ; in such cases, arming no flag will
     * provide an implicit negative confirmation.
     *
     * @param enable if true NO flag is armed
     * @return {@code this} instance
     */
    NutsSession setNo(boolean enable);

    /**
     * equivalent to {@code setNo(true)}
     *
     * @return {@code this} instance
     */
    NutsSession no();

    /**
     * equivalent to {@code setNo(enable)}
     *
     * @param enable new value
     * @return {@code this} instance
     */
    NutsSession no(boolean enable);

    /**
     * true if NO is armed.
     *
     * @return true if NO is armed.
     * @since 0.5.7
     */
    boolean isNo();

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
     * {@link #setOutputFormat(NutsOutputFormat)}. If none of
     * null null     {@link #setIterableFormat(NutsIterableFormat)
     * } or {@link #setOutputFormat(NutsOutputFormat)} has been
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
     * copy into this instance from the given value
     *
     * @param other other session to copy from
     * @return return {@code this} instance
     */
    NutsSession copyFrom(NutsSession other);

    /**
     * change fetch strategy
     *
     * @param mode new strategy or null
     * @return {@code this} instance
     */
    NutsSession fetchStrategy(NutsFetchStrategy mode);

    /**
     * change fetch strategy
     *
     * @param mode new strategy or null
     * @return {@code this} instance
     */
    NutsSession setFetchStrategy(NutsFetchStrategy mode);

    /**
     * change fetch strategy to REMOTE
     *
     * @return {@code this} instance
     */
    NutsSession fetchRemote();

    /**
     * change fetch strategy to OFFLINE
     *
     * @return {@code this} instance
     */
    NutsSession fetchOffline();

    /**
     * change fetch strategy to ONLINE
     *
     * @return {@code this} instance
     */
    NutsSession fetchOnline();

//    /**
//     * change fetch strategy to INSTALLED
//     * @return {@code this} instance
//     */
//    NutsSession fetchInstalled();

    /**
     * change fetch strategy to ANYWHERE
     *
     * @return {@code this} instance
     */
    NutsSession fetchAnyWhere();

    /**
     * return current fetch strategy.
     * When no strategy (or null strategy) was set, return workspace
     * strategy default strategy. When none defines use ONLINE
     *
     * @return {@code this} instance
     */
    NutsFetchStrategy getFetchStrategy();

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
     * return registered listeners for the given type. Supported types are :
     * <ul>
     * <li>{@link NutsWorkspaceListener}</li>
     * <li>{@link NutsInstallListener}</li>
     * <li>{@link NutsMapListener}</li>
     * <li>{@link NutsRepositoryListener}</li>
     * </ul>
     *
     * @param <T>  listener type
     * @param type listener type class
     * @return registered listeners
     */
    <T extends NutsListener> T[] getListeners(Class<T> type);

    /**
     * return all registered listeners.
     *
     * @return all registered listeners.
     */
    NutsListener[] getListeners();

    /**
     * set session terminal
     *
     * @param terminal session terminal
     * @return {@code this} instance
     */
    NutsSession setTerminal(NutsSessionTerminal terminal);

    /**
     * set session property
     *
     * @param key   property key
     * @param value property value
     * @return {@code this} instance
     */
    NutsSession setProperty(String key, Object value);

    /**
     * add session properties
     *
     * @param properties properties
     * @return {@code this} instance
     */
    NutsSession setProperties(Map<String, Object> properties);

    /**
     * return defined properties
     *
     * @return defined properties
     */
    Map<String, Object> getProperties();

    /**
     * return property value or null
     *
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
     *
     * @param confirm confirm type.
     * @return {@code this} instance
     */
    NutsSession confirm(NutsConfirmationMode confirm);

    /**
     * set confirm mode.
     *
     * @param confirm confirm type.
     * @return {@code this} instance
     */
    NutsSession setConfirm(NutsConfirmationMode confirm);

    /**
     * set output format options (clear and add)
     *
     * @param options output format options.
     * @return {@code this} instance
     */
    NutsSession setOutputFormatOptions(String... options);

    /**
     * add output format options
     *
     * @param options output format options.
     * @return {@code this} instance
     */
    NutsSession addOutputFormatOptions(String... options);

    /**
     * output format options
     *
     * @return output format options
     */
    String[] getOutputFormatOptions();

    /**
     * current output stream
     *
     * @return current output stream
     */
    PrintStream out();

    InputStream in();

    /**
     * current error stream
     *
     * @return current error stream
     */
    PrintStream err();

    /**
     * return iterable output
     *
     * @return iterable output
     */
    NutsIterableOutput getIterableOutput();

    /**
     * return iterable output format
     *
     * @return iterable output format
     */
    NutsIterableFormat getIterableFormat();

    /**
     * set iterable output format
     *
     * @param value iterable output format
     * @return {@code this} instance
     */
    NutsSession setIterableFormat(NutsIterableFormat value);

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
     * true when considering transitive repositories.
     *
     * @return true when considering transitive repositories
     */
    boolean isTransitive();

    /**
     * consider transitive repositories
     *
     * @param value nullable value
     * @return {@code this} instance
     */
    NutsSession setTransitive(Boolean value);

    /**
     * true when using cache
     *
     * @return true when using cache
     */
    boolean isCached();

    /**
     * use cache
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsSession setCached(Boolean value);


    /**
     * true when using indexes
     *
     * @return true when using indexes
     */
    boolean isIndexed();

    /**
     * use index
     *
     * @param value value
     * @return {@code this} instance
     */
    NutsSession setIndexed(Boolean value);


    /**
     * set expire instant. Expire time is used to expire any cached
     * file that was downloaded before the given date/time.
     *
     * @param value value
     * @return {@code this} instance
     * @since 0.8.0
     */
    NutsSession setExpireTime(Instant value);

    /**
     * return expired date/time or zero if not set.
     * Expire time is used to expire any cached file that was downloaded before the given date/time
     *
     * @return expired date/time or zero
     * @since 0.8.0
     */
    Instant getExpireTime();

    /**
     * return progress options
     *
     * @return progress options
     */
    String getProgressOptions();

    /**
     * change progress options
     *
     * @param progressOptions options
     * @return {@code this} instance
     */
    NutsSession setProgressOptions(String progressOptions);

    /**
     * This is a helper method to create and Object format initialized with this
     * session instance and the given object to print.
     *
     * {@code thisSession.getWorkspace().object().setSession(thisSession).value(any)}
     * <br>
     * Using this method is recommended to print objects to default format (json, xml,...)
     *
     * @param any any object to print in the configured/default format
     * @return new instance of {@link NutsObjectFormat}
     * @since 0.6.0
     */
    NutsObjectFormat formatObject(Object any);
}
