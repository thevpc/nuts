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
import net.thevpc.nuts.spi.NutsComponent;

import java.util.Collection;
import java.util.List;

/**
 * @app.category Format
 */
public interface NutsTexts extends NutsComponent {
    static NutsTexts of(NutsSession session) {
        NutsApiUtils.checkSession(session);
        return session.extensions().createSupported(NutsTexts.class, true, null);
    }

    NutsSession getSession();

    NutsTexts setSession(NutsSession session);

    NutsTextBuilder builder();

    NutsText ofBlank();

    NutsText toText(Object t);

    NutsTextPlain ofPlain(String t);

    NutsTextList ofList(NutsText... nodes);

    NutsTextList ofList(Collection<NutsText> nodes);

    NutsTextStyled ofStyled(String other, NutsTextStyles styles);

    NutsTextStyled ofStyled(NutsString other, NutsTextStyles styles);

    NutsTextStyled ofStyled(NutsText other, NutsTextStyles styles);

    /**
     * apply style to the given text or return it as is if no style is to be applied.
     *
     * @param other  text to apply style to.
     * @param styles styles to apply
     * @return the given text with the applied style
     */
    NutsText applyStyles(NutsText other, NutsTextStyles styles);

    NutsText applyStyles(NutsText other, NutsTextStyle... styles);

    /**
     * apply style to the given text or return it as is if no style is to be applied.
     *
     * @param other  text to apply style to.
     * @param styles styles to apply
     * @return the given text with the applied style
     */
    NutsText applyStyles(NutsString other, NutsTextStyles styles);

    NutsText applyStyles(NutsString other, NutsTextStyle... styles);

    NutsTextStyled ofStyled(String other, NutsTextStyle styles);

    NutsTextStyled ofStyled(NutsString other, NutsTextStyle styles);

    NutsTextStyled ofStyled(NutsText other, NutsTextStyle styles);

    NutsTextCommand ofCommand(NutsTerminalCommand command);

    NutsTextCode ofCode(String lang, String text);

    NutsTextNumbering ofNumbering();

    NutsTextNumbering ofNumbering(String pattern);

    NutsTextAnchor ofAnchor(String anchorName);

    NutsTextLink ofLink(NutsText value);

    NutsTextFormatTheme getTheme();

    NutsTexts setTheme(NutsTextFormatTheme theme);

    NutsTexts setTheme(String themeName);

    NutsCodeHighlighter getCodeHighlighter(String kind);

    NutsTexts addCodeHighlighter(NutsCodeHighlighter format);

    NutsTexts removeCodeHighlighter(String id);

    List<NutsCodeHighlighter> getCodeHighlighters();

    NutsText parse(String t);

    NutsTextParser parser();

}
