package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.elem.path.NElementPathImpl;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.text.NTreeVisitResult;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNObjectElement extends AbstractNListContainerElement implements NObjectElement {

    private List<NElement> values;
    private String name;
    private List<NElement> params;

    public DefaultNObjectElement(String name, List<NElement> params, List<NElement> values) {
        this(name,params,values,null,null,null);
    }

    public DefaultNObjectElement(String name, List<NElement> params, List<NElement> values, List<NBoundAffix> affixes, List<NElementDiagnostic> diagnostics,NElementMetadata metadata) {
        super(
                name == null && params == null ? NElementType.OBJECT
                        : name == null && params != null ? NElementType.PARAM_OBJECT
                        : name != null && params == null ? NElementType.NAMED_OBJECT
                        : NElementType.FULL_OBJECT,
                affixes,diagnostics,metadata);
        if (name != null) {
            NAssert.requireNamedTrue(NElementUtils.isValidElementName(name), "valid name : " + name);
        }
        this.name = name;
        this.params = CoreNUtils.copyAndUnmodifiableNullableList(params);
        this.values = CoreNUtils.copyAndUnmodifiableList(values);
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
    public List<NPairElement> pairs() {
        return values.stream().filter(NElement::isPair).map(x -> x.asPair().get()).collect(Collectors.toList());
    }

    @Override
    public Iterator<NElement> iterator() {
        return children().iterator();
    }

    @Override
    public NOptional<NElement> get(int index) {
        if (index < 0 || index >= values.size()) {
            return NOptional.ofNamedEmpty("property at " + index);
        }
        return NOptional.of(values.get(index));
    }

    @Override
    public NOptional<NElement> getAt(int index) {
        if (index < 0 || index >= values.size()) {
            return NOptional.ofNamedEmpty("property at " + index);
        }
        return NOptional.of(values.get(index));
    }

    @Override
    public NOptional<NElement> get(String s) {
//        NElements elements = NElements.of();
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
        return ret;
    }

    @Override
    public NOptional<NElement> get(NElement s) {
        List<NElement> a = getAll(s);
        return NOptional.ofNamedSingleton(a, "property " + s);
    }

    @Override
    public List<NElement> getAll(NElement s) {
        List<NElement> ret = new ArrayList<>();
        for (NElement x : values) {
            if (x instanceof NPairElement) {
                NPairElement e = (NPairElement) x;
                if (Objects.equals(e.key(), s)) {
                    ret.add(e.value());
                }
            }
        }
        return ret;
    }


    @Override
    public List<NElement> children() {
        return values;
    }

    @Override
    public Stream<NElement> stream() {
        return values.stream();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NObjectElementBuilder builder() {
        return NElement.ofObjectBuilder().copyFrom(this);
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 61 * hash + Objects.hashCode(this.values);
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
        final DefaultNObjectElement other = (DefaultNObjectElement) obj;
        if (!Objects.equals(this.values, other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isBlank() {
        return values.isEmpty();
    }

    public NOptional<String> name() {
        return NOptional.ofNamed(name, name);
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

    @Override
    public List<NElement> resolveAll(String pattern) {
        pattern = NStringUtils.trimToNull(pattern);
        NElementPathImpl pp = new NElementPathImpl(pattern);
        NElement[] nElements = pp.resolveReversed(this);
        return new ArrayList<>(Arrays.asList(nElements));
    }
}
