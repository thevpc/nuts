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
package net.thevpc.nuts.toolbox.ndoc.doc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author thevpc
 */
public class MdDocletConfig {
    private String target;
    private String backend;
    private List<String> src = new ArrayList<>();
    private List<String> packages = new ArrayList<>();

    public String getTarget() {
        return target;
    }

    public MdDocletConfig setTarget(String target) {
        this.target = target;
        return this;
    }

    public String getBackend() {
        return backend;
    }

    public MdDocletConfig setBackend(String backend) {
        this.backend = backend;
        return this;
    }

    public MdDocletConfig addSource(String src){
        this.src.add(src);
        return this;
    }

    public MdDocletConfig addSources(Collection<String> srcs){
        for (String s : srcs) {
            addSource(s);
        }
        return this;
    }

    public String[] getSources() {
        return src.toArray(new String[0]);
    }

    public MdDocletConfig addPackage(String src){
        this.packages.add(src);
        return this;
    }

    public MdDocletConfig addPackages(Collection<String> srcs){
        for (String s : srcs) {
            addPackage(s);
        }
        return this;
    }

    public String[] getPackages() {
        return packages.toArray(new String[0]);
    }
}
