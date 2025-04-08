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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @app.category Input Output
 */
public class NDefaultTerminalSpec implements NTerminalSpec {
    private final Map<String, Object> other = new HashMap<>();
    private Boolean autoComplete;
    private NSystemTerminalBase parent;

    @Override
    public NSystemTerminalBase getParent() {
        return parent;
    }

    @Override
    public NTerminalSpec setParent(NSystemTerminalBase parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public Boolean getAutoComplete() {
        return autoComplete;
    }

    @Override
    public NTerminalSpec setAutoComplete(Boolean autoComplete) {
        this.autoComplete = autoComplete;
        return this;
    }

    @Override
    public Object get(String name) {
        return other.get(name);
    }

    @Override
    public NTerminalSpec setProperty(String name, Object o) {
        other.put(name, o);
        return this;
    }

    @Override
    public NTerminalSpec copyFrom(NTerminalSpec other) {
        this.autoComplete = other.getAutoComplete();
        setProperties(other.getProperties());
        return this;
    }

    @Override
    public NTerminalSpec setProperties(Map<String, Object> other) {
        if (other != null) {
            for (Map.Entry<String, Object> e : other.entrySet()) {
                setProperty(e.getKey(), e.getValue());
            }
        }
        return this;
    }

    @Override
    public Map<String, Object> getProperties() {
        return other;
    }

    @Override
    public int hashCode() {
        return Objects.hash(autoComplete, parent, other);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDefaultTerminalSpec that = (NDefaultTerminalSpec) o;
        return Objects.equals(autoComplete, that.autoComplete) && Objects.equals(parent, that.parent) && Objects.equals(other, that.other);
    }
}
