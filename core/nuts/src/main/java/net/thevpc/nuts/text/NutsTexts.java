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
import net.thevpc.nuts.spi.NutsComponent;
import net.thevpc.nuts.spi.NutsFormatSPI;
import net.thevpc.nuts.util.NutsUtils;

import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NutsTexts extends NutsComponent {
    static NutsTexts of(NutsSession session) {
        NutsUtils.requireSession(session);
        return session.extensions().createSupported(NutsTexts.class, true, null);
    }

    NutsSession getSession();

    NutsTexts setSession(NutsSession session);

    NutsTextBuilder ofBuilder();

    NutsText ofBlank();

    NutsText ofText(Object t);

    NutsTextPlain ofPlain(String t);

    NutsTextList ofList(NutsText... nodes);

    NutsTextList ofList(Collection<NutsText> nodes);

    NutsText ofStyled(String other, NutsTextStyles styles);

    NutsText ofStyled(NutsMessage other, NutsTextStyles styles);

    NutsText ofStyled(NutsString other, NutsTextStyles styles);

    NutsText ofStyled(NutsText other, NutsTextStyles styles);


    NutsText ofStyled(String other, NutsTextStyle style);

    NutsText ofStyled(NutsMessage other, NutsTextStyle style);

    NutsText ofStyled(NutsString other, NutsTextStyle style);

    NutsText ofStyled(NutsText other, NutsTextStyle style);

    NutsTextTitle ofTitle(String other, int level);

    NutsTextTitle ofTitle(NutsString other, int level);

    NutsTextTitle ofTitle(NutsText other, int level);

    NutsTextCommand ofCommand(NutsTerminalCommand command);

    NutsTextCode ofCode(String lang, String text, char sep);

    NutsTextCode ofCode(String lang, String text);

    NutsText ofCodeOrCommand(String lang, String text);

    NutsText ofCodeOrCommand(String text);

    NutsText ofCodeOrCommand(String lang, String text, char sep);

    NutsTitleSequence ofNumbering();

    NutsTitleSequence ofNumbering(String pattern);

    NutsTextAnchor ofAnchor(String anchorName);

    NutsTextLink ofLink(String value, char sep);

    NutsTextAnchor ofAnchor(String anchorName, char sep);

    NutsTextLink ofLink(String value);

    NutsTextFormatTheme getTheme();

    NutsTexts setTheme(NutsTextFormatTheme theme);

    NutsTexts setTheme(String themeName);

    NutsCodeHighlighter getCodeHighlighter(String kind);

    NutsTexts addCodeHighlighter(NutsCodeHighlighter format);

    NutsTexts removeCodeHighlighter(String id);

    List<NutsCodeHighlighter> getCodeHighlighters();

    NutsText parse(String t);

    NutsTextParser parser();

    void traverseDFS(NutsText text, NutsTextVisitor visitor);

    void traverseBFS(NutsText text, NutsTextVisitor visitor);

    NutsText transform(NutsText text, NutsTextTransformConfig config);

    NutsText transform(NutsText text, NutsTextTransformer transformer, NutsTextTransformConfig config);

    String escapeText(String str);

    String filterText(String text);


    NutsTextInclude ofInclude(String value);

    NutsTextInclude ofInclude(String value, char sep);

    NutsFormat createFormat(NutsFormatSPI value);
}
