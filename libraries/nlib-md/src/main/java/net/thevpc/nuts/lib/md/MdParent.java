/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
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
package net.thevpc.nuts.lib.md;

import java.util.Arrays;

/**
 *
 * @author thevpc
 */
public abstract class MdParent extends MdAbstractElement implements MdListElement {

    private MdElement[] elements;
    public MdParent(MdElement[] content) {
        this.elements = content==null?new MdElement[0] :content;
    }

    public MdElement[] getChildren() {
        return elements;
    }

    public MdElement getChild(int i) {
        return elements[i];
    }

    public int size() {
        return elements.length;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdParent mdParent = (MdParent) o;
        return Arrays.equals(elements, mdParent.elements);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(elements);
    }
}
