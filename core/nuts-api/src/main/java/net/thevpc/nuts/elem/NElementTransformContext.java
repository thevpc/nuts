package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.NOptional;

import java.util.Map;

/**
 * NElementTransformContext interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NElementTransformContext {
    /**
     * Path.
     *
     * @return path result
     */
    NElementPath path();

    /**
     * Element.
     *
     * @return element result
     */
    NElement element();

    /**
     * Checks if is tail.
     *
     * @return is tail result
     */
    boolean isTail();

    /**
     * Properties.
     *
     * @return properties result
     */
    Map<String, Object> properties();

    /**
     * With tail.
     *
     * @param tail tail
     * @return with tail result
     */
    NElementTransformContext withTail(boolean tail);

    /**
     * With path.
     *
     * @param path path
     * @return with path result
     */
    NElementTransformContext withPath(NElementPath path);

    /**
     * With element.
     *
     * @param element element
     * @return with element result
     */
    NElementTransformContext withElement(NElement element);

    /**
     * Shared config.
     *
     * @return shared config result
     */
    Map<String, Object> sharedConfig();

    /**
     * With property.
     *
     * @param key key
     * @param value value
     * @return with property result
     */
    NElementTransformContext withProperty(String key, Object value);

    /**
     * Returns the property.
     *
     * @param key key
     * @return get property result
     */
    <T> NOptional<T> getProperty(String key);

    /**
     * Returns the shared property.
     *
     * @param key key
     * @return get shared property result
     */
    <T> NOptional<T> getSharedProperty(String key);

    /**
     * With properties.
     *
     * @param properties properties
     * @return with properties result
     */
    NElementTransformContext withProperties(Map<String, Object> properties);
}
