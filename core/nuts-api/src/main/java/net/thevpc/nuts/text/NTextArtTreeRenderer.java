/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting
 * a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.text;

/**
 * @author vpc
 */
public interface NTextArtTreeRenderer extends NTextArtRenderer {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTextArtTreeRenderer of() {
        return NTextArt.of().treeRenderer().get();
    }

    /**
     * Creates a new instance of of.
     *
     * @param name name
     * @return of result
     */
    static NTextArtTreeRenderer of(String name) {
        return NTextArt.of().getTreeRenderer(name).get();
    }

    /**
     * Checks if is infinite.
     *
     * @return is infinite result
     */
    boolean isInfinite();

    /**
     * Infinite.
     *
     * @param infinite infinite
     * @return infinite result
     */
    NTextArtTreeRenderer infinite(boolean infinite);

    /**
     * update node format
     *
     * @param nodeFormat new node format
     * @return {@code this} instance
     */
    NTextArtTreeRenderer nodeFormat(NTreeNodeFormat nodeFormat);

    /**
     * Link format.
     *
     * @return link format result
     */
    NTreeLinkFormat linkFormat();

    /**
     * Link format.
     *
     * @param linkFormatter link formatter
     * @return link format result
     */
    NTextArtTreeRenderer linkFormat(NTreeLinkFormat linkFormatter);

    /**
     * Checks if is omit root.
     *
     * @return is omit root result
     */
    boolean isOmitRoot();

    /**
     * Omit root.
     *
     * @param hideRoot hide root
     * @return omit root result
     */
    NTextArtTreeRenderer omitRoot(boolean hideRoot);

    /**
     * Render.
     *
     * @param text text
     * @return render result
     */
    NText render(NTreeNode text);
}
