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

import net.thevpc.nuts.util.NScorableContext;

import java.util.Objects;

/**
 * Default and dummy NDefaultScorableContext implementation
 *
 * @author thevpc
 * @app.category SPI Base
 */
public class NDefaultScorableContext implements NScorableContext {

    private final Object criteria;

    public NDefaultScorableContext() {
        this.criteria = null;
    }

    /**
     * default constructor
     *
     * @param criteria constraints
     */
    public NDefaultScorableContext(Object criteria) {
        this.criteria = criteria;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCriteria() {
        return (T) criteria;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getCriteria(Class<T> expected) {
        if (criteria == null) {
            return null;
        }
        if (expected.isInstance(criteria)) {
            return (T) criteria;
        }
        return null;
    }

    @Override
    public int hashCode() {
        return Objects.hash(criteria);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NDefaultScorableContext that = (NDefaultScorableContext) o;
        return Objects.equals(criteria, that.criteria);
    }

    @Override
    public String toString() {
        return "NDefaultScorableContext{" +
                "constraints=" + criteria +
                '}';
    }
}
