package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import java.util.Set;

import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.spi.NSupportLevelContext;

public class DefaultNImports implements NImports {

    private DefaultImportModel model;

    public DefaultNImports(NWorkspace workspace) {
        NWorkspaceExt e = NWorkspaceExt.of();
        this.model = e.getModel().importModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public NImports addImports(String... importExpressions) {
        model.add(importExpressions);
        return this;
    }


    @Override
    public NImports clearImports() {
        model.removeAll();
        return this;
    }

    @Override
    public NImports removeImports(String... importExpressions) {
        model.remove(importExpressions);
        return this;
    }

    @Override
    public NImports updateImports(String[] imports) {
        model.set(imports);
        return this;
    }

    @Override
    public boolean isImportedGroupId(String groupId) {
        return model.isImportedGroupId(groupId);
    }

    @Override
    public Set<String> getAllImports() {
        return model.getAll();
    }

    public DefaultImportModel getModel() {
        return model;
    }

}
