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

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.io.PrintStream;
import java.util.Collection;

/**
 * Created by vpc on 5/23/17.
 *
 * @app.category Format
 */
public interface NText extends NBlankable {
    static NText of(String str) {
        return NTexts.of().of(str);
    }

    static NText ofPlain(String str) {
        return NTexts.of().ofPlain(str);
    }

    static NText of(Object str) {
        return NTexts.of().of(str);
    }

    static NText of(NMsg str) {
        return NTexts.of().of(str);
    }

    static NText ofBlank() {
        return NTexts.of().ofBlank();
    }

    static NTextList ofList(NText... nodes) {
        return NTexts.of().ofList(nodes);
    }

    static NTextList ofList(Collection<NText> nodes) {
        return NTexts.of().ofList(nodes);
    }

    static NText ofStyled(String other, NTextStyles styles) {
        return NTexts.of().ofStyled(other, styles);
    }

    static NText ofStyled(NMsg other, NTextStyles styles) {
        return NTexts.of().ofStyled(other, styles);
    }

    static NText ofStyled(NText other, NTextStyles styles) {
        return NTexts.of().ofStyled(other, styles);
    }

    static NText ofStyled(String plainText, NTextStyle style) {
        return NTexts.of().ofStyled(plainText, style);
    }

    static NText ofStyledError(String other) {
        return NTexts.of().ofStyled(other, NTextStyle.error());
    }

    static NText ofStyledPath(String plainPath) {
        return NTexts.of().ofStyled(plainPath, NTextStyle.path());
    }

    static NText ofStyled(NMsg other, NTextStyle style) {
        return NTexts.of().ofStyled(other, style);
    }

    static NText ofStyled(NText other, NTextStyle style) {
        return NTexts.of().ofStyled(other, style);
    }

    static NTextTitle ofTitle(String other, int level) {
        return NTexts.of().ofTitle(other, level);
    }

    static NTextTitle ofTitle(NText other, int level) {
        return NTexts.of().ofTitle(other, level);
    }

    static NTextCmd ofCommand(NTerminalCmd command) {
        return NTexts.of().ofCommand(command);
    }

    static NTextCode ofCode(String lang, String text, String sep) {
        return NTexts.of().ofCode(text, lang, sep);
    }

    static NTextCode ofCode(String lang, String text) {
        return NTexts.of().ofCode(lang, text);
    }

    static NText ofCodeOrCommand(String lang, String text) {
        return NTexts.of().ofCodeOrCommand(lang, text);
    }

    static NText ofCodeOrCommand(String text) {
        return NTexts.of().ofCodeOrCommand(text);
    }

    static NText ofCodeOrCommand(String lang, String text, String sep) {
        return NTexts.of().ofCodeOrCommand(lang, text, sep);
    }

    static NTitleSequence ofNumbering() {
        return NTexts.of().ofNumbering();
    }

    static NTitleSequence ofNumbering(String pattern) {
        return NTexts.of().ofNumbering(pattern);
    }

    static NTextAnchor ofAnchor(String anchorName) {
        return NTexts.of().ofAnchor(anchorName);
    }

    static NTextLink ofLink(String value, String sep) {
        return NTexts.of().ofLink(value, sep);
    }

    static NTextAnchor ofAnchor(String anchorName, String sep) {
        return NTexts.of().ofAnchor(anchorName, sep);
    }

    static NTextLink ofLink(String value) {
        return NTexts.of().ofLink(value);
    }

    static NTextInclude ofInclude(String value) {
        return NTexts.of().ofInclude(value);
    }

    static NTextInclude ofInclude(String value, String sep) {
        return NTexts.of().ofInclude(value, sep);
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


    NTextType getType();

    NTextBuilder builder();


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

    int textLength();

    boolean isEmpty();

    NText simplify();
}
