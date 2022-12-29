package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNObjectElement extends AbstractNObjectElement {

    private Map<NElement, NElement> values = new LinkedHashMap<>();
    private NElements elements;

    public DefaultNObjectElement(Map<NElement, NElement> values, NSession session) {
        super(session);
        if (values != null) {
            for (Map.Entry<NElement, NElement> e : values.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    this.values.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    @Override
    public Iterator<NElementEntry> iterator() {
        return entries().iterator();
    }

    @Override
    public NOptional<NElement> get(String s) {
        if (elements == null) {
            elements = NElements.of(session);
        }
        NPrimitiveElement newKey = elements.ofString(s);
        NElement value = values.get(newKey);
        return NOptional.ofNamed(value, "property " + s);
    }

    @Override
    public NOptional<NElement> get(NElement s) {
        return NOptional.ofNamed(values.get(s), "property " + s);
    }


    @Override
    public Collection<NElementEntry> entries() {
        return values.entrySet().stream().map(x -> new DefaultNElementEntry(x.getKey(), x.getValue())).collect(Collectors.toList());
    }

    @Override
    public Stream<NElementEntry> stream() {
        return values.entrySet().stream().map(x -> new DefaultNElementEntry(x.getKey(), x.getValue()));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NObjectElementBuilder builder() {
        return NElements.of(session).ofObject().set(this);
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
}
