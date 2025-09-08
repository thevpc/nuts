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

import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NStream;

import java.util.*;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextList extends AbstractNText implements NTextList {

    private final List<NText> children = new ArrayList<NText>();

    public DefaultNTextList(NText... children) {
        super();
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
    public NTextType type() {
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
        List<NText> all = new NTextListSimplifier().setInlineBuilders(false).addAll(children).toList();
        if (all.isEmpty()) {
            return DefaultNTextPlain.EMPTY;
        }
        if (all.size() == 1) {
            return all.get(0);
        }
        if (!all.equals(children)) {
            return new DefaultNTextList(all.toArray(new NText[0]));
        }
        return this;
    }

    @Override
    public boolean isNormalized() {
        for (NText child : children) {
            if (!child.isNormalized()) {
                return false;
            }
        }
        return true;
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
    public int length() {
        int count = 0;
        for (NText child : children) {
            count += child.length();
        }
        return count;
//        return immutable().textLength();
    }

    @Override
    public NText immutable() {
        return this;
    }


    @Override
    public NStream<NPrimitiveText> toCharStream() {
        if(children.isEmpty()) {
            return NStream.ofEmpty();
        }
        NStream<NPrimitiveText> s=children.get(0).toCharStream();
        for (int i = 1; i <children.size(); i++) {
            s=s.concat(children.get(i).toCharStream());
        }
        return s;
    }

    @Override
    public boolean isWhitespace() {
        boolean hasContent = false;
        for (NText child : children) {
            if (child.isEmpty()) {
                continue;
            }
            if (!child.isWhitespace()) {
                return false;
            }
            hasContent = true;
        }
        return hasContent;
    }

    @Override
    public List<NPrimitiveText> toCharList() {
        List<NPrimitiveText> all = new ArrayList<>();
        for (NText child : children) {
            all.addAll(child.toCharList());
        }
        return all;
    }

    @Override
    public NText substring(int start, int end) {
        if (start < 0 || end < start || end > this.length()) {
            throw new IndexOutOfBoundsException("Invalid start or end");
        }
        List<NText> result = new ArrayList<>();
        int pos = 0;
        for (NText child : getChildren()) {
            int childLen = child.filteredText().length();
            int childStart = pos;
            int childEnd = pos + childLen;
            if (childEnd <= start) {
                // before range
            } else if (childStart >= end) {
                // after range
                break;
            } else {
                int subStart = Math.max(start - childStart, 0);
                int subEnd = Math.min(end - childStart, childLen);
                result.add(child.substring(subStart, subEnd)); // delegate to child
            }
            pos += childLen;
        }
        return new DefaultNTextList(result.toArray(new NText[0]));
    }

    @Override
    public List<NText> split(String separators, boolean keepSeparators) {
        List<NText> result = new ArrayList<>();
        NTextBuilder current = NTextBuilder.of();

        for (NText child : getChildren()) {
            List<NText> parts = child.split(separators, keepSeparators); // recursively split child
            for (NText part : parts) {
                String s = part.filteredText();
                if (keepSeparators && s.length() == 1 && separators.indexOf(s.charAt(0)) >= 0) {
                    if (current.length() > 0) {
                        result.add(current.build());
                        current = NTextBuilder.of();
                    }
                    result.add(part); // separator as own element
                } else {
                    current.append(part); // normal text
                }
            }
        }

        if (current.length() > 0) {
            result.add(current.build());
        }

        return result;
    }

    @Override
    public NText trimLeft() {
        List<NText> children = new ArrayList<>(getChildren());
        boolean trimmed = false;
        for (int i = 0; i < children.size(); i++) {
            NText u = children.get(i);
            NText c = u.trimLeft();
            trimmed |= (u != c);
            int l = c.length();
            if (l > 0) {
                children.set(i, c);
                break;
            } else {
                children.remove(i);
                i--;
            }
        }
        if (!trimmed) {
            return this;
        }
        return new DefaultNTextList(children.toArray(new NText[0]));
    }

    @Override
    public NText trimRight() {
        List<NText> children = new ArrayList<>(getChildren());
        boolean trimmed = false;
        for (int i = children.size() - 1; i >= 0; i--) {
            NText u = children.get(i);
            NText c = u.trimRight();  // delegate to child
            trimmed |= (u != c);
            int l = c.length();
            if (l > 0) {
                children.set(i, c);
                break;
            } else {
                children.remove(i);
            }
        }
        if (!trimmed) {
            return this;
        }
        return new DefaultNTextList(children.toArray(new NText[0]));
    }

    @Override
    public NText trim() {
        return trimLeft().trimRight();
    }
}
