package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DefaultNutsFetch implements NutsFetch {

    private NutsId id;
    private DefaultNutsWorkspace ws;
    private NutsSession session;
    private String location;
    private List<NutsDependencyScope> scopes = new ArrayList<>();
    private boolean includeDependencies = false;
    private boolean includeEffective = false;
    private boolean includeFile = true;
    private boolean includeInstallInformation = true;
    private boolean ignoreCache = false;
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

    @Override
    public NutsFetch addScope(NutsDependencyScope[] scopes) {
        if (scopes != null) {
            for (NutsDependencyScope scope : scopes) {
                if (scope != null) {
                    this.scopes.add(scope);
                }
            }
        }
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
    public NutsFetch setIgnoreCache() {
        return setIgnoreCache(true);
    }

    @Override
    public boolean isIncludeDependencies() {
        return includeDependencies;
    }

    @Override
    public NutsDefinition fetchDefinition() {
        NutsDefinition def = ws.fetchDefinition(id, location,includeFile || includeInstallInformation, includeEffective, includeInstallInformation, ignoreCache, session);
        loadDeps(def.getId());
        return def;
    }

    @Override
    public NutsContent fetchContent() {
        NutsDefinition def = ws.fetchDefinition(id, location,true, false, false, ignoreCache, session);
        loadDeps(def.getId());
        return def.getContent();
    }

    @Override
    public NutsId fetchId() {
        NutsDefinition def = ws.fetchDefinition(id, location,includeFile || includeInstallInformation, includeEffective, includeInstallInformation, ignoreCache, session);
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
        NutsDefinition def = ws.fetchDefinition(id, location,false, includeEffective, false, ignoreCache, session);
        loadDeps(def.getId());
        if (includeEffective) {
            return def.getEffectiveDescriptor();
        }
        return def.getDescriptor();
    }

    @Override
    public String fetchFile() {
        NutsDefinition def = ws.fetchDefinition(id, location,true, false, false, ignoreCache, session);
        return def.getContent().getFile();
    }

    private void loadDeps(NutsId id) {
        if (includeDependencies) {
            NutsDependencyScope[] s = (scopes == null || scopes.size() == 0) ?
                    new NutsDependencyScope[]{NutsDependencyScope.PROFILE_RUN}
                    : scopes.toArray(new NutsDependencyScope[0]);
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
}
