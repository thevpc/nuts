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

    static NTextArt of() {
        return NExtensions.of(NTextArt.class);
    }

    List<NTextArtTableRenderer> getTableRenderers();

    List<NTextArtTreeRenderer> getTreeRenderers();

    List<NTextArtTextRenderer> getTextRenderers();

    List<NTextArtImageRenderer> getImageRenderers();

    <T extends NTextArtRenderer> List<NTextArtRenderer> getRenderers(Class<T> rendererType);

    List<NTextArtRenderer> getRenderers();

    NOptional<NTextArtRenderer> loadRenderer(NPath path);

    NOptional<NTextArtRenderer> getDefaultRenderer();

    NOptional<NTextArtRenderer> getRenderer(String rendererName);

    NOptional<NTextArtTreeRenderer> loadTreeRenderer(NPath path);

    NOptional<NTextArtTableRenderer> loadTableRenderer(NPath path);

    NOptional<NTextArtTextRenderer> loadTextRenderer(NPath path);

    NOptional<NTextArtImageRenderer> loadImageRenderer(NPath path);


    NOptional<NTextArtImageRenderer> getImageRenderer(String rendererName);

    NOptional<NTextArtTextRenderer> getTextRenderer(String rendererName);

    NOptional<NTextArtTableRenderer> getTableRenderer(String rendererName);

    NOptional<NTextArtTreeRenderer> getTreeRenderer(String rendererName);

    NOptional<NTextArtTextRenderer> getTextRenderer();

    NOptional<NTextArtImageRenderer> getImageRenderer();

    NOptional<NTextArtTableRenderer> getTableRenderer();

    NOptional<NTextArtTreeRenderer> getTreeRenderer();

}
