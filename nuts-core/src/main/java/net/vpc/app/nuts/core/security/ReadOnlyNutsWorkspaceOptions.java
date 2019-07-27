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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.core.security;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

import net.vpc.app.nuts.NutsConfirmationMode;
import net.vpc.app.nuts.NutsDefaultWorkspaceOptions;
import net.vpc.app.nuts.NutsExecutionType;
import net.vpc.app.nuts.NutsLogConfig;
import net.vpc.app.nuts.NutsOsFamily;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsStoreLocationStrategy;
import net.vpc.app.nuts.NutsTerminalMode;
import net.vpc.app.nuts.NutsWorkspaceOpenMode;
import net.vpc.app.nuts.NutsWorkspaceOptions;
import net.vpc.app.nuts.NutsWorkspaceOptionsFormat;

/**
 *
 * @author vpc
 */
public class ReadOnlyNutsWorkspaceOptions implements NutsWorkspaceOptions {

    private final NutsWorkspaceOptions options;

    public ReadOnlyNutsWorkspaceOptions(NutsWorkspaceOptions options) {
        this.options = options;
    }

    @Override
    public NutsDefaultWorkspaceOptions copy() {
        return options.copy();
    }

    @Override
    public NutsWorkspaceOptionsFormat format() {
        return options.format();
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
    public long getCreationTime() {
        return options.getCreationTime();
    }

    @Override
    public boolean isDry() {
        return options.isDry();
    }

    @Override
    public String[] getExcludedExtensions() {
        return options.getExcludedExtensions();
    }

    @Override
    public String[] getExcludedRepositories() {
        return options.getExcludedRepositories();
    }

    @Override
    public NutsExecutionType getExecutionType() {
        return options.getExecutionType();
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
    public NutsWorkspaceOpenMode getOpenMode() {
        return options.getOpenMode();
    }

    @Override
    public NutsOutputFormat getOutputFormat() {
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
    public String[] getTransientRepositories() {
        return options.getTransientRepositories();
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
    public boolean isGlobal() {
        return options.isGlobal();
    }

    @Override
    public boolean isGui() {
        return options.isGui();
    }

    @Override
    public boolean isInherited() {
        return options.isInherited();
    }

    @Override
    public boolean isReadOnly() {
        return options.isReadOnly();
    }

    @Override
    public boolean isRecover() {
        return options.isRecover();
    }

    @Override
    public boolean isReset() {
        return options.isReset();
    }

    @Override
    public boolean isSkipCompanions() {
        return options.isSkipCompanions();
    }

    @Override
    public boolean isSkipWelcome() {
        return options.isSkipWelcome();
    }

    @Override
    public boolean isTrace() {
        return options.isTrace();
    }

}
