package net.thevpc.nuts.runtime.standalone.dependency.format;

import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NDependencyFormat;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;

@NComponentScope(NScopeType.PROTOTYPE)
public class DefaultNDependencyFormat extends DefaultFormatBase<NDependencyFormat> implements NDependencyFormat {

    private boolean omitRepository;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitQuery = false;
    //    private boolean omitFace = true;
    private boolean highlightImportedGroup;
    private NDependency value;
    private Set<String> queryPropertiesOmitted = new HashSet<>();

    public DefaultNDependencyFormat(NWorkspace workspace) {
        super("dependency-format");
    }

    public NDependencyFormat setNtf(boolean ntf) {
        super.setNtf(ntf);
        return this;
    }

    @Override
    public boolean isOmitRepository() {
        return omitRepository;
    }

    @Override
    public NDependencyFormat setOmitRepository(boolean omitRepository) {
        this.omitRepository = omitRepository;
        return this;
    }

    @Override
    public boolean isOmitGroupId() {
        return omitGroup;
    }

    @Override
    public NDependencyFormat setOmitGroupId(boolean omitGroup) {
        this.omitGroup = omitGroup;
        return this;
    }

    @Override
    public boolean isOmitImportedGroupId() {
        return omitImportedGroup;
    }

    @Override
    public NDependencyFormat setOmitImportedGroup(boolean omitImportedGroup) {
        this.omitImportedGroup = omitImportedGroup;
        return this;
    }

    @Override
    public boolean isOmitOtherProperties() {
        return omitQuery;
    }

    @Override
    public NDependencyFormat setOmitOtherProperties(boolean value) {
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
    public NDependencyFormat setHighlightImportedGroup(boolean highlightImportedGroup) {
        this.highlightImportedGroup = highlightImportedGroup;
        return this;
    }

    @Override
    public NText format() {
        NIdBuilder id = value.toId().builder();
        Map<String, String> q = id.getProperties();
        for (Map.Entry<String, String> e : q.entrySet()) {
            switch (e.getKey()) {
                case NConstants.IdProperties.SCOPE:
                case NConstants.IdProperties.OPTIONAL:
                case NConstants.IdProperties.EXCLUSIONS:
                case NConstants.IdProperties.TYPE:
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
        NIdFormat id1 = NIdFormat.of();
        for (String omitQueryProperty : getOmitQueryProperties()) {
            id1.setOmitProperty(omitQueryProperty,true);
        }
        return id1
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
    public NDependency getValue() {
        return value;
    }

    @Override
    public NDependencyFormat setValue(NDependency id) {
        this.value = id;
        return this;
    }


    @Override
    public boolean isOmitOptional() {
        return isOmitQueryProperty(NConstants.IdProperties.OPTIONAL);
    }

    @Override
    public NDependencyFormat setOmitOptional(boolean value) {
        return setOmitQueryProperty(NConstants.IdProperties.OPTIONAL, value);
    }

    @Override
    public boolean isOmitExclusions() {
        return isOmitQueryProperty(NConstants.IdProperties.EXCLUSIONS);
    }

    @Override
    public NDependencyFormat setOmitExclusions(boolean value) {
        return setOmitQueryProperty(NConstants.IdProperties.EXCLUSIONS, value);
    }

    @Override
    public boolean isOmitScope() {
        return isOmitQueryProperty(NConstants.IdProperties.SCOPE);
    }

    @Override
    public NDependencyFormat setOmitScope(boolean value) {
        return setOmitQueryProperty(NConstants.IdProperties.SCOPE, value);
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
    public NDependencyFormat setOmitQueryProperty(String name, boolean value) {
        if (value) {
            queryPropertiesOmitted.add(name);
        } else {
            queryPropertiesOmitted.remove(name);
        }
        return this;
    }

    @Override
    public void print(NPrintStream out) {
        out.print(format());
    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        boolean enabled=aa.isNonCommented();
        switch(aa.key()) {
            case "--omit-env": {
                cmdLine.withNextFlag((v, a) -> setOmitOtherProperties(v));
                return true;
            }
//            case "--omit-face": {
//                setOmitFace(cmdLine.nextBooleanValue().get(session));
//                return true;
//            }
            case "--omit-group": {
                cmdLine.withNextFlag((v, a) -> setOmitGroupId(v));
                return true;
            }
            case "--omit-imported-group": {
                cmdLine.withNextFlag((v, a) -> setOmitImportedGroup(v));
                return true;
            }
            case "--omit-repo": {
                cmdLine.withNextFlag((v, a) -> setOmitRepository(v));
                return true;
            }
            case "--highlight-imported-group": {
                cmdLine.withNextFlag((v, a) -> setHighlightImportedGroup(v));
                return true;
            }
        }
        return false;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
