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
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.ext.ssh.SShConnection;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17. ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class SshCommand extends NShellBuiltinDefault {

    public SshCommand() {
        super("ssh", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options o = context.getOptions();
        NArg a;
        NSession session = context.getSession();
        if (!o.cmd.isEmpty()) {
            o.cmd.add(cmdLine.next().flatMap(NLiteral::asString).get());
            return true;
        } else if (cmdLine.peek().get().isNonOption()) {
            if (o.address == null) {
                o.address = cmdLine.next().flatMap(NLiteral::asString).get();
            } else {
                o.cmd.add(cmdLine.next().flatMap(NLiteral::asString).get());
            }
            return true;
        } else if ((a = cmdLine.next("--nuts").orNull()) != null) {
            if (o.acceptDashNuts) {
                o.invokeNuts = true;
            } else {
                o.cmd.add(a.asString().get());
            }
            return true;
        } else if ((a = cmdLine.next("--nuts-jre").orNull()) != null) {
            if (o.acceptDashNuts) {
                o.nutsJre = a.getStringValue().get();
            } else {
                o.cmd.add(a.asString().get());
            }
            return true;
        } else if (o.address == null || cmdLine.peek().get().isNonOption()) {
            o.acceptDashNuts = false;
            o.cmd.add(cmdLine.next().flatMap(NLiteral::asString).get());
            return true;
        }

        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options o = context.getOptions();
        // address --nuts [nuts options] args
        NSession session = context.getSession();
        NAssert.requireNonBlank(o.address, "ssh address");
        NAssert.requireNonBlank(o.cmd, () -> NMsg.ofPlain("missing ssh command. Interactive ssh is not yet supported!"));
        ShellHelper.WsSshListener listener = new ShellHelper.WsSshListener(session);
        try (SShConnection sshSession = new SShConnection(o.address,
                session.in(),
                NOut.asOutputStream(),
                session.err().asOutputStream(),
                session)
                .addListener(listener)) {
            List<String> cmd = new ArrayList<>();
            if (o.invokeNuts) {
                String workspace = null;
                NCmdLine c = NCmdLine.of(o.cmd.subList(1, o.cmd.size()));
                NArg arg = null;
                while (c.hasNext()) {
                    if ((arg = c.next("--workspace").orNull()) != null) {
                        workspace = c.nextNonOption().get().asString().get();
                    } else if (c.peek().isPresent() && c.peek().get().isNonOption()) {
                        break;
                    } else {
                        c.skip();
                    }
                }
                if (!NBlankable.isBlank(o.nutsCommand)) {
                    cmd.add(o.nutsCommand);
                } else {
                    String userHome = null;
                    sshSession.failFast()
                            .redirectErrorStream()
                            .grabOutputString().exec("echo", "$HOME");
                    userHome = sshSession.getOutputString().trim();
                    if (NBlankable.isBlank(workspace)) {
                        workspace = userHome + "/.config/nuts/" + NConstants.Names.DEFAULT_WORKSPACE_NAME;
                    }
                    boolean nutsCommandFound = false;
                    int r = sshSession.setFailFast(false).
                            grabOutputString()
                            .redirectErrorStream().exec("ls", workspace + "/nuts");
                    if (0 == r) {
                        //found
                        nutsCommandFound = true;
                    }
                    if (!nutsCommandFound) {
                        NPath from = NSearchCmd.of().addId(session.getWorkspace().getApiId()).getResultDefinitions().findFirst().get().getContent().orNull();
                        NAssert.requireNonNull(from, "jar file");
                        context.out().println(NMsg.ofC("Detected nuts.jar location : %s", from));
                        String bootApiFileName = "nuts-" + session.getWorkspace().getApiId() + ".jar";
                        sshSession.failFast().copyLocalToRemote(from.toString(), workspace + "/" + bootApiFileName, true);
                        String javaCmd = null;
                        if (o.nutsJre != null) {
                            javaCmd = (o.nutsJre + "/bin/java");
                        } else {
                            javaCmd = ("java");
                        }
                    }
                    cmd.add(workspace + "/nuts");
                }
            }
            cmd.addAll(o.cmd);
            sshSession.grabOutputString(false).failFast().exec(cmd);
        }
    }

    private static class Options {
        boolean acceptDashNuts = true;
        boolean invokeNuts;
        String nutsCommand;
        String nutsJre;
        String address;
        List<String> cmd = new ArrayList<>();
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
