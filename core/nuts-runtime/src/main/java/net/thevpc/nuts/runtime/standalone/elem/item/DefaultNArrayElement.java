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
public class DefaultNArrayElement extends AbstractNListContainerElement
        implements NArrayElement {

    private final List<NElement> values;
    private String name;
    private List<NElement> params;

    public DefaultNArrayElement(String name, List<NElement> params, List<NElement> values) {
        this(name,params,values,null,null,null);
    }

    public DefaultNArrayElement(String name, List<NElement> params, List<NElement> values, List<NBoundAffix> affixes,
                                List<NElementDiagnostic> diagnostics,NElementMetadata metadata) {
        super(
                name == null && params == null ? NElementType.ARRAY
                        : name == null && params != null ? NElementType.PARAM_ARRAY
                        : name != null && params == null ? NElementType.NAMED_ARRAY
                        : NElementType.FULL_ARRAY,
                affixes,diagnostics,metadata);
        if (name != null) {
            NAssert.requireNamedTrue(NElementUtils.isValidElementName(name), "valid name : "+name);
        }
        this.values = CoreNUtils.copyAndUnmodifiableList(values);
        this.name = name;
        this.params = CoreNUtils.copyAndUnmodifiableNullableList(params);
    }

    protected NTreeVisitResult traverseChildren(NElementVisitor visitor) {
        NTreeVisitResult r = traverseList(visitor, params);
        if(r!=NTreeVisitResult.CONTINUE){
            return r;
        }
        if(r==NTreeVisitResult.SKIP_SIBLINGS){
            return NTreeVisitResult.CONTINUE;
        }
        r = traverseList(visitor, values); // body
        return r;
    }


    @Override
    public boolean isNamed(String name) {
        return isNamed() && Objects.equals(name, this.name);
    }

    @Override
    public List<NElement> children() {
        return values;
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public Stream<NElement> stream() {
        return values.stream();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, values.size()));
    }


    @Override
    public NOptional<NElement> getAt(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofError(() -> NMsg.ofC("invalid array index %s not in [%s,%s[", index, 0, values.size()));
    }


    @Override
    public NArrayElementBuilder builder() {
        return NElement.ofArrayBuilder()
                .copyFrom(this);
    }

    @Override
    public Iterator<NElement> iterator() {
        return values.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNArrayElement nElements = (DefaultNArrayElement) o;
        return Objects.deepEquals(values, nElements.values)
                && Objects.equals(name, nElements.name)
                && Objects.equals(params, nElements.params)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), values.hashCode(), name, params);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public boolean isBlank() {
        return values.isEmpty() && NBlankable.isBlank(name) && (params == null || params.isEmpty());
    }


    @Override
    public NOptional<NElement> get(String s) {
        for (NElement x : values) {
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
        if (NLiteral.of(s).asInt().isPresent()) {
            return get(NLiteral.of(s).asInt().get());
        }
        return NOptional.ofNamedEmpty("property " + s);
    }

    @Override
    public List<NElement> getAll(String s) {
        List<NElement> ret = new ArrayList<>();
        for (NElement x : values) {
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
        if (ret.isEmpty()) {
            if (NLiteral.of(s).asInt().isPresent()) {
                NOptional<NElement> u = get(NLiteral.of(s).asInt().get());
                if (u.isPresent()) {
                    ret.add(u.get());
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
        if (s.isAnyString()) {
            NOptional<Integer> ii = NLiteral.of(s.asStringValue().get()).asInt();
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

    public NOptional<String> name() {
        return NOptional.ofNamed(name,name);
    }

    public boolean isNamed() {
        return name != null;
    }

    public boolean isParametrized() {
        return params != null;
    }

    public NOptional<List<NElement>> params() {
        return params == null ? NOptional.ofNamedEmpty("params") : NOptional.of(Collections.unmodifiableList(params));
    }

    public int paramsCount() {
        return params == null ? null : params.size();
    }

    public NElement param(int index) {
        return params == null ? null : params.get(index);
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
}
