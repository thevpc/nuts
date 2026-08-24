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

import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

import java.io.InputStream;
import java.util.stream.Stream;

/**
 * @author vpc
 */
public interface NTextArtRendererFactory extends NComponent {

    /**
     * Load.
     *
     * @param path path
     * @return load result
     */
    NOptional<NTextArtRenderer> load(NInputSource path);
    /**
     * Load.
     *
     * @param path path
     * @return load result
     */
    NOptional<NTextArtRenderer> load(InputStream path);
    /**
     * List renderers.
     *
     * @param rendererType renderer type
     * @return list renderers result
     */
    Stream<NTextArtRenderer> listRenderers(Class<? extends NTextArtRenderer> rendererType);
    /**
     * List renderers.
     *
     * @return list renderers result
     */
    Stream<NTextArtRenderer> listRenderers();
    /**
     * Returns the renderer.
     *
     * @param fontName font name
     * @return get renderer result
     */
    NOptional<NTextArtRenderer> getRenderer(String fontName);
}
