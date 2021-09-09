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
import net.thevpc.nuts.runtime.bundles.parsers.QueryStringParser;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.util.Map;
import java.util.function.Function;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNutsIdBuilder implements NutsIdBuilder {

    private transient NutsSession session;
    private String groupId;
    private String artifactId;
    private NutsVersion version;
    private NutsEnvConditionBuilder condition;
    private transient QueryStringParser propertiesQuery = new QueryStringParser(true, (name, value) -> {
        if (name != null) {
            switch (name) {
                case NutsConstants.IdProperties.VERSION: {
                    setVersion(value);
                    return true;
                }
                case NutsConstants.IdProperties.OS: {
                    condition.setOs(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.ARCH: {
                    condition.setArch(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.PLATFORM: {
                    condition.setPlatform(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.OS_DIST: {
                    condition.setOsDist(new String[]{value});
                    return true;
                }
                case NutsConstants.IdProperties.DESKTOP_ENVIRONMENT: {
                    condition.setDesktopEnvironment(new String[]{value});
                    return true;
                }
            }
        }
        return false;
    });

    public DefaultNutsIdBuilder(NutsSession session) {
        this.session=session;
        this.condition=session.getWorkspace().descriptor().envConditionBuilder();
    }

    public DefaultNutsIdBuilder(NutsId id,NutsSession session) {
        this.session=session;
        this.condition=session.getWorkspace().descriptor().envConditionBuilder();
        setGroupId(id.getGroupId());
        setArtifactId(id.getArtifactId());
        setVersion(id.getVersion());
        setProperties(id.getPropertiesQuery());
    }

    public DefaultNutsIdBuilder(String groupId, String artifactId, NutsVersion version, String propertiesQuery,NutsSession session) {
        this.session=session;
        this.condition=session.getWorkspace().descriptor().envConditionBuilder();
        this.groupId = NutsUtilStrings.trimToNull(groupId);
        this.artifactId = NutsUtilStrings.trimToNull(artifactId);
        this.version = version == null ? session.getWorkspace().version().parser().parse("") : version;
        setProperties(propertiesQuery);
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
        this.version = value == null ? session.getWorkspace().version().parser().parse("") : value;
        return this;
    }

    @Override
    public NutsIdBuilder setVersion(String value) {
        this.version = session.getWorkspace().version().parser().parse(value);
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
        String s = getProperties().get(NutsConstants.IdProperties.CLASSIFIER);
        return NutsUtilStrings.trimToNull(s);
    }

    @Override
    public NutsIdBuilder setClassifier(String value) {
        return setProperty(NutsConstants.IdProperties.CLASSIFIER, NutsUtilStrings.trimToNull(value));
//                .setQuery(NutsConstants.QUERY_EMPTY_ENV, true);
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
        return session.getWorkspace().descriptor().envConditionBuilder()
                .setOs(CoreStringUtils.parseAndTrimToDistinctArray(getProperties().get(NutsConstants.IdProperties.OS)))
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
        this.propertiesQuery.setProperties(propertiesQuery);
        return this;
    }

    @Override
    public NutsIdBuilder addProperties(String propertiesQuery) {
        this.propertiesQuery.addProperties(propertiesQuery);
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
        if (NutsUtilStrings.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getShortName() {
        if (NutsUtilStrings.isBlank(groupId)) {
            return NutsUtilStrings.trim(artifactId);
        }
        return NutsUtilStrings.trim(groupId) + ":" + NutsUtilStrings.trim(artifactId);
    }

    @Override
    public String getLongName() {
        String s = getShortName();
        NutsVersion v = getVersion();
        if (v.isBlank()) {
            return s;
        }
        return s + "#" + v;
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
        StringBuilder sb = new StringBuilder();
        if (!NutsUtilStrings.isBlank(groupId)) {
            sb.append(groupId).append(":");
        }
        sb.append(artifactId);
        if (!NutsUtilStrings.isBlank(version.getValue())) {
            sb.append("#").append(version);
        }
        if (!propertiesQuery.isEmpty()) {
            sb.append("?");
            sb.append(propertiesQuery.getPropertiesQuery());
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

        if (groupId != null ? !groupId.equals(nutsId.groupId) : nutsId.groupId != null) {
            return false;
        }
        if (artifactId != null ? !artifactId.equals(nutsId.artifactId) : nutsId.artifactId != null) {
            return false;
        }
        if (version != null ? !version.equals(nutsId.version) : nutsId.version != null) {
            return false;
        }
        return propertiesQuery != null ? propertiesQuery.equals(nutsId.propertiesQuery) : nutsId.propertiesQuery == null;

    }

    @Override
    public int hashCode() {
        int result =  (groupId != null ? groupId.hashCode() : 0);
        result = 31 * result + (artifactId != null ? artifactId.hashCode() : 0);
        result = 31 * result + (version != null ? version.hashCode() : 0);
        result = 31 * result + (propertiesQuery != null ? propertiesQuery.hashCode() : 0);
        return result;
    }

    @Override
    public NutsIdBuilder apply(Function<String, String> properties) {
        setGroupId(CoreNutsUtils.applyStringProperties(this.getGroupId(), properties));
        setArtifactId(CoreNutsUtils.applyStringProperties(this.getArtifactId(), properties));
        setVersion(CoreNutsUtils.applyStringProperties(this.getVersion().getValue(), properties));
        setProperties(CoreNutsUtils.applyMapProperties(this.getProperties(), properties));
        return this;
    }

    @Override
    public NutsIdBuilder omitImportedGroupId() {
        String g = getGroupId();
        if(g!=null && g.length()>0){
            if(session.getWorkspace().imports().isImportedGroupId(g)){
                setGroupId(null);
            }
        }
        return this;
    }

    @Override
    public NutsId build() {
        return new DefaultNutsId(
                groupId, artifactId, version == null ? null : version.getValue(), propertiesQuery.getPropertiesQuery(),session
        );
    }

}
