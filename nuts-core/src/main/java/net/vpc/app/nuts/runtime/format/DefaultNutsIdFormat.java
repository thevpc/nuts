package net.vpc.app.nuts.runtime.format;

import java.io.PrintStream;

import net.vpc.app.nuts.*;

import java.util.*;

import net.vpc.app.nuts.runtime.util.NutsDependencyScopes;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

public class DefaultNutsIdFormat extends DefaultFormatBase<NutsIdFormat> implements NutsIdFormat {

    private boolean omitNamespace;
    private boolean omitGroup;
    private boolean omitImportedGroup;
    private boolean omitProperties = true;
    private boolean highlightImportedGroup;
    private boolean highlightScope;
    private boolean highlightOptional;
    private Set<String> omittedProperties =new HashSet<>();
    private NutsId id;

    public DefaultNutsIdFormat(NutsWorkspace ws) {
        super(ws, "id-format");
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
        return setOmitProperty(NutsConstants.IdProperties.CLASSIFIER,value);
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
        if(value){
            omittedProperties.add(name);
        }else{
            omittedProperties.remove(name);
        }
        return this;
    }

    @Override
    public NutsIdFormat omitProperty(String name, boolean value) {
        return setOmitProperty(name,true);
    }

    @Override
    public NutsIdFormat omitProperty(String name) {
        return omitProperty(name,true);
    }

    @Override
    public String format() {
        if(id==null){
            return "<null>";
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
        NutsTerminalFormat tf = getWorkspace().io().term().getTerminalFormat();
        StringBuilder sb = new StringBuilder();
        if (!isOmitNamespace()) {
            if (!CoreStringUtils.isBlank(id.getNamespace())) {
                sb.append("<<");
                sb.append(tf.escapeText(id.getNamespace() + "://"));
                sb.append(">>");
            }
        }
        if (!isOmitGroupId()) {
            if (!CoreStringUtils.isBlank(id.getGroupId())) {
                boolean importedGroup2 = "net.vpc.app.nuts".equals(id.getGroupId());
                boolean importedGroup = getWorkspace().imports().getAll().contains(id.getGroupId());
                if (!(importedGroup && isOmitImportedGroupId())) {
                    if (importedGroup || importedGroup2) {
                        sb.append("<<");
                        sb.append(tf.escapeText(id.getGroupId()));
                        sb.append(">>");
                    } else {
                        sb.append(tf.escapeText(id.getGroupId()));
                    }
                    sb.append(":");
                }
            }
        }
        sb.append("[[");
        sb.append(tf.escapeText(id.getArtifactId()));
        sb.append("]]");
        if (!CoreStringUtils.isBlank(id.getVersion().getValue())) {
            sb.append("\\#");
            sb.append(tf.escapeText(id.getVersion().toString()));
        }
        boolean firstQ = true;

//        if (!CoreStringUtils.isBlank(alternative)) {
//            if (firstQ) {
//                sb.append("{{\\?}}");
//                firstQ = false;
//            } else {
//                sb.append("{{\\&}}");
//            }
//            sb.append("{{alternative}}=**");
//            sb.append("**");
//            sb.append(tf.escapeText(alternative));
//            sb.append("**");
//        }

        if (!CoreStringUtils.isBlank(classifier)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{classifier}}=**");
            sb.append("**");
            sb.append(tf.escapeText(classifier));
            sb.append("**");
        }

//        if (highlightScope) {
        if (!NutsDependencyScopes.isDefaultScope(scope)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{scope}}=");
            sb.append("**");
            sb.append(tf.escapeText(scope));
            sb.append("**");
        }
//        }
//        if (highlightOptional) {
        if (!CoreStringUtils.isBlank(optional) && !"false".equalsIgnoreCase(optional)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{optional}}=");
            sb.append("**");
            sb.append(tf.escapeText(optional));
            sb.append("**");
        }
//        }
        if (!CoreStringUtils.isBlank(exclusions)) {
            if (firstQ) {
                sb.append("{{\\?}}");
                firstQ = false;
            } else {
                sb.append("{{\\&}}");
            }
            sb.append("{{exclusions}}=");
            sb.append("@@");
            sb.append(tf.escapeText(exclusions));
            sb.append("@@");
        }
        if (!CoreStringUtils.isBlank(id.getPropertiesQuery())) {
            Set<String> otherKeys=new TreeSet<>(queryMap.keySet());
            for (String k : otherKeys) {
                String v = queryMap.get(k);
                if(v!=null) {
                    if (firstQ) {
                        sb.append("{{\\?}}");
                        firstQ = false;
                    } else {
                        sb.append("{{\\&}}");
                    }
                    sb.append("<<").append(tf.escapeText(v)).append(">>");
                    sb.append("=");
                    sb.append(tf.escapeText(v));
                }
            }
        }
        return sb.toString();
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
    public NutsIdFormat set(NutsId id) {
        return setValue(id);
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
        boolean enabled=a.isEnabled();
        switch (a.getStringKey()) {
            case "--omit-env": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    omitOtherProperties(val);
                }
                return true;
            }
            case "--omit-face": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    omitFace(val);
                }
                return true;
            }
            case "--omit-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    omitGroupId(val);
                }
                return true;
            }
            case "--omit-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    omitImportedGroupId(val);
                }
                return true;
            }
            case "--omit-namespace": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    omitNamespace(val);
                }
                return true;
            }
            case "--highlight-imported-group": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    highlightImportedGroupId(val);
                }
                return true;
            }
            case "--highlight-optional": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    highlightOptional(val);
                }
                return true;
            }
            case "--highlight-scope": {
                boolean val = cmdLine.nextBoolean().getBooleanValue();
                if(enabled) {
                    highlightScope(val);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        return "NutsIdFormat{" +
                "omitNamespace=" + omitNamespace +
                ", omitGroup=" + omitGroup +
                ", omitImportedGroup=" + omitImportedGroup +
                ", omitProperties=" + omitProperties +
                ", highlightImportedGroup=" + highlightImportedGroup +
                ", highlightScope=" + highlightScope +
                ", highlightOptional=" + highlightOptional +
                ", omittedProperties=" + omittedProperties +
                ", id=" + id +
                '}';
    }
}
