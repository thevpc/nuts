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
package net.thevpc.nuts.runtime.standalone.text.parser;


import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.DefaultNutsTexts;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextCode;
import net.thevpc.nuts.text.NutsTextType;
import net.thevpc.nuts.text.NutsTexts;

import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextCode extends NutsTextSpecialBase implements NutsTextCode {

    private final String text;

    public DefaultNutsTextCode(NutsSession session, String start, String kind, String separator, String end, String text) {
        super(session, start, kind,
                (kind != null && kind.length() > 0
                        &&
                        text != null && text.length() > 0
                        && (separator == null || separator.isEmpty())) ? " " : separator
                , end);
        this.text = text;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public NutsText highlight(NutsSession session) {
        NutsTexts txt = NutsTexts.of(session);
        NutsCodeHighlighter t = ((DefaultNutsTexts) txt)
                .setSession(session)
                .resolveCodeHighlighter(getKind());
        return t.stringToText(text, txt, session);
    }

    @Override
    public NutsTextType getType() {
        return NutsTextType.CODE;
    }

    public String getText() {
        return text;
    }

    @Override
    public String getQualifier() {
        return getKind();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNutsTextCode that = (DefaultNutsTextCode) o;
        return
                Objects.equals(text, that.text)
                && Objects.equals(getQualifier(), that.getQualifier())
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), text);
    }

    @Override
    public String filteredText() {
        return text == null ? "" : text;
    }

    @Override
    public int textLength() {
        return text == null ? 0 : text.length();
    }
}
