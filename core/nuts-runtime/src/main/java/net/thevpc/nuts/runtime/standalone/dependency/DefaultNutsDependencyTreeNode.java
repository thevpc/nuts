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
package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.NutsDependency;
import net.thevpc.nuts.NutsDependencyTreeNode;

import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNutsDependencyTreeNode implements NutsDependencyTreeNode {

    public static final long serialVersionUID = 1L;
    private final NutsDependency dependency;
    private final List<NutsDependencyTreeNode> children;
    private final boolean partial;

    public DefaultNutsDependencyTreeNode(NutsDependency dependency, List<NutsDependencyTreeNode> children, boolean partial) {
        this.dependency = dependency;
        this.children = children;
        this.partial = partial;
    }

    @Override
    public NutsDependency getDependency() {
        return dependency;
    }

    @Override
    public List<NutsDependencyTreeNode> getChildren() {
        return children;
    }

    @Override
    public boolean isPartial() {
        return partial;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(dependency.toString());
        if (partial) {
            if (s.indexOf("?")>=0) {
                s.append("&partial=true");
            } else {
                s.append("?partial=true");
            }
        }
        if (children.size()>0) {
            if (s.indexOf("?")>=0) {
                s.append("&children-count=").append(children.size());
            } else {
                s.append("?children-count=").append(children.size());
            }
        }
        return s.toString();
    }

}
