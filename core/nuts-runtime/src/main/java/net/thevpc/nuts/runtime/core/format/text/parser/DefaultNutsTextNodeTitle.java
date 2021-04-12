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
 *
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
package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeTitle;
import net.thevpc.nuts.NutsTextNodeType;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextNodeTitle extends AbstractNutsTextNode implements NutsTextNodeTitle {

    private final String start;
    private NutsTextNode child;
    private int level;

    public DefaultNutsTextNodeTitle(NutsSession ws,String start, int level, NutsTextNode child) {
        super(ws);
        this.start = start;
        this.level = level;
        this.child = child;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.TITLE;
    }

    public String getTextStyleCode() {
        String s= start.trim();
        int u = s.indexOf(')');
        return s.substring(0,u);//+start.charAt(0);
    }

    public String getStart() {
        return start;
    }

    public NutsTextNode getChild() {
        return child;
    }

}
