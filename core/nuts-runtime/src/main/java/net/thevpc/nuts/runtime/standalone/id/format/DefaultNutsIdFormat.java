package net.thevpc.nuts.runtime.standalone.id.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.dependency.NutsDependencyScopes;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.QueryStringParser;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringMapParser;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.*;

public class DefaultNutsIdFormat extends DefaultFormatBase<NutsIdFormat> implements NutsIdFormat {

    private boolean omitRepository;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitProperties;
    private boolean highlightImportedGroup;
    private Set<String> omittedProperties = new HashSet<>();
    private NutsId id;

    public DefaultNutsIdFormat(NutsSession session) {
        super(session, "id-format");
    }

    public NutsIdFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public boolean isOmitRepository() {
        return omitRepository;
    }

    @Override
    public NutsIdFormat setOmitRepository(boolean value) {
        this.omitRepository = value;
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
    public boolean isOmitClassifier() {
        return isOmitProperty(NutsConstants.IdProperties.CLASSIFIER);
    }

    @Override
    public NutsIdFormat setOmitClassifier(boolean value) {
        return setOmitProperty(NutsConstants.IdProperties.CLASSIFIER, value);
    }

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
    public NutsId getValue() {
        return id;
    }

    @Override
    public NutsIdFormat setValue(NutsId id) {
        this.id = id;
        return this;
    }

    @Override
    public NutsString format() {
        checkSession();
        if (id == null) {
            return isNtf() ?
                    NutsTexts.of(getSession()).ofStyled("<null>", NutsTextStyle.of(NutsTextStyleType.BOOLEAN))
                    : NutsTexts.of(getSession()).ofPlain("<null>")
                    ;
        }
        Map<String, String> queryMap = id.getProperties();
        String scope = queryMap.remove(NutsConstants.IdProperties.SCOPE);
        String optional = queryMap.remove(NutsConstants.IdProperties.OPTIONAL);
        String classifier = queryMap.remove(NutsConstants.IdProperties.CLASSIFIER);
        String exclusions = queryMap.remove(NutsConstants.IdProperties.EXCLUSIONS);
        String repo = queryMap.remove(NutsConstants.IdProperties.REPO);
        NutsIdBuilder idBuilder = id.builder();
        if (isOmitOtherProperties()) {
            idBuilder.setProperties(new LinkedHashMap<>());
        }
        if (isOmitFace()) {
            idBuilder.setProperty(NutsConstants.IdProperties.FACE, null);
        }
        id = idBuilder.build();
        NutsTextBuilder sb = NutsTexts.of(getSession()).builder();
        if (!isOmitGroupId()) {
            if (!NutsBlankable.isBlank(id.getGroupId())) {
                boolean importedGroup2 = "net.thevpc.nuts".equals(id.getGroupId());
                boolean importedGroup = getSession().imports().getAllImports().contains(id.getGroupId());
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
        sb.append(id.getArtifactId(), NutsTextStyle.primary1());
        if (!NutsBlankable.isBlank(id.getVersion().getValue())) {
            sb.append("#", NutsTextStyle.separator());
            sb.append(id.getVersion());
        }
        boolean firstQ = true;

        if (!NutsBlankable.isBlank(classifier)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("classifier", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encode(classifier));
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
            sb.append(_encode(scope));
        }
//        }
//        if (highlightOptional) {
        if (!NutsBlankable.isBlank(optional) && !"false".equalsIgnoreCase(optional)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("optional", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encode(optional));
        }
//        }
        if (!isOmitRepository()) {
            if (!NutsBlankable.isBlank(id.getRepository())) {
                if (firstQ) {
                    sb.append("?", NutsTextStyle.separator());
                    firstQ = false;
                } else {
                    sb.append("&", NutsTextStyle.separator());
                }
                sb.append("repo", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
                sb.append(_encode(id.getRepository()), NutsTextStyle.pale());
            }
        }
        for (Map.Entry<String, String> e : CoreFilterUtils.toMap(id.getCondition()).entrySet()) {
            String kk=e.getKey();
            String kv=e.getValue();
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append(_encode(kk), NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encode(kv));
        }
        if (!NutsBlankable.isBlank(exclusions)) {
            if (firstQ) {
                sb.append("?", NutsTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NutsTextStyle.separator());
            }
            sb.append("exclusions", NutsTextStyle.keyword(2)).append("=", NutsTextStyle.separator());
            sb.append(_encode(exclusions), NutsTextStyle.warn());
        }
        if (!NutsBlankable.isBlank(id.getPropertiesQuery())) {
            Set<String> otherKeys = new TreeSet<>(queryMap.keySet());
            for (String k : otherKeys) {
                String v2 = queryMap.get(k);
                if (v2 != null) {
                    if (firstQ) {
                        sb.append("?", NutsTextStyle.separator());
                        firstQ = false;
                    } else {
                        sb.append("&", NutsTextStyle.separator());
                    }
                    sb.append(k, NutsTextStyle.pale());
                    sb.append("=", NutsTextStyle.separator());
                    sb.append(_encode(v2));
                }
            }
        }
        if (isNtf()) {
            return sb.immutable();
        } else {
            return NutsTexts.of(getSession()).ofPlain(sb.filteredText());
        }
    }

    private String _encode(String s){
        return QueryStringParser.QPARSER.encode(s,false, StringMapParser.QuoteType.SIMPLE);
    }

    @Override
    public void print(NutsPrintStream out) {
        out.print(format());
    }

    @Override
    public String toString() {
        return "NutsIdFormat{"
                + "omitRepository=" + omitRepository
                + ", omitGroup=" + omitGroup
                + ", omitImportedGroup=" + omitImportedGroup
                + ", omitProperties=" + omitProperties
                + ", highlightImportedGroup=" + highlightImportedGroup
                + ", omittedProperties=" + omittedProperties
                + ", id=" + id
                + '}';
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.getKey().getString()) {
            case "--omit-env": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setOmitOtherProperties(val);
                }
                return true;
            }
            case "--omit-face": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setOmitFace(val);
                }
                return true;
            }
            case "--omit-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setOmitGroupId(val);
                }
                return true;
            }
            case "--omit-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setOmitImportedGroupId(val);
                }
                return true;
            }
            case "--omit-repo": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setOmitRepository(val);
                }
                return true;
            }
            case "--highlight-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if (enabled) {
                    setHighlightImportedGroupId(val);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
