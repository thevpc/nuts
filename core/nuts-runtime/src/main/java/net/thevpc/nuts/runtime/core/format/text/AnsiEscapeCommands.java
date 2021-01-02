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
package net.thevpc.nuts.runtime.core.format.text;

import java.awt.*;

/**
 *
 * @author thevpc
 */
public class AnsiEscapeCommands {

    public static final AnsiEscapeCommand MOVE_UP = new TextCursor("MOVE_UP");
    public static final AnsiEscapeCommand MOVE_LINE_START = new TextCursor("MOVE_LINE_START");
    public static final AnsiEscapeCommand LATER_RESET_LINE = new TextCursor("LATER_RESET_LINE");

    public static final AnsiEscapeCommand list(AnsiEscapeCommand... all) {
        if (all == null) {
            return null;
        }
        if (all.length == 1) {
            return all[0];
        }
        return new AnsiEscapeCommandList(all);
    }
}
