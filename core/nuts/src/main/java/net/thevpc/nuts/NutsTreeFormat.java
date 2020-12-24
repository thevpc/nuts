/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts;

/**
 * Tree Format handles terminal output in Tree format.
 * It is one of the many formats supported bu nuts such as plain,table, xml, json.
 * To use Tree format, given an instance ws of Nuts Workspace you can :
 * <pre>
 *     ws.
 * </pre>
 * @author thevpc
 * @since 0.5.5
 * @category Format
 */
public interface NutsTreeFormat extends NutsObjectFormat {

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
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
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
    NutsTreeFormat setValue(NutsTreeModel tree);

}
