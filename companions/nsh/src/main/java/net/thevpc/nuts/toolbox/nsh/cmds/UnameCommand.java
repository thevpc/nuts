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

import net.thevpc.nuts.NutsArchFamily;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class UnameCommand extends SimpleJShellBuiltin {

    public UnameCommand() {
        super("uname", DEFAULT_SUPPORT,Options.class);
    }


    @Override
    protected boolean configureFirst(NutsCommandLine cmdLine, JShellExecutionContext context) {
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
    protected void execBuiltin(NutsCommandLine commandLine, JShellExecutionContext context) {
        Options config = context.getOptions();
        NutsSession ws = context.getSession();

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
        switch (context.getSession().getOutputFormat()) {
            case PLAIN: {
                List<String> sb = new ArrayList<>();
                if (rr.arch != null) {
                    sb.add(rr.arch.toString());
                }
                if (rr.os != null) {
                    sb.add(rr.os.toString());
                }
                if (rr.osDist != null) {
                    sb.add(rr.osDist.toString());
                }
                if (sb.isEmpty()) {
                    sb.add("UNKNOWN");
                }
                context.getSession().out().println(String.join(" ", sb));
                break;
            }
            default: {
                context.getSession().out().printlnf(rr);
            }
        }
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

}
