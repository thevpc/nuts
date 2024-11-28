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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyled;
import net.thevpc.nuts.text.NTextStyles;
import net.thevpc.nuts.text.NTextType;

import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextStyled extends AbstractNText implements NTextStyled {

    private final String start;
    private final String end;
    private NText child;
    private NTextStyles textStyles;
    private boolean completed;

    public DefaultNTextStyled(NWorkspace workspace, NText child, NTextStyles textStyle) {
        this(workspace, "##", "##", child, true, textStyle);
    }

    public DefaultNTextStyled(NWorkspace workspace, String start, String end, NText child, boolean completed, NTextStyles textStyle) {
        super(workspace);
        this.start = start;
        this.end = end;
        this.child = child;
        this.completed = completed;
        this.textStyles = textStyle;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public NTextStyles getStyles() {
        return textStyles;
    }

    @Override
    public NTextType getType() {
        return NTextType.STYLED;
    }


    public String getEnd() {
        return end;
    }

    public String getStart() {
        return start;
    }


    @Override
    public NText getChild() {
        return child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNTextStyled that = (DefaultNTextStyled) o;
        return completed == that.completed && Objects.equals(start, that.start) && Objects.equals(end, that.end) && Objects.equals(child, that.child) && Objects.equals(textStyles, that.textStyles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end, child, textStyles, completed);
    }

    @Override
    public String filteredText() {
        return child.filteredText();
    }

    @Override
    public int textLength() {
        return child.textLength();
    }

    @Override
    public NText immutable() {
        return this;
    }
}
