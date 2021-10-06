package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import net.thevpc.nuts.runtime.standalone.DefaultNutsWorkspace;

public class DefaultImportModel {

    private NutsWorkspace workspace;
    private Set<String> cachedImports;

    public DefaultImportModel(NutsWorkspace ws) {
        this.workspace = ws;
    }

    public void invalidateCache() {
        cachedImports = null;
    }

    public Set<String> getCachedImports() {
        return cachedImports;
    }

    public void add(String[] importExpressions, NutsSession session) {
        Set<String> imports = new LinkedHashSet<>();
        if (getStoreModelMain().getImports() != null) {
            imports.addAll(getStoreModelMain().getImports());
        }
        if (importExpressions != null) {
            for (String importExpression : importExpressions) {
                if (importExpression != null) {
                    for (String s : importExpression.split("[,;: ]")) {
                        imports.add(s.trim());
                    }
                }
            }
        }
        String[] arr = imports.toArray(new String[0]);
//        Arrays.sort(arr);
        set(arr, session);
    }

    public void removeAll(NutsSession session) {
        set(null, session);
    }

    public void remove(String[] importExpressions, NutsSession session) {
        if (getStoreModelMain().getImports() != null) {
            Set<String> imports = new LinkedHashSet<>();
            for (String importExpression : getStoreModelMain().getImports()) {
                imports.addAll(parseImports(importExpression));
            }
            if (importExpressions != null) {
                for (String importExpression : importExpressions) {
                    imports.removeAll(parseImports(importExpression));
                }
            }
            String[] arr = imports.toArray(new String[0]);
//        Arrays.sort(arr);
            set(arr, session);
        }
    }

    public void set(String[] imports, NutsSession session) {
        Set<String> simports = new LinkedHashSet<>();
        if (imports != null) {
            for (String s : imports) {
                simports.addAll(parseImports(s));
            }
        }
        getStoreModelMain().setImports(new ArrayList<>(simports));
        NutsWorkspaceConfigManagerExt.of(session.config())
                .getModel()
                .fireConfigurationChanged("import", session, ConfigEventType.MAIN);
    }

    public boolean isImportedGroupId(String groupId) {
        return getAll().contains(groupId);
    }

    public Set<String> getAll() {
        if (cachedImports == null) {
            Set<String> all = new LinkedHashSet<>();
            if (getStoreModelMain().getImports() != null) {
                all.addAll(getStoreModelMain().getImports());
            }
            return cachedImports = Collections.unmodifiableSet(all);
        }
        return cachedImports;
    }

    NutsWorkspaceConfigMain getStoreModelMain() {
        return ((DefaultNutsWorkspace) workspace).getConfigModel().getStoreModelMain();
    }

    protected Set<String> parseImports(String importExpression) {
        Set<String> imports = new LinkedHashSet<>();
        if (importExpression != null) {
            for (String s : importExpression.split("[,;: \t\n]")) {
                if (!s.isEmpty()) {
                    imports.add(s.trim());
                }
            }
        }
        return imports;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    
}
