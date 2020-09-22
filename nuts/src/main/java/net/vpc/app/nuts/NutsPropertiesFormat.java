/**
 * ====================================================================
 *            vpc-common-io : common reusable library for
 *                          input/output
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Map;

/**
 * Class formatting Map/Properties objects
 * @category Format
 */
public interface NutsPropertiesFormat extends NutsFormat {

    /**
     * set model to format
     * @param map model to format
     * @return {@code this} instance
     */
    NutsPropertiesFormat model(Map map);

    /**
     * set model to format
     * @param map model to format
     * @return {@code this} instance
     */
    NutsPropertiesFormat setModel(Map map);

    /**
     * return model to format
     * @return model to format
     */
    Map getModel();

    /**
     * return true is key has to be sorted when formatting
     * @return true is key has to be sorted when formatting
     */
    boolean isSort();

    /**
     * return key/value separator, default is " = "
     * @return key/value separator
     */
    String getSeparator();

    /**
     * set key/value separator
     * @param separator key/value separator
     * @return {@code this} instance
     */
    NutsPropertiesFormat separator(String separator);

    /**
     * set key/value separator
     * @param separator key/value separator
     * @return {@code this} instance
     */
    NutsPropertiesFormat setSeparator(String separator);

    /**
     * enable key sorting
     * @return {@code this} instance
     */
    NutsPropertiesFormat sort();

    /**
     * enable/disable key sorting
     * @param sort when true enable sorting
     * @return {@code this} instance
     */
    NutsPropertiesFormat sort(boolean sort);

    /**
     * enable/disable key sorting
     * @param sort when true enable sorting
     * @return {@code this} instance
     */
    NutsPropertiesFormat setSort(boolean sort);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsPropertiesFormat setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsPropertiesFormat configure(boolean skipUnsupported, String... args);
}
