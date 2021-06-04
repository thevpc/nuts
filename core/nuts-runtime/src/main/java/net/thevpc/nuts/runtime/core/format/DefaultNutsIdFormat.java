package net.thevpc.nuts.runtime.core.format;

import java.io.PrintStream;

import net.thevpc.nuts.*;

import java.util.*;

import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsDependencyScopes;

public class DefaultNutsIdFormat extends DefaultFormatBase<NutsIdFormat> implements NutsIdFormat {

    private boolean omitNamespace;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitProperties = true;
    private boolean highlightImportedGroup;
    private boolean highlightScope;
    private boolean highlightOptional;
    private Set<String> omittedProperties = new HashSet<>();
    private NutsId id;

    public DefaultNutsIdFormat(NutsWorkspace ws) {
        super(ws, "id-format");
    }

    public NutsIdFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public boolean isOmitNamespace() {
        return omitNamespace;
    }

    @Override
    public NutsIdFormat setOmitNamespace(boolean value) {
        this.omitNamespace = value;
        return this;
    }

    @Override
    public boolean isOmitGroupId() {
        return omitGroup;
    }

    @Override
    public NutsIdFormat setOmitGroupId(boolean value) {
        this.omitGroup = value;
        return this;
    }

    @Override
    public boolean isOmitImportedGroupId() {
        return omitImportedGroup;
    }

    @Override
    public NutsIdFormat setOmitImportedGroupId(boolean value) {
        this.omitImportedGroup = value;
        return this;
    }

    @Override
    public boolean isOmitOtherProperties() {
        return omitProperties;
    }

    @Override
    public NutsIdFormat setOmitOtherProperties(boolean value) {
        this.omitProperties = value;
        return this;
    }

    @Override
    public NutsIdFormat omitOtherProperties(boolean value) {
        return setOmitOtherProperties(value);
    }

    @Override
    public boolean isOmitFace() {
        return isOmitProperty(NutsConstants.IdProperties.FACE);
    }

    @Override
    public NutsIdFormat setOmitFace(boolean value) {
        return setOmitProperty(NutsConstants.IdProperties.FACE, value);
    }

    @Override
    public boolean isHighlightImportedGroupId() {
        return highlightImportedGroup;
    }

    @Override
    public NutsIdFormat setHighlightImportedGroupId(boolean value) {
        this.highlightImportedGroup = value;
        return this;
    }

    @Override
    public boolean isHighlightScope() {
        return highlightScope;
    }

    @Override
    public NutsIdFormat setHighlightScope(boolean value) {
        this.highlightScope = value;
        return this;
    }

    @Override
    public boolean isHighlightOptional() {
        return highlightOptional;
    }

    @Override
    public NutsIdFormat setHighlightOptional(boolean value) {
        this.highlightOptional = value;
        return this;
    }

    @Override
    public boolean isOmitClassifier() {
        return isOmitProperty(NutsConstants.IdProperties.CLASSIFIER);
    }

    @Override
    public NutsIdFormat setOmitClassifier(boolean value) {
        return setOmitProperty(NutsConstants.IdProperties.CLASSIFIER, value);
    }

    @Override
    public NutsIdFormat omitClassifier(boolean value) {
        return setOmitClassifier(value);
    }

    @Override
    public NutsIdFormat omitClassifier() {
        return omitClassifier(true);
    }

//    @Override
//    public boolean isOmitAlternative() {
//        return isOmitProperty(NutsConstants.IdProperties.ALTERNATIVE);
//    }
//
//    @Override
//    public NutsIdFormat setOmitAlternative(boolean value) {
//        return setOmitProperty(NutsConstants.IdProperties.ALTERNATIVE,value);
//    }
//
//    @Override
//    public NutsIdFormat omitAlternative(boolean value) {
//        return setOmitAlternative(value);
//    }
//
//    @Override
//    public NutsIdFormat omitAlternative() {
//        return omitAlternative(true);
//    }
    @Override
    public String[] getOmitProperties() {
        return omittedProperties.toArray(new String[0]);
    }

    @Override
    public boolean isOmitProperty(String name) {
        return omittedProperties.contains(name);
    }

    @Override
    public NutsIdFormat setOmitProperty(String name, boolean value) {
        if (value) {
            omittedProperties.add(name);
        } else {
            omittedProperties.remove(name);
        }
        return this;
    }

    @Override
    public NutsIdFormat omitProperty(String name, boolean value) {
        return setOmitProperty(name, true);
    }

    @Override
    public NutsIdFormat omitProperty(String name) {
        return omitProperty(name, true);
    }

    @Override
    public NutsString format() {
        checkSession();
        if (id == null) {
            return isNtf()?
                    getSession().getWorkspace().text().forStyled("<null>",NutsTextStyle.of(NutsTextStyleType.BOOLEAN))
                    :getSession().getWorkspace().text().forPlain("<null>")
                    ;
        }
        Map<String, String> queryMap = id.getProperties();
        String scope = queryMap.remove(NutsConstants.IdProperties.SCOPE);
        String optional = queryMap.remove(NutsConstants.IdProperties.OPTIONAL);
        String classifier = queryMap.remove(NutsConstants.IdProperties.CLASSIFIER);
//        String alternative = queryMap.remove(NutsConstants.IdProperties.ALTERNATIVE);
        String exclusions = queryMap.remove(NutsConstants.IdProperties.EXCLUSIONS);
        NutsIdBuilder idBuilder = id.builder();
        if (isOmitOtherProperties()) {
            idBuilder.setProperties(new LinkedHashMap<>());
        }
        if (isOmitFace()) {
            idBuilder.setProperty(NutsConstants.IdProperties.FACE, null);
        }
        id = idBuilder.build();
//        NutsTextFormatManager tf = getWorkspace().text();
        NutsTextBuilder sb = getSession().getWorkspace().text().builder();
        if (!isOmitNamespace()) {
            if (!CoreStringUtils.isBlank(id.getNamespace())) {
                sb.append(id.getNamespace() + "://", NutsTextStyle.pale());
            }
        }
        if (!isOmitGroupId()) {
            if (!CoreStringUtils.isBlank(id.getGroupId())) {
                boolean importedGroup2 = "net.thevpc.nuts".equals(id.getGroupId());
                boolean importedGroup = getSession().getWorkspace().imports().getAll().contains(id.getGroupId());
                if (!(importedGroup && isOmitImportedGroupId())) {
                    if (importedGroup || importedGroup2) {
                        sb.append(id.getGroupId(), NutsTextStyle.pale());
                    } else {
                        sb.append(id.getGroupId());
                    }
                    sb.append(":", NutsTextStyle.separator());
                }
            }
        }
        sb.append(id.getArtifactId(), NutsTextStyle.primary(1));
        if (!CoreStringUtils.isBlank(id.getVersion().getValue())) {
            sb.append("#", NutsTextStyle.separator());
            sb.append(id.getVersion());
        }
        boolean firstQ = true;

        if (!CoreStringUtils.isBlank(classifier)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("classifier", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(classifier);
        }

//        if (highlightScope) {
        if (!NutsDependencyScopes.isDefaultScope(scope)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("scope", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(scope);
        }
//        }
//        if (highlightOptional) {
        if (!CoreStringUtils.isBlank(optional) && !"false".equalsIgnoreCase(optional)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("optional", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(optional);
        }
//        }
        if (!CoreStringUtils.isBlank(exclusions)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("exclusions", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(exclusions, NutsTextStyle.error());
        }
        if (!CoreStringUtils.isBlank(id.getPropertiesQuery())) {
            Set<String> otherKeys = new TreeSet<>(queryMap.keySet());
            for (String k : otherKeys) {
                String v = queryMap.get(k);
                if (v != null) {
                    if (firstQ) {
                        sb.append("?", NutsTextStyle.separator());
                        firstQ = false;
                    } else {
                        sb.append("&", NutsTextStyle.separator());
                    }
                    sb.append(k, NutsTextStyle.pale());
                    sb.append("=", NutsTextStyle.separator());
                    sb.append(v);
                }
            }
        }
        if (isNtf()) {
            return sb.immutable();
        } else {
            return getSession().getWorkspace().text().forPlain(sb.filteredText());
        }
    }

    @Override
    public NutsId getValue() {
        return id;
    }

    @Override
    public NutsIdFormat value(NutsId id) {
        return setValue(id);
    }

    @Override
    public NutsIdFormat setValue(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public void print(PrintStream out) {
        out.print(format());
    }

    @Override
    public NutsIdFormat omitNamespace(boolean value) {
        return setOmitNamespace(value);
    }

    @Override
    public NutsIdFormat omitNamespace() {
        return omitNamespace(true);
    }

    @Override
    public NutsIdFormat omitGroupId(boolean value) {
        return setOmitGroupId(value);
    }

    @Override
    public NutsIdFormat omitGroupId() {
        return omitGroupId(true);
    }

    @Override
    public NutsIdFormat omitImportedGroupId(boolean value) {
        return setOmitImportedGroupId(value);
    }

    @Override
    public NutsIdFormat omitImportedGroupId() {
        return omitImportedGroupId(true);
    }

    @Override
    public NutsIdFormat omitOtherProperties() {
        return omitOtherProperties(true);
    }

    @Override
    public NutsIdFormat omitFace(boolean value) {
        return setOmitFace(value);
    }

    @Override
    public NutsIdFormat omitFace() {
        return omitFace(true);
    }

    @Override
    public NutsIdFormat highlightImportedGroupId(boolean value) {
        return setHighlightImportedGroupId(value);
    }

    @Override
    public NutsIdFormat highlightImportedGroupId() {
        return highlightImportedGroupId(true);
    }

    @Override
    public NutsIdFormat highlightScope(boolean value) {
        return setHighlightScope(value);
    }

    @Override
    public NutsIdFormat highlightScope() {
        return highlightScope(true);
    }

    @Override
    public NutsIdFormat highlightOptional(boolean value) {
        return setHighlightOptional(value);
    }

    @Override
    public NutsIdFormat highlightOptional() {
        return highlightOptional(true);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "--omit-env": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    omitOtherProperties(val);
                }
                return true;
            }
            case "--omit-face": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    omitFace(val);
                }
                return true;
            }
            case "--omit-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    omitGroupId(val);
                }
                return true;
            }
            case "--omit-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    omitImportedGroupId(val);
                }
                return true;
            }
            case "--omit-namespace": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    omitNamespace(val);
                }
                return true;
            }
            case "--highlight-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    highlightImportedGroupId(val);
                }
                return true;
            }
            case "--highlight-optional": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    highlightOptional(val);
                }
                return true;
            }
            case "--highlight-scope": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    highlightScope(val);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NutsIdFormat{"
                + "omitNamespace=" + omitNamespace
                + ", omitGroup=" + omitGroup
                + ", omitImportedGroup=" + omitImportedGroup
                + ", omitProperties=" + omitProperties
                + ", highlightImportedGroup=" + highlightImportedGroup
                + ", highlightScope=" + highlightScope
                + ", highlightOptional=" + highlightOptional
                + ", omittedProperties=" + omittedProperties
                + ", id=" + id
                + '}';
    }
}
