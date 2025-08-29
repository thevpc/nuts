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
package net.thevpc.nuts.runtime.standalone.text.art;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.text.art.figlet.FigletNTextArtImageRenderer;
import net.thevpc.nuts.runtime.standalone.text.art.img.PixelNTextArtImageRenderer;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.text.NTextArt;
import net.thevpc.nuts.text.NTextArtImageRenderer;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.text.NTextArtRenderer;

/**
 * @author vpc
 */
@NComponentScope(NScopeType.WORKSPACE)
public class NTextArtImpl implements NTextArt {

    private void loadResourceList(String rendererType, Map<String, NTextArtRenderer> all) {
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/textart/" + rendererType + ".lst");
            for (URL url : NCollections.list(resources)) {
                for (String line : NPath.of(url).lines()) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        String id = rendererType + ":" + line;
                        getRenderer(id).ifPresent(x -> {
                            all.put(id, x);
                        });
                    }
                }
            }
        } catch (IOException ex) {
            // just ignore
        }
        try {
            Enumeration<URL> resources = NTextArtImpl.class.getClassLoader().getResources("META-INF/textart/" + rendererType + ".lst");
            for (URL url : NCollections.list(resources)) {
                for (String line : NPath.of(url).lines()) {
                    line = line.trim();
                    if (!line.isEmpty() && !line.startsWith("#")) {
                        String id = rendererType + ":" + line;
                        getRenderer(id).ifPresent(x -> {
                            all.put(id, x);
                        });
                    }
                }
            }
        } catch (IOException ex) {
            // just ignore
        }
    }

    @Override
    public List<NTextArtRenderer> getRenderers() {

        Map<String, NTextArtRenderer> all = new LinkedHashMap<>();
        for (String id : new String[]{
                "pixel:cipher",
                "pixel:hash",
                "pixel:dot",
                "pixel:star",
                "pixel:dollar",
                "pixel:standard"}) {
            all.put(id, getRenderer(id).get());
        }
        loadResourceList("pixel", all);
        loadResourceList("figlet", all);
        return new ArrayList<>(all.values());
    }

    @Override
    public NOptional<NTextArtRenderer> loadRenderer(NPath path) {
        if (path.isRegularFile()) {
            return parseRenderer(path.readString());
        } else {
            return NOptional.ofEmpty(NMsg.ofC("renderer file not found : %s", path));
        }
    }

    @Override
    public NOptional<NTextArtRenderer> parseRenderer(String rendererDefinition) {
        if (rendererDefinition.startsWith("flf2")) {
            try {
                return NOptional.of(new FigletNTextArtImageRenderer(new ByteArrayInputStream(rendererDefinition.getBytes()))
                );
            } catch (Exception ex) {
                return NOptional.ofError(NMsg.ofC("figet renderer loading failed : %s", ex));
            }
        } else if (rendererDefinition.startsWith("pixel{")) {
            try {
                return NOptional.of(new PixelNTextArtImageRenderer(new ByteArrayInputStream(rendererDefinition.getBytes()))
                );
            } catch (Exception ex) {
                return NOptional.ofError(NMsg.ofC("pixel renderer loading failed : %s", ex));
            }
        } else {
            return NOptional.ofEmpty(NMsg.ofC("renderer spec not supported"));
        }
    }

    public NOptional<NTextArtRenderer> getDefaultRenderer() {
        return getRenderer("figlet:banner");
    }

    @Override
    public NOptional<NTextArtRenderer> getRenderer(String fontName) {
        if (fontName != null) {
            if (fontName.startsWith("figlet:")) {
                try {
                    return FigletNTextArtImageRenderer.ofName(fontName.substring("figlet:".length())).instanceOf(NTextArtRenderer.class);
                } catch (Exception e) {
                    return NOptional.ofNamedEmpty(fontName);
                }
            }
            if (fontName.startsWith("pixel:")) {
                try {
                    return PixelNTextArtImageRenderer.ofName(fontName.substring("pixel:".length())).instanceOf(NTextArtRenderer.class);
                } catch (Exception e) {
                    return NOptional.ofNamedEmpty(fontName);
                }
            }
        }
        return NOptional.ofNamedEmpty(fontName);
    }

    @Override
    public NOptional<NTextArtImageRenderer> loadImageRenderer(NPath path) {
        return loadRenderer(path).instanceOf(NTextArtImageRenderer.class);
    }

    @Override
    public NOptional<NTextArtImageRenderer> parseImageRenderer(String rendererDefinition) {
        return parseRenderer(rendererDefinition).instanceOf(NTextArtImageRenderer.class);
    }

    @Override
    public NOptional<NTextArtImageRenderer> getImageRenderer(String rendererName) {
        return getRenderer(rendererName).instanceOf(NTextArtImageRenderer.class);
    }

    @Override
    public NOptional<NTextArtImageRenderer> getDefaultImageRenderer() {
        return getRenderer("pixel:standard").instanceOf(NTextArtImageRenderer.class);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
