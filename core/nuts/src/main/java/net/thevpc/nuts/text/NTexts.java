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

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.spi.NFormatSPI;

import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NTexts extends NComponent, NSessionProvider{
    static NTexts of(NSession session) {
       return NExtensions.of(session).createSupported(NTexts.class);
    }

    NTexts setSession(NSession session);

    NTextBuilder ofBuilder();

    NText ofBlank();

    NText ofText(Object t);

    NTextPlain ofPlain(String t);

    NTextList ofList(NText... nodes);

    NTextList ofList(Collection<NText> nodes);

    NText ofStyled(String other, NTextStyles styles);

    NText ofStyled(NMsg other, NTextStyles styles);

    NText ofStyled(NString other, NTextStyles styles);

    NText ofStyled(NText other, NTextStyles styles);


    NText ofStyled(String other, NTextStyle style);

    NText ofStyled(NMsg other, NTextStyle style);

    NText ofStyled(NString other, NTextStyle style);

    NText ofStyled(NText other, NTextStyle style);

    NTextTitle ofTitle(String other, int level);

    NTextTitle ofTitle(NString other, int level);

    NTextTitle ofTitle(NText other, int level);

    NTextCommand ofCommand(NTerminalCommand command);

    NTextCode ofCode(String lang, String text, char sep);

    NTextCode ofCode(String lang, String text);

    NText ofCodeOrCommand(String lang, String text);

    NText ofCodeOrCommand(String text);

    NText ofCodeOrCommand(String lang, String text, char sep);

    NTitleSequence ofNumbering();

    NTitleSequence ofNumbering(String pattern);

    NTextAnchor ofAnchor(String anchorName);

    NTextLink ofLink(String value, char sep);

    NTextAnchor ofAnchor(String anchorName, char sep);

    NTextLink ofLink(String value);

    NTextFormatTheme getTheme();

    NTexts setTheme(NTextFormatTheme theme);

    NTexts setTheme(String themeName);

    NCodeHighlighter getCodeHighlighter(String kind);

    NTexts addCodeHighlighter(NCodeHighlighter format);

    NTexts removeCodeHighlighter(String id);

    List<NCodeHighlighter> getCodeHighlighters();

    NText parse(String t);

    NTextParser parser();

    void traverseDFS(NText text, NTextVisitor visitor);

    void traverseBFS(NText text, NTextVisitor visitor);

    NText transform(NText text, NTextTransformConfig config);

    NText transform(NText text, NTextTransformer transformer, NTextTransformConfig config);

    String escapeText(String str);

    String filterText(String text);


    NTextInclude ofInclude(String value);

    NTextInclude ofInclude(String value, char sep);

    NFormat createFormat(NFormatSPI value);
}
