/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsDescriptorBuilder;
import net.vpc.app.nuts.core.DefaultNutsDependency;
import net.vpc.app.nuts.core.DefaultNutsId;
import net.vpc.app.nuts.core.DefaultNutsVersion;
import net.vpc.app.nuts.core.util.CoreNutsUtils;
import net.vpc.app.nuts.core.util.common.MapStringMapper;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CorePlatformUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.mvn.ArchetypeCatalogParser;
import net.vpc.app.nuts.core.util.mvn.MavenMetadata;
import net.vpc.app.nuts.core.util.mvn.MavenMetadataParser;
import net.vpc.app.nuts.core.util.mvn.Pom;
import net.vpc.app.nuts.core.util.mvn.PomDependency;
import net.vpc.app.nuts.core.util.mvn.PomId;
import net.vpc.app.nuts.core.util.mvn.PomIdFilter;
import net.vpc.app.nuts.core.util.mvn.PomXmlParser;

/**
 * Created by vpc on 2/20/17.
 */
public class MavenUtils {

    private static final Logger log = Logger.getLogger(MavenUtils.class.getName());

    public static NutsId[] toNutsId(PomId[] ids) {
        NutsId[] a = new NutsId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            a[i] = toNutsId(ids[i]);
        }
        return a;
    }

    public static NutsDependency[] toNutsDependencies(PomDependency[] deps) {
        NutsDependency[] a = new NutsDependency[deps.length];
        for (int i = 0; i < deps.length; i++) {
            a[i] = toNutsDependency(deps[i]);
        }
        return a;
    }

    public static NutsId toNutsId(PomId d) {
        return new DefaultNutsId(
                null,
                d.getGroupId(),
                d.getArtifactId(),
                toNutsVersion(d.getVersion()),
                ""
        );
    }

    public static NutsDependency toNutsDependency(PomDependency d) {
        return new DefaultNutsDependency(
                null,
                d.getGroupId(),
                d.getArtifactId(),
                d.getClassifier(),
                DefaultNutsVersion.valueOf(toNutsVersion((d.getVersion()))),
                d.getScope(),
                d.getOptional(),
                toNutsId(d.getExclusions())
        );
    }

    public static NutsDescriptor parsePomXml(InputStream stream, String urlDesc) {
        long startTime = System.currentTimeMillis();
        try {
            if (stream == null) {
                return null;
            }
            byte[] bytes = CoreIOUtils.loadByteArray(stream);
            Pom pom = new PomXmlParser().parse(new ByteArrayInputStream(bytes));
            boolean executable = false;// !"maven-archetype".equals(packaging.toString()); // default is true :)
            boolean application = false;// !"maven-archetype".equals(packaging.toString()); // default is true :)
            if ("true".equals(pom.getProperties().get("nuts.executable"))) {
                executable = true;
            } else if (new String(bytes)
                    .matches("^.*?((<mainClass>)|(<goal>exec-war-only</goal>)).*$")) {
                executable = true;
            }
            if ("true".equals(pom.getProperties().get("nuts.application"))) {
                application = true;
            }
            if (application) {
                executable = true;
            }
            if (pom.getPackaging().isEmpty()) {
                pom.setPackaging("jar");
            }

            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom file {0} (time {1})", new Object[]{urlDesc, CoreCommonUtils.formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[SUCCESS] Loading pom file {0}", new Object[]{urlDesc});
            }

            return new DefaultNutsDescriptorBuilder()
                    .setId(toNutsId(pom.getPomId()))
                    .setParents(pom.getParent() == null ? new NutsId[0] : new NutsId[]{toNutsId(pom.getParent())})
                    .setPackaging(pom.getPackaging())
                    .setExecutable(executable)
                    .setNutsApplication(application)
                    .setName(pom.getArtifactId())
                    .setDescription(pom.getDescription())
                    .setPlatform(new String[]{"java"})
                    .setDependencies(toNutsDependencies(pom.getDependencies()))
                    .setStandardDependencies(toNutsDependencies(pom.getDependenciesManagement()))
                    .setProperties(pom.getProperties())
                    .build();
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            if (time > 0) {
                log.log(Level.CONFIG, "[ERROR  ] Caching pom file {0} (time {1})", new Object[]{urlDesc, CoreCommonUtils.formatPeriodMilli(time)});
            } else {
                log.log(Level.CONFIG, "[ERROR  ] Caching pom file {0}", new Object[]{urlDesc});
            }
            throw new NutsParseException("Error Parsing " + urlDesc, e);
        }
    }

    public static String toNutsVersion(String version) {
        /// maven : [cc] [co) (oc] (oo)
        /// nuts  : [cc] [co[ ]oc] ]oo[
        return version == null ? null : version.replace("(", "]").replace(")", "[");
    }

    public static NutsDescriptor parsePomXml(InputStream stream, NutsWorkspace ws, NutsRepositorySession session, String urlDesc) throws IOException {
        NutsDescriptor nutsDescriptor = null;
//        if (session == null) {
//            session = ws.createSession();
//        }
        try {
            try {
//            bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = MavenUtils.parsePomXml(stream, urlDesc);
                HashMap<String, String> properties = new HashMap<>();
                NutsId parentId = null;
                for (NutsId nutsId : nutsDescriptor.getParents()) {
                    parentId = nutsId;
                }
                NutsDescriptor parentDescriptor = null;
                if (parentId != null) {
                    if (!CoreNutsUtils.isEffectiveId(parentId)) {
                        try {
                            parentDescriptor = ws.fetch().id(parentId).setEffective(true)
                                    .setSession(session.getSession())
                                    .setTransitive(true)
                                    .setFetchStratery(
                                            session.getFetchMode() == NutsFetchMode.REMOTE ? NutsFetchStrategy.ONLINE
                                            : NutsFetchStrategy.OFFLINE
                                    ).getResultDescriptor();
                        } catch (NutsException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            throw new NutsNotFoundException(nutsDescriptor.getId(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + parentId, ex);
                        }
                        parentId = parentDescriptor.getId();
                    }
                }
                if (parentId != null) {
                    properties.put("parent.groupId", parentId.getGroup());
                    properties.put("parent.artifactId", parentId.getName());
                    properties.put("parent.version", parentId.getVersion().getValue());

                    properties.put("project.parent.groupId", parentId.getGroup());
                    properties.put("project.parent.artifactId", parentId.getName());
                    properties.put("project.parent.version", parentId.getVersion().getValue());
                    nutsDescriptor = nutsDescriptor/*.setProperties(properties, true)*/.applyProperties(properties);
                }
                NutsId thisId = nutsDescriptor.getId();
                if (!CoreNutsUtils.isEffectiveId(thisId)) {
                    if (parentId != null) {
                        if (CoreStringUtils.isBlank(thisId.getGroup())) {
                            thisId = thisId.setGroup(parentId.getGroup());
                        }
                        if (CoreStringUtils.isBlank(thisId.getVersion().getValue())) {
                            thisId = thisId.setVersion(parentId.getVersion().getValue());
                        }
                    }
                    HashMap<NutsId, NutsDescriptor> cache = new HashMap<>();
                    Set<String> done = new HashSet<>();
                    Stack<NutsId> todo = new Stack<>();
                    todo.push(nutsDescriptor.getId());
                    cache.put(nutsDescriptor.getId(), nutsDescriptor);
                    while (todo.isEmpty()) {
                        NutsId pid = todo.pop();
                        NutsDescriptor d = cache.get(pid);
                        if (d == null) {
                            try {
                                d = ws.fetch().id(pid).setEffective(true).setSession(session.getSession()).getResultDescriptor();
                            } catch (NutsException ex) {
                                throw ex;
                            } catch (Exception ex) {
                                throw new NutsNotFoundException(nutsDescriptor.getId(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + pid, ex);
                            }
                        }
                        done.add(pid.getSimpleName());
                        if (CoreNutsUtils.containsVars(thisId)) {
                            thisId.apply(new MapStringMapper(d.getProperties()));
                        } else {
                            break;
                        }
                        for (NutsId nutsId : d.getParents()) {
                            if (!done.contains(nutsId.getSimpleName())) {
                                todo.push(nutsId);
                            }
                        }
                    }
                    if (CoreNutsUtils.containsVars(thisId)) {
                        throw new NutsNotFoundException(nutsDescriptor.getId(), "Unable to resolve " + nutsDescriptor.getId() + " parent " + parentId, null);
                    }
                    nutsDescriptor = nutsDescriptor.setId(thisId);
                }
                String nutsPackaging = nutsDescriptor.getProperties().get("nuts-packaging");
                if (!CoreStringUtils.isBlank(nutsPackaging)) {
                    nutsDescriptor = nutsDescriptor.setPackaging(nutsPackaging);
                }
                properties.put("pom.groupId", thisId.getGroup());
                properties.put("pom.version", thisId.getVersion().getValue());
                properties.put("pom.artifactId", thisId.getName());
                properties.put("project.groupId", thisId.getGroup());
                properties.put("project.artifactId", thisId.getName());
                properties.put("project.version", thisId.getVersion().getValue());
                properties.put("version", thisId.getVersion().getValue());
                nutsDescriptor = nutsDescriptor/*.setProperties(properties, true)*/.applyProperties(properties);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (Exception ex) {
            throw new NutsParseException("Error Parsing " + urlDesc, ex);
        }
        return nutsDescriptor;
    }

    public static Iterator<NutsId> createArchetypeCatalogIterator(InputStream stream, NutsIdFilter filter, boolean autoClose) {
        Iterator<PomId> it = ArchetypeCatalogParser.createArchetypeCatalogIterator(stream, filter == null ? null : new PomIdFilter() {
            @Override
            public boolean accept(PomId id) {
                return filter.accept(MavenUtils.toNutsId(id));
            }
        }, autoClose);
        return new Iterator<NutsId>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public NutsId next() {
                return MavenUtils.toNutsId(it.next());
            }
        };
    }

    public static MavenMetadata parseMavenMetaData(InputStream metadataStream) {
        MavenMetadata s = MavenMetadataParser.parseMavenMetaData(metadataStream);
        if (s == null) {
            return s;
        }
        for (Iterator<String> iterator = s.getVersions().iterator(); iterator.hasNext();) {
            String version = iterator.next();
            if (s.getLatest().length() > 0 && DefaultNutsVersion.compareVersions(version, s.getLatest()) > 0) {
                iterator.remove();
            }
        }
        return s;
    }
}
