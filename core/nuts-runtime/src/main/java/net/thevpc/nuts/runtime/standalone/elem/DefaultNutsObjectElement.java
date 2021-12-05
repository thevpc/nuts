package net.thevpc.nuts.runtime.standalone.elem;

import net.thevpc.nuts.*;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultNutsObjectElement extends AbstractNutsObjectElement {

    private Map<NutsElement, NutsElement> values = new LinkedHashMap<>();

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
        return children().iterator();
    }

    @Override
    public NutsElement get(String s) {
        DefaultNutsElements element = (DefaultNutsElements) NutsElements.of(session);
        return values.get(element.ofString(s));//no need for session
    }

    @Override
    public NutsElement get(NutsElement s) {
        return values.get(s);
    }

    @Override
    public NutsElement getSafe(String key) {
        NutsElement a = get(key);
        return a == null ? NutsElements.of(session).ofNull() : a;
    }

    @Override
    public Integer getSafeInt(String key) {
        return getSafe(key).asSafeInt(null);
    }

    @Override
    public Integer getSafeInt(String key, int def) {
        return getSafe(key).asSafeInt(def);
    }

    @Override
    public String getSafeString(String key) {
        return getSafe(key).asString();
    }

    @Override
    public String getSafeString(String key, String def) {
        String s = getSafe(key).asString();
        return s == null ? def : s;
    }

    @Override
    public NutsElement getSafe(NutsElement key) {
        NutsElement a = get(key);
        return a == null ? NutsElements.of(session).ofNull() : a;
    }

    @Override
    public NutsArrayElement getSafeArray(String key) {
        return getSafe(key).asSafeArray(false);
    }

    @Override
    public NutsArrayElement getSafeArray(NutsElement key) {
        return getSafe(key).asSafeArray(false);
    }

    @Override
    public NutsObjectElement getSafeObject(String key) {
        return getSafe(key).asSafeObject(false);
    }

    @Override
    public NutsObjectElement getSafeObject(NutsElement key) {
        return getSafe(key).asSafeObject(false);
    }

    @Override
    public NutsArrayElement getArray(String key) {
        return get(key).asArray();
    }

    @Override
    public NutsArrayElement getArray(NutsElement key) {
        return get(key).asArray();
    }

    @Override
    public NutsObjectElement getObject(String key) {
        NutsElement b = get(key);
        return b == null ? null : b.asObject();
    }

    @Override
    public NutsObjectElement getObject(NutsElement key) {
        return get(key).asObject();
    }

    @Override
    public String getString(String key) {
        NutsElement a = get(key);
        return a == null ? null : a.asPrimitive().getString();
    }

    @Override
    public String getString(NutsElement key) {
        NutsElement a = get(key);
        return a == null ? null : a.asPrimitive().getString();
    }

    @Override
    public boolean getBoolean(String key) {
        NutsElement b = get(key);
        return b != null && b.asPrimitive().getBoolean();
    }

    @Override
    public boolean getBoolean(NutsElement key) {
        NutsElement b = get(key);
        return b != null && b.asPrimitive().getBoolean();
    }

    @Override
    public Number getNumber(String key) {
        NutsElement b = get(key);
        return b == null ? null : b.asPrimitive().getNumber();
    }

    @Override
    public Number getNumber(NutsElement key) {
        NutsElement b = get(key);
        return b == null ? null : b.asPrimitive().getNumber();
    }

    @Override
    public byte getByte(String key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getByte();
    }

    @Override
    public byte getByte(NutsElement key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getByte();
    }

    @Override
    public int getInt(String key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getInt();
    }

    @Override
    public int getInt(NutsElement key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getInt();
    }

    @Override
    public long getLong(String key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getLong();
    }

    @Override
    public long getLong(NutsElement key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getLong();
    }

    @Override
    public short getShort(String key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getShort();
    }

    @Override
    public short getShort(NutsElement key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getShort();
    }

    @Override
    public Instant getInstant(String key) {
        NutsElement b = get(key);
        return b == null ? null : b.asPrimitive().getInstant();
    }

    @Override
    public Instant getInstant(NutsElement key) {
        NutsElement b = get(key);
        return b == null ? null : b.asPrimitive().getInstant();
    }

    @Override
    public float getFloat(String key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getFloat();
    }

    @Override
    public float getFloat(NutsElement key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getFloat();
    }

    @Override
    public double getDouble(String key) {
        NutsElement b = get(key);
        return b == null ? 0 : b.asPrimitive().getDouble();
    }

    @Override
    public double getDouble(NutsElement key) {
        NutsElement b = get(key);
        return b.asPrimitive().getDouble();
    }

    @Override
    public Collection<NutsElementEntry> children() {
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
        return "{" + children().stream().map(x ->
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
