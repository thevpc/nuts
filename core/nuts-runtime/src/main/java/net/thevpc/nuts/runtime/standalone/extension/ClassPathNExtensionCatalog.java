package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.ext.NExtensionCatalog;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NScored;

import java.net.URL;
import java.util.*;

public class ClassPathNExtensionCatalog implements NExtensionCatalog {
    @Override
    public List<NScored<NId>> findExtensions(String type, String pattern) {
        Set<String> visited = new HashSet<>();
        HashSet<NScored<NId>> ids = new HashSet<>();
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("META-INF/nuts-extension-catalog/" + type + ".properties");
            while (resources.hasMoreElements()) {
                URL u = resources.nextElement();
                if (visited.add(u.toString())) {
                    NScoredIdMap ok = NExtensionCatalogManager.loadScoredIds(NPath.of(u)).orNull();
                    if (ok != null) {
                        NId i = ok.map().get(pattern);
                        if (i != null) {
                            ids.add(new net.thevpc.nuts.util.NScored(i, ok.score()));
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // just ignore
        }
        ArrayList<NScored<NId>> s = new ArrayList<>(ids);
        s.sort(Comparator.<NScored<NId>, Integer>comparing(NScored::score).reversed());
        return s;
    }
}
