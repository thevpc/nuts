package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NInstallStatus;
import net.thevpc.nuts.command.NSearchCmd;
import net.thevpc.nuts.core.NRepositoryFilters;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.NCopiable;

import java.util.*;

public class InstallIdCacheItem {
    public NId id;
    public boolean optional;
    public NDefinition definition;
    public List<NDependency> dependencies;
    public NDescriptor effectiveDescriptor;
    public NInstallStatus oldInstallStatus;
    public NInstallStatus currentInstallStatus;

    public static NId normalizeId(NDependency id) {
        return id.toId().getLongId();
    }
    public static NId normalizeId(NId id) {
        return id.getLongId();
    }

    public InstallIdCacheItem(NId id) {
        this.id = normalizeId(id);
        this.optional=id.toDependency().isOptional();
    }


    public List<NDependency> getDependencies() {
        if (dependencies == null) {
            return getDefinition().getDependencies().get().transitive().toList();
        }
        return dependencies;
    }

    public NInstallStatus getOldInstallStatus() {
        if (oldInstallStatus == null) {
            oldInstallStatus = NWorkspaceExt.of().getInstalledRepository().getInstallStatus(id);
        }
        return oldInstallStatus;
    }

    public InstallIdCacheItem setCurrentInstallStatus(NInstallStatus currentInstallStatus) {
        this.currentInstallStatus = currentInstallStatus;
        return this;
    }

    public NInstallStatus getInstallStatus() {
        if (currentInstallStatus == null) {
            return getOldInstallStatus();
        }
        return currentInstallStatus;
    }

    public NDescriptor getEffectiveDescriptor() {
        if (effectiveDescriptor == null) {
            effectiveDescriptor = getDefinition().getEffectiveDescriptor().get();
        }
        return effectiveDescriptor;
    }

    public NPath getContent() {
        if (!getDefinition().getDescriptor().isNoContent()) {
            return getDefinition().getContent().get();
        }
        return null;
    }

    public NDefinition getDefinition() {
        if (definition == null) {
            definition = NSearchCmd.of(id)
                    .failFast()
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .latest()
                    .getResultDefinitions()
                    .findFirst().get();

        }
        return definition;
    }

    public void revalidate(NDefinition definition) {
        this.definition = definition;
        effectiveDescriptor = null;
        dependencies = null;
    }

    public void revalidate(boolean force) {
        if(force){
            definition =NSession.of().copy()
                    .setCached(false) // disable cache
                    .callWith(()-> NSearchCmd.of(id)
                            .failFast()
                            .setRepositoryFilter(NRepositoryFilters.of().installedRepo().neg())
                            .setDependencyFilter(NDependencyFilters.of().byRunnable())
                            .latest()
                            .getResultDefinitions()
                            .findFirst().get());

        }else {
            definition = NSearchCmd.of(id)
                    .failFast()
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .latest()
                    .getResultDefinitions()
                    .findFirst().get();
        }
        effectiveDescriptor = null;
        dependencies = null;
    }
}
