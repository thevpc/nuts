package net.thevpc.nuts.runtime.standalone.dependency.format;

import java.util.*;

import net.thevpc.nuts.artifact.NIdWriter;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.artifact.NIdBuilder;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NDependencyWriter;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultObjectWriterBase;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.text.NText;

@NComponentScope(NScopeType.PROTOTYPE)
@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNDependencyWriter extends DefaultObjectWriterBase<NDependencyWriter> implements NDependencyWriter {

    private boolean omitRepository;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitQuery = false;
    //    private boolean omitFace = true;
    private boolean highlightImportedGroup;
    private Set<String> queryPropertiesOmitted = new HashSet<>();

    public DefaultNDependencyWriter() {
        super("dependency-format");
    }

    public NDependencyWriter ntf(boolean ntf) {
        super.ntf(ntf);
        return this;
    }

    @Override
    public boolean isOmitRepository() {
        return omitRepository;
    }

    @Override
    public NDependencyWriter omitRepository(boolean omitRepository) {
        this.omitRepository = omitRepository;
        return this;
    }

    @Override
    public boolean isOmitGroupId() {
        return omitGroup;
    }

    @Override
    public NDependencyWriter omitGroupId(boolean omitGroup) {
        this.omitGroup = omitGroup;
        return this;
    }

    @Override
    public boolean isOmitImportedGroupId() {
        return omitImportedGroup;
    }

    @Override
    public NDependencyWriter omitImportedGroup(boolean omitImportedGroup) {
        this.omitImportedGroup = omitImportedGroup;
        return this;
    }

    @Override
    public boolean isOmitOtherProperties() {
        return omitQuery;
    }

    @Override
    public NDependencyWriter omitOtherProperties(boolean value) {
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
    public NDependencyWriter highlightImportedGroup(boolean highlightImportedGroup) {
        this.highlightImportedGroup = highlightImportedGroup;
        return this;
    }

    @Override
    public NText format(Object aValue) {
        NIdBuilder id = ((NDependency) aValue).toId().builder();
        Map<String, String> q = id.properties();
        for (Map.Entry<String, String> e : q.entrySet()) {
            switch (e.getKey()) {
                case NConstants.IdProperties.SCOPE:
                case NConstants.IdProperties.OPTIONAL:
                case NConstants.IdProperties.EXCLUSIONS:
                case NConstants.IdProperties.TYPE: {
                    break;
                }
                default: {
                    if (isOmitOtherProperties()) {
                        id.setProperty(e.getKey(), null);
                    }
                }
            }
        }
        NIdWriter id1 = NIdWriter.of();
        for (String omitQueryProperty : omittedQueryProperties()) {
            id1.setOmitProperty(omitQueryProperty, true);
        }
        return id1
                .highlightImportedGroupId(isHighlightImportedGroup())
                .omitOtherProperties(false)
                .omitGroupId(isOmitGroupId())
                .omitImportedGroupId(isOmitImportedGroupId())
                .omitRepository(isOmitRepository())
                .ntf(isNtf())
                .format(id.build());
    }


    @Override
    public boolean isOmitOptional() {
        return isOmitQueryProperty(NConstants.IdProperties.OPTIONAL);
    }

    @Override
    public NDependencyWriter omitOptional(boolean value) {
        return omitQueryProperty(NConstants.IdProperties.OPTIONAL, value);
    }

    @Override
    public boolean isOmitExclusions() {
        return isOmitQueryProperty(NConstants.IdProperties.EXCLUSIONS);
    }

    @Override
    public NDependencyWriter omitExclusions(boolean value) {
        return omitQueryProperty(NConstants.IdProperties.EXCLUSIONS, value);
    }

    @Override
    public boolean isOmitScope() {
        return isOmitQueryProperty(NConstants.IdProperties.SCOPE);
    }

    @Override
    public NDependencyWriter omitScope(boolean value) {
        return omitQueryProperty(NConstants.IdProperties.SCOPE, value);
    }


    @Override
    public List<String> omittedQueryProperties() {
        return new ArrayList<>(queryPropertiesOmitted);
    }

    @Override
    public boolean isOmitQueryProperty(String name) {
        return queryPropertiesOmitted.contains(name);
    }

    @Override
    public NDependencyWriter omitQueryProperty(String name, boolean value) {
        if (value) {
            queryPropertiesOmitted.add(name);
        } else {
            queryPropertiesOmitted.remove(name);
        }
        return this;
    }

    @Override
    public void print(Object aValue, NPrintStream out) {
        out.print(format(aValue));
    }


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg aa = cmdLine.peek().get();
        if (aa == null) {
            return false;
        }
        boolean enabled = aa.isUncommented();
        switch (aa.key()) {
            case "--omit-env": {
                cmdLine.matcher().withAny().matchFlag((v) -> omitOtherProperties(v.booleanValue())).anyMatch();
                return true;
            }
//            case "--omit-face": {
//                setOmitFace(cmdLine.nextBooleanValue().get(session));
//                return true;
//            }
            case "--omit-group": {
                cmdLine.matcher().withAny().matchFlag((v) -> omitGroupId(v.booleanValue())).anyMatch();
                return true;
            }
            case "--omit-imported-group": {
                cmdLine.matcher().withAny().matchFlag((v) -> omitImportedGroup(v.booleanValue())).anyMatch();
                return true;
            }
            case "--omit-repo": {
                cmdLine.matcher().withAny().matchFlag((v) -> omitRepository(v.booleanValue())).anyMatch();
                return true;
            }
            case "--highlight-imported-group": {
                cmdLine.matcher().withAny().matchFlag((v) -> highlightImportedGroup(v.booleanValue())).anyMatch();
                return true;
            }
        }
        return false;
    }

}
