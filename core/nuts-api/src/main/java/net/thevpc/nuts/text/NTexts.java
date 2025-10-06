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

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NTexts extends NComponent {
    static NTexts of() {
        return NExtensions.of(NTexts.class);
    }

    NTextBuilder ofBuilder();

    NText ofBlank();

    NText of(Object t);

    NText of(NMsg t);

    NTextPlain ofPlain(String t);

    NTextList ofList(NText... nodes);

    NTextList ofList(Collection<NText> nodes);

    NText ofStyled(String other, NTextStyles styles);

    NText ofStyled(NMsg other, NTextStyles styles);

    NText ofStyled(NText other, NTextStyles styles);

    NText ofStyled(String plainText, NTextStyle style);

    NText ofStyled(NMsg other, NTextStyle style);

    NText ofStyled(NText other, NTextStyle style);

    NTextTitle ofTitle(String other, int level);

    NTextTitle ofTitle(NText other, int level);

    NTextCmd ofCommand(NTerminalCmd command);

    NTextCode ofCode(String text, String lang, String sep);

    NTextCode ofCode(String lang, String text);

    NText ofCodeOrCommand(String lang, String text);

    NText ofCodeOrCommand(String text);

    NText ofCodeOrCommand(String lang, String text, String sep);

    NTitleSequence ofNumbering();

    NTitleSequence ofNumbering(String pattern);

    NTextAnchor ofAnchor(String anchorName);

    NTextLink ofLink(String value, String sep);

    NTextAnchor ofAnchor(String anchorName, String sep);

    NTextLink ofLink(String value);

    NTextInclude ofInclude(String value);

    NTextInclude ofInclude(String value, String sep);

    NOptional<NTextFormatTheme> getTheme(String name);

    NTextFormatTheme getTheme();

    NTexts setTheme(NTextFormatTheme theme);

    NTexts setTheme(String themeName);

    NCodeHighlighter getCodeHighlighter(String kind);

    NTexts addCodeHighlighter(NCodeHighlighter format);

    NTexts removeCodeHighlighter(String id);

    List<NCodeHighlighter> getCodeHighlighters();

    NText of(String t);

    NTextParser parser();

    void traverseDFS(NText text, NTextVisitor visitor);

    void traverseBFS(NText text, NTextVisitor visitor);

    NText transform(NText text, NTextTransformConfig config);

    NText transform(NText text, NTextTransformer transformer, NTextTransformConfig config);

    NNormalizedText normalize(NText text);

    NNormalizedText normalize(NText text, NTextTransformConfig config);

    NNormalizedText normalize(NText text, NTextTransformer transformer, NTextTransformConfig config);

    String escapeText(String str);

    String filterText(String text);


    NFormat createFormat(NFormatSPI format);

    <T> NFormat createFormat(T object, NTextFormat<T> format);

    <T> NOptional<NTextFormat<T>> createTextFormat(String type, String pattern, Class<T> expectedType);

    <T> NOptional<NStringFormat<T>> createStringFormat(String type, String pattern, Class<T> expectedType);

    NOptional<NTextFormat<Number>> createNumberTextFormat(String type, String pattern);

    NOptional<NStringFormat<Number>> createNumberStringFormat(String type, String pattern);

    public static NText[] trimLeft(NText[] value) {
        if (value == null) {
            return new NText[0];
        }
        int len = value.length;
        if (len == 0) {
            return value;
        }
        int st = 0;
        while ((st < len) && (value[st].isWhitespace())) {
            st++;
        }
        if (st > 0) {
            return Arrays.copyOfRange(value,st, len);
        }
        return value;
    }

    public static NText[] trimRight(NText[] value) {
        if (value == null) {
            return new NPrimitiveText[0];
        }
        int len = value.length;
        if (len == 0) {
            return value;
        }
        int st = len;
        while ((st > 0) && (value[st - 1].isWhitespace())) {
            st--;
        }
        if (st < len) {
            return Arrays.copyOfRange(value,0, st);
        }
        return value;
    }

    public static NText[] trim(NText[] value) {
        if (value == null || value.length == 0) {
            return new NPrimitiveText[0];
        }

        int start = 0;
        int end = value.length - 1;

        while (start <= end && value[start].isWhitespace()) {
            start++;
        }

        while (end >= start && value[end].isWhitespace()) {
            end--;
        }

        if (start == 0 && end == value.length - 1) {
            return value;
        }

        return Arrays.copyOfRange(value, start, end + 1);
    }

    public static NPrimitiveText[] trim(NPrimitiveText[] value) {
        if (value == null || value.length == 0) {
            return new NPrimitiveText[0];
        }

        int start = 0;
        int end = value.length - 1;

        while (start <= end && value[start].isWhitespace()) {
            start++;
        }

        while (end >= start && value[end].isWhitespace()) {
            end--;
        }

        if (start == 0 && end == value.length - 1) {
            return value;
        }

        return Arrays.copyOfRange(value, start, end + 1);
    }

    public static NPrimitiveText[] trimLeft(NPrimitiveText[] value) {
        if (value == null) {
            return new NPrimitiveText[0];
        }
        int len = value.length;
        if (len == 0) {
            return value;
        }
        int st = 0;
        while ((st < len) && (value[st].isWhitespace())) {
            st++;
        }
        if (st > 0) {
            return Arrays.copyOfRange(value,st, len);
        }
        return value;
    }

    public static NPrimitiveText[] trimRight(NPrimitiveText[] value) {
        if (value == null) {
            return new NPrimitiveText[0];
        }
        int len = value.length;
        if (len == 0) {
            return value;
        }
        int st = len;
        while ((st > 0) && (value[st - 1].isWhitespace())) {
            st--;
        }
        if (st < len) {
            return Arrays.copyOfRange(value,0, st);
        }
        return value;
    }
}
