/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 *
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.format.text;

import java.util.Objects;
import java.util.function.Supplier;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTerminalCommand;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.renderer.AnsiStyleStyleApplierResolver;
import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.AnsiStyle;
import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.AnsiStyleStyleApplier;
import net.thevpc.nuts.runtime.core.util.CachedValue;
import net.thevpc.nuts.runtime.core.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

/**
 *
 * @author thevpc
 */
public class DefaultAnsiEscapeCommand extends AnsiEscapeCommand implements AnsiStyleStyleApplier {

    private final NutsTerminalCommand command;

    public DefaultAnsiEscapeCommand(NutsTerminalCommand command) {
        this.command = command;
    }

    public String getName() {
        return command.getName();
    }

    @Override
    public String toString() {
        return getName() + "(" + command.getArgs() + ')';
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 89 * hash + Objects.hashCode(this.command);
        return hash;
    }

    @Override
    public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsSession session, AnsiStyleStyleApplierResolver applierResolver) {
        switch (command.getName()) {
            case NutsTerminalCommand.Ids.MOVE_LINE_START: {
                return old.addCommand("\r");
            }
            case NutsTerminalCommand.Ids.MOVE_TO: {
                String a = command.getArgs();
                if (a != null) {
                    String[] split = a.split("[;v, x]");
                    if (split.length >= 2) {
                        Integer count1 = CoreNumberUtils.convertToInteger(split[0], null);
                        Integer count2 = CoreNumberUtils.convertToInteger(split[1], null);
                        if (count1 != null && count2 != null) {
                            return old.addCommand("\u001b[" + count1 + ";" + count2 + "H");
                        }
                    }
                }
                return old;
            }

            case NutsTerminalCommand.Ids.MOVE_UP: {
                Integer count1 = CoreNumberUtils.convertToInteger(command.getArgs(), null);
                if (count1 != null) {
                    return old.addCommand("\u001b[" + count1 + "A");
                }
                return old;
            }
            case NutsTerminalCommand.Ids.MOVE_DOWN: {
                Integer count1 = CoreNumberUtils.convertToInteger(command.getArgs(), null);
                if (count1 != null) {
                    return old.addCommand("\u001b[" + count1 + "B");
                }
                return old;
            }
            case NutsTerminalCommand.Ids.MOVE_RIGHT: {
                Integer count1 = CoreNumberUtils.convertToInteger(command.getArgs(), null);
                if (count1 != null) {
                    return old.addCommand("\u001b[" + count1 + "C");
                }
                return old;
            }
            case NutsTerminalCommand.Ids.MOVE_LEFT: {
                Integer count1 = CoreNumberUtils.convertToInteger(command.getArgs(), null);
                if (count1 != null) {
                    return old.addCommand("\u001b[" + count1 + "D");
                }
                return old;
            }
            case NutsTerminalCommand.Ids.CLEAR_SCREEN: {
                return old.addCommand("\u001b[" + 2 + "J");
            }
            case NutsTerminalCommand.Ids.CLEAR_SCREEN_FROM_CURSOR: {
                return old.addCommand("\u001b[" + 0 + "J");
            }
            case NutsTerminalCommand.Ids.CLEAR_SCREEN_TO_CURSOR: {
                return old.addCommand("\u001b[" + 1 + "J");
            }
            case NutsTerminalCommand.Ids.CLEAR_LINE: {
                return old.addCommand("\u001b[" + 2 + "K");
            }
            case NutsTerminalCommand.Ids.CLEAR_LINE_FROM_CURSOR: {
                return old.addCommand("\u001b[" + 0 + "K");
            }
            case NutsTerminalCommand.Ids.CLEAR_LINE_TO_CURSOR: {
                return old.addCommand("\u001b[" + 1 + "K");
            }
            case NutsTerminalCommand.Ids.LATER_RESET_LINE: {
                NutsWorkspace ws=session.getWorkspace();
                int tputCallTimeout = ws.env().getOptionAsInt("nuts.term.tput.call.timeout", 60);
                Integer w = ws.env().getOptionAsInt("nuts.term.width", null);
                if (w == null) {
                    CachedValue<Integer> tput_cols = (CachedValue) ws.env().getProperty("nuts.term.tput.call.instance");
                    if (tput_cols == null) {
                        tput_cols = new CachedValue<>(new TputEvaluator(session), tputCallTimeout);
                        ws.env().setProperty("nuts.term.tput.call.instance", tput_cols);
                    }
                    if (out.baseOutput() == System.out) {
                        w = tput_cols.getValue();
                    }
                }
                if (w == null) {
                    w = 120;
                }
                if (w > 0) {
//                return old.addLaterCommand("\u001b[1000D"
//                        + CoreStringUtils.fillString(' ', w)
//                        + "\u001b[1000D"
//                );
                    return old.addLaterCommand("\r"
                            + CoreStringUtils.fillString(' ', w)
                            + "\r"
                    );
                }
                return old;
            }
        }
        return old;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DefaultAnsiEscapeCommand other = (DefaultAnsiEscapeCommand) obj;
        if (!Objects.equals(this.command, other.command)) {
            return false;
        }
        return true;
    }

    static class TputEvaluator implements Supplier<Integer> {

        private final NutsSession session;

        public TputEvaluator(NutsSession session) {
            this.session = session;
        }
        boolean wasError = false;

        @Override
        public Integer get() {
            NutsWorkspace ws = session.getWorkspace();
            switch (ws.env().getOsFamily()) {
                case LINUX:
                case UNIX:
                case MACOS: {
                    try {
                        String d = ws.exec().userCmd().grabOutputString()
                                .setSession(session)
                                .addCommand("tput", "cols")
                                .getOutputString();
                        String s = d.trim();
                        if (d.isEmpty()) {
                            return null;
                        };
                        return Integer.parseInt(d.trim());
                    } catch (Exception ex) {
                        wasError = true;
                        return null;
                    }
                }
            }
            return null;
        }
    }

}
