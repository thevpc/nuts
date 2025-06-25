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
package net.thevpc.nuts.runtime.standalone.dependency.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.NDefaultClassLoaderNode;

/**
 * @author thevpc
 */
public final class NClassLoaderUtils {

    public static NClassLoaderNode definitionToClassLoaderNodeSafer(NDefinition def, NRepositoryFilter repositoryFilter) {
        try {
            return definitionToClassLoaderNode(def, repositoryFilter);
        }catch (NNotFoundException ex){
            return definitionToClassLoaderNode(def, null);
        }
    }

    public static NClassLoaderNode definitionToClassLoaderNode(NDefinition def, NRepositoryFilter repositoryFilter) {
        def.getDependencies().get();
        def.getContent().get();
        def.getContent().flatMap(NPath::toURL).get();
        return new NDefaultClassLoaderNode(
                def.getId(),
                def.getContent().flatMap(NPath::toURL).orNull(),
                true,
                true,
                def.getDependencies().get().transitiveWithSource().stream().map(x -> toClassLoaderNodeWithOptional(x, false, repositoryFilter))
                        .filter(Objects::nonNull)
                        .toArray(NClassLoaderNode[]::new)
        );
    }

    private static NClassLoaderNode toClassLoaderNode(NDependencyTreeNode d, boolean withChildren, NRepositoryFilter repositoryFilter) {
        return toClassLoaderNodeWithOptional(d, false, withChildren, repositoryFilter);
    }

    private static NClassLoaderNode toClassLoaderNodeWithOptional(NDependency d, boolean optional, NRepositoryFilter repositoryFilter) {
        NPath cc = null;
        if (!optional) {
            if (NDependencyUtils.isOptionalDependency(d)) {
                optional = true;
            }
        }
        NId id = d.toId();
        try {
            cc = NSearchCmd.of(id)
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .setRepositoryFilter(repositoryFilter)
                    .latest()
                    .getResultDefinitions()
                    .map(x->x.getContent().orNull())
                    .filter(x->x!=null)
                    .findFirst().orNull();
            if(cc==null){
                //this would happen for pom ids (with no content)
                return null;
            }
        } catch (NNotFoundException ex) {
            //
        }
        if (cc != null) {
            URL url = cc.toURL().orNull();
            if (url != null) {
                List<NClassLoaderNode> aa = new ArrayList<>();
                return new NDefaultClassLoaderNode(
                        id, url, true, true,
                        aa.toArray(new NClassLoaderNode[0])
                );
            }
        }
        if (optional) {
            return null;
        }
        throw new NNotFoundException(id);
    }

    private static NClassLoaderNode toClassLoaderNodeWithOptional(NDependencyTreeNode d, boolean isOptional, boolean withChildren, NRepositoryFilter repositoryFilter) {
        NPath cc = null;
        if (!isOptional) {
            if (!NDependencyUtils.isRequiredDependency(d.getDependency())) {
                isOptional = true;
            }
        }
        try {
            cc = NFetchCmd.of(d.getDependency().toId())
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .setRepositoryFilter(repositoryFilter)
                    .getResultContent();
        } catch (NNotFoundException ex) {
            //
        }
        if (cc != null) {
            URL url = cc.toURL().orNull();
            if (url != null) {
                List<NClassLoaderNode> aa = new ArrayList<>();
                if (withChildren) {
                    for (NDependencyTreeNode child : d.getChildren()) {
                        NClassLoaderNode q = toClassLoaderNodeWithOptional(child, isOptional, true, repositoryFilter);
                        if (q != null) {
                            aa.add(q);
                        }
                    }
                }
                return new NDefaultClassLoaderNode(
                        d.getDependency().toId(), url, true, true,
                        aa.toArray(new NClassLoaderNode[0])
                );
            }
        }
        if (isOptional) {
            return null;
        }
        throw new NNotFoundException(d.getDependency().toId());
    }

    public static URL[] resolveClasspathURLs(ClassLoader contextClassLoader) {
        List<URL> all = new ArrayList<>();
        if (contextClassLoader != null) {
            if (contextClassLoader instanceof URLClassLoader) {
                all.addAll(Arrays.asList(((URLClassLoader) contextClassLoader).getURLs()));
            } else {
                //open jdk 9+ uses module and AppClassLoader no longer extends URLClassLoader
                try {
                    Enumeration<URL> r = contextClassLoader.getResources("META-INF/MANIFEST.MF");
                    while (r.hasMoreElements()) {
                        URL u = r.nextElement();
                        if ("jrt".equals(u.getProtocol())) {
                            //ignore java runtime until we find a way to retrieve their content
                            // In anyways we do not think this is useful for nuts.jar file!
                        } else if ("jar".equals(u.getProtocol())) {
                            if (u.getFile().endsWith("!/META-INF/MANIFEST.MF")) {
                                String jar = u.getFile().substring(0, u.getFile().length() - "!/META-INF/MANIFEST.MF".length());
                                all.add(CoreIOUtils.urlOf(jar));
                            }
                        } else {
                            //ignore any other loading url format!
                        }
                    }
                } catch (IOException | UncheckedIOException ex) {
                    //ignore...
                }
            }
        }
        //Thread.currentThread().getContextClassLoader()
        return all.toArray(new URL[0]);
    }
}
