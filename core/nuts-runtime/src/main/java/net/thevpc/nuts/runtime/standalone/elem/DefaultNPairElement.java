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
package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author thevpc
 */
public class DefaultNPairElement extends AbstractNElement implements NPairElement {

    private final NElement key;
    private final NElement value;

    public DefaultNPairElement(NElement key, NElement value, NElementAnnotation[] annotations, NElementComments comments) {
        super(NElementType.PAIR, annotations, comments);
        this.key = key;
        this.value = value;
    }

    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        NElementPathImpl pp = new NElementPathImpl(pattern);
        NElement[] nElements = pp.resolveReversed(this);
        return new ArrayList<>(Arrays.asList(nElements));
    }

    @Override
    public NPairElementBuilder builder() {
        return new DefaultNPairElementBuilder().copyFrom(this);
    }

    @Override
    public NOptional<Object> asObjectAt(int index) {
        return NOptional.ofEmpty(() -> NMsg.ofC("invalid object at %s", index));
    }

    @Override
    public NElement key() {
        return key;
    }

    @Override
    public NElement value() {
        return value;
    }

    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        NStringBuilder sb = new NStringBuilder();
        sb.append(TsonElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
        String skey = key.toString();
        String svalue = value.toString();
        if (compact) {
            sb.append(skey);
            sb.append(" : ");
            sb.append(svalue);
        } else {
            if (new NStringBuilder(skey).lines().count() > 1) {
                sb.append(skey);
                sb.append("\n : ");
                sb.append(new NStringBuilder(svalue).indent("  ", true));
            } else {
                sb.append(skey);
                sb.append(" : ");
                sb.append(new NStringBuilder(svalue).indent("  ", true));
            }
        }
        sb.append(TsonElementToStringHelper.trailingComments(this, compact));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNPairElement that = (DefaultNPairElement) o;
        return Objects.equals(key, that.key) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

}
