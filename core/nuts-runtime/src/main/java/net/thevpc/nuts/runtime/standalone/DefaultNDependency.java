/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.reserved.NReservedLangUtils;
import net.thevpc.nuts.reserved.NReservedUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringMapFormat;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class DefaultNDependency implements NDependency {
    public static final long serialVersionUID = 1L;
    private final String repository;
    private final String groupId;
    private final String artifactId;
    private final NVersion version;
    private final String scope;
    private final String classifier;
    private final String optional;
    private final String type;
    private final List<NId> exclusions;
    private final String properties;
    private final NEnvCondition condition;

    public DefaultNDependency(String repository, String groupId, String artifactId, String classifier, NVersion version, String scope, String optional, List<NId> exclusions,
                              NEnvCondition condition, String type,
                              Map<String, String> properties) {
        this(repository, groupId, artifactId, classifier, version, scope, optional, exclusions, condition, type, NStringMapFormat.DEFAULT.format(properties));
    }

    public DefaultNDependency(String repository, String groupId, String artifactId, String classifier, NVersion version, String scope, String optional, List<NId> exclusions,
                              NEnvCondition condition, String type,
                              String properties) {
        this.repository = NStringUtils.trimToNull(repository);
        this.groupId = NStringUtils.trimToNull(groupId);
        this.artifactId = NStringUtils.trimToNull(artifactId);
        this.version = version == null ? NVersion.BLANK : version;
        this.classifier = NStringUtils.trimToNull(classifier);
        this.scope = NDependencyScope.parse(scope).orElse(NDependencyScope.API).id();

        String validOptional = NStringUtils.trimToNull(optional);
        if ("false".equalsIgnoreCase(validOptional)) {
            validOptional = null;
        } else if ("true".equalsIgnoreCase(validOptional)) {
            validOptional = "true";//remove case and formatting
        }
        NDependencyScopePattern s = NDependencyScopePattern.parse(scope).orElse(null);
        if(s!=null && s==NDependencyScopePattern.SYSTEM){
            //force to true when system
            validOptional="true";
        }
        this.optional = validOptional;
        this.exclusions = NReservedLangUtils.unmodifiableList(exclusions);
        this.condition = condition == null ? NEnvCondition.BLANK : condition;
        this.type = NStringUtils.trimToNull(type);
        Map<String, String> m = NStringMapFormat.DEFAULT.parse(properties).get();
        for (String k : new String[]{
                NConstants.IdProperties.SCOPE,
                NConstants.IdProperties.OPTIONAL,
                NConstants.IdProperties.TYPE,
                NConstants.IdProperties.REPO,
        }) {
            String old=m.remove(k);
            if(old!=null){
                switch (k){
                    case NConstants.IdProperties.SCOPE:{
                        if(!Objects.equals(old,scope) && !Objects.equals(old,this.scope)){
                            Logger.getLogger(DefaultNDependency.class.getName()).log(Level.WARNING, "unexpected dependency key : {0} {1}<>{2},{3}", new Object[]{k,old,scope,this.scope});
                        }
                        break;
                    }
                    case NConstants.IdProperties.OPTIONAL:{
                        if(!Objects.equals(old,optional) && !Objects.equals(old,this.optional)){
                            Logger.getLogger(DefaultNDependency.class.getName()).log(Level.WARNING, "unexpected dependency key : {0} {1}<>{2},{3}", new Object[]{k,old,optional,this.optional});
                        }
                        break;
                    }
                    case NConstants.IdProperties.TYPE:{
                        if(!Objects.equals(old,type) && !Objects.equals(old,this.type)){
                            Logger.getLogger(DefaultNDependency.class.getName()).log(Level.WARNING, "unexpected dependency key : {0} {1}<>{2},{3}", new Object[]{k,old,type,this.type});
                        }
                        break;
                    }
                    case NConstants.IdProperties.REPO:{
                        if(!Objects.equals(old,repository) && !Objects.equals(old,this.repository)){
                            Logger.getLogger(DefaultNDependency.class.getName()).log(Level.WARNING, "unexpected dependency key : {0} {1}<>{2},{3}", new Object[]{k,old,repository,this.repository});
                        }
                        break;
                    }
                }
            }
        }
        this.properties = NStringUtils.trim(NStringMapFormat.DEFAULT.format(m));
    }

    @Override
    public boolean isBlank() {
        return toId().isBlank();
    }

    @Override
    public NDependencyBuilder builder() {
        return NDependencyBuilder.of().copyFrom(this);
    }

    @Override
    public boolean isOptional() {
        final String o = getOptional();
        return o != null && Boolean.parseBoolean(o);
    }

    @Override
    public String getOptional() {
        return optional;
    }

    @Override
    public boolean isAnyProvided() {
        if (!NBlankable.isBlank(scope)) {
            NDependencyScopePattern u = NDependencyScopePattern.parse(getScope()).orElse(null);
            if(u!=null){
                switch (u) {
                    case PROVIDED:
                    case TEST_PROVIDED:
                        return true;
                }
            }
            return u == NDependencyScopePattern.PROVIDED;
        }
        return false;
    }

    @Override
    public boolean isAnyRuntime() {
        if (!NBlankable.isBlank(scope)) {
            NDependencyScopePattern u = NDependencyScopePattern.parse(getScope()).orElse(null);
            if(u!=null){
                switch (u) {
                    case RUNTIME:
                    case TEST_RUNTIME:
                        return true;
                }
            }
            return u == NDependencyScopePattern.PROVIDED;
        }
        return false;
    }
    @Override
    public boolean isProvided() {
        if (!NBlankable.isBlank(scope)) {
            NDependencyScopePattern u = NDependencyScopePattern.parse(getScope()).orElse(null);
            if(u!=null){
                switch (u) {
                    case PROVIDED:
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isRuntime() {
        if (!NBlankable.isBlank(scope)) {
            NDependencyScopePattern u = NDependencyScopePattern.parse(getScope()).orElse(null);
            if(u!=null){
                switch (u) {
                    case RUNTIME:
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isAnyTest() {
        if (!NBlankable.isBlank(scope)) {
            NDependencyScopePattern u = NDependencyScopePattern.parse(getScope()).orElse(null);
            if(u!=null){
                switch (u) {
                    case TEST:
                    case TEST_COMPILE:
                    case TEST_IMPLEMENTATION:
                    case TEST_RUNTIME:
                    case TEST_SYSTEM:
                    case TEST_PROVIDED:
                    case TEST_OTHER:
                    case TEST_API:
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public String getScope() {
        return scope;
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public NId toId() {
        Map<String, String> m = new LinkedHashMap<>();
        Map<String, String> pp = getProperties();
        m.putAll(pp);
        if (!NReservedUtils.isDependencyDefaultScope(scope)) {
            m.put(NConstants.IdProperties.SCOPE, scope);
        }
        if (!NBlankable.isBlank(optional) && !"false".equals(optional)) {
            m.put(NConstants.IdProperties.OPTIONAL, optional);
        }
        if (!NBlankable.isBlank(type)) {
            m.put(NConstants.IdProperties.TYPE, type);
        }
        if (!NBlankable.isBlank(repository)) {
            m.put(NConstants.IdProperties.REPO, repository);
        }
        if (exclusions.size() > 0) {
            m.put(NConstants.IdProperties.EXCLUSIONS, NReservedUtils.toDependencyExclusionListString(exclusions));
        }
        NId ii = NIdBuilder.of()
                .setGroupId(getGroupId())
                .setArtifactId(getArtifactId())
                .setClassifier(getClassifier())
                .setVersion(getVersion())
                .setCondition(getCondition())
                .setProperties(m).build();
        return ii;
    }

    @Override
    public String getRepository() {
        return repository;
    }

    @Override
    public String getGroupId() {
        return groupId;
    }

    @Override
    public String getArtifactId() {
        return artifactId;
    }

    @Override
    public String getShortName() {
        return NReservedUtils.getIdShortName(groupId,artifactId, classifier);
    }

    @Override
    public String getLongName() {
        return NReservedUtils.getIdLongName(groupId,artifactId, version, classifier);
    }


    @Override
    public String getFullName() {
        return toString();
    }

    @Override
    public NVersion getVersion() {
        return version;
    }

    @Override
    public NEnvCondition getCondition() {
        return condition;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<NId> getExclusions() {
        return exclusions;
    }

    @Override
    public String getPropertiesQuery() {
        return properties;
    }

    @Override
    public Map<String, String> getProperties() {
        return NStringMapFormat.DEFAULT.parse(properties).get();
    }

    @Override
    public int hashCode() {
        return Objects.hash(repository, groupId, artifactId, version, scope, classifier, optional, type, properties, condition, exclusions);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNDependency that = (DefaultNDependency) o;
        return Objects.equals(repository, that.repository) && Objects.equals(groupId, that.groupId) && Objects.equals(artifactId, that.artifactId) && Objects.equals(version, that.version) && Objects.equals(scope, that.scope) && Objects.equals(classifier, that.classifier) && Objects.equals(optional, that.optional) && Objects.equals(type, that.type) && Objects.equals(exclusions, that.exclusions) && Objects.equals(properties, that.properties) && Objects.equals(condition, that.condition);
    }

    @Override
    public String toString() {
        return toId().toString();
    }
}
