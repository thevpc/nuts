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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.vpc.common.javashell.JShellBuiltin;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class EnableCommand extends SimpleNshBuiltin {

    public EnableCommand() {
        super("enable", DEFAULT_SUPPORT);
    }

    private static class Options {

        String file;
        boolean a;
        boolean d;
        boolean n;
        boolean p;
        boolean s;
        Set<String> names = new LinkedHashSet<String>();
        List<String> displayOptions = new ArrayList<String>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final NutsArgument a = commandLine.peek();
        if (a.isOption()) {
            if (a.getStringKey().equals("--sort")) {
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isOption()) {
            switch (a.getStringKey()) {
                case "-a": {
                    options.a = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-d": {
                    options.d = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-n": {
                    options.n = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-p": {
                    options.p = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-s": {
                    options.s = commandLine.nextBoolean().getBooleanValue();
                    return true;
                }
                case "-f": {
                    options.file = commandLine.nextString().getStringValue();
                    return true;
                }
            }
        } else {
            options.names.add(commandLine.next().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        if (options.p || options.names.isEmpty()) {
            Map<String, String> result = new LinkedHashMap<>();
            for (JShellBuiltin command : context.getGlobalContext().builtins().getAll()) {
                result.put(command.getName(), command.isEnabled() ? "enabled" : "disabled");
            }
            context.setPrintlnOutObject(context);
        } else if (options.n) {
            List<String> nobuiltin = new ArrayList<>();
            for (String name : options.names) {
                JShellBuiltin c = context.getGlobalContext().builtins().find(name);
                if (c == null) {
                    nobuiltin.add(name);
                } else {
                    c.setEnabled(false);
                }
            }
            if (!nobuiltin.isEmpty()) {
                context.setErrObject(nobuiltin);
            }
        }
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        if (context.getResult() instanceof Map) {
            for (Map.Entry<String, String> entry : ((Map<String, String>) context.getResult()).entrySet()) {
                context.out().println(entry.getValue() + " " + entry.getKey());
            }
        } else if (context.getResult() instanceof List) {
            for (String s : ((List<String>) context.getResult())) {
                context.out().printf("@@enable: {{%s}} : not a shell builti@@%n", s);
            }
        }
    }

}
