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
package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.NElementToStringHelper;
import net.thevpc.nuts.runtime.standalone.elem.path.NElementPathImpl;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class DefaultNExprElement extends AbstractNElement
        implements NExprElement {

    private final NElement[] values;

    public DefaultNExprElement(List<NElement> values, NElementAnnotation[] annotations, NElementComments comments) {
        super(NElementType.EXPR, annotations, comments);
        this.values = values.toArray(new NElement[0]);
    }


    @Override
    public boolean isCustomTree() {
        if (super.isCustomTree()) {
            return true;
        }
        if (values != null) {
            for (NElement value : values) {
                if (value.isCustomTree()) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isNamed(String name) {
        return false;
    }

    @Override
    public List<NElement> children() {
        return Arrays.asList(values);
    }

    @Override
    public int size() {
        return values.length;
    }

    @Override
    public Stream<NElement> stream() {
        return Arrays.asList(values).stream();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < values.length) {
            return NOptional.of(values[index]);
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, values.length));
    }

    @Override
    public NExprElementBuilder builder() {
        return NElement.ofExprBuilder()
                .copyFrom(this);
    }

    @Override
    public Iterator<NElement> iterator() {
        return Arrays.asList(values).iterator();
    }

    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        NStringBuilder sb = new NStringBuilder();
        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
        for (int i = 0; i < values.length; i++) {
            NElement value = values[i];
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(value.toString(compact));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNExprElement nElements = (DefaultNExprElement) o;
        return Objects.deepEquals(values, nElements.values)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), Arrays.hashCode(values));
    }

    @Override
    public boolean isEmpty() {
        return values.length == 0;
    }

    @Override
    public boolean isBlank() {
        return values.length == 0 || values.length==1 && NBlankable.isBlank(values[0]);
    }


    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        NElementPathImpl pp = new NElementPathImpl(pattern);
        NElement[] nElements = pp.resolveReversed(this);
        return new ArrayList<>(Arrays.asList(nElements));
    }
}
