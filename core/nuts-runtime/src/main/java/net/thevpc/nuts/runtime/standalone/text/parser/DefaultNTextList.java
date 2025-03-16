/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextList;
import net.thevpc.nuts.text.NTextType;

import java.util.*;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextList extends AbstractNText implements NTextList {

    private final List<NText> children = new ArrayList<NText>();

    public DefaultNTextList(NWorkspace workspace, NText... children) {
        super(workspace);
        if (children != null) {
            for (NText c : children) {
                if (c != null) {
                    this.children.add(c);
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (NText child : children) {
            if (!child.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NTextType getType() {
        return NTextType.LIST;
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public NText get(int index) {
        return children.get(index);
    }

    @Override
    public List<NText> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public NText simplify() {
        if (isEmpty()) {
            return new DefaultNTextPlain(workspace, "");
        }
        if (size() == 1) {
            return get(0);
        }
        return this;
    }

    @Override
    public Iterator<NText> iterator() {
        return Collections.unmodifiableList(children).iterator();
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNTextList nutsTexts = (DefaultNTextList) o;
        return Objects.equals(children, nutsTexts.children);
    }

    @Override
    public String filteredText() {
        StringBuilder sb = new StringBuilder();
        for (NText child : children) {
            sb.append(child.filteredText());
        }
        return sb.toString();
    }

    @Override
    public int textLength() {
        int count = 0;
        for (NText child : children) {
            count += child.textLength();
        }
        return count;
//        return immutable().textLength();
    }

    @Override
    public NText immutable() {
        return this;
    }
}
