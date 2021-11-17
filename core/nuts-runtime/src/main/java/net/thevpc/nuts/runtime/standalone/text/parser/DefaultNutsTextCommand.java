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
package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTerminalCommand;
import net.thevpc.nuts.NutsTextCommand;
import net.thevpc.nuts.NutsTextType;
import net.thevpc.nuts.runtime.standalone.text.AnsiEscapeCommand;
import net.thevpc.nuts.runtime.standalone.text.DefaultAnsiEscapeCommand;

import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNutsTextCommand extends NutsTextSpecialBase implements NutsTextCommand {

    private final NutsTerminalCommand command;

    public DefaultNutsTextCommand(NutsSession session, String start, NutsTerminalCommand command, String separator, String end) {
        super(session, start, command.getName(),
                (command.getArgs() != null && command.getArgs().length() > 0 && (separator == null || separator.isEmpty())) ? " " : separator
                , end);
        this.command = command;
    }
    @Override
    public boolean isEmpty() {
        return command==null;
    }

    public static AnsiEscapeCommand parseAnsiEscapeCommand(NutsTerminalCommand v, NutsSession ws) {
        //this might be a command !!
        if(v==null){
            return null;
        }
        return new DefaultAnsiEscapeCommand(v);
    }

    @Override
    public NutsTextType getType() {
        return NutsTextType.COMMAND;
    }

    @Override
    public NutsTerminalCommand getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNutsTextCommand that = (DefaultNutsTextCommand) o;
        return Objects.equals(command, that.command);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), command);
    }

    @Override
    public String filteredText() {
        return "";
    }
}
