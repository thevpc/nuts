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

import java.text.MessageFormat;
import java.util.Formatter;

public class NutsMessage {

    private final String message;
    private final NutsTextFormatStyle style;
    private final Object[] params;

    public NutsMessage(NutsTextFormatStyle style, String message, Object... params) {
        if (message == null || params == null || style == null) {
            throw new NullPointerException();
        }
        this.style = style;
        if (style == NutsTextFormatStyle.PLAIN || style == NutsTextFormatStyle.FORMATTED) {
            if (params != null && params.length > 0) {
                throw new IllegalArgumentException("arguments are not supported for " + style);
            }
        }
        this.message = message;
        this.params = params;
    }

    public static NutsMessage formatted(String message) {
        return new NutsMessage(NutsTextFormatStyle.FORMATTED, message);
    }

    public static NutsMessage plain(String message) {
        return new NutsMessage(NutsTextFormatStyle.PLAIN, message);
    }

    public static NutsMessage cstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.CSTYLE, message, params);
    }

    public static NutsMessage jstyle(String message, Object... params) {
        return new NutsMessage(NutsTextFormatStyle.JSTYLE, message, params);
    }

    public NutsTextFormatStyle getStyle() {
        return style;
    }

    public String getMessage() {
        return message;
    }

    public Object[] getParams() {
        return params;
    }

    public NutsString toNutsString(NutsSession session) {
        return NutsTexts.of(session).toText(this);
    }

    @Override
    public String toString() {
        switch (style) {
            case CSTYLE: {
                StringBuilder sb = new StringBuilder();
                new Formatter(sb).format(message, params);
                return sb.toString();
            }
            case JSTYLE: {
                return MessageFormat.format(message, params);
            }
            case FORMATTED: {
                return message;
            }
            case PLAIN: {
                return message;
            }
        }
        return "NutsMessage{" + "message=" + message + ", style=" + style + ", params=" + params + '}';
    }

}
