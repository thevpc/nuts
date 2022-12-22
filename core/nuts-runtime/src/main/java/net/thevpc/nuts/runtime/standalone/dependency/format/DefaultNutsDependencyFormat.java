package net.thevpc.nuts.runtime.standalone.dependency.format;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.io.NutsPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

public class DefaultNutsDependencyFormat extends DefaultFormatBase<NutsDependencyFormat> implements NutsDependencyFormat {

    private boolean omitRepository;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitQuery = false;
    //    private boolean omitFace = true;
    private boolean highlightImportedGroup;
    private NutsDependency value;
    private Set<String> queryPropertiesOmitted = new HashSet<>();

    public DefaultNutsDependencyFormat(NutsSession session) {
        super(session, "dependency-format");
    }

    public NutsDependencyFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public boolean isOmitRepository() {
        return omitRepository;
    }

    @Override
    public NutsDependencyFormat setOmitRepository(boolean omitRepository) {
        this.omitRepository = omitRepository;
        return this;
    }

    @Override
    public boolean isOmitGroupId() {
        return omitGroup;
    }

    @Override
    public NutsDependencyFormat setOmitGroupId(boolean omitGroup) {
        this.omitGroup = omitGroup;
        return this;
    }

    @Override
    public boolean isOmitImportedGroupId() {
        return omitImportedGroup;
    }

    @Override
    public NutsDependencyFormat setOmitImportedGroup(boolean omitImportedGroup) {
        this.omitImportedGroup = omitImportedGroup;
        return this;
    }

    @Override
    public boolean isOmitOtherProperties() {
        return omitQuery;
    }

    @Override
    public NutsDependencyFormat setOmitOtherProperties(boolean value) {
        this.omitQuery = value;
        return this;
    }

//    @Override
//    public boolean isOmitFace() {
//        return omitFace;
//    }
//
//    @Override
//    public NutsDependencyFormat setOmitFace(boolean omitFace) {
//        this.omitFace = omitFace;
//        return this;
//    }

    @Override
    public boolean isHighlightImportedGroup() {
        return highlightImportedGroup;
    }

    @Override
    public NutsDependencyFormat setHighlightImportedGroup(boolean highlightImportedGroup) {
        this.highlightImportedGroup = highlightImportedGroup;
        return this;
    }

    @Override
    public NutsString format() {
        NutsIdBuilder id = value.toId().builder();
        Map<String, String> q = id.getProperties();
        for (Map.Entry<String, String> e : q.entrySet()) {
            switch (e.getKey()) {
                case NutsConstants.IdProperties.SCOPE:
                case NutsConstants.IdProperties.OPTIONAL:
                case NutsConstants.IdProperties.CLASSIFIER:
                case NutsConstants.IdProperties.EXCLUSIONS: 
                case NutsConstants.IdProperties.TYPE: 
                {
                    break;
                }
                default: {
                    if (isOmitOtherProperties()) {
                        id.setProperty(e.getKey(), null);
                    }
                }
            }
        }
        NutsIdFormat id1 = NutsIdFormat.of(getSession());
        for (String omitQueryProperty : getOmitQueryProperties()) {
            id1.setOmitProperty(omitQueryProperty,true);
        }
        return id1
                .setSession(getSession())
                .setValue(id.build())
                .setHighlightImportedGroupId(isHighlightImportedGroup())
                .setOmitOtherProperties(false)
                .setOmitGroupId(isOmitGroupId())
                .setOmitImportedGroupId(isOmitImportedGroupId())
                .setOmitRepository(isOmitRepository())
                .setNtf(isNtf())
                .format();
    }

    @Override
    public NutsDependency getValue() {
        return value;
    }

    @Override
    public NutsDependencyFormat setValue(NutsDependency id) {
        this.value = id;
        return this;
    }


    @Override
    public boolean isOmitClassifier() {
        return isOmitQueryProperty(NutsConstants.IdProperties.CLASSIFIER);
    }

    @Override
    public NutsDependencyFormat setOmitClassifier(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.CLASSIFIER, value);
    }

    @Override
    public boolean isOmitOptional() {
        return isOmitQueryProperty(NutsConstants.IdProperties.OPTIONAL);
    }

    @Override
    public NutsDependencyFormat setOmitOptional(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.OPTIONAL, value);
    }

    @Override
    public boolean isOmitExclusions() {
        return isOmitQueryProperty(NutsConstants.IdProperties.EXCLUSIONS);
    }

    @Override
    public NutsDependencyFormat setOmitExclusions(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.EXCLUSIONS, value);
    }

    @Override
    public boolean isOmitScope() {
        return isOmitQueryProperty(NutsConstants.IdProperties.SCOPE);
    }

    @Override
    public NutsDependencyFormat setOmitScope(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.SCOPE, value);
    }


    @Override
    public List<String> getOmitQueryProperties() {
        return new ArrayList<>(queryPropertiesOmitted);
    }

    @Override
    public boolean isOmitQueryProperty(String name) {
        return queryPropertiesOmitted.contains(name);
    }

    @Override
    public NutsDependencyFormat setOmitQueryProperty(String name, boolean value) {
        if (value) {
            queryPropertiesOmitted.add(name);
        } else {
            queryPropertiesOmitted.remove(name);
        }
        return this;
    }

    @Override
    public void print(NutsPrintStream out) {
        out.print(format());
    }


    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        NutsSession session = getSession();
        NutsArgument aa = commandLine.peek().get(session);
        if (aa == null) {
            return false;
        }
        boolean enabled=aa.isActive();
        switch(aa.key()) {
            case "--omit-env": {
                commandLine.withNextBoolean((v, a, s) -> setOmitOtherProperties(v));
                return true;
            }
//            case "--omit-face": {
//                setOmitFace(commandLine.nextBooleanValue().get(session));
//                return true;
//            }
            case "--omit-group": {
                commandLine.withNextBoolean((v, a, s) -> setOmitGroupId(v));
                return true;
            }
            case "--omit-imported-group": {
                commandLine.withNextBoolean((v, a, s) -> setOmitImportedGroup(v));
                return true;
            }
            case "--omit-repo": {
                commandLine.withNextBoolean((v, a, s) -> setOmitRepository(v));
                return true;
            }
            case "--highlight-imported-group": {
                commandLine.withNextBoolean((v, a, s) -> setHighlightImportedGroup(v));
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
