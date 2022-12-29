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
package net.thevpc.nuts.cmdline;

import java.io.Serializable;
import java.util.Objects;

/**
 * Argument Candidate used in Auto Complete.
 * <p>
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.5
 */
public class DefaultNArgumentCandidate implements Serializable, NArgumentCandidate {

    private final String value;
    private final String display;

    /**
     * @param value value
     */
    public DefaultNArgumentCandidate(String value) {
        this.value = value;
        this.display = value;
    }

    public DefaultNArgumentCandidate(String value, String display) {
        this.value = value;
        this.display = display;
    }

    /**
     * argument value
     *
     * @return argument value
     */
    @Override
    public String getValue() {
        return value;
    }

    /**
     * human display
     *
     * @return human display
     */
    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, display);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNArgumentCandidate that = (DefaultNArgumentCandidate) o;
        return Objects.equals(value, that.value) && Objects.equals(display, that.display);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
