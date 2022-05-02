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
package net.thevpc.nuts.spi;

import java.io.Serializable;
import java.util.Map;

/**
 * @app.category Input Output
 */
public interface NutsTerminalSpec extends Serializable {
    NutsSystemTerminalBase getParent();

    NutsTerminalSpec setParent(NutsSystemTerminalBase parent);

    Boolean getAutoComplete();

    NutsTerminalSpec setAutoComplete(Boolean autoComplete);

    Object get(String name);

    NutsTerminalSpec setProperty(String name, Object o);

    NutsTerminalSpec setAll(NutsTerminalSpec other);

    NutsTerminalSpec setAll(Map<String, Object> other);

    Map<String, Object> getProperties();
}
