package net.vpc.app.nuts.runtime.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.runtime.util.CoreNutsUtils;

import net.vpc.app.nuts.runtime.config.DefaultNutsDependencyBuilder;

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
    private Set<String> queryPropertiesOmitted=new HashSet<>();

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
        NutsIdBuilder id = value.getId().builder();
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
        NutsIdFormat id1 = ws.id();
        for (String omitQueryProperty : getOmitQueryProperties()) {
            id1.omitProperty(omitQueryProperty);
        }
        return id1
                .session(getSession())
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
    public NutsDependencyFormat value(NutsDependency id) {
        return setValue(id);
    }

    @Override
    public NutsDependencyFormat setValue(NutsDependency id) {
        this.value = id;
        return this;
    }

    @Override
    public NutsDependency parse(String dependency) {
        return CoreNutsUtils.parseNutsDependency(ws, dependency);
    }

    @Override
    public NutsDependency parseRequired(String dependency) {
        NutsDependency d = parse(dependency);
        if (d == null) {
            throw new NutsParseException(ws, "Invalid Dependency format : " + dependency);
        }
        return d;
    }

    @Override
    public boolean isOmitClassifier() {
        return isOmitQueryProperty(NutsConstants.IdProperties.CLASSIFIER);
    }

    @Override
    public NutsDependencyFormat setOmitClassifier(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.CLASSIFIER,value);
    }

    @Override
    public NutsDependencyFormat omitClassifier(boolean value) {
        return setOmitClassifier(value);
    }

    @Override
    public NutsDependencyFormat omitClassifier() {
        return omitClassifier(true);
    }


    @Override
    public boolean isOmitOptional() {
        return isOmitQueryProperty(NutsConstants.IdProperties.OPTIONAL);
    }

    @Override
    public NutsDependencyFormat setOmitOptional(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.OPTIONAL,value);
    }

    @Override
    public NutsDependencyFormat omitOptional(boolean value) {
        return setOmitOptional(value);
    }

    @Override
    public NutsDependencyFormat omitOptional() {
        return omitOptional(true);
    }


    @Override
    public boolean isOmitExclusions() {
        return isOmitQueryProperty(NutsConstants.IdProperties.EXCLUSIONS);
    }

    @Override
    public NutsDependencyFormat setOmitExclusions(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.EXCLUSIONS,value);
    }

    @Override
    public NutsDependencyFormat omitExclusions(boolean value) {
        return setOmitExclusions(value);
    }

    @Override
    public NutsDependencyFormat omitExclusions() {
        return omitExclusions(true);
    }

    @Override
    public boolean isOmitScope() {
        return isOmitQueryProperty(NutsConstants.IdProperties.SCOPE);
    }

    @Override
    public NutsDependencyFormat setOmitScope(boolean value) {
        return setOmitQueryProperty(NutsConstants.IdProperties.SCOPE,value);
    }

    @Override
    public NutsDependencyFormat omitScope(boolean value) {
        return setOmitScope(value);
    }

    @Override
    public NutsDependencyFormat omitScope() {
        return omitScope(true);
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
        if(value){
            queryPropertiesOmitted.add(name);
        }else{
            queryPropertiesOmitted.remove(name);
        }
        return this;
    }

    @Override
    public NutsDependencyFormat omitQueryProperty(String name, boolean value) {
        return setOmitQueryProperty(name,true);
    }

    @Override
    public NutsDependencyFormat omitQueryProperty(String name) {
        return omitQueryProperty(name,true);
    }

    @Override
    public NutsDependencyFormat omitNamespace(boolean omitNamespace) {
        return setOmitNamespace(omitNamespace);
    }

    @Override
    public NutsDependencyFormat omitNamespace() {
        return omitNamespace(true);
    }

    @Override
    public NutsDependencyFormat omitGroupId(boolean omitGroup) {
        return setOmitGroupId(omitGroup);
    }

    @Override
    public NutsDependencyFormat omitGroupId() {
        return omitGroupId(true);
    }

    @Override
    public NutsDependencyFormat highlightImportedGroup(boolean highlightImportedGroup) {
        return setHighlightImportedGroup(highlightImportedGroup);
    }

    @Override
    public NutsDependencyFormat highlightImportedGroup() {
        return highlightImportedGroup(true);
    }

    @Override
    public NutsDependencyFormat highlightScope(boolean highlightScope) {
        return setHighlightScope(highlightScope);
    }

    @Override
    public NutsDependencyFormat highlightScope() {
        return highlightScope(true);
    }

    @Override
    public NutsDependencyFormat highlightOptional(boolean highlightOptional) {
        return setHighlightOptional(true);
    }

    @Override
    public NutsDependencyFormat highlightOptional() {
        return highlightOptional(true);
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
    public NutsDependencyBuilder builder() {
        return new DefaultNutsDependencyBuilder();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmdLine) {
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--omit-env": {
                setOmitOtherProperties(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
//            case "--omit-face": {
//                setOmitFace(cmdLine.nextBoolean().getBooleanValue());
//                return true;
//            }
            case "--omit-group": {
                setOmitGroupId(cmdLine.nextBoolean().getBooleanValue());
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
