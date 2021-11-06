/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.core.model;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.expr.QueryStringParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsIdBuilder implements NutsIdBuilder {

    private transient NutsSession session;
    private String groupId;
    private String artifactId;
    private String classifier;
    private NutsVersion version;
    private NutsEnvConditionBuilder condition;
    private transient QueryStringParser propertiesQuery = new QueryStringParser(true, (name, value) -> {
        if (name != null) {
            switch (name) {
                case NutsConstants.IdProperties.VERSION: {
                    setVersion(value);
                    return true;
                }
                case NutsConstants.IdProperties.CLASSIFIER: {
                    setClassifier(value);
                    return true;
                }
                case NutsConstants.IdProperties.OS: {
                    condition.setOs(CoreStringUtils.parseAndTrimToDistinctArray(value));
                    return true;
                }
                case NutsConstants.IdProperties.ARCH: {
                    condition.setArch(CoreStringUtils.parseAndTrimToDistinctArray(value));
                    return true;
                }
                case NutsConstants.IdProperties.PLATFORM: {
                    condition.setPlatform(CoreStringUtils.parseAndTrimToDistinctArray(value));
                    return true;
                }
                case NutsConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(CoreStringUtils.parseAndTrimToDistinctArray(value));
                    return true;
                }
                case NutsConstants.IdProperties.DESKTOP_ENVIRONMENT: {
                    condition.setDesktopEnvironment(CoreStringUtils.parseAndTrimToDistinctArray(value));
                    return true;
                }
            }
        }
        return false;
    });

    public DefaultNutsIdBuilder(NutsSession session) {
        this.session=session;
        this.condition=NutsEnvConditionBuilder.of(session);
    }

    public DefaultNutsIdBuilder(NutsId id,NutsSession session) {
        this.session=session;
        this.condition=NutsEnvConditionBuilder.of(session);
        setGroupId(id.getGroupId());
        setArtifactId(id.getArtifactId());
        setVersion(id.getVersion());
        setClassifier(id.getClassifier());
        setProperties(id.getPropertiesQuery());
    }

    public DefaultNutsIdBuilder(String groupId, String artifactId, NutsVersion version, String classifier,String propertiesQuery,NutsSession session) {
        this.session=session;
        this.condition=NutsEnvConditionBuilder.of(session);
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? NutsVersion.of("",session) : version;

        String c0 = NutsUtilStrings.trimToNull(classifier);
        String c1 = null;
        Map<String,String> properties=propertiesQuery==null?new LinkedHashMap<>() : QueryStringParser.parseMap(propertiesQuery,session);
        if(!properties.isEmpty()) {
            c1=properties.remove(NutsConstants.IdProperties.CLASSIFIER);
        }
        if(c0==null){
            if(c1!=null) {
                c0 = NutsUtilStrings.trimToNull(c1);
            }
        }
        this.classifier = c0;
        setProperties(properties);
    }

    @Override
    public NutsIdBuilder setAll(NutsId id) {
        if (id == null) {
            clear();
        } else {
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setProperties(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NutsIdBuilder clear() {
        setGroupId(null);
        setArtifactId(null);
        setVersion((NutsVersion) null);
        setProperties("");
        return this;
    }

    @Override
    public NutsIdBuilder setAll(NutsIdBuilder id) {
        if (id == null) {
            clear();
        } else {
            setGroupId(id.getGroupId());
            setArtifactId(id.getArtifactId());
            setVersion(id.getVersion());
            setProperties(id.getPropertiesQuery());
        }
        return this;
    }

    @Override
    public NutsIdBuilder setGroupId(String value) {
        this.groupId = NutsUtilStrings.trimToNull(value);
        return this;
    }

    @Override
    public NutsIdBuilder setRepository(String value) {
        return setProperty(NutsConstants.IdProperties.REPO, NutsUtilStrings.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setVersion(NutsVersion value) {
        this.version = value == null ? NutsVersion.of("",session) : value;
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(String value) {
        this.version = NutsVersion.of(value,session);
        return this;
    }

    @Override
    public DefaultNutsIdBuilder setArtifactId(String value) {
        this.artifactId = NutsUtilStrings.trimToNull(value);
        return this;
    }

    @Override
    public String getFace() {
        String s = getProperties().get(NutsConstants.IdProperties.FACE);
        return NutsUtilStrings.trimToNull(s);
    }

//    @Override
//    public String getAlternative() {
//        String s = getProperties().get(NutsConstants.IdProperties.ALTERNATIVE);
//        return NutsUtilStrings.trimToNull(s);
//    }


    @Override
    public String getPackaging() {
        String s = getProperties().get(NutsConstants.IdProperties.PACKAGING);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public NutsIdBuilder setFace(String value) {
        return setProperty(NutsConstants.IdProperties.FACE, NutsUtilStrings.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
    }

    @Override
    public NutsIdBuilder setFaceContent() {
        return setFace(NutsConstants.QueryFaces.CONTENT);
    }

    @Override
    public NutsIdBuilder setFaceDescriptor() {
        return setFace(NutsConstants.QueryFaces.DESCRIPTOR);
    }

//    @Override
//    public NutsIdBuilder setAlternative(String value) {
//        return setProperty(NutsConstants.IdProperties.ALTERNATIVE, NutsUtilStrings.trimToNull(value));
////                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
//    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public NutsIdBuilder setClassifier(String value) {
        this.classifier=NutsUtilStrings.trimToNull(value);
        return this;
    }

    @Override
    public NutsIdBuilder setPackaging(String value) {
        return setProperty(NutsConstants.IdProperties.PACKAGING, NutsUtilStrings.trimToNull(value));
    }

    @Override
    public NutsIdBuilder setCondition(NutsEnvCondition c) {
        if(c==null){
            setProperty(NutsConstants.IdProperties.OS, null);
            setProperty(NutsConstants.IdProperties.OS_DIST, null);
            setProperty(NutsConstants.IdProperties.ARCH, null);
            setProperty(NutsConstants.IdProperties.PLATFORM, null);
            setProperty(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT, null);
        }else{
            setProperty(NutsConstants.IdProperties.OS, CoreStringUtils.joinAndTrimToNull(c.getOs()));
            setProperty(NutsConstants.IdProperties.OS_DIST, CoreStringUtils.joinAndTrimToNull(c.getOsDist()));
            setProperty(NutsConstants.IdProperties.ARCH, CoreStringUtils.joinAndTrimToNull(c.getArch()));
            setProperty(NutsConstants.IdProperties.PLATFORM, CoreStringUtils.joinAndTrimToNull(c.getPlatform()));
            setProperty(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT, CoreStringUtils.joinAndTrimToNull(c.getDesktopEnvironment()));

        }
        return this;
    }

    @Override
    public NutsEnvConditionBuilder getCondition() {
        return NutsEnvConditionBuilder.of(session)
                .setOs(CoreStringUtils.parseAndTrimToDistinctArray(getProperties().get(NutsConstants.IdProperties.OS)))
                .setArch(CoreStringUtils.parseAndTrimToDistinctArray(getProperties().get(NutsConstants.IdProperties.ARCH)))
                .setOsDist(CoreStringUtils.parseAndTrimToDistinctArray(getProperties().get(NutsConstants.IdProperties.OS_DIST)))
                .setPlatform(CoreStringUtils.parseAndTrimToDistinctArray(getProperties().get(NutsConstants.IdProperties.PLATFORM)))
                .setDesktopEnvironment(CoreStringUtils.parseAndTrimToDistinctArray(getProperties().get(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT)))
                ;
    }

    @Override
    public NutsIdBuilder setCondition(NutsEnvConditionBuilder c) {
        if(c==null){
            return setCondition((NutsEnvCondition) null);
        }else {
            return setCondition(c.build());
        }
    }


    @Override
    public NutsIdBuilder setProperty(String property, String value) {
        switch (property){
            case NutsConstants.IdProperties.OS:{
                condition.setOs(CoreStringUtils.parseAndTrimToDistinctArray(value));
                break;
            }
            case NutsConstants.IdProperties.OS_DIST:{
                condition.setOsDist(CoreStringUtils.parseAndTrimToDistinctArray(value));
                break;
            }
            case NutsConstants.IdProperties.ARCH:{
                condition.setArch(CoreStringUtils.parseAndTrimToDistinctArray(value));
                break;
            }
            case NutsConstants.IdProperties.PLATFORM:{
                condition.setPlatform(CoreStringUtils.parseAndTrimToDistinctArray(value));
                break;
            }
            case NutsConstants.IdProperties.DESKTOP_ENVIRONMENT:{
                condition.setDesktopEnvironment(CoreStringUtils.parseAndTrimToDistinctArray(value));
                break;
            }
            case NutsConstants.IdProperties.CLASSIFIER:{
                setClassifier(value);
                break;
            }
            default:{
                propertiesQuery.setProperty(property, value);
            }
        }
        return this;
    }


    @Override
    public NutsIdBuilder setProperties(Map<String, String> queryMap) {
        propertiesQuery.clear();
        for (Map.Entry<String, String> e : queryMap.entrySet()) {
            setProperty(e.getKey(),e.getValue());
        }
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(Map<String, String> queryMap) {
        propertiesQuery.addProperties(queryMap);
        return this;
    }

    @Override
    public NutsIdBuilder setProperties(String propertiesQuery) {
        this.propertiesQuery.setProperties(propertiesQuery,session);
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(String propertiesQuery) {
        this.propertiesQuery.addProperties(propertiesQuery,session);
        return this;
    }

    @Override
    public String getPropertiesQuery() {
        return QueryStringParser.formatPropertiesQuery(getProperties());
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> m = propertiesQuery.getProperties();
//        String s=CoreStringUtils.joinAndTrimToNull(condition.getOs());
//        if(s!=null){
//            m.put(NutsConstants.IdProperties.OS,s);
//        }
//        s=CoreStringUtils.joinAndTrimToNull(condition.getOsDist());
//        if(s!=null){
//            m.put(NutsConstants.IdProperties.OS_DIST,s);
//        }
//        s=CoreStringUtils.joinAndTrimToNull(condition.getArch());
//        if(s!=null){
//            m.put(NutsConstants.IdProperties.ARCH,s);
//        }
//        s=CoreStringUtils.joinAndTrimToNull(condition.getPlatform());
//        if(s!=null){
//            m.put(NutsConstants.IdProperties.PLATFORM,s);
//        }
//        s=CoreStringUtils.joinAndTrimToNull(condition.getDesktopEnvironment());
//        if(s!=null){
//            m.put(NutsConstants.IdProperties.DESKTOP_ENVIRONMENT,s);
//        }
        return m;
    }

    @Override
    public String getRepository() {
        return NutsUtilStrings.trimToNull(getProperties().get(NutsConstants.IdProperties.REPO));
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getFullName() {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getShortName() {
        if (NutsBlankable.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getLongName() {
        StringBuilder sb=new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsUtilStrings.trim(artifactId));
        NutsVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        if(!NutsBlankable.isBlank(classifier)){
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
        }
        return sb.toString();
    }


    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public NutsVersion getVersion() {
        return version;
    }

    @Override
    public String toString() {
        StringBuilder sb=new StringBuilder();
        if (!NutsBlankable.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(NutsUtilStrings.trim(artifactId));
        NutsVersion v = getVersion();
        if (!v.isBlank()) {
            sb.append("#");
            sb.append(v);
        }
        String properties=propertiesQuery.getPropertiesQuery();
        if(!NutsBlankable.isBlank(classifier)){
            sb.append("?");
            sb.append("classifier=");
            sb.append(classifier);
            if (!NutsBlankable.isBlank(properties)) {
                sb.append("&");
                sb.append(properties);
            }
        }else{
            if (!NutsBlankable.isBlank(properties)) {
                sb.append("?");
                sb.append(properties);
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DefaultNutsIdBuilder nutsId = (DefaultNutsIdBuilder) o;

        if (!Objects.equals(groupId, nutsId.groupId)) {
            return false;
        }
        if (!Objects.equals(artifactId, nutsId.artifactId)) {
            return false;
        }
        if (!Objects.equals(version, nutsId.version)) {
            return false;
        }
        if (!Objects.equals(classifier, nutsId.classifier)) {
            return false;
        }
        return Objects.equals(propertiesQuery, nutsId.propertiesQuery);

    }

    @Override
    public int hashCode() {
        int result =  (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (classifier != null ? classifier.hashCode() : 0);
        result = 31 * result + (propertiesQuery != null ? propertiesQuery.hashCode() : 0);
        return result;
    }

    @Override
    public NutsIdBuilder apply(Function<String, String> properties) {
        setGroupId(CoreNutsUtils.applyStringProperties(this.getGroupId(), properties));
        setArtifactId(CoreNutsUtils.applyStringProperties(this.getArtifactId(), properties));
        setVersion(CoreNutsUtils.applyStringProperties(this.getVersion().getValue(), properties));
        setClassifier(CoreNutsUtils.applyStringProperties(this.getClassifier(), properties));
        setProperties(CoreNutsUtils.applyMapProperties(this.getProperties(), properties));
        return this;
    }

    @Override
    public NutsIdBuilder omitImportedGroupId() {
        String g = getGroupId();
        if(g!=null && g.length()>0){
            if(session.imports().isImportedGroupId(g)){
                setGroupId(null);
            }
        }
        return this;
    }

    @Override
    public NutsId build() {
        return new DefaultNutsId(
                groupId, artifactId, version, classifier,propertiesQuery.getPropertiesQuery(),session
        );
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }
}
