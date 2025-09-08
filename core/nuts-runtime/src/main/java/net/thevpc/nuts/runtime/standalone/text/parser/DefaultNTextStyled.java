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

import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NStream;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextStyled extends AbstractNText implements NTextStyled {

    private final String start;
    private final String end;
    private NText child;
    private NTextStyles textStyles;
    private boolean completed;

    public static NText appendStyle(NText any, NTextStyles textStyles) {
        if (textStyles == null || textStyles.isPlain()) {
            return any;
        }
        if (any instanceof NTextStyled) {
            NTextStyled base = (NTextStyled) any;
            NTextStyles styles = base.getStyles();
            NTextStyles newStyles = styles.append(textStyles);
            return new DefaultNTextStyled(base.getChild(), newStyles);
        }
        return new DefaultNTextStyled(any, textStyles);
    }

    public DefaultNTextStyled(NText child, NTextStyles textStyle) {
        this("##", "##", child, true, textStyle);
    }

    public DefaultNTextStyled(String start, String end, NText child, boolean completed, NTextStyles textStyles) {
        super();
        this.start = start;
        this.end = end;
        this.child = child;
        this.completed = completed;
        this.textStyles = textStyles;
    }

    @Override
    public boolean isEmpty() {
        return child.isEmpty();
    }

    @Override
    public NTextStyles getStyles() {
        return textStyles;
    }

    @Override
    public NTextType type() {
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
    public int length() {
        return child.length();
    }

    @Override
    public NText immutable() {
        return this;
    }

    @Override
    public NText simplify() {
        NText c = child.simplify();
        if (child.equals(DefaultNTextPlain.EMPTY)) {
            return DefaultNTextPlain.EMPTY;
        }
        if (!c.equals(child)) {
            return new DefaultNTextStyled(start, end, c, completed, textStyles);
        }
        return this;
    }

    @Override
    public List<NPrimitiveText> toCharList() {
        List<NPrimitiveText> all = new ArrayList<>();
        for (NPrimitiveText child : child.toCharList()) {
            all.add((NPrimitiveText) DefaultNTextStyled.appendStyle(child, textStyles));
        }
        return all;
    }

    @Override
    public boolean isWhitespace() {
        return child.isWhitespace();
    }

    @Override
    public NStream<NPrimitiveText> toCharStream() {
        return child.toCharStream().map(x -> (NPrimitiveText) DefaultNTextStyled.appendStyle(x, textStyles));
    }

    @Override
    public NText substring(int start, int end) {
        return new DefaultNTextStyled(getStart(), getEnd(), child.substring(start, end), completed, textStyles);
    }

    @Override
    public List<NText> split(String separator, boolean returnSeparator) {
        return child.split(separator, returnSeparator).stream().map(x -> DefaultNTextStyled.appendStyle(x, textStyles)).collect(Collectors.toList());
    }

    @Override
    public NText trimLeft() {
        NText c = child.trimLeft();
        if (c == child) {
            return this;
        }
        return new DefaultNTextStyled(getStart(), getEnd(), c, completed, textStyles);
    }

    @Override
    public NText trimRight() {
        NText c = child.trimRight();
        if (c == child) {
            return this;
        }
        return new DefaultNTextStyled(getStart(), getEnd(), c, completed, textStyles);
    }

    @Override
    public NText trim() {
        NText c = child.trim();
        if (c == child) {
            return this;
        }
        return new DefaultNTextStyled(getStart(), getEnd(), c, completed, textStyles);
    }

}
