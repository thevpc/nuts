/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.text;

import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.pipeline.NStream;

import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NTextBuilder extends NText, Iterable<NText> {

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTextBuilder of() {
        NSession s = NSession.get().orNull();
        if (s == null) {
            return new NTextBuilderPlain();
        }
        return NTextRPI.of().createBuilder();
    }

    /**
     * Style generator.
     *
     * @return style generator result
     */
    NTextStyleGenerator styleGenerator();

    /**
     * Style generator.
     *
     * @param styleGenerator style generator
     * @return style generator result
     */
    NTextBuilder styleGenerator(NTextStyleGenerator styleGenerator);

    /**
     * Append command.
     *
     * @param command command
     * @return append command result
     */
    NTextBuilder appendCommand(NTerminalCmd command);

    /**
     * Append code.
     *
     * @param lang lang
     * @param text text
     * @return append code result
     */
    NTextBuilder appendCode(String lang, String text);

    /**
     * Append hash style.
     *
     * @param text text
     * @return append hash style result
     */
    NTextBuilder appendHashStyle(Object text);

    /**
     * Append random style.
     *
     * @param text text
     * @return append random style result
     */
    NTextBuilder appendRandomStyle(Object text);

    /**
     * Append hash style.
     *
     * @param text text
     * @param hash hash
     * @return append hash style result
     */
    NTextBuilder appendHashStyle(Object text, Object hash);

    /**
     * Append.
     *
     * @param text text
     * @param style style
     * @return append result
     */
    NTextBuilder append(Object text, NTextStyle style);

    /**
     * Append.
     *
     * @param text text
     * @param styles styles
     * @return append result
     */
    NTextBuilder append(Object text, NTextStyles styles);

    /**
     * Append.
     *
     * @param node node
     * @return append result
     */
    NTextBuilder append(Object node);

    /**
     * Append.
     *
     * @param node node
     * @return append result
     */
    NTextBuilder append(NText node);

    /**
     * Append joined.
     *
     * @param separator separator
     * @param others others
     * @return append joined result
     */
    NTextBuilder appendJoined(Object separator, Collection<?> others);

    /**
     * Append all.
     *
     * @param others others
     * @return append all result
     */
    NTextBuilder appendAll(Collection<?> others);

    /**
     * Append all.
     *
     * @param others others
     * @return append all result
     */
    NTextBuilder appendAll(NText[] others);

    /**
     * Build.
     *
     * @return build result
     */
    NText build();

    /**
     * Children.
     *
     * @return children result
     */
    List<NText> children();

    /**
     * Delete.
     *
     * @param start start
     * @param end end
     * @return delete result
     */
    NTextBuilder delete(int start, int end);

    /**
     * Insert.
     *
     * @param at at
     * @param newTexts new texts
     * @return insert result
     */
    NTextBuilder insert(int at, NText... newTexts);

    /**
     * Replace.
     *
     * @param from from
     * @param to to
     * @param newTexts new texts
     * @return replace result
     */
    NTextBuilder replace(int from, int to, NText... newTexts);

    String toString();

    /**
     * Size.
     *
     * @return size result
     */
    int size();

    /**
     * Returns the get.
     *
     * @param index index
     * @return get result
     */
    NText get(int index);

    /**
     * Strip.
     *
     * @return strip result
     */
    NTextBuilder strip();

    /**
     * Strip left.
     *
     * @return strip left result
     */
    NTextBuilder stripLeft();

    /**
     * Strip right.
     *
     * @return strip right result
     */
    NTextBuilder stripRight();

    /**
     * replaces the builder content with the simplest text in the form of suite
     * of plain or styled text elements. the possible returned types are plain
     * text (NutsTextPlain) if there is no styling or styled plain
     * (NutsTextStyled) if any style is detected.
     * <p>
     * Compound nodes are flattened so than the returned instance is one of the
     * following: - a single line plain text (plain text than either does not
     * include any newline or is a single newline) - a styled plain (style nodes
     * that have a single line plain text child)
     *
     * @return {@code this} instance with flattened children
     */
    NTextBuilder flatten();

    /**
     * Removes the specified at.
     *
     * @param index index
     * @return remove at result
     */
    NTextBuilder removeAt(int index);

    /**
     * returns a stream of flattened text lines
     *
     * @return a stream of flattened text lines
     */
    NStream<NTextBuilder> lines();

    /**
     * return new Builder containing a flattened line read from the start of
     * this builder
     *
     * @return new Builder containing a flattened line read from the start of
     * this builder
     */
    NTextBuilder readLine();

    /**
     * New line.
     *
     * @return new line result
     */
    NTextBuilder newLine();

    /**
     * Clear.
     *
     * @return clear result
     */
    NTextBuilder clear();

    /**
     * Indent.
     *
     * @param prefix prefix
     * @return indent result
     */
    NTextBuilder indent(NText prefix);

    /**
     * Indent.
     *
     * @param prefix prefix
     * @param skipFirstLine skip first line
     * @return indent result
     */
    NTextBuilder indent(NText prefix, boolean skipFirstLine);
}
