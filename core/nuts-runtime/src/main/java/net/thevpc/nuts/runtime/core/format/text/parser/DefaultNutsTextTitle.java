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
import net.thevpc.nuts.NutsTextTitle;
import net.thevpc.nuts.NutsTextType;
import net.thevpc.nuts.NutsText;

import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextTitle extends AbstractNutsText implements NutsTextTitle {

    private final String start;
    private NutsText child;
    private int level;

    public DefaultNutsTextTitle(NutsSession session, String start, int level, NutsText child) {
        super(session);
        this.start = start;
        this.level = level;
        this.child = child;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public NutsTextType getType() {
        return NutsTextType.TITLE;
    }

    public String getTextStyleCode() {
        String s= start.trim();
        int u = s.indexOf(')');
        return s.substring(0,u);//+start.charAt(0);
    }

    public String getStart() {
        return start;
    }

    public NutsText getChild() {
        return child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsTextTitle that = (DefaultNutsTextTitle) o;
        return level == that.level && Objects.equals(start, that.start) && Objects.equals(child, that.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, child, level);
    }
}
