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

import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.javashell.ShellHistory;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class HistoryCommand extends AbstractNutsCommand {

    enum Action {
        CLEAR,
        DELETE,
        REMOVE_DUPLICATES,
        WRITE,
        READ,
        PRINT
    }

    public HistoryCommand() {
        super("history", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        Argument a;
        class Options {
            public String sval;
            int ival = -1;
            Action action = Action.PRINT;
        }
        Options o = new Options();
        while (cmdLine.hasNext()) {
            if (context.configure(cmdLine)) {
                //
            } else if (cmdLine.readOption("-c", "--clear") != null) {
                o.action = Action.CLEAR;
                cmdLine.unexpectedArgument(getName());
            } else if ((a = cmdLine.readStringOption("-d", "--delete")) != null) {
                o.action = Action.DELETE;
                o.ival = a.getIntValue();
                cmdLine.unexpectedArgument(getName());
            } else if ((a = cmdLine.readOption("-D", "--remove-duplicates")) != null) {
                o.action = Action.REMOVE_DUPLICATES;
                cmdLine.unexpectedArgument(getName());
            } else if ((a = cmdLine.readOption("-w", "--write")) != null) {
                o.action = Action.WRITE;
                if(a.isKeyVal()){
                    o.sval=a.getStringValue();
                }else if(!cmdLine.isEmpty()){
                    o.sval=cmdLine.read().getString();
                }
                cmdLine.unexpectedArgument(getName());
            } else if ((a = cmdLine.readOption("-r", "--read")) != null) {
                o.action = Action.READ;
                if(a.isKeyVal()){
                    o.sval=a.getStringValue();
                }else if(!cmdLine.isEmpty()){
                    o.sval=cmdLine.read().getString();
                }
                cmdLine.unexpectedArgument(getName());
            } else {
                if (cmdLine.get(0).getInt(0) != 0) {
                    o.action = Action.PRINT;
                    o.ival = Math.abs(cmdLine.read().getInt());
                } else {
                    cmdLine.unexpectedArgument(getName());
                }
            }
        }
        if (!cmdLine.isExecMode()) {
            return 0;
        }
        ShellHistory shistory = context.getShell().getHistory();
        switch (o.action) {
            case PRINT: {
                PrintStream out = context.out();
                List<String> history = shistory.getElements(o.ival <= 0 ? 1000 : o.ival);
                int offset = shistory.size() - history.size();
                for (int i = 0; i < history.size(); i++) {
                    String historyElement = history.get(i);
                    out.println(StringUtils.formatRight(String.valueOf(offset + i + 1), 5) + ". " + historyElement);
                }
                return 0;
            }
            case CLEAR: {
                shistory.clear();
                return 0;
            }
            case REMOVE_DUPLICATES: {
                shistory.removeDuplicates();
                return 0;
            }
            case DELETE: {
                shistory.remove(o.ival - 1);
                return 0;
            }
            case WRITE: {
                if(o.sval==null) {
                    shistory.save();
                }else{
                    shistory.save(new File(context.getWorkspace().getIOManager().resolvePath(o.sval)));
                }
                return 0;
            }
            case READ: {
                if(o.sval==null) {
                    shistory.clear();
                    shistory.load();
                }else{
                    shistory.load(new File(context.getWorkspace().getIOManager().resolvePath(o.sval)));
                }
                return 0;
            }
        }
        return 0;
    }
}
