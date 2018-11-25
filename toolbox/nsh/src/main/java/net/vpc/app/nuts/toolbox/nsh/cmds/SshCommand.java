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

import net.vpc.app.nuts.NutsDependencySearch;
import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.commandline.Argument;
import net.vpc.common.io.FileUtils;
import net.vpc.common.ssh.SShConnection;
import net.vpc.common.commandline.CommandLine;
import net.vpc.common.strings.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 * ssh copy credits to Chanaka Lakmal from
 * https://medium.com/ldclakmal/scp-with-java-b7b7dbcdbc85
 */
public class SshCommand extends AbstractNutsCommand {

    public SshCommand() {
        super("ssh", DEFAULT_SUPPORT);
    }

    private static class Options {
        boolean verbose;
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
                if((a=cmdLine.readOption("--nuts"))!=null) {
                    o.invokeNuts = true;
                }else if((a=cmdLine.readStringOption("--nuts-jre"))!=null){
                    o.nutsJre =a.getStringValue();
                }else if((a=cmdLine.readBooleanOption("--verbose"))!=null){
                    o.verbose =a.getBooleanValue();
                }else{
                    //suppose this is an other nuts option
                    //just consume the rest as of the command
                    while (cmdLine.hasNext()) {
                        o.cmd.add(cmdLine.read().getExpression());
                    }
                }
            } else {
                if(o.address==null){
                    o.address=cmdLine.read().getExpression();
                }else {
                    while (cmdLine.hasNext()) {
                        o.cmd.add(cmdLine.read().getExpression());
                    }
                }
            }
        }
        if (o.address==null) {
            throw new IllegalArgumentException("Missing ssh address");
        }
        if (o.cmd.isEmpty()) {
            throw new IllegalArgumentException("Missing ssh command. Interactive ssh is not yet supported!");
        }
        ShellHelper.WsSshListener listener = o.verbose?new ShellHelper.WsSshListener(context.getSession()):null;
        try (SShConnection sshSession = new SShConnection(o.address)
             .addListener(listener)
        ) {
            List<String> cmd = new ArrayList<>();
            if(o.invokeNuts){
                String home=null;
                String workspace=null;
                CommandLine c=new CommandLine(o.cmd.subList(1,o.cmd.size()));
                Argument arg=null;
                while(c.hasNext()){
                    if((arg=c.readOption("--home"))!=null) {
                        home = c.readNonOption().getString();
                    }else if((arg=c.readOption("--workspace"))!=null){
                        workspace=c.readNonOption().getString();
                    }else if(c.isNonOption()){
                        break;
                    }
                }
                if(!StringUtils.isEmpty(o.nutsCommand)){
                    cmd.add(o.nutsCommand);
                }else{
                    String userHome=null;
                    sshSession.setFailFast()
                            .setRedirectErrorStream(true)
                            .grabOutputString().exec("echo","$HOME");
                    userHome= sshSession.getOutputString().trim();
                    if(StringUtils.isEmpty(home)){
                        home=userHome+"/.nuts";
                    }
                    if(StringUtils.isEmpty(workspace)){
                        workspace=home+"/default-workspace";
                    }
                    String goodJar=null;
                    for (String jar : new String[]{
                            home+"/bootstrap/net/vpc/app/nuts/nuts/CURRENT/nuts.jar",
                            userHome+"/bin/nuts.jar",
                            userHome+"/usr/local/nuts/nuts.jar",
                    }) {
                        SShConnection sShConnection = sshSession.setFailFast(false).
                                grabOutputString()
                                .setRedirectErrorStream(true);
                        int r = sShConnection.exec("ls", jar);
                        if(0== r){
                            //found
                            goodJar=jar.trim();
                            break;
                        }
                    }
                    if(goodJar==null){
                        String from = context.getWorkspace().getConfigManager().resolveNutsJarFile();
                        if(from==null){
                            throw new IllegalArgumentException("Unable to resolve Nuts Jar File");
                        }else {
                            context.getFormattedOut().printf("Detected nuts.jar location : %s\n", from);
                            sshSession.setFailFast(true).copyLocalToRemote(from, home + "/bootstrap/net/vpc/app/nuts/nuts/CURRENT/nuts.jar", true);
                            goodJar=home + "/bootstrap/net/vpc/app/nuts/nuts/CURRENT/nuts.jar";
//                            NutsFile[] deps = context.getWorkspace().fetchDependencies(new NutsDependencySearch(context.getWorkspace().getRuntimeId())
//                                    .setIncludeMain(true),
//                                    context.getSession());
//                            for (NutsFile dep : deps) {
//                                sshSession.setFailFast(true).copyLocalToRemote(dep.getFile(), home + "/bootstrap"
//                                        +"/"+dep.getId().getGroup().replace('.','/')
//                                        +"/"+dep.getId().getName()
//                                        +"/"+dep.getId().getVersion()
//                                        +"/"+context.getWorkspace().getNutsFileName(dep.getId(),"jar")
//                                        , true);
//                            }
                        }
                    }
                    if(o.nutsJre!=null){
                        cmd.add(o.nutsJre+ FileUtils.getNativePath("/bin/java"));
                    }else{
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
