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
package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTerminalCmd;
import net.thevpc.nuts.text.NTextCmd;
import net.thevpc.nuts.text.NTextType;

import java.util.Objects;

/**
 * Created by vpc on 5/23/17.
 */
public class DefaultNTextCommand extends NTextSpecialBase implements NTextCmd {

    private final NTerminalCmd command;

    public DefaultNTextCommand(NSession session, String start, NTerminalCmd command, String separator, String end) {
        super(session, start, command.getName(),
                (command.getArgs() != null && command.getArgs().size() > 0 && (separator == null || separator.isEmpty())) ? " " : separator
                , end);
        this.command = command;
    }
    @Override
    public boolean isEmpty() {
        return command==null;
    }

    @Override
    public NTextType getType() {
        return NTextType.COMMAND;
    }

    @Override
    public NTerminalCmd getCommand() {
        return command;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        DefaultNTextCommand that = (DefaultNTextCommand) o;
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

    @Override
    public int textLength() {
        return 0;
    }
}
