package net.thevpc.nuts.runtime.standalone.elem.item;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementPath;
import net.thevpc.nuts.elem.NElementTransformContext;
import net.thevpc.nuts.util.NOptional;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultNElementTransformContext implements NElementTransformContext {
    private NElementPath path;
    private NElement element;
    private final Map<String, Object> properties; // Immutable/Scoped
    private final Map<String, Object> sharedConfig; // Mutable/Global
    private final boolean lastElement; // Mutable/Global

    public DefaultNElementTransformContext(NElement element) {
        this.element = element;
        this.path = NElementPath.ofRoot();
        this.properties = Collections.emptyMap();
        this.sharedConfig = new HashMap<>();
        this.lastElement = true;
    }

    public DefaultNElementTransformContext(NElement element, NElementPath path,
                                           Map<String, Object> properties,
                                           Map<String, Object> sharedConfig, boolean lastElement) {
        this.element = element;
        this.path = path;
        // Ensure properties is unmodifiable to enforce the "withProperty" pattern
        this.properties = (properties == null || properties.isEmpty()) ? Collections.emptyMap() :
                Collections.unmodifiableMap(new HashMap<>(properties));
        // sharedConfig is the SAME instance across the whole tree
        this.sharedConfig = sharedConfig == null ? new HashMap<>() : sharedConfig;
        this.lastElement = lastElement;
    }

    public Map<String, Object> properties() {
        return properties;
    }

    @Override
    public NElementPath path() {
        return path;
    }

    @Override
    public NElement element() {
        return element;
    }

    public NElementTransformContext next(NElementPath path, NElement element) {
        return newInstance(element, path, properties, sharedConfig, lastElement);
    }

    @Override
    public NElementTransformContext withPath(NElementPath path) {
        return newInstance(element, path, properties, sharedConfig, lastElement);
    }

    @Override
    public NElementTransformContext withElement(NElement element) {
        return newInstance(element, path, properties, sharedConfig, lastElement);
    }

    @Override
    public <T> NOptional<T> getProperty(String key) {
        return NOptional.ofNamed((T) properties.get(key), key);
    }

    @Override
    public <T> NOptional<T> getSharedProperty(String key) {
        return NOptional.ofNamed((T) sharedConfig.get(key), key);
    }

    @Override
    public boolean isTail() {
        return lastElement;
    }

    @Override
    public NElementTransformContext withTail(boolean tail) {
        return newInstance(element, path, properties, sharedConfig, tail);
    }

    @Override
    public NElementTransformContext withProperty(String key, Object value) {
        if (key == null) {
            return this;
        }
        if (Objects.equals(properties.get(key), value)) {
            return this;
        }
        HashMap<String, Object> p = new HashMap<>(properties);
        if (value == null) {
            p.remove(key);
        } else {
            p.put(key, value);
        }
        return newInstance(element, path, properties, sharedConfig, lastElement);
    }

    public Map<String, Object> sharedConfig() {
        return sharedConfig;
    }

    public NElementTransformContext withProperties(Map<String, Object> properties) {
        if (properties == null) {
            return this;
        }
        boolean changed = false;
        HashMap<String, Object> p = new HashMap<>(properties);
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            String key = e.getKey();
            Object value = e.getValue();
            if (!Objects.equals(properties.get(key), value)) {
                if (value == null) {
                    properties.remove(key);
                } else {
                    p.put(key, value);
                }
                changed = true;
            }
        }
        if (!changed) {
            return this;
        }
        return newInstance(element, path, properties, sharedConfig, lastElement);
    }

    protected NElementTransformContext newInstance(NElement element, NElementPath path,
                                                   Map<String, Object> properties,
                                                   Map<String, Object> sharedConfig,
                                                   boolean lastElement) {
        return new DefaultNElementTransformContext(element, path, properties, sharedConfig, lastElement);
    }

}
