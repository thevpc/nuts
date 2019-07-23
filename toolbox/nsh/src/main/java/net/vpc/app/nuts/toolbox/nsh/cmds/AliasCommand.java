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
import net.vpc.common.javashell.JShell;
import net.vpc.app.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
public class AliasCommand extends SimpleNshBuiltin {

    public AliasCommand() {
        super("alias", DEFAULT_SUPPORT);
    }

    private static class Options {

        LinkedHashMap<String, String> add = new LinkedHashMap<String, String>();
        Set<String> show = new LinkedHashSet<String>();
        List<String> displayOptions = new ArrayList<String>();
    }

    private static class ResultItem {

        String name;
        String value;

        public ResultItem(String name, String value) {
            this.name = name;
            this.value = value;
        }

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
                commandLine.skip();
                options.displayOptions.add(a.toString());
                return true;
            }
        } else if (a.isKeyValue()) {
            commandLine.skip();
            options.add.put(a.getStringKey(), a.getStringValue());
            return true;
        } else {
            commandLine.skip();
            options.show.add(a.getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        JShell shell = context.getShell();
        if (options.add.isEmpty() && options.show.isEmpty()) {
            options.show.addAll(context.getRootContext().aliases().getAll());
        }
        for (Map.Entry<String, String> entry : options.add.entrySet()) {
            context.getRootContext().aliases().set(entry.getKey(), entry.getValue());
        }
        List<ResultItem> outRes = new ArrayList<>();
        List<ResultItem> errRes = new ArrayList<>();
        for (String a : options.show) {
            final String v = context.getRootContext().aliases().get(a);
            if (v == null) {
                errRes.add(new ResultItem(a, v));
            } else {
                outRes.add(new ResultItem(a, v));
            }
        }
        context.setPrintlnOutObject(outRes);
        if (!errRes.isEmpty()) {
            context.setErrObject(errRes);
        }
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context) {
        List<ResultItem> r = context.getResult();
        for (ResultItem resultItem : r) {
            if (resultItem.value == null) {
                context.out().printf("alias : %s @@not found@@%n", resultItem.name);
            } else {
                context.out().printf("alias : %s ='%s'%n", resultItem.name, resultItem.value);
            }
        }
    }

}
