package net.vpc.app.nuts.extensions.util;

import net.vpc.app.nuts.*;
import net.vpc.common.strings.StringUtils;

import java.util.Map;

public class DefaultNutsIdFormat implements NutsIdFormat {
    private NutsWorkspace ws;
    private boolean omitNamespace;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitEnv = true;
    private boolean omitFace = true;
    private boolean highlightImportedGroup;
    private boolean highlightScope;
    private boolean highlightOptional;

    public DefaultNutsIdFormat(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public boolean isOmitNamespace() {
        return omitNamespace;
    }

    @Override
    public NutsIdFormat setOmitNamespace(boolean omitNamespace) {
        this.omitNamespace = omitNamespace;
        return this;
    }

    @Override
    public boolean isOmitGroup() {
        return omitGroup;
    }

    @Override
    public NutsIdFormat setOmitGroup(boolean omitGroup) {
        this.omitGroup = omitGroup;
        return this;
    }

    @Override
    public boolean isOmitImportedGroup() {
        return omitImportedGroup;
    }

    @Override
    public NutsIdFormat setOmitImportedGroup(boolean omitImportedGroup) {
        this.omitImportedGroup = omitImportedGroup;
        return this;
    }

    @Override
    public boolean isOmitEnv() {
        return omitEnv;
    }

    @Override
    public NutsIdFormat setOmitEnv(boolean omitEnv) {
        this.omitEnv = omitEnv;
        return this;
    }

    @Override
    public boolean isOmitFace() {
        return omitFace;
    }

    @Override
    public NutsIdFormat setOmitFace(boolean omitFace) {
        this.omitFace = omitFace;
        return this;
    }

    @Override
    public boolean isHighlightImportedGroup() {
        return highlightImportedGroup;
    }

    @Override
    public NutsIdFormat setHighlightImportedGroup(boolean highlightImportedGroup) {
        this.highlightImportedGroup = highlightImportedGroup;
        return this;
    }

    @Override
    public boolean isHighlightScope() {
        return highlightScope;
    }

    @Override
    public NutsIdFormat setHighlightScope(boolean highlightScope) {
        this.highlightScope = highlightScope;
        return this;
    }

    @Override
    public boolean isHighlightOptional() {
        return highlightOptional;
    }

    @Override
    public NutsIdFormat setHighlightOptional(boolean highlightOptional) {
        this.highlightOptional = highlightOptional;
        return this;
    }

    public String format(NutsId id) {
        Map<String, String> m = id.getQueryMap();
        String scope = m.get("scope");
        String optional = m.get("optional");
        String classifier = m.get("classifier");
        NutsIdBuilder idBuilder = id.builder();
        if (omitEnv) {
            idBuilder.setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
        }
        if (omitFace) {
            idBuilder.setQueryProperty(NutsConstants.QUERY_FACE, null);
        }
        id = idBuilder.build();
        StringBuilder sb = new StringBuilder();
        if (!omitNamespace) {
            if (!StringUtils.isEmpty(id.getNamespace())) {
                sb.append(id.getNamespace()).append("://");
            }
        }
        if (!omitGroup) {
            if (!StringUtils.isEmpty(id.getGroup())) {
                boolean importedGroup = false;
                for (String anImport : ws.getConfigManager().getImports()) {
                    if (id.getGroup().equals(anImport)) {
                        importedGroup = true;
                        break;
                    }
                }
                if (!(importedGroup && omitImportedGroup)) {
                    if (importedGroup) {
                        sb.append("<<");
                        sb.append(ws.escapeText(id.getGroup()));
                        sb.append(">>");
                    } else {
                        sb.append(ws.escapeText(id.getGroup()));
                    }
                    sb.append(":");
                }
            }
        }
        sb.append("[[");
        sb.append(ws.escapeText(id.getName()));
        sb.append("]]");
        if (!StringUtils.isEmpty(id.getVersion().getValue())) {
            sb.append("#");
            sb.append(ws.escapeText(id.getVersion().toString()));
        }
        if (!StringUtils.isEmpty(id.getQuery())) {
            sb.append("?");
            sb.append(ws.escapeText(id.getQuery()));
        }

        if(!StringUtils.isEmpty(classifier)){
            sb.append(" ##");
            sb.append(ws.escapeText(classifier));
            sb.append("##");
        }

        if (highlightScope) {
            if (!StringUtils.isEmpty(scope)) {
                sb.append(" **");
                sb.append(ws.escapeText(scope));
                sb.append("**");
            }
        }
        if (highlightOptional) {
            if (!StringUtils.isEmpty(optional)) {
                if ("true".equals(optional)) {
                    optional = "optional";
                } else {
                    optional = "optional=" + optional;
                }
                sb.append(" **");
                sb.append(ws.escapeText(optional));
                sb.append("**");
            }
        }
        return sb.toString();
    }

}
