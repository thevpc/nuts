package net.thevpc.nuts.runtime.standalone.text.art.table;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.io.NInputSource;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.text.art.NTextArtImpl;
import net.thevpc.nuts.runtime.standalone.text.art.figlet.FigletNTextArtImageRenderer;
import net.thevpc.nuts.runtime.standalone.text.art.img.PixelNTextArtImageRenderer;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextArtImageRenderer;
import net.thevpc.nuts.text.NTextArtRenderer;
import net.thevpc.nuts.text.NTextArtRendererFactory;
import net.thevpc.nuts.text.NTextArtTextRenderer;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NOptional;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class TableNTextArtRendererFactory implements NTextArtRendererFactory {
    String rendererType = "table";

    @Override
    public NOptional<NTextArtRenderer> load(NInputSource path) {
        try {
            return NOptional.of(new PixelNTextArtImageRenderer(path));
        } catch (Exception e) {
            return NOptional.ofNamedEmpty(rendererType + " renderer");
        }
    }

    @Override
    public NOptional<NTextArtRenderer> load(InputStream path) {
        try {
            return NOptional.of(new PixelNTextArtImageRenderer(path, null));
        } catch (Exception e) {
            return NOptional.ofNamedEmpty(rendererType + " renderer");
        }
    }

    public Stream<NTextArtRenderer> listRenderers(Class<? extends NTextArtRenderer> rendererType) {
        if (
                rendererType.isAssignableFrom(NTextArtTextRenderer.class)
                        || rendererType.isAssignableFrom(NTextArtImageRenderer.class)
        ) {
            return listRenderers();
        }
        return Stream.empty();
    }

    @Override
    public Stream<NTextArtRenderer> listRenderers() {
        Map<String, NTextArtRenderer> all = new HashMap<>();
        for (ClassLoader classLoader : new ClassLoader[]{Thread.currentThread().getContextClassLoader(), NTextArtImpl.class.getClassLoader()}) {
            try {
                Enumeration<URL> resources = classLoader.getResources("META-INF/textart/" + rendererType + ".lst");
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
        return all.values().stream();
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public NOptional<NTextArtRenderer> getRenderer(String renderName) {
        if (renderName.startsWith(rendererType + ":")) {
            try {
                String n = renderName.substring(rendererType.length() + 1);
                DefaultNTextArtTableRenderer r = new DefaultNTextArtTableRenderer().setBorder(n);
                return NOptional.of(r);
            } catch (Exception e) {
                return NOptional.ofNamedEmpty(renderName);
            }
        }
        return NOptional.ofNamedEmpty(renderName);
    }
}
