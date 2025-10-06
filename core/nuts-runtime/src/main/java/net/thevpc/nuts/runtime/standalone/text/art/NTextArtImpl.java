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

import java.util.*;
import java.util.stream.Collectors;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

/**
 * @author vpc
 */
@NComponentScope(NScopeType.WORKSPACE)
public class NTextArtImpl implements NTextArt {
    private List<NTextArtRendererFactory> factories;

    public List<NTextArtRendererFactory> getFactories() {
        if (factories == null) {
            factories = NExtensions.of().createComponents(NTextArtRendererFactory.class, null);
        }
        return factories;
    }

    @Override
    public List<NTextArtRenderer> getRenderers() {
        List<NTextArtRenderer> all = new ArrayList<>();
        for (NTextArtRendererFactory factory : getFactories()) {
            for (NTextArtRenderer nTextArtRenderer : factory.listRenderers().collect(Collectors.toList())) {
                all.add(nTextArtRenderer);
            }
        }
        return all;
    }

    @Override
    public List<NTextArtTableRenderer> getTableRenderers() {
        return NStream.ofIterable(getRenderers()).instanceOf(NTextArtTableRenderer.class).toList();
    }

    @Override
    public List<NTextArtTreeRenderer> getTreeRenderers() {
        return NStream.ofIterable(getRenderers()).instanceOf(NTextArtTreeRenderer.class).toList();
    }

    @Override
    public List<NTextArtTextRenderer> getTextRenderers() {
        return NStream.ofIterable(getRenderers()).instanceOf(NTextArtTextRenderer.class).toList();
    }


    @Override
    public List<NTextArtImageRenderer> getImageRenderers() {
        return NStream.ofIterable(getRenderers()).instanceOf(NTextArtImageRenderer.class).collect(Collectors.toList());
    }


    public <T extends NTextArtRenderer> List<NTextArtRenderer> getRenderers(Class<T> rendererType){
        return NStream.ofIterable(getRenderers())
                .instanceOf(rendererType).collect(Collectors.toList());
    }

    @Override
    public NOptional<NTextArtRenderer> loadRenderer(NPath path) {
        for (NTextArtRendererFactory factory : getFactories()) {
            NOptional<NTextArtRenderer> p = factory.load(path);
            if (p.isPresent()) {
                return p;
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("renderer file not found : %s", path));
    }


    public NOptional<NTextArtRenderer> getDefaultRenderer() {
        return getRenderer("figlet:banner");
    }

    @Override
    public NOptional<NTextArtRenderer> getRenderer(String rendererName) {
        for (NTextArtRendererFactory factory : getFactories()) {
            NOptional<NTextArtRenderer> p = factory.getRenderer(rendererName);
            if (p.isPresent()) {
                return p;
            }
        }
        return NOptional.ofNamedEmpty(rendererName);
    }

    @Override
    public NOptional<NTextArtImageRenderer> loadImageRenderer(NPath path) {
        return loadRenderer(path).instanceOf(NTextArtImageRenderer.class);
    }

    @Override
    public NOptional<NTextArtImageRenderer> getImageRenderer(String rendererName) {
        return getRenderer(rendererName).instanceOf(NTextArtImageRenderer.class);
    }

    @Override
    public NOptional<NTextArtTextRenderer> loadTextRenderer(NPath path) {
        return loadRenderer(path).instanceOf(NTextArtTextRenderer.class);
    }

    @Override
    public NOptional<NTextArtTextRenderer> getTextRenderer(String rendererName) {
        return getRenderer(rendererName).instanceOf(NTextArtTextRenderer.class);
    }


    @Override
    public NOptional<NTextArtTableRenderer> loadTableRenderer(NPath path) {
        return loadRenderer(path).instanceOf(NTextArtTableRenderer.class);
    }

    @Override
    public NOptional<NTextArtTableRenderer> getTableRenderer(String rendererName) {
        return getRenderer(rendererName).instanceOf(NTextArtTableRenderer.class);
    }

    @Override
    public NOptional<NTextArtTreeRenderer> loadTreeRenderer(NPath path) {
        return loadRenderer(path).instanceOf(NTextArtTreeRenderer.class);
    }

    @Override
    public NOptional<NTextArtTreeRenderer> getTreeRenderer(String rendererName) {
        return getRenderer(rendererName).instanceOf(NTextArtTreeRenderer.class);
    }

    @Override
    public NOptional<NTextArtImageRenderer> getImageRenderer() {
        return getImageRenderer("pixel:standard").instanceOf(NTextArtImageRenderer.class);
    }

    @Override
    public NOptional<NTextArtTextRenderer> getTextRenderer() {
        return getTextRenderer("figlet:banner");
    }

    @Override
    public NOptional<NTextArtTableRenderer> getTableRenderer() {
        return getTableRenderer("table:default");
    }

    @Override
    public NOptional<NTextArtTreeRenderer> getTreeRenderer() {
        return getTreeRenderer("tree:default");
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
