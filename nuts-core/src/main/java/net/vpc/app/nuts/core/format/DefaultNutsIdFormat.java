package net.vpc.app.nuts.core.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.util.Map;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

public class DefaultNutsIdFormat extends DefaultFormatBase<NutsIdFormat> implements NutsIdFormat {

    private boolean omitNamespace;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitEnv = true;
    private boolean omitFace = true;
    private boolean highlightImportedGroup;
    private boolean highlightScope;
    private boolean highlightOptional;
    private NutsId id;

    public DefaultNutsIdFormat(NutsWorkspace ws) {
        super(ws, "id-format");
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

    @Override
    public String format() {
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
            idBuilder.setQueryProperty(NutsConstants.QueryKeys.FACE, null);
        }
        id = idBuilder.build();
        NutsTerminalFormat tf = ws.io().getTerminalFormat();
        StringBuilder sb = new StringBuilder();
        if (!omitNamespace) {
            if (!CoreStringUtils.isBlank(id.getNamespace())) {
                sb.append("<<");
                sb.append(tf.escapeText(id.getNamespace() + "://"));
                sb.append(">>");
            }
        }
        if (!omitGroup) {
            if (!CoreStringUtils.isBlank(id.getGroup())) {
                boolean importedGroup = ws.config().getImports().contains(id.getGroup());
                if (!(importedGroup && omitImportedGroup)) {
                    if (importedGroup) {
                        sb.append("<<");
                        sb.append(tf.escapeText(id.getGroup()));
                        sb.append(">>");
                    } else {
                        sb.append(tf.escapeText(id.getGroup()));
                    }
                    sb.append(":");
                }
            }
        }
        sb.append("[[");
        sb.append(tf.escapeText(id.getName()));
        sb.append("]]");
        if (!CoreStringUtils.isBlank(id.getVersion().getValue())) {
            sb.append("#");
            sb.append(tf.escapeText(id.getVersion().toString()));
        }
        boolean firstQ = true;

        if (!CoreStringUtils.isBlank(classifier)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{classifier}}=**");
            sb.append("**");
            sb.append(tf.escapeText(classifier));
            sb.append("**");
        }

//        if (highlightScope) {
        if (!CoreStringUtils.isBlank(scope) && !"compile".equalsIgnoreCase(scope)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{scope}}=");
            sb.append("**");
            sb.append(tf.escapeText(scope));
            sb.append("**");
        }
//        }
//        if (highlightOptional) {
        if (!CoreStringUtils.isBlank(optional) && !"false".equalsIgnoreCase(optional)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{optional}}=");
            sb.append("**");
            sb.append(tf.escapeText(optional));
            sb.append("**");
        }
//        }
        if (!CoreStringUtils.isBlank(exclusions)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{exclusions}}=");
            sb.append("@@");
            sb.append(tf.escapeText(exclusions));
            sb.append("@@");
        }
        if (!CoreStringUtils.isBlank(id.getQuery())) {
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
                            sb.append("{{\\?}}");
                            firstQ = false;
                        } else {
                            sb.append("{{\\&}}");
                        }
                        sb.append("<<").append(tf.escapeText(ee.getKey())).append(">>=");
                        sb.append(tf.escapeText(exclusions));
//                        sb.append("");
                    }
                }

            }
//            sb.append("?");
//            sb.append(ws.escapeText(id.getQuery()));
        }
        return sb.toString();
    }

    public NutsId getValue() {
        return id;
    }

    public NutsIdFormat set(NutsId id) {
        return setValue(id);
    }

    public NutsIdFormat setValue(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public void print(Writer out) {
        try {
            out.write(format());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--omit-env": {
                setOmitEnv(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-face": {
                setOmitFace(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-group": {
                setOmitGroup(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-imported-group": {
                setOmitImportedGroup(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--omit-namespace": {
                setOmitNamespace(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--highlight-imported-group": {
                setHighlightImportedGroup(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--highlight-optional": {
                setHighlightOptional(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
            case "--highlight-scope": {
                setHighlightScope(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
        }
        return false;
    }
}
