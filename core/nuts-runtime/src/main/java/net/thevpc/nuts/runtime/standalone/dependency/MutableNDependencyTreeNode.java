/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.dependency;

import java.util.ArrayList;
import java.util.List;

import net.thevpc.nuts.NDependency;
import net.thevpc.nuts.NDependencyTreeNode;

/**
 *
 * @author thevpc
 */
public class MutableNDependencyTreeNode implements NDependencyTreeNode {

    public static final long serialVersionUID = 1L;
    private NDependency dependency;
    private List<NDependencyTreeNode> children;
    private boolean partial;

    public MutableNDependencyTreeNode() {
    }
    
    public MutableNDependencyTreeNode(NDependencyTreeNode n) {
        this.dependency=n.getDependency();
        this.children=new ArrayList<>(n.getChildren());
        this.partial=n.isPartial();
    }
    

    public void setDependency(NDependency dependency) {
        this.dependency = dependency;
    }

    public void setChildren(List<NDependencyTreeNode> children) {
        this.children = children;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    @Override
    public NDependency getDependency() {
        return dependency;
    }

    @Override
    public List<NDependencyTreeNode> getChildren() {
        return children;
    }

    @Override
    public boolean isPartial() {
        return partial;
    }
}
