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
package net.thevpc.nuts.runtime.standalone.security;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.CoreNutsWorkspaceOptions;

import java.io.InputStream;
import java.io.PrintStream;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;

/**
 * @author thevpc
 */
public class ReadOnlyNutsWorkspaceOptions implements NutsWorkspaceOptions {

    private final NutsWorkspaceOptions options;
    private final NutsSession session;

    public ReadOnlyNutsWorkspaceOptions(NutsWorkspaceOptions options, NutsSession session) {
        this.options = options;
        this.session = session;
    }

    @Override
    public NutsWorkspaceOptionsFormat formatter() {
        return options.formatter();
    }

    @Override
    public String getApiVersion() {
        return options.getApiVersion();
    }

    @Override
    public String[] getApplicationArguments() {
        return options.getApplicationArguments();
    }

    @Override
    public String getArchetype() {
        return options.getArchetype();
    }

    @Override
    public Supplier<ClassLoader> getClassLoaderSupplier() {
        return options.getClassLoaderSupplier();
    }

    @Override
    public NutsConfirmationMode getConfirm() {
        return options.getConfirm();
    }

    @Override
    public boolean isDry() {
        return options.isDry();
    }

    @Override
    public Boolean getDry() {
        return options.getDry();
    }

    @Override
    public long getCreationTime() {
        return options.getCreationTime();
    }

    @Override
    public String[] getExcludedExtensions() {
        return options.getExcludedExtensions();
    }

    //    @Override
//    public String[] getExcludedRepositories() {
//        return options.getExcludedRepositories();
//    }
    @Override
    public NutsExecutionType getExecutionType() {
        return options.getExecutionType();
    }

    @Override
    public NutsRunAs getRunAs() {
        return options.getRunAs();
    }

    @Override
    public String[] getExecutorOptions() {
        String[] v = options.getExecutorOptions();
        return v == null ? null : Arrays.copyOf(v, v.length);
    }

    @Override
    public String getHomeLocation(NutsOsFamily layout, NutsStoreLocation location) {
        return options.getHomeLocation(layout, location);
    }

    @Override
    public Map<String, String> getHomeLocations() {
        Map<String, String> v = options.getHomeLocations();
        return v == null ? null : Collections.unmodifiableMap(v);
    }

    @Override
    public String getJavaCommand() {
        return options.getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
        return options.getJavaOptions();
    }

    @Override
    public NutsLogConfig getLogConfig() {
        return options.getLogConfig();
    }

    @Override
    public String getName() {
        return options.getName();
    }

    @Override
    public NutsOpenMode getOpenMode() {
        return options.getOpenMode();
    }

    @Override
    public NutsContentType getOutputFormat() {
        return options.getOutputFormat();
    }

    @Override
    public String[] getOutputFormatOptions() {
        return options.getOutputFormatOptions();
    }

    @Override
    public char[] getCredentials() {
        return options.getCredentials();
    }

    @Override
    public NutsStoreLocationStrategy getRepositoryStoreLocationStrategy() {
        return options.getRepositoryStoreLocationStrategy();
    }

    @Override
    public String getRuntimeId() {
        return options.getRuntimeId();
    }

    @Override
    public String getStoreLocation(NutsStoreLocation folder) {
        return options.getStoreLocation(folder);
    }

    @Override
    public NutsOsFamily getStoreLocationLayout() {
        return options.getStoreLocationLayout();
    }

    @Override
    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return options.getStoreLocationStrategy();
    }

    @Override
    public Map<String, String> getStoreLocations() {
        return options.getStoreLocations();
    }

    @Override
    public NutsTerminalMode getTerminalMode() {
        return options.getTerminalMode();
    }

    @Override
    public String[] getRepositories() {
        return options.getRepositories();
    }

    @Override
    public String getUserName() {
        return options.getUserName();
    }

    @Override
    public String getWorkspace() {
        return options.getWorkspace();
    }

    @Override
    public boolean isDebug() {
        return options.isDebug();
    }

    @Override
    public Boolean getDebug() {
        return options.getDebug();
    }

    @Override
    public boolean isGlobal() {
        return options.isGlobal();
    }

    @Override
    public Boolean getGlobal() {
        return options.getGlobal();
    }

    @Override
    public boolean isGui() {
        return options.isGui();
    }

    @Override
    public Boolean getGui() {
        return options.getGui();
    }

    @Override
    public boolean isInherited() {
        return options.isInherited();
    }

    @Override
    public Boolean getInherited() {
        return options.getInherited();
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public Boolean getReadOnly() {
        return options.getReadOnly();
    }

    @Override
    public boolean isRecover() {
        return options.isRecover();
    }

    @Override
    public Boolean getRecover() {
        return options.getRecover();
    }

    @Override
    public boolean isReset() {
        return options.isReset();
    }

    @Override
    public Boolean getReset() {
        return options.getReset();
    }

    @Override
    public boolean isCommandVersion() {
        return options.isCommandVersion();
    }

    @Override
    public Boolean getCommandVersion() {
        return options.getCommandVersion();
    }

    @Override
    public boolean isCommandHelp() {
        return options.isCommandHelp();
    }

    @Override
    public Boolean getCommandHelp() {
        return options.getCommandHelp();
    }
//
//    @Override
//    public String getBootRepositories() {
//        return options.getBootRepositories();
//    }

    @Override
    public boolean isSkipCompanions() {
        return options.isSkipCompanions();
    }

    @Override
    public Boolean getSkipCompanions() {
        return options.getSkipCompanions();
    }

    @Override
    public boolean isSkipWelcome() {
        return options.isSkipWelcome();
    }

    @Override
    public Boolean getSkipWelcome() {
        return options.getSkipWelcome();
    }

    @Override
    public String getOutLinePrefix() {
        return options.getOutLinePrefix();
    }

    @Override
    public String getErrLinePrefix() {
        return options.getErrLinePrefix();
    }

    @Override
    public boolean isSkipBoot() {
        return options.isSkipBoot();
    }

    @Override
    public Boolean getSkipBoot() {
        return options.getSkipBoot();
    }

    @Override
    public boolean isTrace() {
        return options.isTrace();
    }

    @Override
    public Boolean getTrace() {
        return options.getTrace();
    }

    @Override
    public String getProgressOptions() {
        return options.getProgressOptions();
    }

    @Override
    public boolean isCached() {
        return options.isCached();
    }

    @Override
    public Boolean getCached() {
        return options.getCached();
    }

    @Override
    public boolean isIndexed() {
        return options.isIndexed();
    }

    @Override
    public Boolean getIndexed() {
        return options.getIndexed();
    }

    @Override
    public boolean isTransitive() {
        return options.isTransitive();
    }

    @Override
    public Boolean getTransitive() {
        return options.getTransitive();
    }

    @Override
    public boolean isBot() {
        return options.isBot();
    }

    @Override
    public Boolean getBot() {
        return options.getBot();
    }

    @Override
    public NutsFetchStrategy getFetchStrategy() {
        return options.getFetchStrategy();
    }

    @Override
    public InputStream getStdin() {
        return options.getStdin();
    }

    @Override
    public PrintStream getStdout() {
        return options.getStdout();
    }

    @Override
    public PrintStream getStderr() {
        return options.getStderr();
    }

    @Override
    public ExecutorService getExecutorService() {
        return options.getExecutorService();
    }

    @Override
    public Instant getExpireTime() {
        return options.getExpireTime();
    }

    @Override
    public boolean isSkipErrors() {
        return options.isSkipErrors();
    }

    @Override
    public Boolean getSkipErrors() {
        return options.getSkipErrors();
    }

    @Override
    public boolean isSwitchWorkspace() {
        return options.isSwitchWorkspace();
    }

    @Override
    public Boolean getSwitchWorkspace() {
        return options.getSwitchWorkspace();
    }

    @Override
    public NutsMessage[] getErrors() {
        return options.getErrors();
    }

    @Override
    public String[] getProperties() {
        return options.getProperties();
    }

    @Override
    public String getLocale() {
        return options.getLocale();
    }

    @Override
    public String getTheme() {
        return options.getTheme();
    }

    @Override
    public NutsWorkspaceOptionsBuilder builder() {
        return new CoreNutsWorkspaceOptions(session).setAll(this);
    }
}
