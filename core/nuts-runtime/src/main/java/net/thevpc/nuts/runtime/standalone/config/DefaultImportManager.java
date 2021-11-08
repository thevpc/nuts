package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import java.util.Set;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultImportManager implements NutsImportManager {

    private DefaultImportModel model;
    private NutsSession session;

    public DefaultImportManager(DefaultImportModel model) {
        this.model = model;
    }

    @Override
    public NutsImportManager addImports(String... importExpressions) {
        checkSession();
        model.add(importExpressions, session);
        return this;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NutsImportManager clearImports() {
        checkSession();
        model.removeAll(session);
        return this;
    }

    @Override
    public NutsImportManager removeImports(String... importExpressions) {
        checkSession();
        model.remove(importExpressions, session);
        return this;
    }

    @Override
    public NutsImportManager updateImports(String[] imports) {
        checkSession();
        model.set(imports, session);
        return this;
    }

    @Override
    public boolean isImportedGroupId(String groupId) {
        checkSession();
        return model.isImportedGroupId(groupId);
    }

    @Override
    public Set<String> getAllImports() {
        checkSession();
        return model.getAll();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public DefaultImportManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    public DefaultImportModel getModel() {
        return model;
    }

}
