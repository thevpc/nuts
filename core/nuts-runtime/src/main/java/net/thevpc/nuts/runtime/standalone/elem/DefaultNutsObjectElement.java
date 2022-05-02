package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNutsObjectElement extends AbstractNutsObjectElement {

    private Map<NutsElement, NutsElement> values = new LinkedHashMap<>();
    private NutsElements elements;

    public DefaultNutsObjectElement(Map<NutsElement, NutsElement> values, NutsSession session) {
        super(session);
        if (values != null) {
            for (Map.Entry<NutsElement, NutsElement> e : values.entrySet()) {
                if (e.getKey() != null && e.getValue() != null) {
                    this.values.put(e.getKey(), e.getValue());
                }
            }
        }
    }

    @Override
    public Iterator<NutsElementEntry> iterator() {
        return entries().iterator();
    }

    @Override
    public NutsOptional<NutsElement> get(String s) {
        if (elements == null) {
            elements = NutsElements.of(session);
        }
        NutsPrimitiveElement newKey = elements.ofString(s);
        NutsElement value = values.get(newKey);
        return NutsOptional.of(value, session -> NutsMessage.cstyle("field not found : %s", s));
    }

    @Override
    public NutsOptional<NutsElement> get(NutsElement s) {
        return NutsOptional.of(values.get(s), session -> NutsMessage.cstyle("field not found : %s", s));
    }


    @Override
    public Collection<NutsElementEntry> entries() {
        return values.entrySet().stream()
                .map(x -> new DefaultNutsElementEntry(x.getKey(), x.getValue()))
                .collect(Collectors.toList());
    }

    @Override
    public Stream<NutsElementEntry> stream() {
        return values.entrySet().stream()
                .map(x -> new DefaultNutsElementEntry(x.getKey(), x.getValue()));
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public NutsObjectElementBuilder builder() {
        return NutsElements.of(session)
                .ofObject()
                .set(this);
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
        final DefaultNutsObjectElement other = (DefaultNutsObjectElement) obj;
        if (!Objects.equals(this.values, other.values)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "{" + entries().stream().map(x ->
                x.getKey()
                        + ":"
                        + x.getValue().toString()
        ).collect(Collectors.joining(", ")) + "}";
    }

    @Override
    public boolean isBlank() {
        return values.isEmpty();
    }
}
