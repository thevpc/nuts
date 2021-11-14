/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellHistory;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class HistoryCommand extends SimpleJShellBuiltin {

    public HistoryCommand() {
        super("history", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        NutsArgument a;
        if (commandLine.next("-c", "--clear") != null) {
            options.action = Action.CLEAR;
            commandLine.setCommandName(getName()).unexpectedArgument();
            return true;
        } else if ((a = commandLine.nextString("-d", "--delete")) != null) {
            options.action = Action.DELETE;
            options.ival = a.getValue().getInt();
            commandLine.setCommandName(getName()).unexpectedArgument();
            return true;
        } else if ((a = commandLine.next("-D", "--remove-duplicates")) != null) {
            options.action = Action.REMOVE_DUPLICATES;
            commandLine.setCommandName(getName()).unexpectedArgument();
            return true;
        } else if ((a = commandLine.next("-w", "--write")) != null) {
            options.action = Action.WRITE;
            if (a.isKeyValue()) {
                options.sval = a.getValue().getString();
            } else if (!commandLine.isEmpty()) {
                options.sval = commandLine.next().getString();
            }
            commandLine.setCommandName(getName()).unexpectedArgument();
            return true;
        } else if ((a = commandLine.next("-r", "--read")) != null) {
            options.action = Action.READ;
            if (a.isKeyValue()) {
                options.sval = a.getValue().getString();
            } else if (!commandLine.isEmpty()) {
                options.sval = commandLine.next().getString();
            }
            commandLine.setCommandName(getName()).unexpectedArgument();
            return true;
        } else {
            if (commandLine.peek().getAll().getInt(0) != 0) {
                options.action = Action.PRINT;
                options.ival = Math.abs(commandLine.next().getAll().getInt());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options options = context.getOptions();
        JShellHistory shistory = context.getShell().getHistory();
        NutsSession session = context.getSession();
        switch (options.action) {
            case PRINT: {
                List<String> history = shistory.getElements(options.ival <= 0 ? 1000 : options.ival);
                int offset = shistory.size() - history.size();
                LinkedHashMap<String, String> result = new LinkedHashMap<>();
                for (int i = 0; i < history.size(); i++) {
                    String historyElement = history.get(i);
                    result.put(String.valueOf(offset + i + 1), historyElement);
                }
                session.out().printlnf(result);
                break;
            }
            case CLEAR: {
                shistory.clear();
                return;
            }
            case REMOVE_DUPLICATES: {
                shistory.removeDuplicates();
                return;
            }
            case DELETE: {
                shistory.remove(options.ival - 1);
                return;
            }
            case WRITE: {
                try {
                    if (options.sval == null) {

                        shistory.save();
                    } else {
                        shistory.save(NutsPath.of(options.sval, session).toAbsolute(session.locations().getWorkspaceLocation()));
                    }
                } catch (IOException ex) {
                    throw new NutsExecutionException(session, NutsMessage.cstyle("%s", ex), ex, 100);
                }
                return;
            }
            case READ: {
                try {
                    if (options.sval == null) {
                        shistory.clear();
                        shistory.load();
                    } else {
                        shistory.load(NutsPath.of(options.sval, session).toAbsolute(session.locations().getWorkspaceLocation()));
                    }
                } catch (IOException ex) {
                    throw new NutsExecutionException(session, NutsMessage.cstyle("%s", ex), ex, 100);
                }
                return;
            }
            default: {
                throw new NutsUnsupportedArgumentException(session, NutsMessage.cstyle("unsupported %s", String.valueOf(options.action)));
            }
        }
    }

    private enum Action {
        CLEAR,
        DELETE,
        REMOVE_DUPLICATES,
        WRITE,
        READ,
        PRINT
    }

    private static class Options {

        public String sval;
        int ival = -1;
        Action action = Action.PRINT;
    }
}
