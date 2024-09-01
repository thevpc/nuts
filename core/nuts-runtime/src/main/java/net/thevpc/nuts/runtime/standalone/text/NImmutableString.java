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
package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NBlankable;

import java.io.StringReader;

/**
 * @author thevpc
 */
public class NImmutableString implements NString {
    private final String value;
    private transient final NSession session;

    public NImmutableString(NSession session, String value) {
        this.session = session;
        this.value = value == null ? "" : value;
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
        NImmutableString that = (NImmutableString) o;
        return value.equals(that.value);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public NImmutableString immutable() {
        return this;
    }

    @Override
    public String filteredText() {
        return NTexts.of(session).filterText(value);
    }

    public int textLength() {
        return filteredText().length();
    }

    public NText toText() {
        return NTexts.of(session).parser().parse(new StringReader(value));
    }

    @Override
    public boolean isEmpty() {
        return textLength() == 0;
    }

    @Override
    public NTextBuilder builder() {
        return NTexts.of(session).ofBuilder().append(this);
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(filteredText());
    }

}
