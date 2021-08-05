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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.lib.ssh.SShConnection;
import net.thevpc.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.thevpc.nuts.toolbox.nsh.bundles._StringUtils;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.*;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class SshCommand extends AbstractNshBuiltin {

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

    public int execImpl(String[] args, JShellExecutionContext context) {
        NutsCommandLine commandLine = cmdLine(args, context);
        Options o = new Options();
        NutsArgument a;
        // address --nuts [nuts options] args
        boolean acceptDashNuts = true;
        while (commandLine.hasNext()) {
            if (!o.cmd.isEmpty()) {
                o.cmd.add(commandLine.next().getString());
            } else if (commandLine.peek().isNonOption()) {
                if (o.address == null) {
                    o.address = commandLine.next().getString();
                } else {
                    o.cmd.add(commandLine.next().getString());
                }
            } else if ((a = commandLine.next("--nuts")) != null) {
                if (acceptDashNuts) {
                    o.invokeNuts = true;
                } else {
                    o.cmd.add(a.getString());
                }
            } else if ((a = commandLine.next("--nuts-jre")) != null) {
                if (acceptDashNuts) {
                    o.nutsJre = a.getStringValue();
                } else {
                    o.cmd.add(a.getString());
                }
            } else if (o.address == null || commandLine.peek().isNonOption()) {
                acceptDashNuts = false;
                o.cmd.add(commandLine.next().getString());
            } else {
                context.configureLast(commandLine);
            }
        }
        if (o.address == null) {
            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("missing ssh address"), 2);
        }
        if (o.cmd.isEmpty()) {
            throw new NutsExecutionException(context.getSession(),
                    NutsMessage.cstyle("missing ssh command. Interactive ssh is not yet supported!")
                    , 2);
        }
        final NutsWorkspace ws = context.getWorkspace();
        ShellHelper.WsSshListener listener = new ShellHelper.WsSshListener(context.getSession());
        try (SShConnection sshSession = new SShConnection(o.address,context.getSession())
                .addListener(listener)) {
            List<String> cmd = new ArrayList<>();
            if (o.invokeNuts) {
                String workspace = null;
                NutsCommandLine c = context.getWorkspace().commandLine().create(o.cmd.subList(1, o.cmd.size()).toArray(new String[0]));
                NutsArgument arg = null;
                while (c.hasNext()) {
                    if ((arg = c.next("--workspace")) != null) {
                        workspace = c.requireNonOption().next().getString();
                    } else if (c.peek().isNonOption()) {
                        break;
                    } else {
                        c.skip();
                    }
                }
                if (!_StringUtils.isBlank(o.nutsCommand)) {
                    cmd.add(o.nutsCommand);
                } else {
                    String userHome = null;
                    sshSession.setFailFast(true)
                            .setRedirectErrorStream(true)
                            .grabOutputString().exec("echo", "$HOME");
                    userHome = sshSession.getOutputString().trim();
                    if (_StringUtils.isBlank(workspace)) {
                        workspace = userHome + "/.config/nuts/" + NutsConstants.Names.DEFAULT_WORKSPACE_NAME;
                    }
                    boolean nutsCommandFound = false;
                    int r = sshSession.setFailFast(false).
                            grabOutputString()
                            .setRedirectErrorStream(true).exec("ls", workspace + "/nuts");
                    if (0 == r) {
                        //found
                        nutsCommandFound = true;
                    }
                    if (!nutsCommandFound) {
                        Path from = ws.search().addId(ws.getApiId()).getResultDefinitions().required().getPath();
                        if (from == null) {
                            throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("unable to resolve Nuts Jar File"), 2);
                        } else {
                            context.out().printf("Detected nuts.jar location : %s\n", from);
                            String bootApiFileName = "nuts-" + ws.getApiId() + ".jar";
                            sshSession.setFailFast(true).copyLocalToRemote(from.toString(), workspace + "/" + bootApiFileName, true);
                            String javaCmd = null;
                            if (o.nutsJre != null) {
                                javaCmd = (o.nutsJre + "/bin/java");
                            } else {
                                javaCmd = ("java");
                            }
                            if (sshSession.exec(javaCmd, "-jar", workspace + "/" + bootApiFileName, "-y", "install", "nadmin", "--yes") != 0) {
                                throw new NutsExecutionException(context.getSession(), NutsMessage.cstyle("install remote nuts failed"), 2);
                            }
                        }
                    }
                    cmd.add(workspace + "/nuts");
                }
            }
            cmd.addAll(o.cmd);
            sshSession.grabOutputString(false).setFailFast(true).exec(cmd);
        }
        return 0;
    }

}
