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

/**
 * Tree Format handles terminal output in Tree format.
 * It is one of the many formats supported bu nuts such as plain,table, xml, json.
 * To use Tree format, given an instance ws of Nuts Workspace you can :
 * <pre>
 *     ws.
 * </pre>
 * @author vpc
 * @since 0.5.5
 */
public interface NutsTreeFormat extends NutsFormat {

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsTreeFormat session(NutsSession session);

    /**
     * update session
     *
     * @param session session
     * @return {@code this instance}
     */
    @Override
    NutsTreeFormat setSession(NutsSession session);

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
    NutsTreeFormat configure(boolean skipUnsupported, String... args);

    /**
     * return node format
     * @return node format
     */
    NutsTreeNodeFormat getNodeFormat();

    /**
     * update node format
     * @param nodeFormat new node format
     * @return {@code this} instance
     */
    NutsTreeFormat setNodeFormat(NutsTreeNodeFormat nodeFormat);

    /**
     * update node format
     * @param nodeFormat new node format
     * @return {@code this} instance
     */
    NutsTreeFormat nodeFormat(NutsTreeNodeFormat nodeFormat);

    /**
     * return linkFormat
     * @return linkFormat
     */
    NutsTreeLinkFormat getLinkFormat();

    /**
     * update link format
     * @param linkFormat new link format
     * @return {@code this} instance
     */
    NutsTreeFormat setLinkFormat(NutsTreeLinkFormat linkFormat);

    /**
     * update link format
     * @param linkFormat new link format
     * @return {@code this} instance
     */
    NutsTreeFormat linkFormat(NutsTreeLinkFormat linkFormat);

    /**
     * return tree model
     * @return tree model
     */
    NutsTreeModel getModel();

    /**
     * update tree model
     * @param tree new tree model
     * @return {@code this} instance
     */
    NutsTreeFormat setModel(NutsTreeModel tree);

    /**
     * update tree model
     * @param tree new tree model
     * @return {@code this} instance
     */
    NutsTreeFormat model(NutsTreeModel tree);

}
