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
package net.thevpc.nuts.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.io.PrintStream;

/**
 * @app.category Base
 */
public interface NString extends NBlankable {

    static NString of(NMsg str) {
        NSession s = NSession.of().orNull();
        if (s == null) {
            return new NImmutableString(str == null ? null : str.toString());
        }
        return NTexts.of().ofText(str);
    }

    static NString of(String str) {
        NSession s = NSession.of().orNull();
        if (s == null) {
            return new NImmutableString(str);
        }
        return NTexts.of().parse(str);
    }

    static NString ofPlain(String str) {
        NSession s = NSession.of().orNull();
        if (s == null) {
            return new NImmutableString(str);
        }
        return NTexts.of().ofPlain(str);
    }

    NString immutable();

    /**
     * this method removes all special "nuts print format" sequences support
     * and returns the raw string to be printed on an
     * ordinary {@link PrintStream}
     *
     * @return string without any escape sequences so that the text printed
     * correctly on any non formatted {@link PrintStream}
     */
    String filteredText();

    String toString();

    /**
     * text length after filtering all special characters
     *
     * @return effective length after filtering the text
     */

    int textLength();

    NText toText();

    boolean isEmpty();

    NTextBuilder builder();
}
