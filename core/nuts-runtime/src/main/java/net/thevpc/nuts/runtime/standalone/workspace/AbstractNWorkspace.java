/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import net.thevpc.nuts.boot.NBootWorkspaceImpl;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.reserved.NScopedWorkspace;
import net.thevpc.nuts.runtime.standalone.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.runtime.standalone.event.DefaultNWorkspaceEventModel;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NObservableMapListener;
import net.thevpc.nuts.util.NRunnable;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by vpc on 1/6/17.
 */
public abstract class AbstractNWorkspace implements NWorkspace {
    protected NBootOptionsInfo callerBootOptionsInfo;

    public AbstractNWorkspace(NBootOptionsInfo callerBootOptionsInfo) {
        this.callerBootOptionsInfo=callerBootOptionsInfo;
    }

    @Override
    public void runWith(NRunnable runnable) {
        NScopedWorkspace.runWith(this,runnable);
    }

    @Override
    public <T> T callWith(NCallable<T> callable) {
        return NScopedWorkspace.callWith(this,callable);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext criteria) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public String toString() {
        return "NutsWorkspace{"
                + getName()
                + '}';
    }

    @Override
    public void close() {

    }

    @Override
    public NWorkspace share() {
        NScopedWorkspace.setSharedWorkspaceInstance(this);
        return this;
    }

    @Override
    public NWorkspace setSharedInstance() {
        NScopedWorkspace.setSharedWorkspaceInstance(this);
        return this;
    }

    @Override
    public boolean isSharedInstance() {
        return NScopedWorkspace.getSharedWorkspaceInstance()==this;
    }

    /// //////////////////////////////

    private DefaultNWorkspaceEventModel eventsModel() {
        return ((NWorkspaceExt)this).getModel().eventsModel;
    }

    @Override
    public NWorkspace removeRepositoryListener(NRepositoryListener listener) {
        eventsModel().removeRepositoryListener(listener);
        return this;
    }

    @Override
    public NWorkspace addRepositoryListener(NRepositoryListener listener) {
        eventsModel().addRepositoryListener(listener);
        return this;
    }

    @Override
    public List<NRepositoryListener> getRepositoryListeners() {
        return eventsModel().getRepositoryListeners();
    }

    @Override
    public NWorkspace addUserPropertyListener(NObservableMapListener<String, Object> listener) {
        eventsModel().addUserPropertyListener(listener);
        return this;
    }

    @Override
    public NWorkspace removeUserPropertyListener(NObservableMapListener<String, Object> listener) {
        eventsModel().removeUserPropertyListener(listener);
        return this;
    }

    @Override
    public List<NObservableMapListener<String, Object>> getUserPropertyListeners() {
        return eventsModel().getUserPropertyListeners();
    }

    @Override
    public NWorkspace removeWorkspaceListener(NWorkspaceListener listener) {
        eventsModel().removeWorkspaceListener(listener);
        return this;
    }

    @Override
    public NWorkspace addWorkspaceListener(NWorkspaceListener listener) {
        eventsModel().addWorkspaceListener(listener);
        return this;
    }

    @Override
    public List<NWorkspaceListener> getWorkspaceListeners() {
        return eventsModel().getWorkspaceListeners();
    }

    @Override
    public NWorkspace removeInstallListener(NInstallListener listener) {
        eventsModel().removeInstallListener(listener);
        return this;
    }

    @Override
    public NWorkspace addInstallListener(NInstallListener listener) {
        eventsModel().addInstallListener(listener);
        return this;
    }

    @Override
    public List<NInstallListener> getInstallListeners() {
        return eventsModel().getInstallListeners();
    }


    @Override
    public void runBootCommand() {
        runWith(() -> {
            NBootOptions info2=new DefaultNBootOptionsBuilder(callerBootOptionsInfo).build();
            NApp.of().setId(getApiId());
            NLogOp logOp = NLog.of(NBootWorkspaceImpl.class).with().level(Level.CONFIG);
            logOp.verb(NLogVerb.SUCCESS).log(NMsg.ofC("running workspace in %s mode", getRunModeString(info2)));
            NExecCmd execCmd = NExecCmd.of()
                    .setExecutionType(info2.getExecutionType().orNull())
                    .setRunAs(info2.getRunAs().orNull())
                    .failFast();
            List<String> executorOptions = info2.getExecutorOptions().orNull();
            if (executorOptions != null) {
                execCmd.configure(true, executorOptions.toArray(new String[0]));
            }
            NCmdLine executorOptionsCmdLine = NCmdLine.of(executorOptions).setExpandSimpleOptions(false);
            while (executorOptionsCmdLine.hasNext()) {
                execCmd.configureLast(executorOptionsCmdLine);
            }
            if (info2.getApplicationArguments().get().isEmpty()) {
                if (info2.getSkipWelcome().orElse(false)) {
                    return;
                }
                execCmd.addCommand("welcome");
            } else {
                execCmd.addCommand(info2.getApplicationArguments().get());
            }
            execCmd.run();
        });
    }

    private String getRunModeString(NBootOptions options) {
        if (options.getReset().orElse(false)) {
            return "reset";
        } else if (options.getRecover().orElse(false)) {
            return "recover";
        } else {
            return "exec";
        }
    }
}
