/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.elem;

import net.thevpc.nuts.util.*;

import java.util.function.Function;

/**
 * Element type. this an extension of json element types.
 *
 * @author thevpc
 * @app.category Elements
 */
public enum NElementTypeGroup implements NEnum {
    /**
     * null element
     */
    NULL,
    BOOLEAN,
    NUMBER,
    CONTAINER,
    STREAM,
    STRING,
    NAME,
    REGEX,
    TEMPORAL,
    OPERATOR,
    CUSTOM,
    OTHER,
    ;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NElementTypeGroup() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NElementTypeGroup> parse(String value) {
        return NEnumUtils.parseEnum(value, NElementTypeGroup.class, new Function<NEnumUtils.EnumValue, NOptional<NElementTypeGroup>>() {
            @Override
            public NOptional<NElementTypeGroup> apply(NEnumUtils.EnumValue enumValue) {
                return null;
            }
        });
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    public String id() {
        return id;
    }


}
