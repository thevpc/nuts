/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
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
package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyle;
import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyleStyleApplier;

import java.util.Objects;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.text.renderer.AnsiStyleStyleApplierResolver;

/**
 *
 * @author thevpc
 */
public class AnsiEscapeCommandFromNodeStyle extends AnsiEscapeCommand implements AnsiStyleStyleApplier {

    public static AnsiEscapeCommandFromNodeStyle of(NutsTextStyle s){
        if(s.getType().basic()) {
            return new AnsiEscapeCommandFromNodeStyle(s);
        }else {
            throw new IllegalArgumentException("unsupported");
        }
    }

    private NutsTextStyle style;

    public AnsiEscapeCommandFromNodeStyle(NutsTextStyle style) {
        this.style = style;
    }

    @Override
    public String toString() {
        return style.toString();
    }

    public NutsTextStyle getStyle() {
        return style;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + this.style.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final AnsiEscapeCommandFromNodeStyle other = (AnsiEscapeCommandFromNodeStyle) obj;
        if (!Objects.equals(this.style, other.style)) {
            return false;
        }
        return true;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsSession session, AnsiStyleStyleApplierResolver applierResolver) {
        switch (style.getType()){
            case PRIMARY:{
                int variant = style.getVariant();
                if(variant<=1){
                    variant=1;
                }else if(variant>255){
                    variant=255;
                }
                return old.setForeground(""+variant).setIntensity(0);
            }
            case SECONDARY:{
                int variant = style.getVariant();
                if(variant<=1){
                    variant=1;
                }else if(variant>255){
                    variant=255;
                }
                return old.setBackground(""+variant);
            }
            case ITALIC:{
                return old.setItalic(true);
            }
            case BOLD:{
                return old.setBold(true);
            }
            case BLINK:{
                return old.setBlink(true);
            }
            case STRIKED:{
                return old.setStriked(true);
            }
            case REVERSED:{
                return old.setReversed(true);
            }
            case UNDERLINED:{
                return old.setUnderlined(true);
            }
            case FORE_COLOR:{
                return old.setForeground8(style.getVariant());
            }
            case BACK_COLOR:{
                return old.setBackground8(style.getVariant());
            }
            case FORE_TRUE_COLOR:{
                return old.setForeground24(style.getVariant());
            }
            case BACK_TRUE_COLOR:{
                return old.setBackground24(style.getVariant());
            }
        }
        return old;
    }
}
