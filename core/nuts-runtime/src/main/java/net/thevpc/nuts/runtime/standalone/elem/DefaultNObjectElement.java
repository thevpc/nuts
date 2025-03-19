package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNObjectElement extends AbstractNNavigatableElement implements NObjectElement {

    private List<NElement> values = new ArrayList<>();
    private NElements elements;
    private String name;
    private List<NElement> params;

    public DefaultNObjectElement(String name, List<NElement> params, List<NElement> values, NElementAnnotation[] annotations) {
        super(
                name == null && params == null ? NElementType.OBJECT
                        : name == null && params != null ? NElementType.PARAMETRIZED_OBJECT
                        : name != null && params == null ? NElementType.NAMED_OBJECT
                        : NElementType.NAMED_PARAMETRIZED_OBJECT,
                annotations);
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
    public Stream<NPairElement> pairs() {
        return values.stream().filter(NElement::isPair).map(x -> x.asPair().get());
    }

    @Override
    public Iterator<NElement> iterator() {
        return children().iterator();
    }

    @Override
    public NOptional<NElement> get(String s) {
        if (elements == null) {
            elements = NElements.of();
        }
        NPrimitiveElement newKey = elements.ofString(s);
        return get(newKey);
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
        return NElements.of().ofObjectBuilder().set(this);
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
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(annotations().stream().map(x -> x.toString()).collect(Collectors.joining(" ")));
        if(sb.length()>0)
        {
            sb.append(" ");
        }
        if (isNamed()) {
            sb.append(name);
        }
        if (isParametrized()) {
            sb.append("(").append(params().stream().map(x -> x.toString()).collect(Collectors.joining(", "))).append(")");
        }
        sb.append("{").append(children().stream().map(x -> x.toString()).collect(Collectors.joining(", "))).append("}");
        return sb.toString();
    }

    @Override
    public boolean isBlank() {
        return values.isEmpty();
    }

    @Override
    public NOptional<Object> asObjectAt(int index) {
        if (index >= 0 && index < values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofEmpty(() -> NMsg.ofC("invalid object at %s", index));
    }


    public String name() {
        return name;
    }

    public boolean isNamed() {
        return name != null;
    }

    public boolean isParametrized() {
        return params != null;
    }

    public List<NElement> params() {
        return params == null ? null : Collections.unmodifiableList(params);
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
