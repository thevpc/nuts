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

import net.thevpc.nuts.*;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextStyled extends AbstractNutsText implements NutsTextStyled {

    private final String start;
    private final String end;
    private NutsText child;
    private NutsTextNodeStyles textStyles;
    private boolean completed;

    public DefaultNutsTextStyled(NutsSession ws, String start, String end, NutsText child, boolean completed, NutsTextNodeStyles textStyle) {
        super(ws);
        this.start = start;
        this.end = end;
        this.child = child;
        this.completed = completed;
        this.textStyles = textStyle;
    }

    @Override
    public NutsTextNodeStyles getStyles() {
        return textStyles;
    }

    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.STYLED;
    }


    public String getEnd() {
        return end;
    }

    public String getStart() {
        return start;
    }


    @Override
    public NutsText getChild() {
        return child;
    }

}
