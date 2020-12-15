/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsExecutionException;
import net.thevpc.nuts.NutsSingleton;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;
import net.thevpc.jshell.JShellContext;
import net.thevpc.nuts.NutsCommandLine;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class SourceCommand extends SimpleNshBuiltin {

    public SourceCommand() {
        super("source", DEFAULT_SUPPORT);
    }

    private static class Options {

        List<String> files = new ArrayList<>();
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final NutsArgument a = commandLine.peek();
        if (!a.isOption()) {
            options.files.add(commandLine.next().getString());
            return true;
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options options = context.getOptions();
        final String[] paths = context.getExecutionContext().getGlobalContext().vars().get("PATH", "").split(":|;");
        List<String> goodFiles = new ArrayList<>();
        for (String file : options.files) {
            boolean found=false;
            if (!file.contains("/")) {
                for (String path : paths) {
                    if (new File(path, file).isFile()) {
                        file = new File(path, file).getPath();
                        break;
                    }
                }
                if (!new File(file).isFile()) {
                    if (new File(context.getRootContext().getCwd(), file).isFile()) {
                        file = new File(context.getRootContext().getCwd(), file).getPath();
                    }
                }
                if (new File(file).isFile()) {
                    found=true;
                    goodFiles.add(file);
                }
            }
            if(!found){
                goodFiles.add(file);
            }
        }
//        JShellContext c2 = context.getShell().createContext(context.getExecutionContext().getGlobalContext());
//        c2.setArgs(context.getArgs());
        JShellContext c2 = context.getExecutionContext().getGlobalContext();
        for (String goodFile : goodFiles) {
            context.getShell().executeFile(goodFile, context.getArgs(),c2);
        }
    }

}
