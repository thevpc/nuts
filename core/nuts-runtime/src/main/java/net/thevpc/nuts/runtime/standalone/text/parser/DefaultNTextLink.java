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
package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.text.NTextLink;
import net.thevpc.nuts.text.NTextType;

import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextLink extends NTextSpecialBase implements NTextLink {
    private String value;

    public DefaultNTextLink(String separator, String value) {
        super("```!", "link", separator, "```");
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }


    @Override
    public NTextType getType() {
        return NTextType.LINK;
    }

    @Override
    public boolean isEmpty() {
        return NBlankable.isBlank(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNTextLink that = (DefaultNTextLink) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public NText immutable() {
        return this;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), value);
    }

    @Override
    public String filteredText() {
        return value == null ? "" : value;
    }

    @Override
    public int textLength() {
        return value == null ? 0 : value.length();
    }

    @Override
    public NText simplify() {
        if (value.isEmpty()) {
            return DefaultNTextPlain.EMPTY;
        }
        return this;
    }
}
