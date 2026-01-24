package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.Map;

public interface NElementTransformContext {
    NElementPath path();

    NElement element();

    Map<String, Object> properties();

    NElementTransformContext withPath(NElementPath path);

    NElementTransformContext withElement(NElement element);

    Map<String, Object> sharedConfig();

    NElementTransformContext withProperty(String key, Object value);

    <T> NOptional<T> getProperty(String key);

    <T> NOptional<T> getSharedProperty(String key);

    NElementTransformContext withProperties(Map<String, Object> properties);
}
