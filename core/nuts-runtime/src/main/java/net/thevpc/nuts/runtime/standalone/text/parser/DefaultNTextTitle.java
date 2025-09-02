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

import net.thevpc.nuts.runtime.standalone.text.DefaultNTextBuilder;
import net.thevpc.nuts.text.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextTitle extends AbstractNText implements NTextTitle {

    private final String start;
    private NText child;
    private int level;

    public DefaultNTextTitle(String start, int level, NText child) {
        super();
        this.start = start;
        this.level = level;
        this.child = child;
    }

    @Override
    public NTextType type() {
        return NTextType.TITLE;
    }

    public String getTextStyleCode() {
        String s = start.trim();
        int u = s.indexOf(')');
        return s.substring(0, u);//+start.charAt(0);
    }

    public String getStart() {
        return start;
    }

    public NText getChild() {
        return child;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, child, level);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNTextTitle that = (DefaultNTextTitle) o;
        return level == that.level && Objects.equals(start, that.start) && Objects.equals(child, that.child);
    }

    @Override
    public String filteredText() {
        return child.filteredText() + "\n";
    }

    @Override
    public boolean isEmpty() {
        return child.isEmpty();
    }

    @Override
    public int length() {
        // 1 is the length of '\n'
        return child.length() + 1;
    }

    @Override
    public NText immutable() {
        return this;
    }

    @Override
    public NText simplify() {
        NText c = child.simplify();
        if (!c.equals(child)) {
            return new DefaultNTextTitle(start, level, child);
        }
        return this;
    }

    @Override
    public List<NPrimitiveText> toCharList() {
        List<NPrimitiveText> all = new ArrayList<>();
        for (NPrimitiveText aChar : child.toCharList()) {
            all.add((NPrimitiveText) DefaultNTextStyled.appendStyle(aChar, NTextStyles.of(NTextStyle.primary(level))));
        }
        all.add(new DefaultNTextPlain("\n"));
        return all;
    }

    @Override
    public NText substring(int start, int end) {
        return prerender().substring(start, end);
    }

    public List<NText> split(String chars, boolean returnSeparator) {
        return prerender().split(chars, returnSeparator);
    }

    private NText prerender() {
        NTextBuilder sb = new DefaultNTextBuilder();
        sb.append(new DefaultNTextStyled(getChild(), NTextStyles.of(NTextStyle.primary(level))));
        sb.append("\n");
        return sb.build();
    }

    @Override
    public NText trimLeft() {
        NText c = getChild().trimLeft();
        return new DefaultNTextTitle(start, level, c);
    }

    @Override
    public NText trimRight() {
        NText c = getChild().trimRight();
        return new DefaultNTextTitle(start, level, c);
    }

    @Override
    public NText trim() {
        NText c = getChild().trim();
        return new DefaultNTextTitle(start, level, c);
    }

}
