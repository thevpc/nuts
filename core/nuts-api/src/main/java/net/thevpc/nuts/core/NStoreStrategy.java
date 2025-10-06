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
package net.thevpc.nuts.core;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * @app.category Base
 * @since 0.5.4
 */
public enum NStoreStrategy implements NEnum {
    /**
     * Default location strategy. locations will be exploded to distinct roots
     */
    EXPLODED,
    /**
     * locations will be located at the very same root folder
     */
    STANDALONE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NStoreStrategy() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NStoreStrategy> parse(String value) {
        return NEnumUtils.parseEnum(value, NStoreStrategy.class, s->{
            switch (s.getNormalizedValue()) {
                case "S":
                    return NOptional.of(NStoreStrategy.STANDALONE);
                case "E":
                    return NOptional.of(NStoreStrategy.EXPLODED);
            }
            return null;
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
