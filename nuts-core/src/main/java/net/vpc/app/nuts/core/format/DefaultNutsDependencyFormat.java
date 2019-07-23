package net.vpc.app.nuts.core.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import net.vpc.app.nuts.core.DefaultNutsDependencyBuilder;

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
    public boolean isOmitGroup() {
        return omitGroup;
    }

    @Override
    public NutsDependencyFormat setOmitGroup(boolean omitGroup) {
        this.omitGroup = omitGroup;
        return this;
    }

    @Override
    public boolean isOmitImportedGroup() {
        return omitImportedGroup;
    }

    @Override
    public NutsDependencyFormat setOmitImportedGroup(boolean omitImportedGroup) {
        this.omitImportedGroup = omitImportedGroup;
        return this;
    }

    @Override
    public boolean isOmitQuery() {
        return omitQuery;
    }

    @Override
    public NutsDependencyFormat setOmitQuery(boolean value) {
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
        Map<String, String> q = id.getQueryMap();
        for (Iterator<Map.Entry<String, String>> iterator = q.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, String> e = iterator.next();
            switch (e.getKey()) {
                case "scope":
                case "optional":
                case "classifier":
                case "exclusions": {
                    break;
                }
                default: {
                    if(isOmitQuery()) {
                        iterator.remove();
                    }
                }
            }
        }
        NutsIdFormat id1 = ws.id();
        for (String omitQueryProperty : getOmitQueryProperties()) {
            id1.omitQueryProperty(omitQueryProperty);
        }
        return id1
                .session(getSession())
                .setValue(id.build())
                .setHighlightImportedGroup(isHighlightImportedGroup())
                .setHighlightOptional(isHighlightOptional())
                .setHighlightScope(isHighlightScope())
                .setOmitQuery(false)
                .setOmitGroup(isOmitGroup())
                .setOmitImportedGroup(isOmitImportedGroup())
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
        return isOmitQueryProperty("classifier");
    }

    @Override
    public NutsDependencyFormat setOmitClassifier(boolean value) {
        return setOmitQueryProperty("classifier",value);
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
        return isOmitQueryProperty("optional");
    }

    @Override
    public NutsDependencyFormat setOmitOptional(boolean value) {
        return setOmitQueryProperty("optional",value);
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
        return isOmitQueryProperty("exclusions");
    }

    @Override
    public NutsDependencyFormat setOmitExclusions(boolean value) {
        return setOmitQueryProperty("exclusions",value);
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
        return isOmitQueryProperty("scope");
    }

    @Override
    public NutsDependencyFormat setOmitScope(boolean value) {
        return setOmitQueryProperty("scope",value);
    }

    @Override
    public NutsDependencyFormat omitScope(boolean value) {
        return setOmitScope(value);
    }

    @Override
    public NutsDependencyFormat omitScope() {
        return omitScope(true);
    }

    @Override
    public boolean isOmitAlternative() {
        return isOmitQueryProperty("alternative");
    }

    @Override
    public NutsDependencyFormat setOmitAlternative(boolean value) {
        return setOmitQueryProperty("alternative",value);
    }

    @Override
    public NutsDependencyFormat omitAlternative(boolean value) {
        return setOmitAlternative(value);
    }

    @Override
    public NutsDependencyFormat omitAlternative() {
        return omitAlternative(true);
    }

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
                setOmitQuery(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
//            case "--omit-face": {
//                setOmitFace(cmdLine.nextBoolean().getBooleanValue());
//                return true;
//            }
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
