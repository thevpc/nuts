package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import net.thevpc.nuts.NutsVersion;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class WorkspaceAndApiVersion {
    private String workspace;
    private NutsVersion apiVersion;
    private Set<String> updatedPaths;
    private Set<String> nonUpdatedPaths;

    public WorkspaceAndApiVersion(String workspace, NutsVersion apiVersion, String[] updatedPaths, String[] nonUpdatedPaths) {
        this.workspace = workspace;
        this.apiVersion = apiVersion;
        this.updatedPaths = new HashSet<>();
        this.nonUpdatedPaths = new HashSet<>();
        if (updatedPaths != null) {
            this.updatedPaths.addAll(Arrays.asList(updatedPaths));
        }
        if (nonUpdatedPaths != null) {
            this.nonUpdatedPaths.addAll(Arrays.asList(nonUpdatedPaths));
        }
    }

    public String getWorkspace() {
        return workspace;
    }

    public NutsVersion getApiVersion() {
        return apiVersion;
    }

    public Set<String> getUpdatedPaths() {
        return updatedPaths;
    }

    public Set<String> getNonUpdatedPaths() {
        return nonUpdatedPaths;
    }
}
