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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.cmd.cmdline.*;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class ConfigCommand extends AbstractNutsCommand {

    private List<ConfigSubCommand> subCommands;

    public ConfigCommand() {
        super("config", CORE_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) {
        if (subCommands == null) {
            subCommands = new ArrayList<>(
                    context.getValidWorkspace().getFactory().createAllSupported(ConfigSubCommand.class, this)
            );
        }
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        Boolean autoSave = null;
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        boolean empty = true;
        do {
            if (cmdLine.readOnce("--save")) {
                autoSave = true;
                empty = false;
                continue;
            }
            if (cmdLine.readOnce("-h", "-?", "--help")) {
                empty = false;
                if (cmdLine.isExecMode()) {
                    NutsPrintStream out = context.getTerminal().getOut();
                    out.println("update");
                    out.println("check-updates");
                    out.println("create workspace ...");
                    out.println("set workspace ...");
                    out.println("create repo ...");
                    out.println("add repo ...");
                    out.println("remove repo ...");
                    out.println("list repos ...");
                    out.println("add extension ...");
                    out.println("list extensions ...");
                    out.println("edit repo <repoId> ...");
                    out.println("list imports");
                    out.println("clear imports");
                    out.println("list archetypes");
                    out.println("import");
                    out.println("list imports");
                    out.println("clear imports");
                    out.println("unimport");
                    out.println("list users");
                    out.println("add user");
                    out.println("edit user");
                    out.println("passwd");
                    out.println("secure");
                    out.println("unsecure");
                    out.println("set loglevel verbose|fine|finer|finest|error|severe|config|all|none");
                    out.println("");
                    out.println("type 'help config' for more detailed help");
                }
                continue;
            }
            ConfigSubCommand selectedSubCommand = null;
            for (ConfigSubCommand subCommand : subCommands) {
                if (subCommand.exec(cmdLine, this, autoSave, context)) {
                    selectedSubCommand = subCommand;
                    empty = false;
                    break;
                }
            }
            if (selectedSubCommand != null) {
                continue;
            }

            if (!cmdLine.isExecMode()) {
                return -1;
            }
            if (!cmdLine.isEmpty()) {
                NutsPrintStream out = context.getTerminal().getErr();
                out.println("Unexpected " + cmdLine.get(0));
                out.println("type for more help : config -h");
                return 1;
            }
            break;
        } while (!cmdLine.isEmpty());
        if (empty) {
            NutsPrintStream out = context.getTerminal().getErr();
            out.println("Missing config command");
            out.println("type for more help : config -h");
            return 1;
        }
        return 0;
    }

    public void showRepo(NutsCommandContext context, NutsRepository repository, String prefix) {
        boolean enabled = repository.isEnabled();
        String disabledString = enabled ? "" : " <DISABLED>";
        context.getTerminal().getOut().print(prefix);
        if (enabled) {
            context.getTerminal().getOut().print("==" + repository.getRepositoryId() + disabledString + "==");
        } else {
            context.getTerminal().getOut().print("@@" + repository.getRepositoryId() + disabledString + "@@");
        }
        context.getTerminal().getOut().print(" : " + repository);
        context.getTerminal().getOut().println();

    }

    public void showRepoTree(NutsCommandContext context, NutsRepository repository, String prefix) {
        showRepo(context, repository, prefix);
        String prefix1 = prefix + "  ";
        for (NutsRepository c : repository.getMirrors()) {
            showRepoTree(context, c, prefix1);
        }
    }

    public static boolean trySave(NutsCommandContext context, NutsWorkspace workspace, NutsRepository repository, Boolean save, CmdLine cmdLine) {
        if (save == null) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository != null) {
                    save = Boolean.parseBoolean(repository.getEnv("autosave", "false", true));
                } else {
                    save = Boolean.parseBoolean(context.getValidWorkspace().getEnv("autosave", "false"));
                }
            } else {
                save = false;
            }
        } else {
            save = false;
        }
        if (cmdLine != null) {
            while (!cmdLine.isEmpty()) {
                if (cmdLine.read("--save")) {
                    save = true;
                } else {
                    cmdLine.requireEmpty();
                }
            }
        }
        if (save) {
            if (cmdLine == null || cmdLine.isExecMode()) {
                if (repository == null) {
                    workspace.save();
                    context.getTerminal().getOut().println("<<workspace saved.>>");
                } else {
                    context.getTerminal().getOut().println("<<repository " + repository.getRepositoryId() + " saved.>>");
                    repository.save();
                }
            }
        }
        return save;
    }

}
