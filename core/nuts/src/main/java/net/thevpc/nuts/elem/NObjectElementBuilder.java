/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.elem;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NAssert;

import java.util.Collection;
import java.util.Map;

/**
 * Builder for manipulating {@link NObjectElement} instances
 *
 * @author thevpc
 * @app.category Format
 */
public interface NObjectElementBuilder extends NElementBuilder {

    static NObjectElementBuilder of(NSession session) {
        return NElements.of(session).ofObject();
    }

    /**
     * set value for property {@code name}
     *
     * @param name  property name
     * @param value property value. should not be null
     * @return this {@code this} instance
     */
    NObjectElementBuilder set(String name, NElement value);

    NObjectElementBuilder set(String name, boolean value);

    NObjectElementBuilder set(String name, int value);

    NObjectElementBuilder set(String name, double value);

    NObjectElementBuilder set(String name, String value);

    NObjectElementBuilder addAll(Map<NElement, NElement> other);

    /**
     * remove all properties
     *
     * @return this {@code this} instance
     */
    NObjectElementBuilder clear();

    /**
     * remove property
     *
     * @param name property name
     * @return this {@code this} instance
     */
    NObjectElementBuilder remove(String name);

    /**
     * return value for name or null.
     * If multiple values are available return any of them.
     *
     * @param name key name
     * @return value for name or null
     */
    NElement get(String name);

    /**
     * object (key,value) attributes
     *
     * @return object attributes
     */
    Collection<NElementEntry> children();

    /**
     * element count
     *
     * @return element count
     */
    int size();

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be removed.
     *
     * @param other other instance
     * @return this {@code this} instance
     */
    NObjectElementBuilder set(NObjectElement other);

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be removed.
     *
     * @param other other instance
     * @return this {@code this} instance
     */
    NObjectElementBuilder set(NObjectElementBuilder other);

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be retained.
     *
     * @param other other instance
     * @return this {@code this} instance
     */
    NObjectElementBuilder add(NObjectElement other);

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be retained.
     *
     * @param other other instance
     * @return this {@code this} instance
     */
    NObjectElementBuilder add(NObjectElementBuilder other);

    /**
     * add entry key value binding
     *
     * @param entry other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder add(NElementEntry entry);

    /**
     * create a immutable instance of {@link NObjectElement} representing
     * this builder.
     *
     * @return new instance of {@link NObjectElement}
     */
    NObjectElement build();

    NObjectElementBuilder remove(NElement name);

    NObjectElementBuilder set(NElement name, NElement value);

    NObjectElementBuilder set(NElement name, String value);

    NObjectElementBuilder set(NElement name, boolean value);

    NObjectElementBuilder set(NElement name, double value);

    NObjectElementBuilder set(NElement name, int value);

    NElement get(NElement s);

    /**
     * add all  key value entries binding
     *
     * @param entries other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder addAll(NElementEntry... entries);

    /**
     * add all  key value entries binding
     *
     * @param other other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder addAll(NObjectElement other);

    /**
     * add all  key value entries binding
     *
     * @param other other entry
     * @return this {@code this} instance
     */
    NObjectElementBuilder addAll(NObjectElementBuilder other);
}
