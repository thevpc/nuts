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

import java.util.Map;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsSession extends NutsTerminalProvider, NutsPropertiesProvider, NutsConfigurable {

    /**
     * When true, operations are invited to print to output stream extra 
     * information about processing. Output may be in different formats according to
     * {@link #getOutputFormat()} and {@link #getIncrementalOutputFormat()}
     *
     * @return true if trace flag is armed
     */
    boolean isTrace();

    /**
     * true if non incremental and plain formats along with trace flag are armed. 
     * equivalent to {@code isTrace()
     * && !isIncrementalOut()
     * && getOutputFormat() == NutsOutputFormat.PLAIN}
     *
     * @return true plain non incremental format AND trace are armed
     */
    boolean isPlainTrace();

    /**
     * true if incremental format and trace flag are armed. 
     * equivalent to {@code isTrace()
     * && isIncrementalOut()}
     *
     * @return true plain non incremental format AND trace are armed
     */
    boolean isIncrementalTrace();

    /**
     * true if NON incremental and NON plain formats along with trace flag are armed. 
     * equivalent to {@code isTrace()
     * && !isIncrementalOut()
     * && getOutputFormat() == NutsOutputFormat.PLAIN}
     *
     * @return true if NON incremental and NON plain formats along with trace flag are armed. 
     */
    boolean isStructuredTrace();

    /**
     * true if incremental format is armed. equivalent to
     * {@code  getIncrementalOutputFormat()!=null}
     *
     * @return true if incremental format is armed.
     */
    boolean isIncrementalOut();

    /**
     * true if NON incremental and NON plain formats are armed.
     * equivalent to {@code !isIncrementalOut()
     * && getOutputFormat() != NutsOutputFormat.PLAIN}
     *
     * @return true if non incremental format AND structured outpt format are
     * armed.
     */
    boolean isStructuredOut();
    
    /**
     * true if NON incremental and plain format are armed.
     *
     * @return true if non incremental format AND structured outpt format are
     * armed.
     */
    boolean isPlainOut();

    /**
     * change trace flag value.
     * When true, operations are invited to print to output stream information
     * about processing. Output may be in different formats according to
     * {@link #getOutputFormat()} and {@link #getIncrementalOutputFormat()}
     * @param trace new value
     * @return {@code this} instance
     */
    NutsSession setTrace(boolean trace);

    /**
     * equivalent to {@ setConfirm(true)}
     * @return {@code this} instance
     */
    NutsSession trace();

    /**
     * equivalent to {@ setTrace(trace)}
     * @param trace new value
     * @return {@code this} instance
     */
    NutsSession trace(boolean trace);

    /**
     * true if force flag is armed.
     * some operations may require user confirmation before performing critical 
     * operations such as overriding existing values, deleting sensitive informations ; 
     * in such cases, arming force flag will provide an implicit confirmation.
     * 
     * @return true if force flag is armed.
     */
    boolean isForce();

    /**
    /**
     * change force flag value.
     * some operations may require user confirmation before performing critical 
     * operations such as overriding existing values, deleting sensitive informations ; 
     * in such cases, arming force flag will provide an implicit confirmation.
     * 
     * @param enable if true force flag is armed
     * @return {@code this} instance
     */
    NutsSession setForce(boolean enable);

    /**
     * equivalent to {@code setForce(true)}
     * @return {@code this} instance
     */
    NutsSession force();

    /**
     * equivalent to {@code setForce(force)}
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
     * is the value of {@code getIncrementalOutputFormat().getOutputFormat()}
     * whenever {@code getIncrementalOutputFormat()!=null } otherwise it returns
     * simply the value defined by calling
     * {@link #setOutputFormat(net.vpc.app.nuts.NutsOutputFormat)}. If none of {@link #setIncrementalOutputFormat(net.vpc.app.nuts.NutsIncrementalOutputFormat)
     * } or {@link #setOutputFormat(net.vpc.app.nuts.NutsOutputFormat) } has
     * been called (or called with null values) {@link NutsOutputFormat#PLAIN}
     * should be returned.
     *
     * @return effective trace output format
     */
    NutsOutputFormat getOutputFormat();

    NutsIncrementalOutputFormat getIncrementalOutputFormat();

    NutsSession incrementalOutputFormat(NutsIncrementalOutputFormat customFormat);

    NutsSession setIncrementalOutputFormat(NutsIncrementalOutputFormat customFormat);

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

    @Override
    NutsSession setProperty(String key, Object value);

    @Override
    NutsSession setProperties(Map<String, Object> properties);

    /**
     * return confirmation mode or {@link NutsConfirmationMode#CANCEL}
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
}
