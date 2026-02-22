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
package net.thevpc.nuts.elem;

import net.thevpc.nuts.text.NNewLineMode;

import java.util.List;

/**
 * primitive values implementation of Nuts Element type. Nuts Element types are
 * generic JSON like parsable objects.
 *
 * @author thevpc
 * @app.category Elements
 * @since 0.5.6
 */
public interface NStringElement extends NPrimitiveElement {

    /**
     * unparsed raw value of the string, including separators and boundaries
     *
     * @return
     */
    String rawValue();

    /**
     * when block or line string, this is the type of the newline used as suffix (or null if at the end of the file).
     * this value is null for all other string types
     *
     * @return
     */
    NNewLineMode newLineSuffix();

    String stringValue();

    List<NElementLine> lines();

    NPrimitiveElementBuilder builder();

    String literalString();

    NNewLineMode newlineSuffix();
    NStringElement withNewlineSuffix(NNewLineMode nNewLineMode);
}
