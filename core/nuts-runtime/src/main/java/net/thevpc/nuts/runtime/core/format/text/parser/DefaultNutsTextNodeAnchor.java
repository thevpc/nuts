/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
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
package net.thevpc.nuts.runtime.core.format.text.parser;


import net.thevpc.nuts.NutsTextNodeAnchor;
import net.thevpc.nuts.NutsTextNodeType;
import net.thevpc.nuts.NutsWorkspace;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextNodeAnchor extends NutsTextNodeSpecialBase implements NutsTextNodeAnchor {
    private String value;

    public DefaultNutsTextNodeAnchor(NutsWorkspace ws,String start, String separator, String end, String value) {
        super(ws,start, "anchor", separator, end);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.ANCHOR;
    }

}
