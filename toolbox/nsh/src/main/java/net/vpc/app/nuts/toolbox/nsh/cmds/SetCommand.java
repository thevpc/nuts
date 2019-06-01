/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshCommand;
import net.vpc.common.javashell.JShellFunction;

/**
 * Created by vpc on 1/7/17.
 */
public class SetCommand extends SimpleNshCommand {

    public SetCommand() {
        super("set", DEFAULT_SUPPORT);
    }

    private static class Options {

        LinkedHashMap<String, String> vars = new LinkedHashMap<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommand commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (a.isNonOption()) {
            if (a.isKeyValue()) {
                options.vars.put(a.getKey().getString(), a.getValue().getString());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommand commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.vars.isEmpty()) {
            List<String> results = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : context.getCommandContext().vars().getAll().entrySet()) {
                results.add(entry.getKey() + "=" + entry.getValue());
            }
            for (JShellFunction function : context.getGlobalContext().functions().getAll()) {
                results.add(function.getDefinition());
            }
            context.setOutObject(results);
        } else {
            for (Map.Entry<String, String> entry : options.vars.entrySet()) {
                context.getCommandContext().vars().set(entry.getKey(), entry.getValue());
            }
        }
    }
}
