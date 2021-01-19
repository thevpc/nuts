package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultImportManager implements NutsImportManager {
    private NutsWorkspace ws;
    private Set<String> cachedImports;

    public DefaultImportManager(NutsWorkspace ws) {
        this.ws = ws;
    }

    public void invalidateCache(){
        cachedImports=null;
    }

    public Set<String> getCachedImports() {
        return cachedImports;
    }

    @Override
    public void add(String[] importExpressions, NutsAddOptions options) {
        options = CoreNutsUtils.validate(options, ws);
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
        set(arr, CoreNutsUtils.toUpdateOptions(options));
    }

    @Override
    public void removeAll(NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        set(null, CoreNutsUtils.toUpdateOptions(options));
    }

    @Override
    public void remove(String[] importExpressions, NutsRemoveOptions options) {
        options = CoreNutsUtils.validate(options, ws);
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
            set(arr, CoreNutsUtils.toUpdateOptions(options));
        }
    }

    @Override
    public void set(String[] imports, NutsUpdateOptions options) {
        options = CoreNutsUtils.validate(options, ws);
        Set<String> simports = new LinkedHashSet<>();
        if (imports != null) {
            for (String s : imports) {
                simports.addAll(parseImports(s));
            }
        }
        getStoreModelMain().setImports(new ArrayList<>(simports));
        NutsWorkspaceConfigManagerExt.of(ws.config()).fireConfigurationChanged("import", options.getSession(), ConfigEventType.MAIN);
    }

    @Override
    public boolean isImportedGroupId(String groupId) {
        return getAll().contains(groupId);
    }

    @Override
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
    NutsWorkspaceConfigMain getStoreModelMain(){
        return ((DefaultNutsWorkspaceConfigManager)ws.config()).getStoreModelMain();
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


}
