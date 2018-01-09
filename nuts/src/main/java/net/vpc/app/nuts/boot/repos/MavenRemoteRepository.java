/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.boot.repos;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.util.*;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenRemoteRepository extends AbstractMavenRepository {

    private static final Logger log = Logger.getLogger(MavenRemoteRepository.class.getName());

    public MavenRemoteRepository(String repositoryId, String url, NutsWorkspace workspace, File root) {
        super(new NutsRepositoryConfig(repositoryId, url, "maven"), workspace, root, SPEED_SLOW);
    }


    @Override
    public Iterator<NutsId> findVersionsImpl(NutsId id, NutsVersionFilter versionFilter, NutsSession session) throws IOException {
        //maven-metadata.xml

        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = IOUtils.buildUrl(getConfig().getLocation(), groupId.replaceAll("\\.", "/") + "/" + artifactId + "/maven-metadata.xml");
            log.log(Level.FINEST, "{0} downloading maven {1} url {2}", new Object[]{StringUtils.alignLeft(getRepositoryId(), 20), StringUtils.alignLeft("\'maven-metadata\'", 20), metadataURL});
            try {
                metadataStream = openStream(metadataURL);
            } catch (Exception ex) {
                throw new NutsNotFoundException(id);
            }
            MavenUtils.MavenMetadataInfo info = MavenUtils.parseMavenMetaData(metadataStream);
            if (info != null) {
                for (String version : info.getVersions()) {
                    if (versionFilter == null || versionFilter.accept(new NutsVersion(version))) {
                        ret.add(
                                new NutsId(
                                        null,
                                        groupId,
                                        artifactId,
                                        version,
                                        ""
                                )
                        );
                    }
                }
            }
        } finally {
            if (metadataStream != null) {
                metadataStream.close();
            }
        }
        return ret.iterator();
    }

    @Override
    public Iterator<NutsId> findVersionsImpl(NutsId id, NutsDescriptorFilter versionFilter, NutsSession session) throws IOException {
        //maven-metadata.xml

        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = IOUtils.buildUrl(getConfig().getLocation(), groupId.replaceAll("\\.", "/") + "/" + artifactId + "/maven-metadata.xml");
            log.log(Level.FINEST, "{0} downloading maven {1} url {2}", new Object[]{StringUtils.alignLeft(getRepositoryId(), 20), StringUtils.alignLeft("\'maven-metadata\'", 20), metadataURL});
            try {
                metadataStream = openStream(metadataURL);
            } catch (Exception ex) {
                throw new NutsNotFoundException(id);
            }
            MavenUtils.MavenMetadataInfo info = MavenUtils.parseMavenMetaData(metadataStream);
            if (info != null) {
                for (String version : info.getVersions()) {
                    NutsId nutsId = id.setVersion(version);
                    DelegateNutsDescriptor descriptor = new DelegateNutsDescriptor() {
                        @Override
                        protected NutsDescriptor getBase() {
                            try {
                                return fetchDescriptor(nutsId, session);
                            } catch (IOException e) {
                                throw new RuntimeException("Unable to fetch descriptor " + id);
                            }
                        }

                        @Override
                        public NutsId getId() {
                            return nutsId;
                        }

                        @Override
                        public String[] getArch() {
                            return new String[0];
                        }

                        @Override
                        public String[] getOs() {
                            return new String[0];
                        }

                        @Override
                        public String[] getOsdist() {
                            return new String[0];
                        }

                        @Override
                        public String[] getPlatform() {
                            return new String[0];
                        }

                        @Override
                        public String getFace() {
                            return NutsConstants.QUERY_FACE_DEFAULT_VALUE;
                        }

                    };
                    if (versionFilter == null || versionFilter.accept(descriptor)) {
                        ret.add(
                                new NutsId(
                                        null,
                                        groupId,
                                        artifactId,
                                        version,
                                        ""
                                )
                        );
                    }
                }
            }
        } finally {
            if (metadataStream != null) {
                metadataStream.close();
            }
        }
        return ret.iterator();
    }

    public Iterator<NutsId> findImpl(final NutsDescriptorFilter filter, NutsSession session) throws IOException {
        String url = IOUtils.buildUrl(getConfig().getLocation(), "/archetype-catalog.xml");
        log.log(Level.FINEST, "{0} downloading maven {1} url {2}", new Object[]{StringUtils.alignLeft(getRepositoryId(), 20), StringUtils.alignLeft("\'archetype-catalog\'", 20), url});
        return parseArchetypeCatalog(openStream(url), filter);
    }

    public NutsFile fetchImpl(NutsId id, NutsSession session) throws IOException {
        NutsDescriptor descriptor = getWorkspace().fetchDescriptor(id.toString(), false, session);
        NutsDescriptor ed = getWorkspace().fetchEffectiveDescriptor(descriptor, session);
        File tempFile = IOUtils.createTempFile(descriptor);
        fetch(id, session, tempFile);
        return new NutsFile(ed.getId(), descriptor, tempFile, false, true);
    }

    private Iterator<NutsId> parseArchetypeCatalog(final InputStream stream, NutsDescriptorFilter filter0) {
        return new Iterator<NutsId>() {
            NutsId last;
            NutsDescriptorFilter filter = filter0;
            InputStream stream2 = stream;
            XMLInputFactory factory;
            XMLEventReader eventReader;
            Stack<String> nodePath = new Stack<>();
            StringBuilder groupId = new StringBuilder();
            StringBuilder artifactId = new StringBuilder();
            StringBuilder version = new StringBuilder();

            {
                factory = XMLInputFactory.newInstance();
                factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
                try {
                    eventReader = factory.createXMLEventReader(new InputStreamReader(stream2));
                } catch (XMLStreamException e) {
                    //
                }
            }

            @Override
            public boolean hasNext() {
                if (eventReader == null) {
                    return false;
                }
                try {
                    while (eventReader.hasNext()) {
                        XMLEvent event = eventReader.nextEvent();
                        switch (event.getEventType()) {
                            case XMLStreamConstants.START_ELEMENT: {
                                StartElement startElement = event.asStartElement();
                                String qName = startElement.getName().getLocalPart();
                                nodePath.push(qName);
                                if (MavenUtils.isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype")) {
                                    StringUtils.clear(groupId);
                                    StringUtils.clear(artifactId);
                                    StringUtils.clear(version);
                                }
                                break;
                            }
                            case XMLStreamConstants.CHARACTERS: {
                                if (MavenUtils.isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype", "groupId")) {
                                    groupId.append(event.asCharacters().getData());
                                } else if (MavenUtils.isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype", "artifactId")) {
                                    artifactId.append(event.asCharacters().getData());
                                } else if (MavenUtils.isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype", "version")) {
                                    version.append(event.asCharacters().getData());
                                }
                                break;
                            }
                            case XMLStreamConstants.END_ELEMENT: {
                                EndElement endElement = event.asEndElement();
                                String localPart = endElement.getName().getLocalPart();
                                if (MavenUtils.isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype")) {
                                    last = new NutsId(
                                            null, groupId.toString(), artifactId.toString(), version.toString(), ""
                                    );
                                    DelegateNutsDescriptor d = new DelegateNutsDescriptor() {
                                        NutsId id = last;
                                        NutsDescriptor base = null;

                                        @Override
                                        protected NutsDescriptor getBase() {
                                            if (base == null) {
                                                try {
                                                    base = fetchDescriptor(last, null);
                                                } catch (IOException e) {
                                                    throw new RuntimeException("Unable to fetch descriptor " + id);
                                                }
                                            }
                                            return base;
                                        }

                                        @Override
                                        public NutsId getId() {
                                            return id;
                                        }
                                    };
                                    if (filter == null || filter.accept(d)) {
                                        nodePath.pop();
                                        return true;
                                    }
                                }
                                nodePath.pop();
                                break;
                            }
                        }
                    }
                } catch (XMLStreamException ex) {
                    return false;
                }
                return false;
            }

            @Override
            public NutsId next() {
                return last;
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    stream2 = null;
                }
            }
        };
    }

    @Override
    protected String getPath(NutsId id, String extension) {
        String groupId = id.getGroup();
        String artifactId = id.getName();
        String version = id.getVersion().getValue();
        return (IOUtils.buildUrl(getConfig().getLocation(), groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + extension));
    }

    @Override
    protected InputStream openStream(String path) throws IOException {
        InputStream stream = NutsUtils.getHttpClientFacade(getWorkspace(), path).open();
        if (stream != null) {
            if (!path.toLowerCase().startsWith("file://")) {
                log.log(Level.INFO, "downloading url {0}", new Object[]{path});
            } else {
                log.log(Level.FINEST, "downloading url {0}", new Object[]{path});
            }
        } else {
            log.log(Level.FINEST, "downloading url failed : {0}", new Object[]{path});
        }
        return stream;
    }

    @Override
    public boolean isAllowedFetch(NutsSession session) {
        return super.isAllowedFetch(session) && session.getFetchMode() != FetchMode.OFFLINE;
    }
}
