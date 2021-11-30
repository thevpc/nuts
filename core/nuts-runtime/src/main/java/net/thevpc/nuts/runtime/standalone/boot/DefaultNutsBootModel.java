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
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.session.DefaultNutsSession;
import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNutsArgument;
import net.thevpc.nuts.runtime.standalone.io.terminals.DefaultNutsSessionTerminalFromSystem;
import net.thevpc.nuts.runtime.standalone.io.terminals.DefaultNutsSystemTerminalBaseBoot;
import net.thevpc.nuts.runtime.standalone.io.terminals.DefaultSystemTerminal;
import net.thevpc.nuts.runtime.optional.jansi.OptionalJansi;
import net.thevpc.nuts.runtime.standalone.io.printstream.NutsPrintStreamSystem;
import net.thevpc.nuts.runtime.standalone.workspace.CoreNutsWorkspaceInitInformation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 */
public class DefaultNutsBootModel implements NutsBootModel {

    protected NutsWorkspace workspace;
    protected boolean firstBoot;
    protected boolean initializing;
    protected CoreNutsWorkspaceInitInformation workspaceInitInformation;
    protected NutsSession bootSession;
    private NutsPrintStream stdout;
    private NutsPrintStream stderr;
    private InputStream stdin;
    private Map<String, NutsVal> customBootOptions = new LinkedHashMap<>();
    private StdFd bootStdFd;

    public DefaultNutsBootModel(NutsWorkspace workspace, CoreNutsWorkspaceInitInformation workspaceInitInformation) {
        this.workspace = workspace;
        this.initializing = true;
        this.workspaceInitInformation = workspaceInitInformation;
        this.bootSession = new DefaultNutsSession(workspace, this.workspaceInitInformation.getOptions());
        bootStdFd = detectAnsiTerminalSupport(NutsOsFamily.getCurrent());
        NutsTerminalMode terminalMode = workspaceInitInformation.getOptions().getTerminalMode();
        if (terminalMode == null) {
            if (workspaceInitInformation.getOptions().isBot()) {
                terminalMode = NutsTerminalMode.FILTERED;
            } else {
                if(bootStdFd.ansi) {
                    terminalMode = NutsTerminalMode.FORMATTED;
                }else{
                    terminalMode = NutsTerminalMode.FILTERED;
                }
            }
        }
        stdout =new NutsPrintStreamSystem(bootStdFd.out,null,null,bootStdFd.ansi,
                this.bootSession).setMode(terminalMode);
        stderr =new NutsPrintStreamSystem(bootStdFd.err,null,null,bootStdFd.ansi,
                this.bootSession).setMode(terminalMode);
        stdin=bootStdFd.in;
        DefaultSystemTerminal sys = new DefaultSystemTerminal(new DefaultNutsSystemTerminalBaseBoot(this));
        bootSession.setTerminal(new DefaultNutsSessionTerminalFromSystem(bootSession,sys));

        String[] properties = workspaceInitInformation.getOptions().getCustomOptions();
        if (properties != null) {
            for (String property : properties) {
                if (property != null) {
                    DefaultNutsArgument a = new DefaultNutsArgument(property);
                    if(a.isActive()) {
                        String key = a.getKey().getString();
                        this.customBootOptions.put(key, a.getValue());
                    }
                }
            }
        }
    }

    public CoreNutsWorkspaceInitInformation getInitOptions() {
        return workspaceInitInformation;
    }

    public NutsWorkspaceOptions getBootOptions() {
        return workspaceInitInformation.getOptions();
    }

    public StdFd getBootStdFd() {
        return bootStdFd;
    }

    @Override
    public boolean isInitializing() {
        return initializing;
    }

    public DefaultNutsBootModel setInitializing(boolean initializing) {
        this.initializing = initializing;
        return this;
    }

    public boolean isFirstBoot() {
        return firstBoot;
    }

    public DefaultNutsBootModel setFirstBoot(boolean firstBoot) {
        this.firstBoot = firstBoot;
        return this;
    }

    @Override
    public NutsPrintStream stdout() {
        return stdout;
    }

    @Override
    public NutsPrintStream stderr() {
        return stderr;
    }

    @Override
    public InputStream stdin() {
        return stdin;
    }

    public NutsSession bootSession() {
        return bootSession;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public static StdFd detectAnsiTerminalSupport(NutsOsFamily os) {
        if(System.console()!=null) {
            switch (os){
                case LINUX:
                case MACOS:
                case UNIX:{
                    return new StdFd(System.in,System.out,System.err,true);
                }
                case WINDOWS:{
                    if(CorePlatformUtils.IS_CYGWIN || CorePlatformUtils.IS_MINGW_XTERM){
                        List<String> flags=new ArrayList<>();
                        if(CorePlatformUtils.IS_CYGWIN){
                            flags.add("cygwin");
                        }
                        if(CorePlatformUtils.IS_MINGW_XTERM){
                            flags.add("mingw");
                        }
                        return new StdFd(System.in,System.out,System.err,true,flags.toArray(new String[0]));
                    }
                    if(OptionalJansi.isAvailable()){
                        return OptionalJansi.resolveStdFd();
                    }
                    return new StdFd(System.in,System.out,System.err,false);
                }
                default:{
                    if(OptionalJansi.isAvailable()){
                        return OptionalJansi.resolveStdFd();
                    }
                    return new StdFd(System.in,System.out,System.err,false);
                }
            }
        }
        return new StdFd(System.in,System.out,System.err,false);
    }

    public Map<String, NutsVal> getCustomBootOptions() {
        return customBootOptions;
    }
}
