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
package net.thevpc.nuts.runtime.standalone.text.parser;


import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextCode extends NTextSpecialBase implements NTextCode {

    private final String text;

    public DefaultNTextCode(String start, String kind, String separator, String end, String text) {
        super(start, kind,
                (kind != null && kind.length() > 0
                        &&
                        text != null && text.length() > 0
                        && (separator == null || separator.isEmpty())) ? " " : separator
                , end);
        this.text = text;
    }

    @Override
    public boolean isEmpty() {
        return text.isEmpty();
    }

    @Override
    public NText highlight() {
        NTexts txt = NTexts.of();
        NCodeHighlighter t = ((DefaultNTexts) txt)
                .resolveCodeHighlighter(getKind());
        return t.stringToText(text, txt);
    }

    @Override
    public NText immutable() {
        return this;
    }

    @Override
    public NTextType type() {
        return NTextType.CODE;
    }

    public String getValue() {
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
        DefaultNTextCode that = (DefaultNTextCode) o;
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
    public int length() {
        return text == null ? 0 : text.length();
    }

    @Override
    public NText simplify() {
        if (text.isEmpty()) {
            return DefaultNTextPlain.EMPTY;
        }
        return this;
    }

    @Override
    public boolean isWhitespace() {
        return highlight().isWhitespace();
    }


    @Override
    public NStream<NPrimitiveText> toCharStream() {
        return highlight().toCharStream();
    }

    @Override
    public NText substring(int start, int end) {
        return new DefaultNTextCode(
                this.getStart(), getKind(), getSeparator(), getEnd(),
                getValue().substring(start, end)
        );
    }

    public List<NText> split(String chars, boolean returnSeparator) {
        return NStringUtils.split(getValue(), chars,false,false)
                .stream().map(x->new DefaultNTextCode(
                        this.getStart(), getKind(), getSeparator(), getEnd(),
                        x
                )).collect(Collectors.toList());
    }

    @Override
    public NText trimLeft() {
        String c = NStringUtils.trimLeft(text);
        if(Objects.equals(text, c)){
            return this;
        }
        return new DefaultNTextCode(getStart(),getKind(),getSeparator(),getEnd(), c);
    }

    @Override
    public NText trimRight() {
        String c = NStringUtils.trimRight(text);
        if(Objects.equals(text, c)){
            return this;
        }
        return new DefaultNTextCode(getStart(),getKind(),getSeparator(),getEnd(), c);
    }

    @Override
    public NText trim() {
        String c = NStringUtils.trim(text);
        if(Objects.equals(text, c)){
            return this;
        }
        return new DefaultNTextCode(getStart(),getKind(),getSeparator(),getEnd(), c);
    }


}
