package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class DefaultNutsFetch implements NutsFetch {

    private NutsId id;
    private final DefaultNutsWorkspace ws;
    private NutsSession session;
    private String location;
    private Set<NutsDependencyScope> scope = EnumSet.noneOf(NutsDependencyScope.class);
    private boolean includeDependencies = false;
    private boolean includeEffective = false;
    private boolean includeFile = true;
    private boolean includeInstallInformation = true;
    private boolean ignoreCache = false;
    private boolean preferInstalled = false;
    private boolean installedOnly = false;
    private Boolean acceptOptional = null;

    public DefaultNutsFetch(DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsFetch setId(String id) {
        this.id = ws.getParseManager().parseRequiredId(id);
        return this;
    }

    @Override
    public NutsFetch setId(NutsId id) {
        if (id == null) {
            throw new NutsParseException("Invalid Id format : null");
        }
        this.id = id;
        return this;
    }

    @Override
    public NutsFetch includeDependencies() {
        return this.includeDependencies(true);
    }

    @Override
    public NutsFetch includeDependencies(boolean include) {
        this.includeDependencies = include;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsFetch setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public Boolean getAcceptOptional() {
        return acceptOptional;
    }

    @Override
    public NutsFetch setAcceptOptional(Boolean acceptOptional) {
        this.acceptOptional = acceptOptional;
        return this;
    }

    @Override
    public NutsFetch setIncludeOptional(boolean includeOptional) {
        return setAcceptOptional(includeOptional ? null : false);
    }

    public boolean isIgnoreCache() {
        return ignoreCache;
    }

    @Override
    public NutsFetch setIgnoreCache(boolean ignoreCache) {
        this.ignoreCache = ignoreCache;
        return this;
    }

    @Override
    public NutsFetch setIncludeDependencies(boolean includeDependencies) {
        this.includeDependencies = includeDependencies;
        return this;
    }

    public boolean isIncludeEffective() {
        return includeEffective;
    }

    @Override
    public NutsFetch setIncludeEffective(boolean includeEffective) {
        this.includeEffective = includeEffective;
        return this;
    }

    public boolean isIncludeFile() {
        return includeFile;
    }

    @Override
    public NutsFetch setIncludeFile(boolean includeFile) {
        this.includeFile = includeFile;
        return this;
    }

    public boolean isIncludeInstallInformation() {
        return includeInstallInformation;
    }

    @Override
    public NutsFetch setIncludeInstallInformation(boolean includeInstallInformation) {
        this.includeInstallInformation = includeInstallInformation;
        return this;
    }

    @Override
    public NutsFetch ignoreCache() {
        return setIgnoreCache(true);
    }

    @Override
    public boolean isIncludeDependencies() {
        return includeDependencies;
    }

    @Override
    public NutsDefinition fetchDefinition() {
        NutsDefinition def = ws.fetchDefinition(id, creationFetchOptions(), session);
        loadDeps(def.getId());
        return def;
    }

    private DefaultFetchOptions creationFetchOptions() {
        return new DefaultFetchOptions()
                .setCopyTo(location)
                .setContent(isIncludeFile() || isIncludeInstallInformation())
                .setEffectiveDesc(isIncludeEffective())
                .setInstallInfo(isIncludeInstallInformation())
                .setIgnoreCache(isIgnoreCache())
                .setPreferInstalled(isPreferInstalled())
                .setInstalledOnly(isInstalledOnly());
    }

    @Override
    public NutsDefinition fetchDefinitionOrNull() {
        try {
            return fetchDefinition();
        } catch (NutsNotFoundException ex) {
            return null;
        }
    }

    @Override
    public NutsContent fetchContentOrNull() {
        try {
            return fetchContent();
        } catch (NutsNotFoundException ex) {
            return null;
        }
    }

    @Override
    public NutsId fetchIdOrNull() {
        try {
            return fetchId();
        } catch (NutsNotFoundException ex) {
            return null;
        }
    }

    @Override
    public String fetchFileOrNull() {
        try {
            return fetchFile();
        } catch (NutsNotFoundException ex) {
            return null;
        }
    }

    @Override
    public NutsDescriptor fetchDescriptorOrNull() {
        try {
            return fetchDescriptor();
        } catch (NutsNotFoundException ex) {
            return null;
        }
    }

    @Override
    public NutsContent fetchContent() {

        NutsDefinition def = ws.fetchDefinition(id, creationFetchOptions().setContent(true).setEffectiveDesc(false).setInstallInfo(false), session);
        loadDeps(def.getId());
        return def.getContent();
    }

    @Override
    public NutsId fetchId() {
        NutsDefinition def = ws.fetchDefinition(id, creationFetchOptions(), session);
        loadDeps(def.getId());
        if (includeEffective) {
            return ws.resolveEffectiveId(def.getEffectiveDescriptor(), session);
        }
        return def.getId();
    }

    @Override
    public String fetchContentHash() {
        String f = fetchDefinition().getContent().getFile();
        FileInputStream is = null;
        try {
            try {
                return ws.getIOManager().computeHash((is = new FileInputStream(f)));
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(ex);
        }
    }

    @Override
    public String fetchDescriptorHash() {
        NutsDescriptor d = fetchDescriptor();
        return ws.getIOManager().computeHash(new ByteArrayInputStream(
                ws.getFormatManager().createDescriptorFormat().format(d).getBytes()
        ));
    }

    @Override
    public NutsDescriptor fetchDescriptor() {

        NutsDefinition def = ws.fetchDefinition(id, creationFetchOptions().setContent(false).setInstallInfo(false), session);
        loadDeps(def.getId());
        if (includeEffective) {
            return def.getEffectiveDescriptor();
        }
        return def.getDescriptor();
    }

    @Override
    public String fetchFile() {

        NutsDefinition def = ws.fetchDefinition(id, creationFetchOptions().setContent(true).setEffectiveDesc(false).setInstallInfo(false), session);
        return def.getContent().getFile();
    }

    private void loadDeps(NutsId id) {
        if (includeDependencies) {
            NutsDependencyScope[] s = (scope == null || scope.isEmpty())
                    ? new NutsDependencyScope[]{NutsDependencyScope.PROFILE_RUN}
                    : scope.toArray(new NutsDependencyScope[0]);
            ws.createQuery().addId(id).setSession(session)
                    .addScope(s)
                    .setAcceptOptional(acceptOptional)
                    .dependenciesOnly().fetch();

        }
    }

    public String getLocation() {
        return location;
    }

    @Override
    public NutsFetch setLocation(String location) {
        this.location = location;
        return this;
    }

    @Override
    public NutsFetch setDefaultLocation() {
        this.location = null;
        return this;
    }

    @Override
    public boolean isPreferInstalled() {
        return preferInstalled;
    }

    @Override
    public NutsFetch setPreferInstalled(boolean preferInstalled) {
        this.preferInstalled = preferInstalled;
        return this;
    }

    @Override
    public boolean isInstalledOnly() {
        return installedOnly;
    }

    @Override
    public NutsFetch setInstalledOnly(boolean installedOnly) {
        this.installedOnly = installedOnly;
        return this;
    }

    @Override
    public NutsFetch setScope(NutsDependencyScope scope) {
        return setScope(scope == null ? null : EnumSet.of(scope));
    }

    @Override
    public NutsFetch setScope(NutsDependencyScope... scope) {
        return setScope(scope == null ? null : EnumSet.<NutsDependencyScope>copyOf(Arrays.asList(scope)));
    }

    @Override
    public NutsFetch setScope(Collection<NutsDependencyScope> scope) {
        this.scope = scope == null ? EnumSet.noneOf(NutsDependencyScope.class) : EnumSet.<NutsDependencyScope>copyOf(scope);
        return this;
    }

    @Override
    public NutsFetch addScope(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return this;
    }

    @Override
    public NutsFetch addScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return this;
    }

    @Override
    public NutsFetch addScope(NutsDependencyScope... scope) {
        this.scope = NutsDependencyScope.add(this.scope, scope);
        return this;
    }

    @Override
    public NutsFetch removeScope(Collection<NutsDependencyScope> scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
        return this;
    }

    @Override
    public NutsFetch removeScope(NutsDependencyScope scope) {
        this.scope = NutsDependencyScope.remove(this.scope, scope);
        return this;
    }
}
