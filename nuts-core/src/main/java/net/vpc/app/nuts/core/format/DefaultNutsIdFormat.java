package net.vpc.app.nuts.core.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.util.*;

import net.vpc.app.nuts.core.DefaultNutsIdBuilder;
import net.vpc.app.nuts.core.bridges.maven.mvnutil.PomId;
import net.vpc.app.nuts.core.bridges.maven.mvnutil.PomIdResolver;
import net.vpc.app.nuts.core.util.NutsDependencyScopes;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

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
    public NutsIdFormat setOmitNamespace(boolean omitNamespace) {
        this.omitNamespace = omitNamespace;
        return this;
    }

    @Override
    public boolean isOmitGroup() {
        return omitGroup;
    }

    @Override
    public NutsIdFormat setOmitGroup(boolean omitGroup) {
        this.omitGroup = omitGroup;
        return this;
    }

    @Override
    public boolean isOmitImportedGroup() {
        return omitImportedGroup;
    }

    @Override
    public NutsIdFormat setOmitImportedGroup(boolean omitImportedGroup) {
        this.omitImportedGroup = omitImportedGroup;
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
    public NutsIdFormat setOmitFace(boolean omitFace) {
        return setOmitProperty(NutsConstants.IdProperties.FACE,omitFace);
    }

    @Override
    public boolean isHighlightImportedGroup() {
        return highlightImportedGroup;
    }

    @Override
    public NutsIdFormat setHighlightImportedGroup(boolean highlightImportedGroup) {
        this.highlightImportedGroup = highlightImportedGroup;
        return this;
    }

    @Override
    public boolean isHighlightScope() {
        return highlightScope;
    }

    @Override
    public NutsIdFormat setHighlightScope(boolean highlightScope) {
        this.highlightScope = highlightScope;
        return this;
    }

    @Override
    public boolean isHighlightOptional() {
        return highlightOptional;
    }

    @Override
    public NutsIdFormat setHighlightOptional(boolean highlightOptional) {
        this.highlightOptional = highlightOptional;
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
        NutsTerminalFormat tf = ws.io().getTerminalFormat();
        StringBuilder sb = new StringBuilder();
        if (!isOmitNamespace()) {
            if (!CoreStringUtils.isBlank(id.getNamespace())) {
                sb.append("<<");
                sb.append(tf.escapeText(id.getNamespace() + "://"));
                sb.append(">>");
            }
        }
        if (!isOmitGroup()) {
            if (!CoreStringUtils.isBlank(id.getGroupId())) {
                boolean importedGroup = ws.config().getImports().contains(id.getGroupId());
                if (!(importedGroup && isOmitImportedGroup())) {
                    if (importedGroup) {
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
            sb.append("#");
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
    public NutsId parse(String id) {
        return CoreNutsUtils.parseNutsId(id);
    }

    @Override
    public NutsId parseRequired(String nutFormat) {
        NutsId id = CoreNutsUtils.parseNutsId(nutFormat);
        if (id == null) {
            throw new NutsParseException(ws, "Invalid Id format : " + nutFormat);
        }
        return id;
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
    public NutsIdBuilder builder() {
        return new DefaultNutsIdBuilder();
    }

    @Override
    public NutsId resolveId(Class clazz) {
        PomId u = PomIdResolver.resolvePomId(clazz, null);
        if (u == null) {
            return null;
        }
        return parse(u.getGroupId() + ":" + u.getArtifactId() + "#" + u.getVersion());
    }

    @Override
    public NutsId[] resolveIds(Class clazz) {
        PomId[] u = PomIdResolver.resolvePomIds(clazz);
        NutsId[] all = new NutsId[u.length];
        for (int i = 0; i < all.length; i++) {
            all[i] = parse(u[i].getGroupId() + ":" + u[i].getArtifactId() + "#" + u[i].getVersion());
        }
        return all;
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
            case "--omit-face": {
                setOmitFace(cmdLine.nextBoolean().getBooleanValue());
                return true;
            }
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
