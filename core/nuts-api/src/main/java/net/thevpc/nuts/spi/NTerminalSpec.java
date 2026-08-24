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
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.spi;

import net.thevpc.nuts.spi.base.NSystemTerminalBase;

import java.io.Serializable;
import java.util.Map;

/**
 * @app.category Input Output
 */
public interface NTerminalSpec extends Serializable {
    /**
     * Parent.
     *
     * @return parent result
     */
    NSystemTerminalBase parent();

    /**
     * Parent.
     *
     * @param parent parent
     * @return parent result
     */
    NTerminalSpec parent(NSystemTerminalBase parent);

    /**
     * Auto complete.
     *
     * @return auto complete result
     */
    Boolean autoComplete();

    /**
     * Auto complete.
     *
     * @param autoComplete auto complete
     * @return auto complete result
     */
    NTerminalSpec autoComplete(Boolean autoComplete);

    /**
     * Returns the property.
     *
     * @param name name
     * @return get property result
     */
    Object getProperty(String name);

    /**
     * Property.
     *
     * @param name name
     * @param o o
     * @return property result
     */
    NTerminalSpec property(String name, Object o);

    /**
     * Copy from.
     *
     * @param other other
     * @return copy from result
     */
    NTerminalSpec copyFrom(NTerminalSpec other);

    /**
     * Properties.
     *
     * @param other other
     * @return properties result
     */
    NTerminalSpec properties(Map<String, Object> other);

    /**
     * Properties.
     *
     * @return properties result
     */
    Map<String, Object> properties();
}
