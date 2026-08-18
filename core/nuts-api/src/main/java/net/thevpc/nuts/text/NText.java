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
    static NText of(String str) {
        return NTextRPI.of().createText(str);
    }

    static NText ofPlain(String str) {
        return NTextRPI.of().createPlain(str);
    }

    static NText ofNewLine() {
        return ofPlain("\n");
    }

    static NText of(Object str) {
        return NTextRPI.of().createText(str);
    }

    static NText of(NMsg str) {
        return NTextRPI.of().createText(str);
    }

    static NText ofBlank() {
        return NTextRPI.of().createBlank();
    }

    static NTextList ofList(NText... nodes) {
        return NTextRPI.of().createList(nodes);
    }

    static NTextList ofList(Collection<NText> nodes) {
        return NTextRPI.of().createList(nodes);
    }

    static NText ofStyled(String other, NTextStyles styles) {
        return NTextRPI.of().createStyled(other, styles);
    }

    static NText ofStyled(NMsg other, NTextStyles styles) {
        return NTextRPI.of().createStyled(other, styles);
    }

    static NText ofStyled(NText other, NTextStyles styles) {
        return NTextRPI.of().createStyled(other, styles);
    }

    static NText ofStyled(String plainText, NTextStyle style) {
        return NTextRPI.of().createStyled(plainText, style);
    }

    static NText ofStyledError(String other) {
        return NTextRPI.of().createStyled(other, NTextStyle.error());
    }

    static NText ofStyledPath(String plainPath) {
        return NTextRPI.of().createStyled(plainPath, NTextStyle.path());
    }

    static NText ofStyled(NMsg other, NTextStyle style) {
        return NTextRPI.of().createStyled(other, style);
    }

    static NText ofStyled(NText other, NTextStyle style) {
        return NTextRPI.of().createStyled(other, style);
    }

    static NTextTitle ofTitle(String other, int level) {
        return NTextRPI.of().createTitle(other, level);
    }

    static NTextTitle ofTitle(NText other, int level) {
        return NTextRPI.of().createTitle(other, level);
    }

    static NTextCmd ofCommand(NTerminalCmd command) {
        return NTextRPI.of().createCommand(command);
    }

    static NTextCode ofCode(String lang, String text, String sep) {
        return NTextRPI.of().createCode(text, lang, sep);
    }

    static NTextCode ofCode(String lang, String text) {
        return NTextRPI.of().createCode(lang, text);
    }

    static NText ofCodeOrCommand(String lang, String text) {
        return NTextRPI.of().createCodeOrCommand(lang, text);
    }

    static NText ofCodeOrCommand(String text) {
        return NTextRPI.of().createCodeOrCommand(text);
    }

    static NText ofCodeOrCommand(String lang, String text, String sep) {
        return NTextRPI.of().createCodeOrCommand(lang, text, sep);
    }

    static NTitleSequence ofNumbering() {
        return NTextRPI.of().createNumbering();
    }

    static NTitleSequence ofNumbering(String pattern) {
        return NTextRPI.of().createNumbering(pattern);
    }

    static NTextAnchor ofAnchor(String anchorName) {
        return NTextRPI.of().createAnchor(anchorName);
    }

    static NTextLink ofLink(String value, String sep) {
        return NTextRPI.of().createLink(value, sep);
    }

    static NTextAnchor ofAnchor(String anchorName, String sep) {
        return NTextRPI.of().createAnchor(anchorName, sep);
    }

    static NTextLink ofLink(String value) {
        return NTextRPI.of().createLink(value);
    }

    static NTextInclude ofInclude(String value) {
        return NTextRPI.of().createInclude(value);
    }

    static NTextInclude ofInclude(String value, String sep) {
        return NTextRPI.of().createInclude(value, sep);
    }

    static NText ofStyledSuccess(String value) {
        return ofStyled(value, NTextStyle.success());
    }

    static NText ofStyledWarn(String value) {
        return ofStyled(value, NTextStyle.warn());
    }

    static NText ofStyledPrimary1(String value) {
        return ofStyled(value, NTextStyle.primary1());
    }

    static NText ofStyledPrimary2(String value) {
        return ofStyled(value, NTextStyle.primary2());
    }

    static NText ofStyledPrimary3(String value) {
        return ofStyled(value, NTextStyle.primary3());
    }

    static NText ofStyledPrimary4(String value) {
        return ofStyled(value, NTextStyle.primary4());
    }

    static NText ofStyledPrimary5(String value) {
        return ofStyled(value, NTextStyle.primary5());
    }

    static NText ofStyledPrimary6(String value) {
        return ofStyled(value, NTextStyle.primary6());
    }

    static NText ofStyledPrimary7(String value) {
        return ofStyled(value, NTextStyle.primary7());
    }

    static NText ofStyledPrimary8(String value) {
        return ofStyled(value, NTextStyle.primary8());
    }

    static NText ofStyledPrimary9(String value) {
        return ofStyled(value, NTextStyle.primary9());
    }

    static NText ofSpace() {
        return ofPlain(" ");
    }

    static NText ofSpaces(int columns) {
        if (columns <= 0) {
            return ofBlank();
        }
        return ofPlain(NStringUtils.repeat(' ', columns));
    }

    static void traverseDFS(NText text, NTextVisitor visitor) {
        NTextRPI.of().traverseDFS(text, visitor);
    }

    static void traverseBFS(NText text, NTextVisitor visitor) {
        NTextRPI.of().traverseBFS(text, visitor);
    }

    static NText transform(NText text, NTextTransformConfig config) {
        return NTextRPI.of().transform(text, config);
    }

    static NText transform(NText text, NTextTransformer transformer, NTextTransformConfig config) {
        return NTextRPI.of().transform(text, transformer, config);
    }

    static NNormalizedText normalize(NText text){
        return NTextRPI.of().normalize(text);
    }

    static NNormalizedText normalize(NText text, NTextTransformConfig config){
        return NTextRPI.of().normalize(text, config);
    }

    static NNormalizedText normalize(NText text, NTextTransformer transformer, NTextTransformConfig config){
        return NTextRPI.of().normalize(text, transformer, config);
    }

    static NText[] stripLeft(NText[] value) {
        return NTextRPI.stripLeft(value);
    }

    static NText[] stripRight(NText[] value) {
        return NTextRPI.stripRight(value);
    }

    static NText[] strip(NText[] value) {
        return NTextRPI.strip(value);
    }

    static NPrimitiveText[] strip(NPrimitiveText[] value) {
        return NTextRPI.strip(value);
    }

    static NPrimitiveText[] stripLeft(NPrimitiveText[] value) {
        return NTextRPI.stripLeft(value);
    }

    static NPrimitiveText[] stripRight(NPrimitiveText[] value) {
        return NTextRPI.stripRight(value);
    }

    NTextType type();

    NTextBuilder builder();

    NText substring(int start, int end);

    boolean isPrimitive();

    boolean isNormalized();

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

    boolean isEmpty();

    NText simplify();

    List<NText> split(char c);

    List<NText> split(char c, boolean returnSeparator);

    List<NText> split(String separator);

    List<NText> split(String separator, boolean returnSeparator);

    List<NText> splitLines(boolean returnSeparator);

    List<NText> splitLines();

    List<NText> split(Pattern separator, boolean returnSeparator);

    NPrimitiveText[] toCharArray();

    List<NPrimitiveText> toCharList();

    List<NPrimitiveText> toPrimitiveList();

    NStream<NPrimitiveText> toCharStream();

    NText strip();

    NText stripLeft();

    NText stripRight();

    NText repeat(int times);

    NText repeatln(int times);

    NText concat(NText other);

    NText concat(NText... others);

    boolean isString(String anyString);

    boolean isNewLine();

    boolean isWhitespace();

    NNormalizedText normalize();

    NNormalizedText normalize(NTextTransformConfig config);

    NNormalizedText normalize(NTextTransformer transformer, NTextTransformConfig config);
}
