/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Map;
import java.util.Properties;

/**
 * this class is responsible of displaying general information about the current workspace and repositories.
 * Il is invoked by the "info" standard commmad,
 * @author vpc
 * @since 0.5.4
 */
public interface NutsInfoFormat extends NutsFormat {

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsInfoFormat session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsInfoFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsInfoFormat configure(boolean skipUnsupported, String... args);

    /**
     * include a custom property
     * @param key custom property key
     * @param value custom property value
     * @return {@code this} instance
     */
    NutsInfoFormat addProperty(String key, String value);

    /**
     * include custom properties from the given map
     * @param customProperties custom properties
     * @return {@code this} instance
     */
    NutsInfoFormat addProperties(Map<String, String> customProperties);

    /**
     * enable display of all repositories information
     * @return {@code this} instance
     */
    NutsInfoFormat showRepositories();

    /**
     * enable or disable display of all repositories information
     * @param enable if true enable
     * @return {@code this} instance
     */
    NutsInfoFormat showRepositories(boolean enable);

    /**
     * enable or disable display of all repositories information
     * @param enable if true enable
     * @return {@code this} instance
     */
    NutsInfoFormat setShowRepositories(boolean enable);

    /**
     * return true if displaying repositories is enabled
     * @return true if displaying repositories is enabled
     */
    boolean isShowRepositories();

    /**
     * enable fancy (custom, pretty) display mode
     * @param fancy if true enable fancy mode
     * @return {@code this} instance
     */
    NutsInfoFormat setFancy(boolean fancy);

    //TODO
    //NutsInfoFormat fancy(boolean fancy);

    /**
     * return true if fancy mode armed
     * @return true if fancy mode armed
     */
    boolean isFancy();

}
