package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.NutsWorkspaceHelper;
import net.vpc.app.nuts.core.util.NutsWorkspaceUtils;

public class DefaultNutsUndeployCommand implements NutsUndeployCommand {

    private Object result;
    private NutsId id;
    private String repository;
    private boolean trace = true;
    private boolean offline = true;
    private boolean force = false;
    private boolean transitive = true;
    private NutsWorkspace ws;
    private NutsSession session;
    private NutsResultFormatType formatType = NutsResultFormatType.PLAIN;

    public DefaultNutsUndeployCommand(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public NutsId getId() {
        return id;
    }

    @Override
    public NutsUndeployCommand id(NutsId id) {
        return setId(id);
    }

    @Override
    public NutsUndeployCommand id(String id) {
        return setId(id);
    }

    @Override
    public NutsUndeployCommand setId(NutsId id) {
        this.id = id;
        invalidateResult();
        return this;
    }

    @Override
    public NutsUndeployCommand setId(String id) {
        return setId(CoreStringUtils.isBlank(id) ? null : ws.parser().parseRequiredId(id));
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public NutsUndeployCommand setRepository(String repository) {
        this.repository = repository;
        invalidateResult();
        return this;
    }

    @Override
    public boolean isTrace() {
        return trace;
    }

    @Override
    public NutsUndeployCommand setTrace(boolean trace) {
        this.trace = trace;
        invalidateResult();
        return this;
    }

    @Override
    public boolean isForce() {
        return force;
    }

    @Override
    public NutsUndeployCommand setForce(boolean force) {
        this.force = force;
        invalidateResult();
        return this;
    }

    @Override
    public boolean isTransitive() {
        return transitive;
    }

    @Override
    public NutsUndeployCommand setTransitive(boolean transitive) {
        this.transitive = transitive;
        invalidateResult();
        return this;
    }

    public NutsWorkspace getWs() {
        return ws;
    }

    public void setWs(NutsWorkspace ws) {
        this.ws = ws;
        invalidateResult();
    }

    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsUndeployCommand setSession(NutsSession session) {
        this.session = session;
        invalidateResult();
        return this;
    }

    @Override
    public NutsUndeployCommand repository(String repository) {
        return setRepository(repository);
    }

    @Override
    public NutsUndeployCommand session(NutsSession session) {
        return setSession(session);
    }

    @Override
    public NutsUndeployCommand force() {
        return setForce(true);
    }

    @Override
    public NutsUndeployCommand force(boolean force) {
        return setForce(force);
    }

    @Override
    public NutsUndeployCommand trace() {
        return setTrace(true);
    }

    @Override
    public NutsUndeployCommand trace(boolean trace) {
        return setTrace(trace);
    }

    @Override
    public NutsUndeployCommand transitive() {
        return setTransitive(true);
    }

    @Override
    public NutsUndeployCommand transitive(boolean transitive) {
        return setTransitive(transitive);
    }

    @Override
    public NutsUndeployCommand run() {
        NutsWorkspaceUtils.checkReadOnly(ws);

        NutsWorkspaceUtils.checkReadOnly(ws);
        session = NutsWorkspaceUtils.validateSession(ws, session);
        Path tempFile2 = null;
        NutsFetchCommand fetchOptions = ws.fetch().setTransitive(this.isTransitive());
        try {
            NutsDefinition p = ws.find()
                    .repositories(getRepository())
                    .setTransitive(isTransitive())
                    .setFetchStratery(isOffline() ? NutsFetchStrategy.LOCAL : NutsFetchStrategy.LOCAL)
                    .duplicateVersions(false)
                    .lenient(false)
                    .getResultDefinitions()
                    .item();
            NutsRepositorySession rsession = NutsWorkspaceHelper.createRepositorySession(session, p.getRepository(), NutsFetchMode.LOCAL, fetchOptions);
            p.getRepository().undeploy(new DefaultNutsRepositoryUndeploymentOptions()
                    .id(p.getId())
                    .force(isForce())
                    .trace(isTrace()),
                    rsession);
            result = true;
        } finally {
            if (tempFile2 != null) {
                try {
                    Files.delete(tempFile2);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        }
        return this;
    }

    @Override
    public NutsUndeployCommand formatType(NutsResultFormatType formatType) {
        return setFormatType(formatType);
    }

    @Override
    public NutsUndeployCommand setFormatType(NutsResultFormatType formatType) {
        if (formatType == null) {
            formatType = NutsResultFormatType.PLAIN;
        }
        this.formatType = formatType;
        return this;
    }

    @Override
    public NutsUndeployCommand json() {
        return setFormatType(NutsResultFormatType.JSON);
    }

    @Override
    public NutsUndeployCommand plain() {
        return setFormatType(NutsResultFormatType.PLAIN);
    }

    @Override
    public NutsUndeployCommand props() {
        return setFormatType(NutsResultFormatType.PROPS);
    }

    @Override
    public NutsResultFormatType getFormatType() {
        return this.formatType;
    }

    public boolean isOffline() {
        return offline;
    }

    public NutsUndeployCommand setOffline(boolean offline) {
        this.offline = offline;
        invalidateResult();
        return this;
    }

    public NutsUndeployCommand offline() {
        return offline(true);
    }

    public NutsUndeployCommand offline(boolean offline) {
        return setOffline(offline);
    }

    private void invalidateResult() {
        result = null;
    }
}
