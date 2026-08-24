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
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NTextRPI of() {
        return NExtensions.of(NTextRPI.class);
    }

    /**
     * Creates a new instance of create cell spec builder.
     *
     * @return create cell spec builder result
     */
    NTableCellSpecBuilder createCellSpecBuilder();
    /**
     * Creates a new instance of create tree node.
     *
     * @param text text
     * @param children children
     * @return create tree node result
     */
    NTreeNode createTreeNode(NText text, NTreeNode[] children);
    /**
     * Creates a new instance of create builder.
     *
     * @return create builder result
     */
    NTextBuilder createBuilder();

    /**
     * Creates a new instance of create blank.
     *
     * @return create blank result
     */
    NText createBlank();

    /**
     * Creates a new instance of create text.
     *
     * @param t t
     * @return create text result
     */
    NText createText(Object t);

    /**
     * Creates a new instance of create text.
     *
     * @param t t
     * @return create text result
     */
    NText createText(NMsg t);

    /**
     * Creates a new instance of create plain.
     *
     * @param t t
     * @return create plain result
     */
    NTextPlain createPlain(String t);

    /**
     * Creates a new instance of create list.
     *
     * @param nodes nodes
     * @return create list result
     */
    NTextList createList(NText... nodes);

    /**
     * Creates a new instance of create list.
     *
     * @param nodes nodes
     * @return create list result
     */
    NTextList createList(Collection<NText> nodes);

    /**
     * Creates a new instance of create styled.
     *
     * @param other other
     * @param styles styles
     * @return create styled result
     */
    NText createStyled(String other, NTextStyles styles);

    /**
     * Creates a new instance of create styled.
     *
     * @param other other
     * @param styles styles
     * @return create styled result
     */
    NText createStyled(NMsg other, NTextStyles styles);

    /**
     * Creates a new instance of create styled.
     *
     * @param other other
     * @param styles styles
     * @return create styled result
     */
    NText createStyled(NText other, NTextStyles styles);

    /**
     * Creates a new instance of create styled.
     *
     * @param plainText plain text
     * @param style style
     * @return create styled result
     */
    NText createStyled(String plainText, NTextStyle style);

    /**
     * Creates a new instance of create styled.
     *
     * @param other other
     * @param style style
     * @return create styled result
     */
    NText createStyled(NMsg other, NTextStyle style);

    /**
     * Creates a new instance of create styled.
     *
     * @param other other
     * @param style style
     * @return create styled result
     */
    NText createStyled(NText other, NTextStyle style);

    /**
     * Creates a new instance of create title.
     *
     * @param other other
     * @param level level
     * @return create title result
     */
    NTextTitle createTitle(String other, int level);

    /**
     * Creates a new instance of create title.
     *
     * @param other other
     * @param level level
     * @return create title result
     */
    NTextTitle createTitle(NText other, int level);

    /**
     * Creates a new instance of create command.
     *
     * @param command command
     * @return create command result
     */
    NTextCmd createCommand(NTerminalCmd command);

    /**
     * Creates a new instance of create code.
     *
     * @param text text
     * @param lang lang
     * @param sep sep
     * @return create code result
     */
    NTextCode createCode(String text, String lang, String sep);

    /**
     * Creates a new instance of create code.
     *
     * @param lang lang
     * @param text text
     * @return create code result
     */
    NTextCode createCode(String lang, String text);

    /**
     * Creates a new instance of create code or command.
     *
     * @param lang lang
     * @param text text
     * @return create code or command result
     */
    NText createCodeOrCommand(String lang, String text);

    /**
     * Creates a new instance of create code or command.
     *
     * @param text text
     * @return create code or command result
     */
    NText createCodeOrCommand(String text);

    /**
     * Creates a new instance of create code or command.
     *
     * @param lang lang
     * @param text text
     * @param sep sep
     * @return create code or command result
     */
    NText createCodeOrCommand(String lang, String text, String sep);

    /**
     * Creates a new instance of create numbering.
     *
     * @return create numbering result
     */
    NTitleSequence createNumbering();

    /**
     * Creates a new instance of create numbering.
     *
     * @param pattern pattern
     * @return create numbering result
     */
    NTitleSequence createNumbering(String pattern);

    /**
     * Creates a new instance of create anchor.
     *
     * @param anchorName anchor name
     * @return create anchor result
     */
    NTextAnchor createAnchor(String anchorName);

    /**
     * Creates a new instance of create link.
     *
     * @param value value
     * @param sep sep
     * @return create link result
     */
    NTextLink createLink(String value, String sep);

    /**
     * Creates a new instance of create anchor.
     *
     * @param anchorName anchor name
     * @param sep sep
     * @return create anchor result
     */
    NTextAnchor createAnchor(String anchorName, String sep);

    /**
     * Creates a new instance of create link.
     *
     * @param value value
     * @return create link result
     */
    NTextLink createLink(String value);

    /**
     * Creates a new instance of create include.
     *
     * @param value value
     * @return create include result
     */
    NTextInclude createInclude(String value);

    /**
     * Creates a new instance of create include.
     *
     * @param value value
     * @param sep sep
     * @return create include result
     */
    NTextInclude createInclude(String value, String sep);

    /**
     * Returns the theme.
     *
     * @param name name
     * @return get theme result
     */
    NOptional<NTextTheme> getTheme(String name);

    /**
     * Current theme.
     *
     * @return current theme result
     */
    @NGetter
    NTextTheme currentTheme();

    /**
     * Sets the theme.
     *
     * @param theme theme
     * @return set theme result
     */
    @NSetter
    NTextRPI setTheme(NTextTheme theme);

    /**
     * Sets the theme.
     *
     * @param themeName theme name
     * @return set theme result
     */
    @NSetter
    NTextRPI setTheme(String themeName);

    /**
     * Code highlighter.
     *
     * @param kind kind
     * @return code highlighter result
     */
    NCodeHighlighter codeHighlighter(String kind);

    /**
     * Register code highlighter.
     *
     * @param format format
     * @return register code highlighter result
     */
    NTextRPI registerCodeHighlighter(NCodeHighlighter format);

    /**
     * Unregister code highlighter.
     *
     * @param id id
     * @return unregister code highlighter result
     */
    NTextRPI unregisterCodeHighlighter(String id);

    /**
     * Code highlighters.
     *
     * @return code highlighters result
     */
    List<NCodeHighlighter> codeHighlighters();

    /**
     * Creates a new instance of create text.
     *
     * @param t t
     * @return create text result
     */
    NText createText(String t);

    /**
     * Creates a new instance of create parser.
     *
     * @return create parser result
     */
    NTextParser createParser();

    /**
     * Traverse dfs.
     *
     * @param text text
     * @param visitor visitor
     */
    void traverseDFS(NText text, NTextVisitor visitor);

    /**
     * Traverse bfs.
     *
     * @param text text
     * @param visitor visitor
     */
    void traverseBFS(NText text, NTextVisitor visitor);

    /**
     * Transform.
     *
     * @param text text
     * @param config config
     * @return transform result
     */
    NText transform(NText text, NTextTransformConfig config);

    /**
     * Transform.
     *
     * @param text text
     * @param transformer transformer
     * @param config config
     * @return transform result
     */
    NText transform(NText text, NTextTransformer transformer, NTextTransformConfig config);

    /**
     * Normalize.
     *
     * @param text text
     * @return normalize result
     */
    NNormalizedText normalize(NText text);

    /**
     * Normalize.
     *
     * @param text text
     * @param config config
     * @return normalize result
     */
    NNormalizedText normalize(NText text, NTextTransformConfig config);

    /**
     * Normalize.
     *
     * @param text text
     * @param transformer transformer
     * @param config config
     * @return normalize result
     */
    NNormalizedText normalize(NText text, NTextTransformer transformer, NTextTransformConfig config);

    /**
     * Escape text.
     *
     * @param str str
     * @return escape text result
     */
    String escapeText(String str);

    /**
     * Filter text.
     *
     * @param text text
     * @return filter text result
     */
    String filterText(String text);


    /**
     * Creates a new instance of create writer.
     *
     * @param format format
     * @return create writer result
     */
    NOptional<NObjectWriter> createWriter(Object format);

//    <T> NObjectWriter createFormat(T object, NTextFormat<T> format);

    /**
     * Creates a new instance of create text format.
     *
     * @param type type
     * @param pattern pattern
     * @param expectedType expected type
     * @return create text format result
     */
    <T> NOptional<NTextFormat<T>> createTextFormat(String type, String pattern, Class<T> expectedType);

    /**
     * Creates a new instance of create string format.
     *
     * @param type type
     * @param pattern pattern
     * @param expectedType expected type
     * @return create string format result
     */
    <T> NOptional<NStringFormat<T>> createStringFormat(String type, String pattern, Class<T> expectedType);

    /**
     * Creates a new instance of create number string format.
     *
     * @param type type
     * @param pattern pattern
     * @return create number string format result
     */
    NOptional<NStringFormat<Number>> createNumberStringFormat(String type, String pattern);

    /**
     * Strip left.
     *
     * @param value value
     * @return strip left result
     */
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

    /**
     * Strip right.
     *
     * @param value value
     * @return strip right result
     */
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

    /**
     * Strip.
     *
     * @param value value
     * @return strip result
     */
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

    /**
     * Strip.
     *
     * @param value value
     * @return strip result
     */
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

    /**
     * Strip left.
     *
     * @param value value
     * @return strip left result
     */
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

    /**
     * Strip right.
     *
     * @param value value
     * @return strip right result
     */
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
