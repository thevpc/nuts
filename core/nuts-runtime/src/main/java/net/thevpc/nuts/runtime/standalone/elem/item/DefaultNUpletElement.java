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
import net.thevpc.nuts.runtime.standalone.elem.path.NElementPathImpl;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NTreeVisitResult;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author thevpc
 */
public class DefaultNUpletElement extends AbstractNListContainerElement
        implements NUpletElement {

    private final List<NElement> params;
    private String name;

    public DefaultNUpletElement(String name, List<NElement> params, List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics) {
        super(name == null ? NElementType.UPLET
                        : NElementType.NAMED_UPLET,
                affixes, diagnostics);
        if(name!=null){
            NAssert.requireNamedTrue(NElementUtils.isValidElementName(name), "valid name : " + name);
        }
        this.params = CoreNUtils.copyAndUnmodifiableList(params);
        this.name = name;
    }

    protected NTreeVisitResult traverseChildren(NElementVisitor visitor) {
        return traverseList(visitor, params);
    }


//    @Override
//    public NOptional<Object> asObjectAt(int index) {
//        return get(index).map(x -> x);
//    }


    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        NElementPathImpl pp = new NElementPathImpl(pattern);
        NElement[] nElements = pp.resolveReversed(this);
        return new ArrayList<>(Arrays.asList(nElements));
    }

    @Override
    public boolean isNamed(String name) {
        return isNamed() && Objects.equals(name, this.name);
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name,name);
    }

    @Override
    public boolean isNamed() {
        return name != null;
    }

    @Override
    public List<NElement> params() {
        return params;
    }


    @Override
    public List<NElement> children() {
        return params;
    }

    @Override
    public int size() {
        return params.size();
    }

    @Override
    public Stream<NElement> stream() {
        return params.stream();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < params.size()) {
            return NOptional.of(params.get(index));
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, params.size()));
    }

    @Override
    public NOptional<NElement> getAt(int index) {
        if (index >= 0 && index < params.size()) {
            return NOptional.of(params.get(index));
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, params.size()));
    }


    @Override
    public NUpletElementBuilder builder() {
        return NElement.ofUpletBuilder()
                .copyFrom(this);
    }

    @Override
    public Iterator<NElement> iterator() {
        return params.iterator();
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNUpletElement nElements = (DefaultNUpletElement) o;
        return Objects.deepEquals(params, nElements.params)
                && Objects.equals(name, nElements.name())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), params.hashCode(), name);
    }

    @Override
    public boolean isEmpty() {
        return params.isEmpty();
    }

    @Override
    public boolean isBlank() {
        return params.isEmpty() && NBlankable.isBlank(name);
    }

    @Override
    public NOptional<NElement> get(String s) {
        for (NElement x : params) {
            if (x instanceof NPairElement) {
                NPairElement e = (NPairElement) x;
                if (s == null) {
                    if (e.key().isNull()) {
                        return NOptional.of(e.value());
                    }
                } else if (e.key().isAnyString()) {
                    if (Objects.equals(e.key().asStringValue().get(), s)) {
                        return NOptional.of(e.value());
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("property " + s);
    }

    @Override
    public List<NElement> getAll(String s) {
        List<NElement> ret = new ArrayList<>();
        for (NElement x : params) {
            if (x instanceof NPairElement) {
                NPairElement e = (NPairElement) x;
                if (s == null) {
                    if (e.key().isNull()) {
                        ret.add(e.value());
                    }
                } else if (e.key().isAnyString()) {
                    if (Objects.equals(e.key().asStringValue().get(), s)) {
                        ret.add(e.value());
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public NOptional<NElement> get(NElement key) {
        return key.isString() ? key.asStringValue().flatMap(this::get) : key.asIntValue().flatMap(this::get);
    }

    @Override
    public List<NElement> getAll(NElement s) {
        int index = -1;
        if (s.isString()) {
            NOptional<Integer> ii = s.asLiteral().asInt();
            if (ii.isPresent()) {
                index = ii.get();
            } else {
                return Collections.emptyList();
            }
        } else if (s.asIntValue().isPresent()) {
            index = s.asIntValue().get();
        } else {
            return Collections.emptyList();
        }

        NOptional<NElement> a = get(index);
        if (a.isPresent()) {
            return Arrays.asList(a.get());
        }
        return Collections.emptyList();
    }

}
