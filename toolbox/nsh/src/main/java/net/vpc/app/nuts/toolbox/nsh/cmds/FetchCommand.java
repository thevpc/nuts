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

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.app.options.NutsIdNonOption;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.FileNonOption;

import java.io.File;
import java.io.PrintStream;
import java.util.Objects;

/**
 * Created by vpc on 1/7/17.
 */
public class FetchCommand extends AbstractNutsCommand {

    public FetchCommand() {
        super("fetch", DEFAULT_SUPPORT);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        String lastLocationFile = null;
        boolean descMode = false;
        boolean effective = false;
        Argument a;
        do {
            if (context.configure(cmdLine)) {
                //
            }else if (cmdLine.readAll("-t", "--to")) {
                lastLocationFile = (cmdLine.readRequiredNonOption(new FileNonOption("FileOrFolder")).getStringExpression());
            } else if (cmdLine.readAll("-d", "--desc")) {
                descMode = true;
            } else if (cmdLine.readAll("-n", "--nuts")) {
                descMode = false;
            } else if (cmdLine.readAll("-e", "--effective")) {
                effective = true;
            } else {
                NutsWorkspace ws = context.getWorkspace();
                String id = cmdLine.readRequiredNonOption(new NutsIdNonOption("NutsId", context.getWorkspace())).getStringExpression();
                if (cmdLine.isExecMode()) {
                    if (descMode) {
                        NutsDefinition file = null;
                        if (lastLocationFile == null) {
                            file=context.getWorkspace().fetch(id).setIncludeEffective(effective).setSession(context.getSession()).fetchDefinition();
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || new File(context.getShell().getAbsolutePath(lastLocationFile)).isDirectory()) {
                            File folder = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            folder.mkdirs();
                            NutsDescriptor descriptor = context.getWorkspace().fetch(id).setIncludeEffective(effective).setSession(context.getSession()).fetchDescriptor();
                            File target = new File(folder, ws.getConfigManager().getDefaultIdFilename(
                                    context.getWorkspace().getParseManager().parseId(id)
                                    .setFaceDescriptor()
                            ));
                            target=new File(target.getParentFile(),"effective-"+target.getName());
                            context.getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor,target);
                            file = new DefaultNutsDefinition(ws.getParseManager().parseRequiredId(id), descriptor, target.getPath(), false, true, null);
                        } else {
                            File target = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            file=context.getWorkspace().fetch(id).setIncludeEffective(effective).setSession(context.getSession()).fetchDefinition();
                            NutsDescriptor descriptor = file.getDescriptor();
                            context.getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true).format(descriptor,target);
                            lastLocationFile = null;
                        }
                        printFetchedFile(file, context);
                    } else {
                        NutsDefinition file = null;
                        if (lastLocationFile == null) {
                            file = context.getWorkspace().fetch(id).setSession(context.getSession()).fetchDefinition();
                        } else if (lastLocationFile.endsWith("/") || lastLocationFile.endsWith("\\") || new File(context.getShell().getAbsolutePath(lastLocationFile)).isDirectory()) {
                            File folder = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            folder.mkdirs();
                            String fetched = context.getWorkspace().copyTo(id, folder.getPath(), context.getSession());
                            file = new DefaultNutsDefinition(ws.getParseManager().parseRequiredId(id), null, fetched, false, true, null);
                        } else {
                            File simpleFile = new File(context.getShell().getAbsolutePath(lastLocationFile));
                            String fetched = context.getWorkspace().copyTo(id, simpleFile.getPath(), context.getSession());
                            file = new DefaultNutsDefinition(ws.getParseManager().parseRequiredId(id), null, fetched, false, true, null);
                            lastLocationFile = null;
                        }
                        printFetchedFile(file, context);
                    }
                }
            }
        }while (cmdLine.hasNext());
        return 0;
    }

    private void printFetchedFile(NutsDefinition file, NutsCommandContext context) {
        PrintStream out = context.out();
        if (!file.getContent().isCached()) {
            if (file.getContent().isTemporary()) {
                out.printf("%s fetched successfully temporarily to %s\n", file.getId(), file.getContent().getFile());
            } else {
                out.printf("%s fetched successfully\n", file.getId());
            }
        } else {
            if (file.getContent().isTemporary()) {
                out.printf("%s already fetched temporarily to %s\n", file.getId(), file.getContent().getFile());
            } else {
                out.printf("%s already fetched\n", file.getId());
            }
        }
    }


    public class DefaultNutsDefinition implements NutsDefinition{

        private NutsId id;
        private NutsDescriptor descriptor;
        private NutsContent content;
        private NutsDescriptor effectiveDescriptor;

        public DefaultNutsDefinition(NutsId id, NutsDescriptor descriptor, String file, boolean cached, boolean temporary, String installFolder) {
            this.descriptor = descriptor;
            this.content = new NutsContent(file,cached,temporary);
            this.id = id;
        }

        public DefaultNutsDefinition(DefaultNutsDefinition other) {
            if (other != null) {
                this.descriptor = other.descriptor;
                this.id = other.id;
                this.content = other.content;
            }
        }

        @Override
        public NutsDescriptor getEffectiveDescriptor() {
            return effectiveDescriptor;
        }



        public void setId(NutsId id) {
            this.id = id;
        }

        public NutsId getId() {
            return id;
        }


        public NutsDescriptor getDescriptor() {
            return descriptor;
        }

        @Override
        public NutsContent getContent() {
            return content;
        }

        @Override
        public NutsInstallInfo getInstallation() {
            return null;
        }

        public DefaultNutsDefinition copy() {
            return new DefaultNutsDefinition(this);
        }



        @Override
        public int compareTo(NutsDefinition n2) {
            if (n2 == null) {
                return 1;
            }
            if (!(n2 instanceof DefaultNutsDefinition)) {
                return -1;
            }
            NutsId o1 = getId();
            NutsId o2 = ((DefaultNutsDefinition) n2).getId();
            if (o1 == null || o2 == null) {
                if (o1 == o2) {
                    return 0;
                }
                if (o1 == null) {
                    return -1;
                }
                return 1;
            }
            return o1.toString().compareTo(o2.toString());
        }

        @Override
        public NutsRepository getRepository() {
            return null;
        }
    }

}
