package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import java.util.Set;

import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultImportManager implements NImportManager {

    private DefaultImportModel model;
    private NSession session;

    public DefaultImportManager(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().importModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public NImportManager addImports(String... importExpressions) {
        checkSession();
        model.add(importExpressions, session);
        return this;
    }

    private void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public NImportManager clearImports() {
        checkSession();
        model.removeAll(session);
        return this;
    }

    @Override
    public NImportManager removeImports(String... importExpressions) {
        checkSession();
        model.remove(importExpressions, session);
        return this;
    }

    @Override
    public NImportManager updateImports(String[] imports) {
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
    public NSession getSession() {
        return session;
    }

    @Override
    public DefaultImportManager setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    public DefaultImportModel getModel() {
        return model;
    }

}
