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
package net.thevpc.nuts.runtime.standalone.util;

import java.net.URL;
import java.util.Arrays;
import net.thevpc.nuts.NutsClassLoaderNode;
import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsDependencyTreeNode;
import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsNotFoundException;
import net.thevpc.nuts.NutsSession;

/**
 *
 * @author vpc
 */
public final class NutsClassLoaderNodeUtils {

    public static NutsClassLoaderNode definitionToClassLoaderNode(NutsDefinition def, NutsSession session) {
        if (session == null) {
            throw new NullPointerException("session cannot be null");
        }
        if (def.getContent() == null
                || def.getContent().getURL() == null
                || def.getDependencies() == null) {
            throw new NutsIllegalArgumentException(session, "definition must provide content and dependencies");
        }
        return new NutsClassLoaderNode(
                def.getId().toString(),
                def.getContent().getURL(),
                true,
                def.getDependencies().nodes().stream().map(x -> toClassLoaderNode(x, session))
                        .toArray(NutsClassLoaderNode[]::new)
        );
    }

    private static NutsClassLoaderNode toClassLoaderNode(NutsDependencyTreeNode d, NutsSession session) {
        URL url = session.getWorkspace().fetch().setId(d.getDependency().toId())
                .setSession(session)
                .getResultContent().getURL();
        if (url == null) {
            throw new NutsNotFoundException(session, d.getDependency().toId());
        }
        return new NutsClassLoaderNode(
                d.getDependency().toId().toString(), url, true,
                Arrays.stream(d.getChildren()).map(x -> toClassLoaderNode(x, session)).toArray(NutsClassLoaderNode[]::new)
        );
    }

}
