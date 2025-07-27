package net.thevpc.nuts.runtime.standalone.format.elem.item;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.format.elem.path.NElementPathImpl;
import net.thevpc.nuts.runtime.standalone.format.elem.NElementToStringHelper;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNObjectElement extends AbstractNListContainerElement implements NObjectElement {

    private List<NElement> values = new ArrayList<>();
    private String name;
    private List<NElement> params;

    public DefaultNObjectElement(String name, List<NElement> params, List<NElement> values, NElementAnnotation[] annotations, NElementComments comments) {
        super(
                name == null && params == null ? NElementType.OBJECT
                        : name == null && params != null ? NElementType.PARAMETRIZED_OBJECT
                        : name != null && params == null ? NElementType.NAMED_OBJECT
                        : NElementType.NAMED_PARAMETRIZED_OBJECT,
                annotations, comments);
        if (name != null) {
            NAssert.requireTrue(NElementUtils.isValidElementName(name), "valid name : " + name);
        }
        this.name = name;
        this.params = params;
        if (values != null) {
            for (NElement e : values) {
                if (e != null) {
                    this.values.add(e);
                }
            }
        }
    }

    @Override
    public boolean isCustomTree() {
        if (super.isCustomTree()) {
            return true;
        }
        if (params != null) {
            for (NElement value : params) {
                if (value.isCustomTree()) {
                    return true;
                }
            }
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
        NElements elements = NElements.of();
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
        return new ArrayList<>(values);
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

    public String toString() {
        return toString(false);
    }

    @Override
    public String toString(boolean compact) {
        NStringBuilder sb = new NStringBuilder();
        sb.append(NElementToStringHelper.leadingCommentsAndAnnotations(this, compact));
        NElementToStringHelper.appendUplet(name, params, compact, sb);
        sb.append("{");
        NElementToStringHelper.appendChildren(children(), compact, new NElementToStringHelper.SemiCompactInfo().setMaxChildren(10).setMaxLineSize(120), sb);
        sb.append("}");
        sb.append(NElementToStringHelper.trailingComments(this, compact));
        return sb.toString();
    }


    @Override
    public boolean isBlank() {
        return values.isEmpty();
    }

//    @Override
//    public NOptional<Object> asObjectAt(int index) {
//        if (index >= 0 && index < values.size()) {
//            return NOptional.of(values.get(index));
//        }
//        return NOptional.ofEmpty(() -> NMsg.ofC("invalid object at %s", index));
//    }


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
