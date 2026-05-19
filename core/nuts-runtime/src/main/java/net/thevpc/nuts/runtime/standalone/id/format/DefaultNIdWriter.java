package net.thevpc.nuts.runtime.standalone.id.format;

import net.thevpc.nuts.artifact.NEnvCondition;
import net.thevpc.nuts.artifact.NIdWriter;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdBuilder;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;


import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.dependency.NDependencyScopes;
import net.thevpc.nuts.runtime.standalone.format.DefaultObjectWriterBase;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.util.*;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNIdWriter extends DefaultObjectWriterBase<NIdWriter> implements NIdWriter {

    private boolean omitRepository;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitProperties;
    private boolean omitCondition;
    private boolean omitExclusion;
    private boolean highlightImportedGroup;
    private Set<String> omittedProperties = new HashSet<>();

    public DefaultNIdWriter() {
        super("id-format");
    }

    public boolean isOmitCondition() {
        return omitCondition;
    }

    public NIdWriter omitCondition(boolean omitCondition) {
        this.omitCondition = omitCondition;
        return this;
    }

    public boolean isOmitExclusion() {
        return omitExclusion;
    }

    public NIdWriter omitExclusion(boolean omitExclusion) {
        this.omitExclusion = omitExclusion;
        return this;
    }

    public NIdWriter setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public boolean isOmitRepository() {
        return omitRepository;
    }

    @Override
    public NIdWriter omitRepository(boolean value) {
        this.omitRepository = value;
        return this;
    }


    @Override
    public boolean isOmitGroupId() {
        return omitGroup;
    }

    @Override
    public NIdWriter omitGroupId(boolean value) {
        this.omitGroup = value;
        return this;
    }

    @Override
    public boolean isOmitImportedGroupId() {
        return omitImportedGroup;
    }

    @Override
    public NIdWriter omitImportedGroupId(boolean value) {
        this.omitImportedGroup = value;
        return this;
    }


    @Override
    public boolean isOmitOtherProperties() {
        return omitProperties;
    }

    @Override
    public NIdWriter omitOtherProperties(boolean value) {
        this.omitProperties = value;
        return this;
    }


    @Override
    public boolean isOmitFace() {
        return isOmitProperty(NConstants.IdProperties.FACE);
    }

    @Override
    public NIdWriter omitFace(boolean value) {
        return setOmitProperty(NConstants.IdProperties.FACE, value);
    }

    @Override
    public boolean isHighlightImportedGroupId() {
        return highlightImportedGroup;
    }

    @Override
    public NIdWriter highlightImportedGroupId(boolean value) {
        this.highlightImportedGroup = value;
        return this;
    }

    @Override
    public List<String> omitProperties() {
        return new ArrayList<>(omittedProperties);
    }

    @Override
    public boolean isOmitProperty(String name) {
        return omittedProperties.contains(name);
    }

    @Override
    public NIdWriter setOmitProperty(String name, boolean value) {
        if (value) {
            omittedProperties.add(name);
        } else {
            omittedProperties.remove(name);
        }
        return this;
    }

    @Override
    public NText format(Object aValue) {
        if (aValue == null) {
            return isNtf() ?
                    NText.ofStyled("<null>", NTextStyle.of(NTextStyleType.BOOLEAN))
                    : NText.ofPlain("<null>")
                    ;
        }
        NId id=(NId)aValue;
        Map<String, String> queryMap = id.properties();
        String scope = queryMap.remove(NConstants.IdProperties.SCOPE);
        String optional = queryMap.remove(NConstants.IdProperties.OPTIONAL);
        String classifier = id.classifier();
        NEnvCondition condition = id.condition();
        String exclusions = queryMap.remove(NConstants.IdProperties.EXCLUSIONS);
        String repo = queryMap.remove(NConstants.IdProperties.REPO);
        NIdBuilder idBuilder = id.builder();
        if (isOmitOtherProperties()) {
            idBuilder.clearProperties();
            idBuilder.condition(NEnvCondition.BLANK);
        }else if (isOmitFace()) {
            idBuilder.setProperty(NConstants.IdProperties.FACE, null);
        }
        id = idBuilder.build();
        NTextBuilder sb = NTextBuilder.of();
        if (NBlankable.isBlank(classifier)) {
            if (!isOmitGroupId()) {
                if (!NBlankable.isBlank(id.groupId())) {
                    boolean importedGroup2 = NConstants.Ids.NUTS_GROUP_ID.equals(id.groupId());
                    boolean importedGroup = NWorkspace.of().getAllImports().contains(id.groupId());
                    if (!(importedGroup && isOmitImportedGroupId())) {
                        if (importedGroup || importedGroup2) {
                            sb.append(id.groupId(), NTextStyle.pale());
                        } else {
                            sb.append(id.groupId());
                        }
                        sb.append(":", NTextStyle.separator());
                    }
                }
            }
            sb.append(id.artifactId(), NTextStyle.primary1());
        } else {
            if (!isOmitGroupId()) {
                if (!NBlankable.isBlank(id.groupId())) {
                    boolean importedGroup2 = NConstants.Ids.NUTS_GROUP_ID.equals(id.groupId());
                    boolean importedGroup = NWorkspace.of().getAllImports().contains(id.groupId());
                    if (!(importedGroup && isOmitImportedGroupId())) {
                        if (importedGroup || importedGroup2) {
                            sb.append(id.groupId(), NTextStyle.pale());
                        } else {
                            sb.append(id.groupId());
                        }
                    }
                }
            }
            sb.append(":", NTextStyle.separator());
            sb.append(id.artifactId(), NTextStyle.primary1());
            sb.append(":", NTextStyle.separator());
            sb.append(id.classifier(), NTextStyle.primary2());
        }


        if (!NBlankable.isBlank(id.version().value())) {
            sb.append("#", NTextStyle.separator());
            sb.append(id.version());
        }
        boolean firstQ = true;

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
        if (!isOmitRepository()) {
            if (!NBlankable.isBlank(id.repository())) {
                if (firstQ) {
                    sb.append("?", NTextStyle.separator());
                    firstQ = false;
                } else {
                    sb.append("&", NTextStyle.separator());
                }
                sb.append("repo", NTextStyle.keyword(2)).append("=", NTextStyle.separator());
                sb.append(_encodeKey(id.repository()), NTextStyle.pale());
            }
        }
        if(!isOmitCondition()) {
            for (Map.Entry<String, String> e : CoreFilterUtils.toMap(condition).entrySet()) {
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
        }
        if(!isOmitExclusion()) {
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
        }
        if (!NBlankable.isBlank(id.propertiesQuery())) {
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
            return NText.ofPlain(sb.filteredText());
        }
    }

    private String _encodeValue(String s) {
        return NStringMapFormat.URL_ENCODER.apply(s);
//        return NStringUtils.formatStringLiteral(s, NElementType.SINGLE_QUOTED_STRING, NSupportMode.PREFERRED,"=&");
    }

    private String _encodeKey(String s) {
        return NStringMapFormat.URL_ENCODER.apply(s);
        //return NStringUtils.formatStringLiteral(s, NElementType.SINGLE_QUOTED_STRING, NSupportMode.PREFERRED,"=&");
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        out.print(format(aValue));
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
                + '}';
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        switch (aa.key()) {
            case "--omit-env": {
                return cmdLine.matcher().matchFlag((v) -> this.omitOtherProperties(v.booleanValue())).anyMatch();
            }
            case "--omit-face": {
                return cmdLine.matcher().matchFlag((v) -> this.omitFace(v.booleanValue())).anyMatch();
            }
            case "--omit-group": {
                return cmdLine.matcher().matchFlag((v) -> this.omitGroupId(v.booleanValue())).anyMatch();
            }
            case "--omit-imported-group": {
                return cmdLine.matcher().matchFlag((v) -> this.omitImportedGroupId(v.booleanValue())).anyMatch();
            }
            case "--omit-repo": {
                return cmdLine.matcher().matchFlag((v) -> this.omitRepository(v.booleanValue())).anyMatch();
            }
            case "--highlight-imported-group": {
                return cmdLine.matcher().matchFlag((v) -> this.highlightImportedGroupId(v.booleanValue())).anyMatch();
            }
        }
        return false;
    }

}
