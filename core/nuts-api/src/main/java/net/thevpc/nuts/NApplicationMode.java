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
package net.thevpc.nuts;

import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NEnumUtils;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;

/**
 * Modes Application can run with
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
public enum NApplicationMode implements NEnum {
    /**
     * Default application execution Mode. This mode is considered if the
     * --nuts-exec-mode=... is not present (or is not the very first argument).
     */
    RUN,
    /**
     * application execution Mode in auto-complete mode in which case
     * application MUST accept FIRST argument in the form of
     * "--nuts-exec-mode=auto-complete &lt;WORD-INDEX&gt;" where &lt;WORD-INDEX&gt; is
     * an optional argument to auto-complete mode. It's important to notice
     * that "--nuts-exec-mode=auto-complete &lt;WORD-INDEX&gt;" is a SINGLE
     * argument, so spaces must be escaped.
     */
    AUTO_COMPLETE,
    /**
     * application execution Mode in install mode in which case application MUST
     * accept FIRST argument in the form of "--nuts-exec-mode=install &lt;ARG&gt;
     * ..." where &lt;ARG&gt; arg an optional arguments to install mode. It's
     * important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is a
     * SINGLE argument, so spaces must be escaped.
     */
    INSTALL,
    /**
     * application execution Mode in uninstall mode in which case application
     * MUST accept FIRST argument in the form of "--nuts-exec-mode=uninstall
     * &lt;ARG&gt; ..." where &lt;ARG&gt; arg an optional arguments to uninstall mode.
     * It's important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is
     * a SINGLE argument, so spaces must be escaped.
     */
    UNINSTALL,
    /**
     * application execution Mode in update mode in which case application MUST
     * accept FIRST argument in the form of "--nuts-exec-mode=update &lt;ARG&gt;
     * ..." where &lt;ARG&gt; arg an optional arguments to update mode. It's
     * important to notice that "--nuts-exec-mode=install &lt;ARG&gt; ..." is a
     * SINGLE argument, so spaces must be escaped.
     */
    UPDATE;

    /**
     * lower-cased identifier for the enum entry
     */
    private final String id;

    /**
     * default constructor
     */
    NApplicationMode() {
        this.id = NNameFormat.ID_NAME.format(name());
    }

    public static NOptional<NApplicationMode> parse(String value) {
        return NEnumUtils.parseEnum(value, NApplicationMode.class);
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
