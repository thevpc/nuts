package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
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
        String exclusions = m.get("exclusions");
        NutsIdBuilder idBuilder = id.builder();
        if (omitEnv) {
            idBuilder.setQuery(CoreNutsUtils.QUERY_EMPTY_ENV, true);
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
                        sb.append(ws.getParseManager().escapeText(id.getGroup()));
                        sb.append(">>");
                    } else {
                        sb.append(ws.getParseManager().escapeText(id.getGroup()));
                    }
                    sb.append(":");
                }
            }
        }
        sb.append("[[");
        sb.append(ws.getParseManager().escapeText(id.getName()));
        sb.append("]]");
        if (!StringUtils.isEmpty(id.getVersion().getValue())) {
            sb.append("#");
            sb.append(ws.getParseManager().escapeText(id.getVersion().toString()));
        }
        boolean firstQ = true;


        if (!StringUtils.isEmpty(classifier)) {
            if (firstQ) {
                sb.append("{{?}}");
                firstQ = false;
            } else {
                sb.append("{{&}}");
            }
            sb.append("{{classifier}}=**");
            sb.append("**");
            sb.append(ws.getParseManager().escapeText(classifier));
            sb.append("**");
        }

//        if (highlightScope) {
        if (!StringUtils.isEmpty(scope) && !"compile".equals(scope)) {
            if (firstQ) {
                sb.append("{{?}}");
                firstQ = false;
            } else {
                sb.append("{{&}}");
            }
            sb.append("{{scope}}=**");
            sb.append("**");
            sb.append(ws.getParseManager().escapeText(scope));
            sb.append("**");
        }
//        }
//        if (highlightOptional) {
        if (!StringUtils.isEmpty(optional) && !"false".equals(optional)) {
            if (firstQ) {
                sb.append("{{?}}");
                firstQ = false;
            } else {
                sb.append("{{&}}");
            }
            sb.append("{{optional}}=**");
            sb.append(ws.getParseManager().escapeText(optional));
            sb.append("**");
        }
//        }
        if (!StringUtils.isEmpty(exclusions)) {
            if (firstQ) {
                sb.append("{{?}}");
                firstQ = false;
            } else {
                sb.append("{{&}}");
            }
            sb.append("{{exclusions}}=@@");
            sb.append(ws.getParseManager().escapeText(exclusions));
            sb.append("@@");
        }
        if (!StringUtils.isEmpty(id.getQuery())) {
            for (Map.Entry<String, String> ee : id.getQueryMap().entrySet()) {
                switch (ee.getKey()) {
                    case "exclusions":
                    case "optional":
                    case "scope":
                    case "classifier": {
                        break;
                    }
                    default: {
                        if (firstQ) {
                            sb.append("{{?}}");
                            firstQ = false;
                        } else {
                            sb.append("{{&}}");
                        }
                        sb.append("<<" + ws.getParseManager().escapeText(ee.getKey()) + ">>=");
                        sb.append(ws.getParseManager().escapeText(exclusions));
//                        sb.append("");
                    }
                }

            }
//            sb.append("?");
//            sb.append(ws.escapeText(id.getQuery()));
        }
        return sb.toString();
    }

}
