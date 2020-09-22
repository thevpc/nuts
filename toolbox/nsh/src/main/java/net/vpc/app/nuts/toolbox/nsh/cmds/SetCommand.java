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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
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
import net.vpc.app.nuts.NutsSingleton;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.vpc.common.javashell.JShellFunction;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class SetCommand extends SimpleNshBuiltin {

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
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        NutsArgument a = commandLine.peek();
        if (a.isNonOption()) {
            if (a.isKeyValue()) {
                options.vars.put(a.getStringKey(), a.getStringValue());
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.vars.isEmpty()) {
            List<String> results = new ArrayList<>();
            for (Map.Entry<Object, Object> entry : context.getExecutionContext().vars().getAll().entrySet()) {
                results.add(entry.getKey() + "=" + entry.getValue());
            }
            for (JShellFunction function : context.getRootContext().functions().getAll()) {
                results.add(function.getDefinition());
            }
            context.setPrintlnOutObject(results);
        } else {
            for (Map.Entry<String, String> entry : options.vars.entrySet()) {
                context.getExecutionContext().vars().set(entry.getKey(), entry.getValue());
            }
        }
    }
}
