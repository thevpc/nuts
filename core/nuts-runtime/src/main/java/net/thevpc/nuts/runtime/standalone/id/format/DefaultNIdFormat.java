package net.thevpc.nuts.runtime.standalone.id.format;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.dependency.NDependencyScopes;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyleType;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

public class DefaultNIdFormat extends DefaultFormatBase<NIdFormat> implements NIdFormat {

    private boolean omitRepository;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitProperties;
    private boolean highlightImportedGroup;
    private Set<String> omittedProperties = new HashSet<>();
    private NId id;

    public DefaultNIdFormat(NSession session) {
        super(session, "id-format");
    }

    public NIdFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public boolean isOmitRepository() {
        return omitRepository;
    }

    @Override
    public NIdFormat setOmitRepository(boolean value) {
        this.omitRepository = value;
        return this;
    }


    @Override
    public boolean isOmitGroupId() {
        return omitGroup;
    }

    @Override
    public NIdFormat setOmitGroupId(boolean value) {
        this.omitGroup = value;
        return this;
    }

    @Override
    public boolean isOmitImportedGroupId() {
        return omitImportedGroup;
    }

    @Override
    public NIdFormat setOmitImportedGroupId(boolean value) {
        this.omitImportedGroup = value;
        return this;
    }


    @Override
    public boolean isOmitOtherProperties() {
        return omitProperties;
    }

    @Override
    public NIdFormat setOmitOtherProperties(boolean value) {
        this.omitProperties = value;
        return this;
    }


    @Override
    public boolean isOmitFace() {
        return isOmitProperty(NConstants.IdProperties.FACE);
    }

    @Override
    public NIdFormat setOmitFace(boolean value) {
        return setOmitProperty(NConstants.IdProperties.FACE, value);
    }

    @Override
    public boolean isHighlightImportedGroupId() {
        return highlightImportedGroup;
    }

    @Override
    public NIdFormat setHighlightImportedGroupId(boolean value) {
        this.highlightImportedGroup = value;
        return this;
    }

    @Override
    public boolean isOmitClassifier() {
        return isOmitProperty(NConstants.IdProperties.CLASSIFIER);
    }

    @Override
    public NIdFormat setOmitClassifier(boolean value) {
        return setOmitProperty(NConstants.IdProperties.CLASSIFIER, value);
    }

    @Override
    public List<String> getOmitProperties() {
        return new ArrayList<>(omittedProperties);
    }

    @Override
    public boolean isOmitProperty(String name) {
        return omittedProperties.contains(name);
    }

    @Override
    public NIdFormat setOmitProperty(String name, boolean value) {
        if (value) {
            omittedProperties.add(name);
        } else {
            omittedProperties.remove(name);
        }
        return this;
    }

    @Override
    public NId getValue() {
        return id;
    }

    @Override
    public NIdFormat setValue(NId id) {
        this.id = id;
        return this;
    }

    @Override
    public NString format() {
        checkSession();
        if (id == null) {
            return isNtf() ?
                    NTexts.of(getSession()).ofStyled("<null>", NTextStyle.of(NTextStyleType.BOOLEAN))
                    : NTexts.of(getSession()).ofPlain("<null>")
                    ;
        }
        Map<String, String> queryMap = id.getProperties();
        String scope = queryMap.remove(NConstants.IdProperties.SCOPE);
        String optional = queryMap.remove(NConstants.IdProperties.OPTIONAL);
        String classifier = id.getClassifier();
        String exclusions = queryMap.remove(NConstants.IdProperties.EXCLUSIONS);
        String repo = queryMap.remove(NConstants.IdProperties.REPO);
        NIdBuilder idBuilder = id.builder();
        if (isOmitOtherProperties()) {
            idBuilder.setProperties(new LinkedHashMap<>());
        }
        if (isOmitFace()) {
            idBuilder.setProperty(NConstants.IdProperties.FACE, null);
        }
        id = idBuilder.build();
        NTextBuilder sb = NTexts.of(getSession()).ofBuilder();
        if (!isOmitGroupId()) {
            if (!NBlankable.isBlank(id.getGroupId())) {
                boolean importedGroup2 = NConstants.Ids.NUTS_GROUP_ID.equals(id.getGroupId());
                boolean importedGroup = NImports.of(getSession()).getAllImports().contains(id.getGroupId());
                if (!(importedGroup && isOmitImportedGroupId())) {
                    if (importedGroup || importedGroup2) {
                        sb.append(id.getGroupId(), NTextStyle.pale());
                    } else {
                        sb.append(id.getGroupId());
                    }
                    sb.append(":", NTextStyle.separator());
                }
            }
        }
        sb.append(id.getArtifactId(), NTextStyle.primary1());
        if (!NBlankable.isBlank(id.getVersion().getValue())) {
            sb.append("#", NTextStyle.separator());
            sb.append(id.getVersion());
        }
        boolean firstQ = true;

        if (!NBlankable.isBlank(classifier)) {
            if (firstQ) {
                sb.append("?", NTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NTextStyle.separator());
            }
            sb.append("classifier", NTextStyle.keyword(2)).append("=", NTextStyle.separator());
            sb.append(_encodeKey(classifier));
        }

//        if (highlightScope) {
        if (!NDependencyScopes.isDefaultScope(scope)) {
            if (firstQ) {
                sb.append("?", NTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NTextStyle.separator());
            }
            sb.append("scope", NTextStyle.keyword(2)).append("=", NTextStyle.separator());
            sb.append(_encodeKey(scope));
        }
//        }
//        if (highlightOptional) {
        if (!NBlankable.isBlank(optional) && !"false".equalsIgnoreCase(optional)) {
            if (firstQ) {
                sb.append("?", NTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NTextStyle.separator());
            }
            sb.append("optional", NTextStyle.keyword(2)).append("=", NTextStyle.separator());
            sb.append(_encodeKey(optional));
        }
//        }
        if (!isOmitRepository()) {
            if (!NBlankable.isBlank(id.getRepository())) {
                if (firstQ) {
                    sb.append("?", NTextStyle.separator());
                    firstQ = false;
                } else {
                    sb.append("&", NTextStyle.separator());
                }
                sb.append("repo", NTextStyle.keyword(2)).append("=", NTextStyle.separator());
                sb.append(_encodeKey(id.getRepository()), NTextStyle.pale());
            }
        }
        for (Map.Entry<String, String> e : CoreFilterUtils.toMap(id.getCondition(), getSession()).entrySet()) {
            String kk = e.getKey();
            String kv = e.getValue();
            if (firstQ) {
                sb.append("?", NTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NTextStyle.separator());
            }
            sb.append(_encodeKey(kk), NTextStyle.keyword(2)).append("=", NTextStyle.separator());
            sb.append(_encodeValue(kv));
        }
        if (!NBlankable.isBlank(exclusions)) {
            if (firstQ) {
                sb.append("?", NTextStyle.separator());
                firstQ = false;
            } else {
                sb.append("&", NTextStyle.separator());
            }
            sb.append("exclusions", NTextStyle.keyword(2)).append("=", NTextStyle.separator());
            sb.append(_encodeKey(exclusions), NTextStyle.warn());
        }
        if (!NBlankable.isBlank(id.getPropertiesQuery())) {
            Set<String> otherKeys = new TreeSet<>(queryMap.keySet());
            for (String k : otherKeys) {
                String v2 = queryMap.get(k);
                if (v2 != null) {
                    if (firstQ) {
                        sb.append("?", NTextStyle.separator());
                        firstQ = false;
                    } else {
                        sb.append("&", NTextStyle.separator());
                    }
                    sb.append(_encodeKey(k), NTextStyle.pale());
                    sb.append("=", NTextStyle.separator());
                    sb.append(_encodeValue(v2));
                }
            }
        }
        if (isNtf()) {
            return sb.immutable();
        } else {
            return NTexts.of(getSession()).ofPlain(sb.filteredText());
        }
    }

    private String _encodeValue(String s) {
        return NStringUtils.formatStringLiteral(s, NStringUtils.QuoteType.SIMPLE, NSupportMode.PREFERRED);
    }

    private String _encodeKey(String s) {
        return NStringUtils.formatStringLiteral(s, NStringUtils.QuoteType.SIMPLE, NSupportMode.PREFERRED);
    }

    @Override
    public void print(NPrintStream out) {
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
    public boolean configureFirst(NCmdLine commandLine) {
        NSession session = getSession();
        NArg aa = commandLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        switch(aa.key()) {
            case "--omit-env": {
                commandLine.withNextFlag((v, a, s) -> this.setOmitOtherProperties(v));
                return true;
            }
            case "--omit-face": {
                commandLine.withNextFlag((v, a, s) -> this.setOmitFace(v));
                return true;
            }
            case "--omit-group": {
                commandLine.withNextFlag((v, a, s) -> this.setOmitGroupId(v));
                return true;
            }
            case "--omit-imported-group": {
                commandLine.withNextFlag((v, a, s) -> this.setOmitImportedGroupId(v));
                return true;
            }
            case "--omit-repo": {
                commandLine.withNextFlag((v, a, s) -> this.setOmitRepository(v));
                return true;
            }
            case "--highlight-imported-group": {
                commandLine.withNextFlag((v, a, s) -> this.setHighlightImportedGroupId(v));
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
