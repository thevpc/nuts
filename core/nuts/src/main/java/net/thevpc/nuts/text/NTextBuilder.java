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
package net.thevpc.nuts.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NStream;

import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NTextBuilder extends NString {
    static NTextBuilder of(NSession session) {
        return NTexts.of(session).ofBuilder();
    }

    NTextStyleGenerator getStyleGenerator();

    NTextBuilder setStyleGenerator(NTextStyleGenerator styleGenerator);

    NTextBuilder appendCommand(NTerminalCmd command);

    NTextBuilder appendCode(String lang, String text);

    NTextBuilder appendHash(Object text);

    NTextBuilder appendRandom(Object text);

    NTextBuilder appendHash(Object text, Object hash);

    NTextBuilder append(Object text, NTextStyle style);

    NTextBuilder append(Object text, NTextStyles styles);

    NTextBuilder append(Object node);

    NTextBuilder append(NText node);

    NTextBuilder appendJoined(Object separator, Collection<?> others);

    NTextBuilder appendAll(Collection<?> others);

    NText build();

    List<NText> getChildren();

    NText subChildren(int from, int to);

    NText substring(int from, int to);

    NTextBuilder insert(int at, NText... newTexts);

    NTextBuilder replace(int from, int to, NText... newTexts);

    NTextBuilder replaceChildren(int from, int to, NText... newTexts);

    String toString();

    int size();

    NText get(int index);

    Iterable<NText> items();

    /**
     * replaces the builder content with the simplest text in the form of suite of plain or styled text elements.
     * the possible returned types are plain text (NutsTextPlain) if there is no styling or
     * styled plain (NutsTextStyled) if any style is detected.
     * <p>
     * Compound nodes are flattened so than the returned instance is one of the following:
     * - a single line plain text (plain text than either does not include any newline or is a single newline)
     * - a styled plain (style nodes that have a single line plain text child)
     *
     * @return {@code this} instance with flattened children
     */
    NTextBuilder flatten();

    NTextBuilder removeAt(int index);

    /**
     * returns a stream of flattened text lines
     *
     * @return a stream of flattened text lines
     */
    NStream<NTextBuilder> lines();

    /**
     * return new Builder containing a flattened line read from the start of this builder
     *
     * @return new Builder containing a flattened line read from the start of this builder
     */
    NTextBuilder readLine();

}
