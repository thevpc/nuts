/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextList extends AbstractNutsText implements NutsTextList {

    private final List<NutsText> children = new ArrayList<NutsText>();

    public DefaultNutsTextList(NutsSession session, NutsText... children) {
        super(session);
//        NutsTextPlain lastPlain=null;
//        NutsTextPlain newPlain=null;
//        if (children != null) {
//            for (NutsText c : children) {
//                if (c != null) {
//                    newPlain=(c instanceof NutsTextPlain)?(NutsTextPlain) c:null;
//                    if(lastPlain!=null && newPlain!=null){
//                        this.children.remove(this.children.size()-1);
//                        newPlain = new DefaultNutsTextPlain(
//                                session, lastPlain.getText() + newPlain.getText()
//                        );
//                        this.children.add(newPlain);
//                    }else {
//                        this.children.add(c);
//                    }
//                    lastPlain=newPlain;
//                }
//            }
//        }
        if (children != null) {
            for (NutsText c : children) {
                if (c != null) {
                    this.children.add(c);
                }
            }
        }
    }

    @Override
    public boolean isEmpty() {
        for (NutsText child : children) {
            if (!child.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public NutsTextType getType() {
        return NutsTextType.LIST;
    }

    @Override
    public int size() {
        return children.size();
    }

    @Override
    public NutsText get(int index) {
        return children.get(index);
    }

    @Override
    public List<NutsText> getChildren() {
        return new ArrayList<>(children);
    }

    @Override
    public NutsText simplify() {
        if (isEmpty()) {
            return new DefaultNutsTextPlain(getSession(), "");
        }
        if (size() == 1) {
            return get(0);
        }
        return this;
    }

    @Override
    public Iterator<NutsText> iterator() {
        return children.iterator();
    }

    @Override
    public int hashCode() {
        return Objects.hash(children);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNutsTextList nutsTexts = (DefaultNutsTextList) o;
        return Objects.equals(children, nutsTexts.children);
    }

    @Override
    public String filteredText() {
        StringBuilder sb=new StringBuilder();
        for (NutsText child : children) {
            sb.append(child.filteredText());
        }
        return sb.toString();
    }
}
