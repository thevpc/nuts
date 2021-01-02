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
package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.NutsTerminalManager;
import net.thevpc.nuts.NutsTextNodeCommand;
import net.thevpc.nuts.NutsTextNodeType;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.FPrintCommands;
import net.thevpc.nuts.runtime.core.format.text.AnsiEscapeCommand;
import net.thevpc.nuts.runtime.core.format.text.AnsiEscapeCommands;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextNodeCommand extends NutsTextNodeSpecialBase implements NutsTextNodeCommand {

    private final String text;

    public DefaultNutsTextNodeCommand(String start, String command, String separator, String end, String text) {
        super(start, command,
                (text!=null && text.length()>0 && (separator==null || separator.isEmpty())) ?" ":separator
                , end);
        this.text = text;
    }
    @Override
    public NutsTextNodeType getType() {
        return NutsTextNodeType.COMMAND;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public String toString() {
        return (getStart() + getKind() + getSeparator() + getText() + getEnd());
    }

    public static AnsiEscapeCommand parseAnsiEscapeCommand(String v, NutsWorkspace ws){
        //this might be a command !!
        v = v.trim();
        switch (v) {
            case NutsTerminalManager.CMD_MOVE_LINE_START: {
                return AnsiEscapeCommands.MOVE_LINE_START;
            }
            case NutsTerminalManager.CMD_LATER_RESET_LINE: {
                return AnsiEscapeCommands.LATER_RESET_LINE;
            }
            case NutsTerminalManager.CMD_MOVE_UP: {
                return AnsiEscapeCommands.MOVE_UP;
            }
            default: {
                return null;
            }
        }
    }
}
