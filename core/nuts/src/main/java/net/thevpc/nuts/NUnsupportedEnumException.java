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
     * @param session   workspace
     * @param enumValue enumeration instance (cannot be null)
     */
    public NUnsupportedEnumException(NSession session, Enum enumValue) {
        this(session, null, enumValue);
    }

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param session   workspace
     * @param enumValue enumeration instance (cannot be null)
     * @param message   message
     */
    public NUnsupportedEnumException(NSession session, NMsg message, Enum enumValue) {
        super(session,
                message != null ? message : NMsg.ofC(
                        "unexpected/unsupported enum %s of type %s", enumValue, enumValue.getClass().getName())
        );
        this.enumValue = enumValue;
    }

    /**
     * create new instance of NutsUnexpectedEnumException
     *
     * @param session     workspace
     * @param enumValue   enumeration instance (cannot be null)
     * @param stringValue invalid value
     * @param message     message
     */
    public NUnsupportedEnumException(NSession session, NMsg message, String stringValue, Enum enumValue) {
        super(session,
                message == null ? (
                        NMsg.ofC("unexpected/unsupported value %s of type %s", stringValue, enumValue.getClass().getName()))
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
