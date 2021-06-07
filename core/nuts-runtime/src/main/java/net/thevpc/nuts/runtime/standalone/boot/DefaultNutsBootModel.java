/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
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
package net.thevpc.nuts.runtime.standalone.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.DefaultNutsSession;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSessionTerminal;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSystemTerminalBase;
import net.thevpc.nuts.runtime.core.terminals.DefaultNutsSystemTerminalBaseBoot;
import net.thevpc.nuts.runtime.core.terminals.DefaultSystemTerminal;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceEnvManagerModel;
import net.thevpc.nuts.runtime.standalone.io.NutsPrintStreamBase;
import net.thevpc.nuts.runtime.standalone.io.NutsPrintStreamSystem;

import java.io.InputStream;

/**
 *
 * @author vpc
 */
public class DefaultNutsBootModel implements NutsBootModel {

    protected NutsWorkspace workspace;
    protected NutsWorkspaceInitInformation workspaceInitInformation;
    protected NutsSession bootSession;
    private NutsPrintStream stdout;
    private NutsPrintStream stderr;
    private InputStream stdin;

    public DefaultNutsBootModel(NutsWorkspace workspace, NutsWorkspaceInitInformation workspaceInitInformation) {
        this.workspace = workspace;
        this.workspaceInitInformation = workspaceInitInformation;
        this.bootSession = new DefaultNutsSession(workspace, workspaceInitInformation.getOptions());
        boolean ansiSupport = detectAnsiTerminalSupport(DefaultNutsWorkspaceEnvManagerModel.getPlatformOsFamily0());
        NutsTerminalMode terminalMode = workspaceInitInformation.getOptions().getTerminalMode();
        if (terminalMode == null) {
            if (workspaceInitInformation.getOptions().isBot()) {
                terminalMode = NutsTerminalMode.FILTERED;
            } else {
                terminalMode = NutsTerminalMode.FORMATTED;
            }
        }

        stdout =new NutsPrintStreamSystem(System.out,null,null,ansiSupport,
                this.bootSession
        ).convert(terminalMode);
        stderr =new NutsPrintStreamSystem(System.err,null,null,ansiSupport,
                this.bootSession
        ).convert(terminalMode);
        stdin=System.in;
        DefaultNutsSystemTerminalBaseBoot base = new DefaultNutsSystemTerminalBaseBoot(this);

        DefaultSystemTerminal sys = new DefaultSystemTerminal(base);
        sys.setSession(bootSession, true);

        DefaultNutsSessionTerminal t = new DefaultNutsSessionTerminal();
        t.setParent(sys);
        t.setSession(bootSession);
        bootSession.setTerminal(t);
    }

    public NutsPrintStream stdout() {
        return stdout;
    }

    public NutsPrintStream stderr() {
        return stderr;
    }

    public InputStream stdin() {
        return stdin;
    }

    public NutsSession bootSession() {
        return bootSession;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsWorkspaceInitInformation getWorkspaceInitInformation() {
        return workspaceInitInformation;
    }

    public static boolean detectAnsiTerminalSupport(NutsOsFamily os) {
        boolean IS_WINDOWS = os == NutsOsFamily.WINDOWS;
        boolean IS_CYGWIN = IS_WINDOWS
                && System.getenv("PWD") != null
                && System.getenv("PWD").startsWith("/")
                && !"cygwin".equals(System.getenv("TERM"));

        boolean IS_MINGW_XTERM = IS_WINDOWS
                && System.getenv("MSYSTEM") != null
                && System.getenv("MSYSTEM").startsWith("MINGW")
                && "xterm".equals(System.getenv("TERM"));
        return (IS_WINDOWS && (IS_CYGWIN || IS_MINGW_XTERM))
                || os == NutsOsFamily.LINUX || os == NutsOsFamily.UNIX
                || os == NutsOsFamily.MACOS
                ;
    }
    
}
