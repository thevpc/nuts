/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.extension;

import net.thevpc.nuts.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NCollections;
import net.thevpc.nuts.util.NMsg;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

/**
 * Simple Implementation of Nuts BootClassLoader
 *
 * @author thevpc
 * @app.category Boot
 */
public class NMutableClassLoaderImpl extends URLClassLoader implements NMutableClassLoader {

    private final List<NDefinition> dependencies = new ArrayList<>();

    public NMutableClassLoaderImpl(ClassLoader parent) {
        super(new URL[0], parent);
    }

    public <S> List<S> loadServices(Class<S> serviceClass) {
        return NCollections.list(ServiceLoader.load(serviceClass, this));
    }

    @Override
    public ClassLoader asClassLoader() {
        return this;
    }

    public List<NDefinition> getLoadedDependencies() {
        return new ArrayList<>(dependencies);
    }

    public boolean loadDependencies(NDependency... allDefinitions) {
        List<NDefinition> ok = new ArrayList<>();
        for (NDependency dep : allDefinitions) {
            NDependency id = dep;
            if (!NBlankable.isBlank(id.getGroupId())) {
                NChronometer ch = NChronometer.startNow();
                NLog.of(NMutableClassLoaderImpl.class).log(NMsg.ofC("searching dependency %s...", id).asConfig().withIntent(NMsgIntent.PROGRESS));
                List<NDefinition> d = NSearchCmd.of(id.toId()).latest()
                        .setInlineDependencies(true)
                        .setDependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultDefinitions().toList();
                if (d.isEmpty()) {
                    throw new NIllegalArgumentException(NMsg.ofC("unable to load %s", dep));
                }
                NLog.of(NMutableClassLoaderImpl.class).log(NMsg.ofC("found dependency %s in %s...", id, ch.stop()).asConfig().withIntent(NMsgIntent.PROGRESS));
                ok.addAll(d);
            } else {
                // some groups
                throw new NIllegalArgumentException(NMsg.ofC("unable to load %s", dep));
            }
        }
        return load(ok.toArray(new NDefinition[0]));
    }

    private boolean load(NDefinition... allDefinitions) {
        List<NDefinition> ok = new ArrayList<>();
        List<URL> urls = new ArrayList<>();
        for (NDefinition id : allDefinitions) {
            if (NBlankable.isBlank(id)) {
                continue;
            }
            if (isLoadedDependency(id.getId())) {
                NLog.of(NMutableClassLoaderImpl.class).log(NMsg.ofC("dependency already loaded %s...", id.getId()).asWarning().withIntent(NMsgIntent.PROGRESS));
                continue;
            }
            URL u = id.getContent().map(x -> x.toURL().orNull()).orNull();
            if (u == null) {
                throw new NIllegalArgumentException(NMsg.ofC("unable to load %s", id.getId()).asError());
            }
            urls.add(u);
            NLog.of(NMutableClassLoaderImpl.class).log(NMsg.ofC("loaded dependency %s...", id.getId()).asConfig().withIntent(NMsgIntent.PROGRESS));
            ok.add(id);
        }
        dependencies.addAll(ok);
        for (URL a : urls) {
            super.addURL(a);
        }
        return !ok.isEmpty();
    }

    public boolean isLoadedDependency(NId id) {
        if (dependencies.stream().anyMatch(x -> x.getId().equalsShortId(id))) {
            return true;
        }

        // try current class loader
        URL s = getResource("META-INF/maven/" + id.getGroupId() + "/" + id.getArtifactId() + "/pom.properties");
        if (s != null) {
            return true;
        }
        s = getResource("META-INF/maven/" + id.getGroupId() + "/" + id.getArtifactId() + "/pom.xml");
        if (s != null) {
            return true;
        }
        s = getResource("META-INF/nuts/" + id.getGroupId() + "/" + id.getArtifactId() + "/nuts.nuts");
        if (s != null) {
            return true;
        }
        return false;
    }


    @Override
    public String toString() {
        return "NMutableClassLoader" + dependencies.stream().map(x -> x.getId().toString()).collect(Collectors.toList());
    }


}
