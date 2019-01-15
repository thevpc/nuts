/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsMinimalCommandLine;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.commandline.FolderNonOption;
import net.vpc.common.commandline.format.PropertiesFormatter;
import net.vpc.common.io.FileUtils;
import net.vpc.common.javashell.AutoCompleteCandidate;

import java.io.File;
import java.io.FileFilter;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class AutocompleteCommand extends AbstractNutsCommand {

    public AutocompleteCommand() {
        super("autocomplete", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        List<String> items = new ArrayList<>();
        CommandLine cmdLine = cmdLine(args, context);
        Argument a;
        int index = -1;
        String cmd = null;
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            } else {
                while (cmdLine.hasNext()) {
                    String s = cmdLine.read().getStringExpression();
                    if (cmd == null) {
                        cmd = s;
                    } else {
                        if (s.startsWith("[]") && index < 0) {
                            index = items.size();
                            items.add(s.substring(2));
                        } else {
                            items.add(s);
                        }
                    }
                }
            }
        }
        if (!cmdLine.isExecMode()) {
            return 0;
        }
        if (cmd == null) {
            throw new NutsExecutionException("Missing Command", 1);
        }
        if (index < 0) {
            index = items.size();
            items.add("");
        }
        List<AutoCompleteCandidate> aa = context.consoleContext().resolveAutoCompleteCandidates(cmd, items, index, NutsMinimalCommandLine.escapeArguments(items.toArray(new String[0])));
        Properties p = new Properties();
        for (AutoCompleteCandidate autoCompleteCandidate : aa) {
            String value = autoCompleteCandidate.getValue();
            String dvalue = autoCompleteCandidate.getDisplay();
            if (dvalue != null && dvalue.equals(value)) {
                dvalue = null;
            }
            p.setProperty(value == null ? "" : value, dvalue == null ? "" : dvalue);
        }
        for (String o : new TreeSet<String>((Set)p.keySet())) {
            if(o.startsWith("-")){
                // option
                context.out().printf("[[%s]]\n",o);
            }else if(o.startsWith("<")){
                context.out().printf("**%s**\n",o);
            }else{
                context.out().printf("<<%s>>\n",o);
            }
        }
        return 0;
    }
}
