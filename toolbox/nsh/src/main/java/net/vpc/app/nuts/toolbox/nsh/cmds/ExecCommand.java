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

import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class ExecCommand extends AbstractNutsCommand {

    public ExecCommand() {
        super("exec", DEFAULT_SUPPORT);
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        List<String> sargs=new ArrayList<>();
        boolean nativeCommand=false;
        boolean command=false;
        while(!cmdLine.isEmpty()){
            if(!command){
                if(cmdLine.read("-n","--native")) {
                    nativeCommand = true;
                }else{
                    command=true;
                }
            }else{
                sargs.add(cmdLine.readValue());
            }
        }
        if (cmdLine.isAutoCompleteMode()) {
            return -1;
        }
//        String[] finalArgs = cmdLine.toArray();
//        if (finalArgs.length > 0 && finalArgs[0].equals("-c")) {
//            int from = 1;
//            if (finalArgs.length > 1 && finalArgs[1].equals("-d")) {
//                from++;
//            }
//            List<String> commands = new ArrayList<>();
//            Map<String, String> env = new HashMap<>();
//            boolean expectEnv = true;
//            for (int i = from; i < finalArgs.length; i++) {
//                String command = finalArgs[i];
//                if (expectEnv) {
//                    String[] s = CoreNutsUtils.splitNameAndValue(command);
//                    if (s != null) {
//                        env.put(s[0], s[1]);
//                    } else {
//                        expectEnv = false;
//                        commands.add(command);
//                    }
//                } else {
//                    commands.add(command);
//                }
//            }
//            if (commands.isEmpty()) {
//                throw new NutsIllegalArgumentException("Missing command");
//            }
//            String[] commandsArray = commands.toArray(new String[commands.size()]);
//            String currentDirectory = context.getCwd();
//            return CoreIOUtils.execAndWait(commandsArray, env, currentDirectory == null ? null : new File(currentDirectory), context.getTerminal(), true);
//        }
        return context.getValidWorkspace()
                .createExecBuilder()
                .setNativeCommand(nativeCommand)
                .setCommand(sargs)
                .setEnv(context.getEnv())
                .setDirectory(context.getCwd())
                .setSession(context.getSession())
                .exec().getResult()
            ;
    }
}
