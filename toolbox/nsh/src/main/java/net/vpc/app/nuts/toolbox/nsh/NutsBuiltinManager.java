/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.common.javashell.JShellBuiltin;
import net.vpc.common.javashell.JShellBuiltinManager;

/**
 *
 * @author vpc
 */
public class NutsBuiltinManager implements JShellBuiltinManager {
        private static final Logger LOG = Logger.getLogger(NutsBuiltinManager.class.getName());

    private Map<String, NshBuiltin> commands = new HashMap<>();

    @Override
    public boolean contains(String cmd) {
        return find(cmd) != null;
    }

    @Override
    public NshBuiltin find(String command) {
        return commands.get(command);
    }

        @Override
    public JShellBuiltin get(String cmd) {
        JShellBuiltin command = find(cmd);
        if (command == null) {
            throw new NoSuchElementException("builtin not found : " + cmd);
        }
        if (!command.isEnabled()) {
            throw new NoSuchElementException("builtin disabled : " + cmd);
        }
        return command;
    }

    @Override
    public void set(JShellBuiltin command) {
        if (!(command instanceof NshBuiltin)) {
            command = new ShellToNshCommand(command);
        }
        boolean b = commands.put(command.getName(), (NshBuiltin) command) == null;
        if (LOG.isLoggable(Level.FINE)) {
            if (b) {
                LOG.log(Level.FINE, "Registering builtin : " + command.getName());
            } else {
                LOG.log(Level.FINE, "Unregistering builtin : " + command.getName());
            }
        }
    }

    @Override
    public void set(JShellBuiltin... cmds) {
        StringBuilder installed = new StringBuilder();
        StringBuilder reinstalled = new StringBuilder();
        int installedCount = 0;
        int reinstalledCount = 0;
        boolean loggable = LOG.isLoggable(Level.FINE);
        for (JShellBuiltin command : cmds) {
            if (!(command instanceof NshBuiltin)) {
                command = new ShellToNshCommand(command);
            }
            boolean b = commands.put(command.getName(), (NshBuiltin) command) == null;
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
                LOG.log(Level.FINE, "Uninstalling Command : " + command);
            }
        }
        return b;
    }

    @Override
    public NshBuiltin[] getAll() {
        return commands.values().toArray(new NshBuiltin[0]);
    }
    
}
