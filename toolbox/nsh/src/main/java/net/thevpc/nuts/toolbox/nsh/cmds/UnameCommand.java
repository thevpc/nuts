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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.nsh.SimpleNshBuiltin;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class UnameCommand extends SimpleNshBuiltin {

    public UnameCommand() {
        super("uname", DEFAULT_SUPPORT);
    }

    private static class Options {

        boolean farch = false;
        boolean fos = false;
        boolean fdist = false;
    }

    private static class Result {

        NutsId osDist;
        NutsId os;
        NutsArchFamily arch;
    }

    @Override
    protected Object createOptions() {
        return new Options();
    }

    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        switch (cmdLine.peek().getKey().getString()) {
            case "-m": {
                config.farch = true;
                return true;
            }
            case "-r": {
                config.fos = true;
                return true;
            }
            case "-d": {
                config.fdist = true;
                return true;
            }
            case "-a": {
                config.fdist = true;
                config.fos = true;
                config.farch = true;
                return true;
            }
        }
        return false;
    }

    @Override
    protected void createResult(NutsCommandLine commandLine, SimpleNshCommandContext context) {
        Options config = context.getOptions();
        NutsWorkspace ws = context.getWorkspace();

        Result rr = new Result();
        rr.osDist = ws.env().getOsDist();
        rr.os = ws.env().getOs();
        rr.arch = ws.env().getArchFamily();
        if (!config.farch && !config.fos && !config.fdist) {
            config.farch = true;
            config.fos = true;
            config.fdist = true;
        }
        if (!config.farch && rr.arch != null) {
            rr.arch = null;
        }
        if (!config.fos && rr.os != null) {
            rr.os = null;
        }
        if (!config.fdist && rr.osDist != null) {
            rr.osDist = null;
        }
        context.setPrintlnOutObject(rr);
    }

    @Override
    protected void printPlainObject(SimpleNshCommandContext context, NutsSession session) {
        Result result = context.getResult();
        List<String> sb = new ArrayList<>();
        if (result.arch != null) {
            sb.add(result.arch.toString());
        }
        if (result.os != null) {
            sb.add(result.os.toString());
        }
        if (result.osDist != null) {
            sb.add(result.osDist.toString());
        }
        if (sb.isEmpty()) {
            sb.add("UNKNOWN");
        }
        context.out().println(String.join(" ", sb));
    }

}
