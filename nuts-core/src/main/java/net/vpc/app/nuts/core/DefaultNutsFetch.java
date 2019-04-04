package net.vpc.app.nuts.core;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.*;

public class DefaultNutsFetch extends DefaultNutsQueryBaseOptions<NutsFetch> implements NutsFetch {

    private final DefaultNutsWorkspace ws;
    private NutsId id;

    public DefaultNutsFetch(DefaultNutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsFetch setId(String id) {
        this.id = ws.parser().parseRequiredId(id);
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
    public NutsDefinition fetchDefinition() {
        NutsDefinition def = ws.fetchDefinition(id, toOptions(), getSession());
        loadDeps(def.getId());
        return def;
    }

    @Override
    public NutsQueryOptions toOptions() {
        return new DefaultNutsQueryOptions().copyFrom(this);
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
    public Path fetchFileOrNull() {
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

        NutsDefinition def = ws.fetchDefinition(id, toOptions().setIncludeFile(true).setIncludeEffective(false).setIncludeInstallInformation(false), getSession());
        loadDeps(def.getId());
        return def.getContent();
    }

    @Override
    public NutsId fetchId() {
        NutsDefinition def = ws.fetchDefinition(id, toOptions(), getSession());
        loadDeps(def.getId());
        if (isIncludeEffective()) {
            return ws.resolveEffectiveId(def.getEffectiveDescriptor(), toOptions(), getSession());
        }
        return def.getId();
    }

    @Override
    public String fetchContentHash() {
        Path f = fetchDefinition().getContent().getPath();
        try {
            try (InputStream in = Files.newInputStream(f)) {
                return ws.io().computeHash((in));
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String fetchDescriptorHash() {
        NutsDescriptor d = fetchDescriptor();
        return ws.io().computeHash(new ByteArrayInputStream(
                ws.formatter().createDescriptorFormat().format(d).getBytes()
        ));
    }

    @Override
    public NutsDescriptor fetchDescriptor() {

        NutsDefinition def = ws.fetchDefinition(id, toOptions().setIncludeFile(false).setIncludeInstallInformation(false), getSession());
        loadDeps(def.getId());
        if (isIncludeEffective()) {
            return def.getEffectiveDescriptor();
        }
        return def.getDescriptor();
    }

    @Override
    public Path fetchFile() {

        NutsDefinition def = ws.fetchDefinition(id, toOptions().setIncludeFile(true).setIncludeEffective(false).setIncludeInstallInformation(false), getSession());
        return def.getContent().getPath();
    }

    private void loadDeps(NutsId id) {
        if (isIncludeDependencies()) {
            NutsDependencyScope[] s = (getScope() == null || getScope().isEmpty())
                    ? new NutsDependencyScope[]{NutsDependencyScope.PROFILE_RUN}
                    : getScope().toArray(new NutsDependencyScope[0]);
            ws.createQuery().addId(id).setSession(getSession()).setFetchStratery(getFetchStrategy())
                    .addScope(s)
                    .setAcceptOptional(getAcceptOptional())
                    .dependenciesOnly().fetch();

        }
    }

    @Override
    public NutsFetch copy() {
        DefaultNutsFetch b = new DefaultNutsFetch(ws);
        b.copyFrom(this);
        return b;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsFetch copyFrom(NutsFetch other) {
        super.copyFrom(other);
        if (other != null) {
            NutsFetch o = other;
            this.id = o.getId();
        }
        return this;
    }
}
