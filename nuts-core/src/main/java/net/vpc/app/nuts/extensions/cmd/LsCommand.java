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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsIllegalArgumentsException;
import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsTerminal;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.FileNonOption;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
public class LsCommand extends AbstractNutsCommand {

    public LsCommand() {
        super("ls", CORE_SUPPORT);
    }

    private static class Options{
        boolean d=false;
        boolean l=false;
    }
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandAutoComplete autoComplete=context.getAutoComplete();
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        boolean any=false;
        Options options=new Options();
        List<File> folders=new ArrayList<>();
        List<File> files=new ArrayList<>();
        List<File> invalids=new ArrayList<>();
        while (!cmdLine.isEmpty()) {
            if(cmdLine.read("-d")) {
                options.d = true;
            }else if(cmdLine.read("-l")){
                options.l=true;
            }else {
                String path = cmdLine.readNonOptionOrError(new FileNonOption("FileOrFolder")).getString();
                File file = CoreIOUtils.createFileByCwd(path, new File(context.getCommandLine().getCwd()));
                if(file.isDirectory()) {
                    folders.add(file);
                }else if(file.exists()){
                    files.add(file);
                }else {
                    invalids.add(file);
                }
            }
        }
        boolean first=true;
        for (File f : invalids) {
            ls(f, options,context,context.getTerminal(),false);
        }
        for (File f : files) {
            first=false;
            ls(f, options,context,context.getTerminal(),false);
        }
        for (File f : folders) {
            if(first){
                first=false;
            }else{
                context.getTerminal().getOut().println();
            }
            ls(f, options,context,context.getTerminal(),folders.size()>0 ||files.size()>0);
        }
        if(invalids.size()+files.size()+folders.size()==0){
            ls(new File(context.getCommandLine().getCwd()),options,context,context.getTerminal(),false);
        }
        return 0;
    }

    private void ls(File path, Options options,NutsCommandContext context,NutsTerminal terminal,boolean addPrefix){
        if(!path.exists()){
            throw new NutsIllegalArgumentsException("ls: cannot access '"+path.getPath()+"': No such file or directory");
        }else if(path.isDirectory()){
            if(addPrefix){
                terminal.getOut().println(path.getName()+":");
                for (File file1 : CoreIOUtils.nonNullArray(path.listFiles())) {
                    ls0(file1,options,terminal);
                }
            }else{
                for (File file1 : CoreIOUtils.nonNullArray(path.listFiles())) {
                    ls0(file1,options,terminal);
                }
            }
        }else{
            ls0(path,options,terminal);
        }
    }

    private void ls0(File path, Options options,NutsTerminal terminal){
        String name = path.getName();
        if(options.l){
            terminal.getOut().print(path.isDirectory()?"d":path.isFile()?"-":"?");
            terminal.getOut().print(" ");
            if(path.isDirectory()){
                name += "/";
                terminal.getOut().println(name);
            }else{
                terminal.getOut().println(name);
            }
        }else{
            if(path.isDirectory()){
                name += "/";
                terminal.getOut().println(name);
            }else{
                terminal.getOut().println(name);
            }
        }
    }
}
