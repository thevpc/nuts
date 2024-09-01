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
package net.thevpc.nuts.toolbox.nsh.cmds;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author thevpc
 */
public class NBuiltinManager implements NShellBuiltinManager {
    private static final Logger LOG = Logger.getLogger(NBuiltinManager.class.getName());

    private Map<String, NShellBuiltin> commands = new HashMap<>();

    @Override
    public NShellBuiltin find(String command) {
        return commands.get(command);
    }

    @Override
    public NShellBuiltin get(String cmd) {
        NShellBuiltin command = find(cmd);
        if (command == null) {
            throw new NoSuchElementException("builtin not found : " + cmd);
        }
        if (!command.isEnabled()) {
            throw new NoSuchElementException("builtin disabled : " + cmd);
        }
        return command;
    }

    @Override
    public boolean contains(String cmd) {
        return find(cmd) != null;
    }

    @Override
    public void set(NShellBuiltin command) {
        boolean b = commands.put(command.getName(), command) == null;
        if (LOG.isLoggable(Level.FINE)) {
            if (b) {
                LOG.log(Level.FINE, "registering builtin : {0}", command.getName());
            } else {
                LOG.log(Level.FINE, "unregistering builtin : {0}", command.getName());
            }
        }
    }

    @Override
    public void set(NShellBuiltin... cmds) {
        StringBuilder installed = new StringBuilder();
        StringBuilder reinstalled = new StringBuilder();
        int installedCount = 0;
        int reinstalledCount = 0;
        boolean loggable = LOG.isLoggable(Level.FINE);
        for (NShellBuiltin command : cmds) {
            boolean b = commands.put(command.getName(), command) == null;
            if (loggable) {
                if (b) {
                    if (installed.length() > 0) {
                        installed.append(", ");
                    }
                    installed.append(command.getName());
                    installedCount++;
                } else {
                    if (reinstalled.length() > 0) {
                        reinstalled.append(", ");
                    }
                    reinstalled.append(command.getName());
                    reinstalledCount++;
                }
            }
        }
        if (loggable) {
            if (installed.length() > 0) {
                installed.insert(0, "Registering " + installedCount + " builtin" + (installedCount > 1 ? "s" : "") + " : ");
            }
            if (reinstalled.length() > 0) {
                installed.append(" ; Unregistering ").append(reinstalledCount).append(" builtin").append(reinstalledCount > 1 ? "s" : "").append(" : ");
                installed.append(reinstalled);
            }
            LOG.log(Level.FINE, installed.toString());
        }
    }

    @Override
    public boolean unset(String command) {
        boolean b = commands.remove(command) != null;
        if (LOG.isLoggable(Level.FINE)) {
            if (b) {
                LOG.log(Level.FINE, "Uninstalling NShellCommandNode : " + command);
            }
        }
        return b;
    }

    @Override
    public NShellBuiltin[] getAll() {
        return commands.values().toArray(new NShellBuiltin[0]);
    }

}
