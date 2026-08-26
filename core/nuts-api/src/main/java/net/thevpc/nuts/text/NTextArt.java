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

import java.util.List;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

/**
 * @author vpc
 */
public interface NTextArt extends NComponent {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTextArt of() {
        return NExtensions.of(NTextArt.class);
    }

    /**
     * Table renderers.
     *
     * @return table renderers result
     */
    List<NTextArtTableRenderer> tableRenderers();

    /**
     * Tree renderers.
     *
     * @return tree renderers result
     */
    List<NTextArtTreeRenderer> treeRenderers();

    /**
     * Text renderers.
     *
     * @return text renderers result
     */
    List<NTextArtTextRenderer> textRenderers();

    /**
     * Image renderers.
     *
     * @return image renderers result
     */
    List<NTextArtImageRenderer> imageRenderers();

    /**
     * Returns the renderers.
     *
     * @param rendererType renderer type
     * @return get renderers result
     */
    <T extends NTextArtRenderer> List<NTextArtRenderer> getRenderers(Class<T> rendererType);

    /**
     * Renderers.
     *
     * @return renderers result
     */
    List<NTextArtRenderer> renderers();

    /**
     * Load renderer.
     *
     * @param path path
     * @return load renderer result
     */
    NOptional<NTextArtRenderer> loadRenderer(NPath path);

    /**
     * Default renderer.
     *
     * @return default renderer result
     */
    NOptional<NTextArtRenderer> defaultRenderer();

    /**
     * Returns the renderer.
     *
     * @param rendererName renderer name
     * @return get renderer result
     */
    NOptional<NTextArtRenderer> getRenderer(String rendererName);

    /**
     * Load tree renderer.
     *
     * @param path path
     * @return load tree renderer result
     */
    NOptional<NTextArtTreeRenderer> loadTreeRenderer(NPath path);

    /**
     * Load table renderer.
     *
     * @param path path
     * @return load table renderer result
     */
    NOptional<NTextArtTableRenderer> loadTableRenderer(NPath path);

    /**
     * Load text renderer.
     *
     * @param path path
     * @return load text renderer result
     */
    NOptional<NTextArtTextRenderer> loadTextRenderer(NPath path);

    /**
     * Load image renderer.
     *
     * @param path path
     * @return load image renderer result
     */
    NOptional<NTextArtImageRenderer> loadImageRenderer(NPath path);


    /**
     * Returns the image renderer.
     *
     * @param rendererName renderer name
     * @return get image renderer result
     */
    NOptional<NTextArtImageRenderer> getImageRenderer(String rendererName);

    /**
     * Returns the text renderer.
     *
     * @param rendererName renderer name
     * @return get text renderer result
     */
    NOptional<NTextArtTextRenderer> getTextRenderer(String rendererName);

    /**
     * Returns the table renderer.
     *
     * @param rendererName renderer name
     * @return get table renderer result
     */
    NOptional<NTextArtTableRenderer> getTableRenderer(String rendererName);

    /**
     * Returns the tree renderer.
     *
     * @param rendererName renderer name
     * @return get tree renderer result
     */
    NOptional<NTextArtTreeRenderer> getTreeRenderer(String rendererName);

    /**
     * Text renderer.
     *
     * @return text renderer result
     */
    NOptional<NTextArtTextRenderer> textRenderer();

    /**
     * Image renderer.
     *
     * @return image renderer result
     */
    NOptional<NTextArtImageRenderer> imageRenderer();

    /**
     * Table renderer.
     *
     * @return table renderer result
     */
    NOptional<NTextArtTableRenderer> tableRenderer();

    /**
     * Tree renderer.
     *
     * @return tree renderer result
     */
    NOptional<NTextArtTreeRenderer> treeRenderer();

}
