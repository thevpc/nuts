package net.thevpc.nuts.runtime.standalone.format;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.thevpc.nuts.*;

public class DefaultNutsDependencyFormat extends DefaultFormatBase<NutsDependencyFormat> implements NutsDependencyFormat {

    private boolean omitNamespace;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitQuery = false;
    //    private boolean omitFace = true;
    private boolean highlightImportedGroup;
    private boolean highlightScope;
    private boolean highlightOptional;
    private NutsDependency value;
    private Set<String> queryPropertiesOmitted = new HashSet<>();

    public DefaultNutsDependencyFormat(NutsWorkspace ws) {
        super(ws, "id-format");
    }

    @Override
    public boolean isOmitNamespace() {
        return omitNamespace;
    }

    @Override
    public NutsDependencyFormat setOmitNamespace(boolean omitNamespace) {
        this.omitNamespace = omitNamespace;
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
    public boolean isHighlightScope() {
        return highlightScope;
    }

    @Override
    public NutsDependencyFormat setHighlightScope(boolean highlightScope) {
        this.highlightScope = highlightScope;
        return this;
    }

    @Override
    public boolean isHighlightOptional() {
        return highlightOptional;
    }

    @Override
    public NutsDependencyFormat setHighlightOptional(boolean highlightOptional) {
        this.highlightOptional = highlightOptional;
        return this;
    }

    @Override
    public String format() {
        NutsIdBuilder id = value.toId().builder();
        Map<String, String> q = id.getProperties();
        for (Map.Entry<String, String> e : q.entrySet()) {
            switch (e.getKey()) {
                case NutsConstants.IdProperties.SCOPE:
                case NutsConstants.IdProperties.OPTIONAL:
//                case NutsConstants.IdProperties.ALTERNATIVE:
                case NutsConstants.IdProperties.CLASSIFIER:
                case NutsConstants.IdProperties.EXCLUSIONS: {
                    break;
                }
                default: {
                    if (isOmitOtherProperties()) {
                        id.setProperty(e.getKey(), null);
                    }
                }
            }
        }
        NutsIdFormat id1 = getWorkspace().id().formatter();
        for (String omitQueryProperty : getOmitQueryProperties()) {
            id1.omitProperty(omitQueryProperty);
        }
        return id1
                .setSession(getSession())
                .setValue(id.build())
                .setHighlightImportedGroupId(isHighlightImportedGroup())
                .setHighlightOptional(isHighlightOptional())
                .setHighlightScope(isHighlightScope())
                .setOmitOtherProperties(false)
                .setOmitGroupId(isOmitGroupId())
                .setOmitImportedGroupId(isOmitImportedGroupId())
                .setOmitNamespace(isOmitNamespace())
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

//    @Override
//    public boolean isOmitAlternative() {
//        return isOmitQueryProperty(NutsConstants.IdProperties.ALTERNATIVE);
//    }
//
//    @Override
//    public NutsDependencyFormat setOmitAlternative(boolean value) {
//        return setOmitQueryProperty(NutsConstants.IdProperties.ALTERNATIVE,value);
//    }
//
//    @Override
//    public NutsDependencyFormat omitAlternative(boolean value) {
//        return setOmitAlternative(value);
//    }
//
//    @Override
//    public NutsDependencyFormat omitAlternative() {
//        return omitAlternative(true);
//    }

    @Override
    public String[] getOmitQueryProperties() {
        return queryPropertiesOmitted.toArray(new String[0]);
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
    public void print(PrintStream out) {
        out.print(format());
    }


    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        boolean enabled=a.isEnabled();
        switch (a.getStringKey()) {
            case "--omit-env": {
                boolean val= cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    setOmitOtherProperties(val);
                }
                return true;
            }
//            case "--omit-face": {
//                setOmitFace(cmdLine.nextBoolean().getBooleanValue());
//                return true;
//            }
            case "--omit-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                setOmitGroupId(val);
                }
                return true;
            }
            case "--omit-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    setOmitImportedGroup(val);
                }
                return true;
            }
            case "--omit-namespace": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    setOmitNamespace(val);
                }
                return true;
            }
            case "--highlight-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    setHighlightImportedGroup(val);
                }
                return true;
            }
            case "--highlight-optional": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    setHighlightOptional(val);
                }
                return true;
            }
            case "--highlight-scope": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    setHighlightScope(val);
                }
                return true;
            }
        }
        return false;
    }
}
