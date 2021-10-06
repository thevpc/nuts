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
package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.NutsWorkspace;

import java.io.StringReader;
import net.thevpc.nuts.NutsText;

/**
 * @author thevpc
 */
public class NutsImmutableString implements NutsString {
    private final String value;
    private transient final NutsSession ws;

    public NutsImmutableString(NutsSession ws, String value) {
        this.ws = ws;
        this.value = value == null ? "" : value;
    }

    public int textLength() {
        return filteredText().length();
    }

    @Override
    public String filteredText() {
        return ws.text().
                parser().filterText(value);
    }

    public NutsText toText() {
        return ws.text().parser().parse(new StringReader(value));
    }

    public String getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsImmutableString that = (NutsImmutableString) o;
        return value.equals(that.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public NutsImmutableString immutable() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return textLength()==0;
    }
}
