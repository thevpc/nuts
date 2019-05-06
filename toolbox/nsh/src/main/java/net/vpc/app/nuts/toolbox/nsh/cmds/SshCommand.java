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

import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.commandline.Argument;
import net.vpc.common.io.FileUtils;
import net.vpc.common.ssh.SShConnection;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.NutsWorkspace;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class SshCommand extends AbstractNutsCommand {

    public SshCommand() {
        super("ssh", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean invokeNuts;
        String nutsCommand;
        String nutsJre;
        String address;
        List<String> cmd = new ArrayList<>();
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        CommandLine cmdLine = cmdLine(args, context);
        Options o = new Options();
        Argument a;
        while (cmdLine.hasNext()) {
            if (cmdLine.isOption()) {
                if (context.configure(cmdLine)) {
                    //
                } else if ((a = cmdLine.readOption("--nuts")) != null) {
                    o.invokeNuts = true;
                } else if ((a = cmdLine.readStringOption("--nuts-jre")) != null) {
                    o.nutsJre = a.getStringValue();
                } else {
                    //suppose this is an other nuts option
                    //just consume the rest as of the command
                    while (cmdLine.hasNext()) {
                        o.cmd.add(cmdLine.read().getExpression());
                    }
                }
            } else {
                o.address = cmdLine.read().getExpression();
                while (cmdLine.hasNext()) {
                    o.cmd.add(cmdLine.read().getExpression());
                }
            }
        }
        if (o.address == null) {
            throw new NutsExecutionException("Missing ssh address", 2);
        }
        if (o.cmd.isEmpty()) {
            throw new NutsExecutionException("Missing ssh command. Interactive ssh is not yet supported!", 2);
        }
        final NutsWorkspace ws = context.getWorkspace();
        ShellHelper.WsSshListener listener = context.isVerbose() ? new ShellHelper.WsSshListener(ws, context.getSession()) : null;
        try (SShConnection sshSession = new SShConnection(o.address)
                .addListener(listener)) {
            List<String> cmd = new ArrayList<>();
            if (o.invokeNuts) {
                String workspace = null;
                CommandLine c = new CommandLine(o.cmd.subList(1, o.cmd.size()));
                Argument arg = null;
                while (c.hasNext()) {
                    if ((arg = c.readOption("--workspace")) != null) {
                        workspace = c.readNonOption().getStringExpression();
                    } else if (c.isNonOption()) {
                        break;
                    } else {
                        c.skip();
                    }
                }
                if (!StringUtils.isEmpty(o.nutsCommand)) {
                    cmd.add(o.nutsCommand);
                } else {
                    String userHome = null;
                    sshSession.setFailFast()
                            .setRedirectErrorStream(true)
                            .grabOutputString().exec("echo", "$HOME");
                    userHome = sshSession.getOutputString().trim();
                    if (StringUtils.isEmpty(workspace)) {
                        workspace = userHome + "/.nuts/default-workspace";
                    }
                    Path t = ws.io().createTempFile("ws-config.json");
                    sshSession.setFailFast(true).copyRemoteToLocal(workspace + "/" + NutsConstants.Files.WORKSPACE_CONFIG_FILE_NAME, t.toString(), true);
                    Map confMap = ws.io().json().read(t, Map.class);
                    String bootApiVersion = confMap == null ? null : (String) confMap.get("bootApiVersion");
                    boolean versionExists = false;
                    boolean goodJarExists = false;
                    if (bootApiVersion == null) {
                        bootApiVersion = ws.config().getApiId().getVersion().toString();
                        versionExists = true;
                    }
                    String bootApiFileName = bootApiVersion + "/nuts-" + bootApiVersion + ".jar";
                    String goodJar = workspace + "/" + NutsConstants.Folders.BOOT
                            + "/net/vpc/app/nuts/nuts/" + bootApiFileName;
                    if (versionExists) {
                        SShConnection sShConnection = sshSession.setFailFast(false).
                                grabOutputString()
                                .setRedirectErrorStream(true);
                        int r = sShConnection.exec("ls", goodJar);
                        if (0 == r) {
                            //found
                            goodJarExists = true;
                        }
                    }

                    if (!goodJarExists) {
                        Path from = ws.find().id(ws.config().getApiId().setVersion(bootApiVersion)).getResultDefinitions().required().getPath();
                        if (from == null) {
                            throw new NutsExecutionException("Unable to resolve Nuts Jar File", 2);
                        } else {
                            context.out().printf("Detected nuts.jar location : %s\n", from);
                            sshSession.setFailFast(true).copyLocalToRemote(from.toString(), workspace + "/" + NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts/CURRENT/" + bootApiFileName, true);
                            goodJar = workspace + "/" + NutsConstants.Folders.BOOT + "/net/vpc/app/nuts/nuts/" + bootApiVersion + "/" + bootApiFileName;
//                            NutsDefinition[] deps = context.getWorkspace().fetchDependencies(new NutsDependencySearch(context.getWorkspace().getRuntimeId())
//                                    .setIncludeMain(true),
//                                    context.getSession());
//                            for (NutsDefinition dep : deps) {
//                                sshSession.setFailFast(true).copyLocalToRemote(dep.getFile(), home + "/"+NutsConstants.Folders.BOOT
//                                        +"/"+dep.getId().getGroup().replace('.','/')
//                                        +"/"+dep.getId().getName()
//                                        +"/"+dep.getId().getVersion()
//                                        +"/"+context.getWorkspace().getFileName(dep.getId(),"jar")
//                                        , true);
//                            }
                        }
                    }
                    if (o.nutsJre != null) {
                        cmd.add(o.nutsJre + FileUtils.getNativePath("/bin/java"));
                    } else {
                        cmd.add("java");
                    }
                    cmd.add("-jar");
                    cmd.add(goodJar);
                }
            }
            cmd.addAll(o.cmd);
            return sshSession.grabOutputString(false).setFailFast(true).exec(cmd);
        }
    }

}
