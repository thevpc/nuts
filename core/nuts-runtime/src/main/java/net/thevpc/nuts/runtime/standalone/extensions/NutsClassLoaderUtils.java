/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.extensions;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

import net.thevpc.nuts.*;

/**
 *
 * @author thevpc
 */
public final class NutsClassLoaderUtils {

    public static NutsClassLoaderNode definitionToClassLoaderNode(NutsDefinition def, NutsSession session) {
        if (session == null) {
            throw new NullPointerException("session cannot be null");
        }
        if (def.getContent() == null
                || def.getContent().getURL() == null
                || def.getDependencies() == null) {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("definition must provide content and dependencies"));
        }
        return new NutsClassLoaderNode(
                def.getId().toString(),
                def.getContent().getURL(),
                true,
                true,
                def.getDependencies().nodes().stream().map(x -> toClassLoaderNode(x, session))
                        .filter(Objects::nonNull)
                        .toArray(NutsClassLoaderNode[]::new)
        );
    }

    private static NutsClassLoaderNode toClassLoaderNode(NutsDependencyTreeNode d, NutsSession session) {
        NutsContent cc=null;
        try {
            cc = session.fetch().setId(d.getDependency().toId())
                    .setSession(session)
                    .getResultContent();
        }catch (NutsNotFoundException ex){
            //
        }
        if (cc == null) {
            if(d.getDependency().isOptional()){
                return null;
            }
            throw new NutsNotFoundException(session, d.getDependency().toId());
        }
        URL url = cc.getURL();
        if (url == null) {
            if(d.getDependency().isOptional()){
                return null;
            }
            throw new NutsNotFoundException(session, d.getDependency().toId());
        }
        return new NutsClassLoaderNode(
                d.getDependency().toId().toString(), url, true, true,
                Arrays.stream(d.getChildren()).map(x -> toClassLoaderNode(x, session)).toArray(NutsClassLoaderNode[]::new)
        );
    }

    public static URL[] resolveClasspathURLs(ClassLoader contextClassLoader) {
        List<URL> all = new ArrayList<>();
        if (contextClassLoader != null) {
            if (contextClassLoader instanceof URLClassLoader) {
                all.addAll(Arrays.asList(((URLClassLoader) contextClassLoader).getURLs()));
            }else{
                //open jdk 9+ uses module and AppClassLoader no longer extends URLClassLoader
                try {
                    Enumeration<URL> r = contextClassLoader.getResources("META-INF/MANIFEST.MF");
                    while (r.hasMoreElements()) {
                        URL u = r.nextElement();
                        if("jrt".equals(u.getProtocol())) {
                            //ignore java runtime until we find a way to retrieve their content
                            // In anyways we do not think this is useful for nuts.jar file!
                        }else if("jar".equals(u.getProtocol())){
                            if(u.getFile().endsWith("!/META-INF/MANIFEST.MF")){
                                String jar = u.getFile().substring(0, u.getFile().length() - "!/META-INF/MANIFEST.MF".length());
                                all.add(new URL(jar));
                            }
                        }else {
                            //ignore any other loading url format!
                        }
                    }
                }catch (IOException ex){
                    //ignore...
                }
            }
        }
        //Thread.currentThread().getContextClassLoader()
        return all.toArray(new URL[0]);
    }
}
