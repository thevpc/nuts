package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNObjectElement extends AbstractNObjectElement {

    private List<NElementEntry> values = new ArrayList<>();
    private Map<NElement, List<Integer>> indexes = new HashMap<>();
    private NElements elements;
    private NElementHeader header;

    public DefaultNObjectElement(List<NElementEntry> values, NElementHeader header, NElementAnnotation[] annotations) {
        super(annotations);
        this.header=header;
        if (values != null) {
            for (NElementEntry e : values) {
                NElement key = e.getKey();
                if (key != null && e.getValue() != null) {
                    int index = this.values.size();
                    this.values.add(new DefaultNElementEntry(key, e.getValue()));
                    indexes.computeIfAbsent(key, x -> new ArrayList<>()).add(index);
                }
            }
        }
    }

    @Override
    public NElementHeader header() {
        return header;
    }

    @Override
    public Iterator<NElementEntry> iterator() {
        return entries().iterator();
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
        List<Integer> integers = indexes.get(s);
        if (integers == null) {
            return new ArrayList<>();
        }
        return integers.stream().map(x -> values.get(x).getValue()).collect(Collectors.toList());
    }


    @Override
    public Collection<NElementEntry> entries() {
        return new ArrayList<>(values);
    }

    @Override
    public Stream<NElementEntry> stream() {
        return values.stream();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NObjectElementBuilder builder() {
        return NElements.of().ofObject().set(this);
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
        return "{" + entries().stream().map(x -> x.getKey() + ":" + x.getValue().toString()).collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public boolean isBlank() {
        return values.isEmpty();
    }

    @Override
    public NOptional<Object> asObjectAt(int index) {
        if(index>=0 && index<values.size()) {
            return NOptional.of(values.get(index));
        }
        return NOptional.ofEmpty(()-> NMsg.ofC("invalid object at %s",index));
    }
}
