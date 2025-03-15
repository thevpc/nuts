package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNObjectElement extends AbstractNObjectElement {

    private List<NElement> values = new ArrayList<>();
    private NElements elements;
    private String name;
    private List<NElement> args;

    public DefaultNObjectElement(String name, List<NElement> args, List<NElement> values, NElementAnnotation[] annotations) {
        super(annotations);
        this.name = name;
        this.args = args;
        if (values != null) {
            for (NElement e : values) {
                if (e != null) {
                    this.values.add(e);
                }
            }
        }
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
            if (x instanceof NElementEntry) {
                NElementEntry e = (NElementEntry) x;
                if (Objects.equals(e.getKey(), s)) {
                    ret.add(e.getValue());
                }
            }
        }
        return ret;
    }


    @Override
    public Collection<NElement> children() {
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
        return "{" + children().stream().map(x -> x.toString()).collect(Collectors.joining(", ")) + "}";
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

    public boolean isWithArgs() {
        return args != null;
    }

    public List<NElement> args() {
        return args == null ? null : Collections.unmodifiableList(args);
    }

    public int argsCount() {
        return args == null ? null : args.size();
    }

    public NElement argAt(int index) {
        return args == null ? null : args.get(index);
    }

}
