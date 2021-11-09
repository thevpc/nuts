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
package net.thevpc.nuts;

import net.thevpc.nuts.boot.NutsApiUtils;

import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NutsTextBuilder extends NutsString {
    static NutsTextBuilder of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return NutsTexts.of(session).builder();
    }

    NutsTextStyleGenerator getStyleGenerator();

    NutsTextBuilder setStyleGenerator(NutsTextStyleGenerator styleGenerator);

    NutsTextWriteConfiguration getConfiguration();

    NutsTextBuilder setConfiguration(NutsTextWriteConfiguration writeConfiguration);

    NutsTextBuilder appendCommand(NutsTerminalCommand command);

    NutsTextBuilder appendCode(String lang, String text);

    NutsTextBuilder appendHash(Object text);

    NutsTextBuilder appendRandom(Object text);

    NutsTextBuilder appendHash(Object text, Object hash);

    NutsTextBuilder append(Object text, NutsTextStyle style);

    NutsTextBuilder append(Object text, NutsTextStyles styles);

    NutsTextBuilder append(Object node);

    NutsTextBuilder append(NutsText node);

    NutsTextBuilder appendJoined(Object separator, Collection<?> others);

    NutsTextBuilder appendAll(Collection<?> others);

    NutsText build();

    NutsTextParser parser();

    List<NutsText> getChildren();

    NutsText subChildren(int from, int to);

    NutsText substring(int from, int to);

    NutsTextBuilder insert(int at, NutsText... newTexts);

    NutsTextBuilder replace(int from, int to, NutsText... newTexts);

    NutsTextBuilder replaceChildren(int from, int to, NutsText... newTexts);

    String toString();

    int size();

    NutsText get(int index);

    Iterable<NutsText> items();

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
    NutsTextBuilder flatten();

    NutsTextBuilder removeAt(int index);

    /**
     * returns a stream of flattened text lines
     *
     * @return a stream of flattened text lines
     */
    NutsStream<NutsTextBuilder> lines();

    /**
     * return new Builder containing a flattened line read from the start of this builder
     *
     * @return new Builder containing a flattened line read from the start of this builder
     */
    NutsTextBuilder readLine();

}
