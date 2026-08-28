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

import net.thevpc.nuts.elem.NElementSimple;
import net.thevpc.nuts.internal.rpi.NTextRPI;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.util.NSince;
import net.thevpc.nuts.util.NStringUtils;

import java.io.PrintStream;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by vpc on 5/23/17.
 *
 * @app.category Format
 */
public interface NText extends NBlankable, NElementSimple {
    /**
     * Creates a new instance of of.
     *
     * @param str str
     * @return of result
     */
    static NText of(String str) {
        return NTextRPI.of().createText(str);
    }

    /**
     * Creates a new instance of of plain.
     *
     * @param str str
     * @return of plain result
     */
    static NText ofPlain(String str) {
        return NTextRPI.of().createPlain(str);
    }

    /**
     * Creates a new instance of of new line.
     *
     * @return of new line result
     */
    static NText ofNewLine() {
        /**
         * Creates a new instance of of plain.
         *
         * @param "\n" "\n"
         * @return of plain result
         */
        return ofPlain("\n");
    }

    /**
     * Creates a new instance of of.
     *
     * @param str str
     * @return of result
     */
    static NText of(Object str) {
        return NTextRPI.of().createText(str);
    }

    /**
     * Creates a new instance of of.
     *
     * @param str str
     * @return of result
     */
    static NText of(NMsg str) {
        return NTextRPI.of().createText(str);
    }

    /**
     * Creates a new instance of of blank.
     *
     * @return of blank result
     */
    static NText ofBlank() {
        return NTextRPI.of().createBlank();
    }

    /**
     * Creates a new instance of of list.
     *
     * @param nodes nodes
     * @return of list result
     */
    static NTextList ofList(NText... nodes) {
        return NTextRPI.of().createList(nodes);
    }

    /**
     * Creates a new instance of of list.
     *
     * @param nodes nodes
     * @return of list result
     */
    static NTextList ofList(Collection<NText> nodes) {
        return NTextRPI.of().createList(nodes);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param other other
     * @param styles styles
     * @return of styled result
     */
    static NText ofStyled(String other, NTextStyles styles) {
        return NTextRPI.of().createStyled(other, styles);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param other other
     * @param styles styles
     * @return of styled result
     */
    static NText ofStyled(NMsg other, NTextStyles styles) {
        return NTextRPI.of().createStyled(other, styles);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param other other
     * @param styles styles
     * @return of styled result
     */
    static NText ofStyled(NText other, NTextStyles styles) {
        return NTextRPI.of().createStyled(other, styles);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param plainText plain text
     * @param style style
     * @return of styled result
     */
    static NText ofStyled(String plainText, NTextStyle style) {
        return NTextRPI.of().createStyled(plainText, style);
    }

    /**
     * Creates a new instance of of styled error.
     *
     * @param other other
     * @return of styled error result
     */
    static NText ofStyledError(String other) {
        return NTextRPI.of().createStyled(other, NTextStyle.error());
    }

    /**
     * Creates a new instance of of styled path.
     *
     * @param plainPath plain path
     * @return of styled path result
     */
    static NText ofStyledPath(String plainPath) {
        return NTextRPI.of().createStyled(plainPath, NTextStyle.path());
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param other other
     * @param style style
     * @return of styled result
     */
    static NText ofStyled(NMsg other, NTextStyle style) {
        return NTextRPI.of().createStyled(other, style);
    }

    /**
     * Creates a new instance of of styled.
     *
     * @param other other
     * @param style style
     * @return of styled result
     */
    static NText ofStyled(NText other, NTextStyle style) {
        return NTextRPI.of().createStyled(other, style);
    }

    /**
     * Creates a new instance of of title.
     *
     * @param other other
     * @param level level
     * @return of title result
     */
    static NTextTitle ofTitle(String other, int level) {
        return NTextRPI.of().createTitle(other, level);
    }

    /**
     * Creates a new instance of of title.
     *
     * @param other other
     * @param level level
     * @return of title result
     */
    static NTextTitle ofTitle(NText other, int level) {
        return NTextRPI.of().createTitle(other, level);
    }

    /**
     * Creates a new instance of of command.
     *
     * @param command command
     * @return of command result
     */
    static NTextCmd ofCommand(NTerminalCmd command) {
        return NTextRPI.of().createCommand(command);
    }

    /**
     * Creates a new instance of of code.
     *
     * @param lang lang
     * @param text text
     * @param sep sep
     * @return of code result
     */
    static NTextCode ofCode(String lang, String text, String sep) {
        return NTextRPI.of().createCode(text, lang, sep);
    }

    /**
     * Creates a new instance of of code.
     *
     * @param lang lang
     * @param text text
     * @return of code result
     */
    static NTextCode ofCode(String lang, String text) {
        return NTextRPI.of().createCode(lang, text);
    }

    /**
     * Creates a new instance of of code or command.
     *
     * @param lang lang
     * @param text text
     * @return of code or command result
     */
    static NText ofCodeOrCommand(String lang, String text) {
        return NTextRPI.of().createCodeOrCommand(lang, text);
    }

    /**
     * Creates a new instance of of code or command.
     *
     * @param text text
     * @return of code or command result
     */
    static NText ofCodeOrCommand(String text) {
        return NTextRPI.of().createCodeOrCommand(text);
    }

    /**
     * Creates a new instance of of code or command.
     *
     * @param lang lang
     * @param text text
     * @param sep sep
     * @return of code or command result
     */
    static NText ofCodeOrCommand(String lang, String text, String sep) {
        return NTextRPI.of().createCodeOrCommand(lang, text, sep);
    }

    /**
     * Creates a new instance of of numbering.
     *
     * @return of numbering result
     */
    static NTitleSequence ofNumbering() {
        return NTextRPI.of().createNumbering();
    }

    /**
     * Creates a new instance of of numbering.
     *
     * @param pattern pattern
     * @return of numbering result
     */
    static NTitleSequence ofNumbering(String pattern) {
        return NTextRPI.of().createNumbering(pattern);
    }

    /**
     * Creates a new instance of of anchor.
     *
     * @param anchorName anchor name
     * @return of anchor result
     */
    static NTextAnchor ofAnchor(String anchorName) {
        return NTextRPI.of().createAnchor(anchorName);
    }

    /**
     * Creates a new instance of of link.
     *
     * @param value value
     * @param sep sep
     * @return of link result
     */
    static NTextLink ofLink(String value, String sep) {
        return NTextRPI.of().createLink(value, sep);
    }

    /**
     * Creates a new instance of of anchor.
     *
     * @param anchorName anchor name
     * @param sep sep
     * @return of anchor result
     */
    static NTextAnchor ofAnchor(String anchorName, String sep) {
        return NTextRPI.of().createAnchor(anchorName, sep);
    }

    /**
     * Creates a new instance of of link.
     *
     * @param value value
     * @return of link result
     */
    static NTextLink ofLink(String value) {
        return NTextRPI.of().createLink(value);
    }

    /**
     * Creates a new instance of of include.
     *
     * @param value value
     * @return of include result
     */
    static NTextInclude ofInclude(String value) {
        return NTextRPI.of().createInclude(value);
    }

    /**
     * Creates a new instance of of include.
     *
     * @param value value
     * @param sep sep
     * @return of include result
     */
    static NTextInclude ofInclude(String value, String sep) {
        return NTextRPI.of().createInclude(value, sep);
    }

    /**
     * Creates a new instance of of styled success.
     *
     * @param value value
     * @return of styled success result
     */
    static NText ofStyledSuccess(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.success() n text style.success()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.success());
    }

    /**
     * Creates a new instance of of styled warn.
     *
     * @param value value
     * @return of styled warn result
     */
    static NText ofStyledWarn(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.warn() n text style.warn()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.warn());
    }

    /**
     * Creates a new instance of of styled primary1.
     *
     * @param value value
     * @return of styled primary1 result
     */
    static NText ofStyledPrimary1(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary1() n text style.primary1()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary1());
    }

    /**
     * Creates a new instance of of styled primary2.
     *
     * @param value value
     * @return of styled primary2 result
     */
    static NText ofStyledPrimary2(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary2() n text style.primary2()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary2());
    }

    /**
     * Creates a new instance of of styled primary3.
     *
     * @param value value
     * @return of styled primary3 result
     */
    static NText ofStyledPrimary3(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary3() n text style.primary3()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary3());
    }

    /**
     * Creates a new instance of of styled primary4.
     *
     * @param value value
     * @return of styled primary4 result
     */
    static NText ofStyledPrimary4(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary4() n text style.primary4()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary4());
    }

    /**
     * Creates a new instance of of styled primary5.
     *
     * @param value value
     * @return of styled primary5 result
     */
    static NText ofStyledPrimary5(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary5() n text style.primary5()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary5());
    }

    /**
     * Creates a new instance of of styled primary6.
     *
     * @param value value
     * @return of styled primary6 result
     */
    static NText ofStyledPrimary6(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary6() n text style.primary6()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary6());
    }

    /**
     * Creates a new instance of of styled primary7.
     *
     * @param value value
     * @return of styled primary7 result
     */
    static NText ofStyledPrimary7(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary7() n text style.primary7()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary7());
    }

    /**
     * Creates a new instance of of styled primary8.
     *
     * @param value value
     * @return of styled primary8 result
     */
    static NText ofStyledPrimary8(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary8() n text style.primary8()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary8());
    }

    /**
     * Creates a new instance of of styled primary9.
     *
     * @param value value
     * @return of styled primary9 result
     */
    static NText ofStyledPrimary9(String value) {
        /**
         * Creates a new instance of of styled.
         *
         * @param value value
         * @param NTextStyle.primary9() n text style.primary9()
         * @return of styled result
         */
        return ofStyled(value, NTextStyle.primary9());
    }

    /**
     * Creates a new instance of of space.
     *
     * @return of space result
     */
    static NText ofSpace() {
        /**
         * Creates a new instance of of plain.
         *
         * @param " "
         * @return of plain result
         */
        return ofPlain(" ");
    }

    /**
     * Creates a new instance of of spaces.
     *
     * @param columns columns
     * @return of spaces result
     */
    static NText ofSpaces(int columns) {
        if (columns <= 0) {
            /**
             * Creates a new instance of of blank.
             *
             * @return of blank result
             */
            return ofBlank();
        }
        /**
         * Creates a new instance of of plain.
         *
         * @param columns) columns)
         * @return of plain result
         */
        return ofPlain(NStringUtils.repeat(' ', columns));
    }

    /**
     * Transform.
     *
     * @param config config
     * @return transform result
     */
    NText transform(NTextTransformConfig config) ;

    /**
     * Transform.
     *
     * @param transformer transformer
     * @param config config
     * @return transform result
     */
    NText transform(NTextTransformer transformer, NTextTransformConfig config) ;


    NStream<NNormalizedText> normalizeStream(NTextTransformer transformer, NTextTransformConfig config);

    /**
     * Strip left.
     *
     * @param value value
     * @return strip left result
     */
    static NText[] stripLeft(NText[] value) {
        return NTextRPI.stripLeft(value);
    }

    /**
     * Strip right.
     *
     * @param value value
     * @return strip right result
     */
    static NText[] stripRight(NText[] value) {
        return NTextRPI.stripRight(value);
    }

    /**
     * Strip.
     *
     * @param value value
     * @return strip result
     */
    static NText[] strip(NText[] value) {
        return NTextRPI.strip(value);
    }

    /**
     * Strip.
     *
     * @param value value
     * @return strip result
     */
    static NPrimitiveText[] strip(NPrimitiveText[] value) {
        return NTextRPI.strip(value);
    }

    /**
     * Strip left.
     *
     * @param value value
     * @return strip left result
     */
    static NPrimitiveText[] stripLeft(NPrimitiveText[] value) {
        return NTextRPI.stripLeft(value);
    }

    /**
     * Strip right.
     *
     * @param value value
     * @return strip right result
     */
    static NPrimitiveText[] stripRight(NPrimitiveText[] value) {
        return NTextRPI.stripRight(value);
    }

    /**
     * Type.
     *
     * @return type result
     */
    NTextType type();

    /**
     * Builder.
     *
     * @return builder result
     */
    NTextBuilder builder();

    /**
     * Substring.
     *
     * @param start start
     * @param end end
     * @return substring result
     */
    NText substring(int start, int end);

    /**
     * Checks if is primitive.
     *
     * @return is primitive result
     */
    boolean isPrimitive();

    /**
     * Checks if is normalized.
     *
     * @return is normalized result
     */
    boolean isNormalized();

    /**
     * Immutable.
     *
     * @return immutable result
     */
    NText immutable();

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

    int length();

    /**
     * Checks if is empty.
     *
     * @return is empty result
     */
    boolean isEmpty();

    /**
     * Simplify.
     *
     * @return simplify result
     */
    NText simplify();

    /**
     * Split.
     *
     * @param c c
     * @return split result
     */
    List<NText> split(char c);

    /**
     * Split.
     *
     * @param c c
     * @param returnSeparator return separator
     * @return split result
     */
    List<NText> split(char c, boolean returnSeparator);

    /**
     * Split.
     *
     * @param separator separator
     * @return split result
     */
    List<NText> split(String separator);

    /**
     * Split.
     *
     * @param separator separator
     * @param returnSeparator return separator
     * @return split result
     */
    List<NText> split(String separator, boolean returnSeparator);

    /**
     * Split lines.
     *
     * @param returnSeparator return separator
     * @return split lines result
     */
    List<NText> splitLines(boolean returnSeparator);

    /**
     * Split lines.
     *
     * @return split lines result
     */
    List<NText> splitLines();

    /**
     * Split.
     *
     * @param separator separator
     * @param returnSeparator return separator
     * @return split result
     */
    List<NText> split(Pattern separator, boolean returnSeparator);

    /**
     * Converts to char array.
     *
     * @return to char array result
     */
    NPrimitiveText[] toCharArray();

    /**
     * Converts to char list.
     *
     * @return to char list result
     */
    List<NPrimitiveText> toCharList();

    /**
     * Converts to primitive list.
     *
     * @return to primitive list result
     */
    List<NPrimitiveText> toPrimitiveList();

    /**
     * Converts to char stream.
     *
     * @return to char stream result
     */
    NStream<NPrimitiveText> toCharStream();

    /**
     * Strip.
     *
     * @return strip result
     */
    NText strip();

    /**
     * Strip left.
     *
     * @return strip left result
     */
    NText stripLeft();

    /**
     * Strip right.
     *
     * @return strip right result
     */
    NText stripRight();

    /**
     * Repeat.
     *
     * @param times times
     * @return repeat result
     */
    NText repeat(int times);

    /**
     * Repeatln.
     *
     * @param times times
     * @return repeatln result
     */
    NText repeatln(int times);

    /**
     * Concat.
     *
     * @param other other
     * @return concat result
     */
    NText concat(NText other);

    /**
     * Concat.
     *
     * @param others others
     * @return concat result
     */
    NText concat(NText... others);

    /**
     * Checks if is string.
     *
     * @param anyString any string
     * @return is string result
     */
    boolean isString(String anyString);

    /**
     * Checks if is new line.
     *
     * @return is new line result
     */
    boolean isNewLine();

    /**
     * Checks if is whitespace.
     *
     * @return is whitespace result
     */
    boolean isWhitespace();

    /**
     * Normalize.
     *
     * @return normalize result
     */
    NNormalizedText normalize();

    /**
     * Normalize.
     *
     * @param config config
     * @return normalize result
     */
    NNormalizedText normalize(NTextTransformConfig config);

    /**
     * Normalize.
     *
     * @param transformer transformer
     * @param config config
     * @return normalize result
     */
    NNormalizedText normalize(NTextTransformer transformer, NTextTransformConfig config);

    /**
     * Traverse dfs.
     *
     * @param visitor visitor
     */
    @NSince("1.0.0")
    NText traverseDFS(NTextVisitor visitor) ;

    /**
     * Traverse bfs.
     *
     * @param visitor visitor
     */
    @NSince("1.0.0")
    NText traverseBFS(NTextVisitor visitor) ;

}
