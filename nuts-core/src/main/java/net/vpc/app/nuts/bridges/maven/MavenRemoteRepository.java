/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.bridges.maven;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.core.NutsIdImpl;
import net.vpc.app.nuts.extensions.core.NutsRepositoryConfigImpl;
import net.vpc.app.nuts.extensions.util.CoreHttpUtils;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.DefaultInputStreamMonitor;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenRemoteRepository extends AbstractMavenRepository {

    private static final Logger log = Logger.getLogger(MavenRemoteRepository.class.getName());

    public MavenRemoteRepository(String repositoryId, String url, NutsWorkspace workspace, NutsRepository parentRepository, File root) {
        super(new NutsRepositoryConfigImpl(repositoryId, url, "maven"), workspace, parentRepository, root, SPEED_SLOW);
    }

    @Override
    public Iterator<NutsId> findVersionsImpl(final NutsId id, NutsIdFilter idFilter, final NutsSession session) {
        //maven-metadata.xml

        String groupId = id.getGroup();
        String artifactId = id.getName();
        InputStream metadataStream = null;
        List<NutsId> ret = new ArrayList<>();
        try {
            String metadataURL = CoreIOUtils.buildUrl(getConfigManager().getLocation(), groupId.replaceAll("\\.", "/") + "/" + artifactId + "/maven-metadata.xml");
            log.log(Level.FINEST, "{0} downloading maven {1} url {2}", new Object[]{CoreStringUtils.alignLeft(getRepositoryId(), 20), CoreStringUtils.alignLeft("\'maven-metadata\'", 20), metadataURL});
            try {
                metadataStream = openStream(metadataURL, id.setFace(CoreNutsUtils.FACE_CATALOG), session);
            } catch (Exception ex) {
                throw new NutsNotFoundException(id);
            }
            MavenUtils.MavenMetadataInfo info = MavenUtils.parseMavenMetaData(metadataStream);
            if (info != null) {
                for (String version : info.getVersions()) {
                    final NutsId nutsId = id.setVersion(version);

                    if (idFilter != null && !idFilter.accept(nutsId)) {
                        continue;
                    }
                    ret.add(
                            new NutsIdImpl(
                                    null,
                                    groupId,
                                    artifactId,
                                    version,
                                    ""
                            )
                    );
                }
            }
        } finally {
            if (metadataStream != null) {
                try {
                    metadataStream.close();
                } catch (IOException e) {
                    throw new NutsIOException(e);
                }
            }
        }
        return ret.iterator();
    }

    @Override
    public Iterator<NutsId> findImpl(final NutsIdFilter filter, NutsSession session) {
        String url = CoreIOUtils.buildUrl(getConfigManager().getLocation(), "/archetype-catalog.xml");
        log.log(Level.FINEST, "{0} downloading maven {1} url {2}", new Object[]{CoreStringUtils.alignLeft(getRepositoryId(), 20), CoreStringUtils.alignLeft("\'archetype-catalog\'", 20), url});
        return parseArchetypeCatalog(openStream(url, CoreNutsUtils.parseNutsId("internal:repository").setQueryProperty("location", getConfigManager().getLocation()).setFace(CoreNutsUtils.FACE_CATALOG), session), filter);
    }

    @Override
    public NutsFile fetchImpl(NutsId id, NutsSession session) {
        NutsDescriptor descriptor = getWorkspace().fetchDescriptor(id.toString(), false, session);
        NutsDescriptor ed = getWorkspace().resolveEffectiveDescriptor(descriptor, session);
        File tempFile = CoreIOUtils.createTempFile(descriptor, false);
        copyTo(id, session, tempFile);
        return new NutsFile(ed.getId(), descriptor, tempFile, false, true, null);
    }

    private Iterator<NutsId> parseArchetypeCatalog(final InputStream stream, final NutsIdFilter filter0) {
        return new Iterator<NutsId>() {
            NutsId last;
            NutsIdFilter filter = filter0;
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
                                    CoreStringUtils.clear(groupId);
                                    CoreStringUtils.clear(artifactId);
                                    CoreStringUtils.clear(version);
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
//                                EndElement endElement = event.asEndElement();
//                                String localPart = endElement.getName().getLocalPart();
                                if (MavenUtils.isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype")) {
                                    last = new NutsIdImpl(
                                            null, groupId.toString(), artifactId.toString(), version.toString(), ""
                                    );
//                                    DelegateNutsDescriptor d = new DelegateNutsDescriptor() {
//                                        NutsId id = last;
//                                        NutsDescriptor base = null;
//
//                                        @Override
//                                        protected NutsDescriptor getBase() {
//                                            if (base == null) {
//                                                base = fetchDescriptor(last, null);
//                                            }
//                                            return base;
//                                        }
//
//                                        @Override
//                                        public NutsId getId() {
//                                            return id;
//                                        }
//                                    };
                                    if (filter == null || filter.accept(last)) {
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
            public void remove() {
                throw new NutsUnsupportedOperationException("remove not supported");
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
        return (CoreIOUtils.buildUrl(getConfigManager().getLocation(), groupId.replaceAll("\\.", "/") + "/" + artifactId + "/" + version + "/" + artifactId + "-" + version + extension));
    }

    @Override
    protected InputStream openStream(String path, Object source, NutsSession session) {
        InputStream stream = null;
        long size = -1;
        try {
            NutsHttpConnectionFacade f = CoreHttpUtils.getHttpClientFacade(getWorkspace(), path);
            try {
                size = f.length();
            } catch (Exception ex) {
                //ignore error
            }
            stream = f.open();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        if (stream != null) {
            if (!path.toLowerCase().startsWith("file://")) {
                log.log(Level.INFO, "downloading url {0}", new Object[]{path});
            } else {
                log.log(Level.FINEST, "downloading url {0}", new Object[]{path});
            }
        } else {
            log.log(Level.FINEST, "downloading url failed : {0}", new Object[]{path});
        }
        boolean monitorable = true;
        if (source instanceof NutsId) {
            NutsId d = (NutsId) source;
            if (CoreNutsUtils.FACE_CATALOG.equals(d.getFace())) {
                monitorable = false;
            }
            if (CoreNutsUtils.FACE_PACKAGE_HASH.equals(d.getFace())) {
                monitorable = false;
            }
            if (CoreNutsUtils.FACE_DESC_HASH.equals(d.getFace())) {
                monitorable = false;
            }
            if (CoreNutsUtils.FACE_DESC.equals(d.getFace())) {
                monitorable = false;
            }
//            if ("archetype-catalog".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("descriptor".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("main-sha1".equals(d.getFace())) {
//                monitorable = false;
//            }
//            if ("descriptor-sha1".equals(d.getFace())) {
//                monitorable = false;
//            }
        }
        NutsPrintStream out = session.getTerminal().getOut();
        if (!monitorable) {
            return stream;
        }
        return CoreIOUtils.monitor(stream, source, String.valueOf(path), size, new DefaultInputStreamMonitor(out));
    }

    @Override
    public void checkAllowedFetch(NutsSession session, NutsId id) {
        super.checkAllowedFetch(session, id);
        if (session.getFetchMode() == NutsFetchMode.OFFLINE) {
            throw new NutsNotFoundException(id.toString());
        }
    }
}
