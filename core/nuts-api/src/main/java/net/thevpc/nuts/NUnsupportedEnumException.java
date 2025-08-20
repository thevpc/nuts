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
package net.thevpc.nuts;

import net.thevpc.nuts.core.NI18n;
import net.thevpc.nuts.util.NMsg;

/**
 * Exception Thrown when for any reason, the enum value is not expected/supported.
 *
 * @author thevpc
 * @app.category Exceptions
 */
public class NUnsupportedEnumException extends NException {
    private Enum enumValue;

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param enumValue enumeration instance (cannot be null)
     */
    public NUnsupportedEnumException(Enum enumValue) {
        this(null, enumValue);
    }

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param message   message
     * @param enumValue enumeration instance (cannot be null)
     */
    public NUnsupportedEnumException(NMsg message, Enum enumValue) {
        super(
                message != null ? message : NMsg.ofC(
                        NI18n.of("unexpected/unsupported enum %s of type %s"), enumValue, enumValue.getClass().getName())
        );
        this.enumValue = enumValue;
    }

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param message     message
     * @param stringValue invalid value
     * @param enumValue   enumeration instance (cannot be null)
     */
    public NUnsupportedEnumException(NMsg message, String stringValue, Enum enumValue) {
        super(
                message == null ? (
                        NMsg.ofC(NI18n.of("unexpected/unsupported value %s of type %s"), stringValue, enumValue.getClass().getName()))
                        : message
        );
    }

    /**
     * enum value
     *
     * @return enum value
     */
    public Enum getEnumValue() {
        return enumValue;
    }


}
