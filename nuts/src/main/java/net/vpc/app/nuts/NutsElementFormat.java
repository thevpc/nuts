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
 * Copyright (C) 2016-2017 Taha BEN SALAH
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

/**
 * Class responsible of manipulating {@link NutsElement} type. It help parsing
 * from, converting to and formatting such types.
 *
 * @author vpc
 * @since 0.5.5
 */
public interface NutsElementFormat extends NutsFormat {

    /**
     * convert any object to valid {@link NutsElement}.
     *
     * @param object object to convert
     * @return converted value
     */
    NutsElement toElement(Object object);

    /**
     * convert element to the specified object if applicable or throw an
     * exception.
     *
     * @param <T> return type
     * @param element element to convert
     * @param clazz class type
     * @return instance of type {@code T} converted from {@code element}
     */
    <T> T fromElement(NutsElement element, Class<T> clazz);

    /**
     * return current value to format.
     *
     * @return current value to format
     * @since 0.5.6
     */
    Object getValue();

    /**
     * set current value to format.
     *
     * @param value value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsElementFormat set(Object value);

    /**
     * set current value to format.
     *
     * @param value value to format
     * @return {@code this} instance
     * @since 0.5.6
     */
    NutsElementFormat setValue(Object value);

    /**
     * set current session.
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsElementFormat setSession(NutsSession session);

    /**
     * compile pathExpression into a valid NutsElementPath that helps filtering
     * elements tree.
     * JSONPath expressions refer to a JSON structure the same way as XPath expression are used with XML documents. 
     * JSONPath expressions can use the dot notation and/or bracket  notations
     *  .store.book[0].title
     *  The trailing root is not necessary : 
     *  .store.book[0].title
     *  You can also use  bracket notation
     *  store['book'][0].title
     *  for input paths.
     * @param pathExpression element path expression
     * @return Element Path filter
     */
    NutsElementPath compilePath(String pathExpression);
    
    /**
     * element builder
     * @return element builder
     */
    NutsElementBuilder builder();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsElementFormat configure(boolean skipUnsupported, String... args);

}
