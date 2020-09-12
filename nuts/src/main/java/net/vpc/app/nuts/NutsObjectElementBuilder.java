/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.util.Collection;

/**
 * Builder for manipulating {@link NutsObjectElement} instances
 * @author vpc
 * @category Format
 */
public interface NutsObjectElementBuilder {

    /**
     * set value for property {@code name}
     * @param name property name
     * @param value property value. should not be null
     * @return this {@code this} instance
     */
    NutsObjectElementBuilder set(String name, NutsElement value);

    /**
     * remove all properties
     * @return this {@code this} instance
     */
    NutsObjectElementBuilder clear();

    /**
     * remove property
     * @param name property name
     * @return this {@code this} instance
     */
    NutsObjectElementBuilder remove(String name);

    /**
     * return value for name or null.
     * If multiple values are available return any of them.
     * @param name key name
     * @return value for name or null
     */
    NutsElement get(String name);

    /**
     * object (key,value) attributes
     * @return object attributes
     */
    Collection<NutsNamedElement> children();

    /**
     * element count
     * @return element count
     */
    int size();

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be removed.
     * @param other other instance
     * @return this {@code this} instance
     */
    NutsObjectElementBuilder set(NutsObjectElement other);

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be removed.
     * @param other other instance
     * @return this {@code this} instance
     */
    NutsObjectElementBuilder set(NutsObjectElementBuilder other);

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be retained.
     * @param other other instance
     * @return this {@code this} instance
     */
    NutsObjectElementBuilder add(NutsObjectElement other);

    /**
     * set all properties from the given {@code other} instance.
     * all properties not found in {@code other} will be retained.
     * @param other other instance
     * @return this {@code this} instance
     */
    NutsObjectElementBuilder add(NutsObjectElementBuilder other);

    /**
     * create a immutable instance of {@link NutsObjectElement} representing
     * this builder.
     * @return new instance of {@link NutsObjectElement}
     */
    NutsObjectElement build();
}
