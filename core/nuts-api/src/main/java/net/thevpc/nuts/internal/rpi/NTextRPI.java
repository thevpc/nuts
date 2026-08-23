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
package net.thevpc.nuts.internal.rpi;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NSetter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NTextRPI extends NComponent {
    static NTextRPI of() {
        return NExtensions.of(NTextRPI.class);
    }

    NTableCellSpecBuilder createCellSpecBuilder();
    NTreeNode createTreeNode(NText text, NTreeNode[] children);
    NTextBuilder createBuilder();

    NText createBlank();

    NText createText(Object t);

    NText createText(NMsg t);

    NTextPlain createPlain(String t);

    NTextList createList(NText... nodes);

    NTextList createList(Collection<NText> nodes);

    NText createStyled(String other, NTextStyles styles);

    NText createStyled(NMsg other, NTextStyles styles);

    NText createStyled(NText other, NTextStyles styles);

    NText createStyled(String plainText, NTextStyle style);

    NText createStyled(NMsg other, NTextStyle style);

    NText createStyled(NText other, NTextStyle style);

    NTextTitle createTitle(String other, int level);

    NTextTitle createTitle(NText other, int level);

    NTextCmd createCommand(NTerminalCmd command);

    NTextCode createCode(String text, String lang, String sep);

    NTextCode createCode(String lang, String text);

    NText createCodeOrCommand(String lang, String text);

    NText createCodeOrCommand(String text);

    NText createCodeOrCommand(String lang, String text, String sep);

    NTitleSequence createNumbering();

    NTitleSequence createNumbering(String pattern);

    NTextAnchor createAnchor(String anchorName);

    NTextLink createLink(String value, String sep);

    NTextAnchor createAnchor(String anchorName, String sep);

    NTextLink createLink(String value);

    NTextInclude createInclude(String value);

    NTextInclude createInclude(String value, String sep);

    NOptional<NTextTheme> getTheme(String name);

    @NGetter
    NTextTheme currentTheme();

    @NSetter
    NTextRPI setTheme(NTextTheme theme);

    @NSetter
    NTextRPI setTheme(String themeName);

    NCodeHighlighter codeHighlighter(String kind);

    NTextRPI registerCodeHighlighter(NCodeHighlighter format);

    NTextRPI unregisterCodeHighlighter(String id);

    List<NCodeHighlighter> codeHighlighters();

    NText createText(String t);

    NTextParser createParser();

    void traverseDFS(NText text, NTextVisitor visitor);

    void traverseBFS(NText text, NTextVisitor visitor);

    NText transform(NText text, NTextTransformConfig config);

    NText transform(NText text, NTextTransformer transformer, NTextTransformConfig config);

    NNormalizedText normalize(NText text);

    NNormalizedText normalize(NText text, NTextTransformConfig config);

    NNormalizedText normalize(NText text, NTextTransformer transformer, NTextTransformConfig config);

    String escapeText(String str);

    String filterText(String text);


    NOptional<NObjectWriter> createWriter(Object format);

//    <T> NObjectWriter createFormat(T object, NTextFormat<T> format);

    <T> NOptional<NTextFormat<T>> createTextFormat(String type, String pattern, Class<T> expectedType);

    <T> NOptional<NStringFormat<T>> createStringFormat(String type, String pattern, Class<T> expectedType);

    NOptional<NStringFormat<Number>> createNumberStringFormat(String type, String pattern);

    static NText[] stripLeft(NText[] value) {
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
            return Arrays.copyOfRange(value, st, len);
        }
        return value;
    }

    static NText[] stripRight(NText[] value) {
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
            return Arrays.copyOfRange(value, 0, st);
        }
        return value;
    }

    static NText[] strip(NText[] value) {
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

    static NPrimitiveText[] strip(NPrimitiveText[] value) {
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

    static NPrimitiveText[] stripLeft(NPrimitiveText[] value) {
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
            return Arrays.copyOfRange(value, st, len);
        }
        return value;
    }

    static NPrimitiveText[] stripRight(NPrimitiveText[] value) {
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
            return Arrays.copyOfRange(value, 0, st);
        }
        return value;
    }
}
