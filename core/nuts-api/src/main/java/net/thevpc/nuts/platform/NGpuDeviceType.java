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
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.platform;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Kind of GPU device, which also decides how its memory is to be read.
 * <p>
 * A dedicated GPU owns its memory, so a total and a free amount are meaningful
 * absolute numbers. An integrated GPU carves its memory out of system RAM, so
 * the very same numbers are either unavailable or a moving share of the host
 * memory, and must not be compared against a dedicated device's figures.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.8.9
 */
public enum NGpuDeviceType implements NEnum {

    /**
     * Discrete GPU owning dedicated video memory.
     */
    DEDICATED_GPU,

    /**
     * GPU integrated with the CPU package, addressing system memory. Covers
     * Intel and AMD integrated graphics as well as Apple Silicon unified memory.
     */
    INTEGRATED_GPU,

    /**
     * The kind of device could not be determined.
     */
    UNKNOWN;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    NGpuDeviceType() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NGpuDeviceType> parse(String value) {
        return NEnumUtils.parseEnum(value, NGpuDeviceType.class, s -> {
            switch (s.normalizedValue()) {
                case "DEDICATED_GPU":
                case "DEDICATED":
                case "DISCRETE":
                case "DGPU":
                    return NOptional.of(DEDICATED_GPU);

                case "INTEGRATED_GPU":
                case "INTEGRATED":
                case "IGPU":
                case "UNIFIED":
                    return NOptional.of(INTEGRATED_GPU);

                case "UNKNOWN":
                    return NOptional.of(UNKNOWN);
            }
            return null;
        });
    }

    /**
     * lower cased identifier.
     *
     * @return lower cased identifier
     */
    @Override
    public String id() {
        return id;
    }
}
