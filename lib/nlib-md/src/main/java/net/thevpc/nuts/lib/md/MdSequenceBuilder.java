/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 *
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
package net.thevpc.nuts.lib.md;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class MdSequenceBuilder implements MdElementBuilder {

    private String code;
    private List<MdElementBuilder> elements=new ArrayList<>();
    private boolean inline;

    public MdSequenceBuilder() {
    }

    @Override
    public MdSequence build() {
        return new MdSequence(
                code,
                elements.stream().map(x->x.build()).toArray(MdElement[]::new),
                inline
        );
    }

    public String getCode() {
        return code;
    }

    public MdSequenceBuilder setCode(String code) {
        this.code = code;
        return this;
    }
    public MdSequenceBuilder add(MdElementBuilder... cells) {
        this.elements.addAll(Arrays.asList(cells));
        return this;
    }

    public MdSequenceBuilder add(MdElement... cells) {
        this.elements.addAll(Arrays.asList(cells).stream().map(x->
                MdFactory.element(x)
        ).collect(Collectors.toList()));
        return this;
    }

    public List<MdElementBuilder> getElements() {
        return elements;
    }

    public MdSequenceBuilder setElements(List<MdElementBuilder> elements) {
        this.elements = elements;
        return this;
    }

    public MdSequenceBuilder inline() {
        return setInline(true);
    }

    public boolean isInline() {
        return inline;
    }

    public MdSequenceBuilder setInline(boolean inline) {
        this.inline = inline;
        return this;
    }
}
