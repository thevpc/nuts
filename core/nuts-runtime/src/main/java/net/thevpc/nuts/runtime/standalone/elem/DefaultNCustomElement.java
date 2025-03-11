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
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 *
 * @author thevpc
 */
class DefaultNCustomElement extends AbstractNElement implements NCustomElement {

    private final Object value;

    DefaultNCustomElement(Object value, NElementAnnotation[] annotations) {
        super(NElementType.CUSTOM, annotations);
        this.value = value;
    }

    @Override
    public NOptional<NElement> resolve(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        if (pattern == null || pattern.equals(".")) {
            return NOptional.of(this);
        }
        return NOptional.ofNamedEmpty(pattern);
    }

    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        if (pattern == null || pattern.equals(".")) {
            return new ArrayList<>(Arrays.asList(this));
        }
        return new ArrayList<>();
    }

    @Override
    public Object getValue() {
        return value;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }


    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 47 * hash + Objects.hashCode(this.value);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultNCustomElement other = (DefaultNCustomElement) obj;
        if (!Objects.equals(this.value, other.value)) {
            return false;
        }
        return true;
    }

    @Override
    public NOptional<Object> asObjectAt(int index) {
        return NLiteral.of(value).asObjectAt(index);
    }

    @Override
    public boolean isBlank() {
        return false;
    }


    @Override
    public NCustomElementBuilder builder() {
        return new DefaultNCustomElementBuilder().addAnnotations(annotations()).setValue(value);
    }
}
