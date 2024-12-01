package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Level;

public class MavenMetadataParser {
    private final NLog LOG;

    public MavenMetadataParser() {
        LOG= NLog.of(MavenMetadataParser.class);
    }

    public String toXmlString(MavenMetadata m) {
        ByteArrayOutputStream s = new ByteArrayOutputStream();
        try (Writer w = new OutputStreamWriter(s)) {
            writeMavenMetaData(m, new StreamResult(s));
            w.flush();
        } catch (TransformerException | ParserConfigurationException | IOException e) {
            throw new NIOException(e);
        }
        return new String(s.toByteArray());
    }

    public void writeMavenMetaData(MavenMetadata m, Path path) {
        try (Writer w = Files.newBufferedWriter(path)) {
            writeMavenMetaData(m, new StreamResult(path.toFile()));
        } catch (TransformerException | ParserConfigurationException | IOException e) {
            throw new NIOException(e);
        }
    }

    public void writeMavenMetaData(MavenMetadata m, StreamResult writer) throws TransformerException, ParserConfigurationException {
        Document document = XmlUtils.createDocument();

        Element metadata = document.createElement("metadata");
        document.appendChild(metadata);

        if (!NBlankable.isBlank(m.getGroupId())) {
            Element groupId = document.createElement("groupId");
            groupId.appendChild(document.createTextNode(m.getGroupId()));
            metadata.appendChild(groupId);
        }
        if (!NBlankable.isBlank(m.getArtifactId())) {
            Element artifactId = document.createElement("artifactId");
            artifactId.appendChild(document.createTextNode(m.getArtifactId()));
            metadata.appendChild(artifactId);
        }

        Element versioning = document.createElement("versioning");
        metadata.appendChild(versioning);

        if (!NBlankable.isBlank(m.getRelease())) {
            Element release = document.createElement("release");
            release.appendChild(document.createTextNode(m.getRelease()));
            versioning.appendChild(release);
        }

        if (!NBlankable.isBlank(m.getLatest())) {
            Element latest = document.createElement("latest");
            latest.appendChild(document.createTextNode(m.getLatest()));
            versioning.appendChild(latest);
        }

        Element versions = document.createElement("versions");
        versioning.appendChild(versions);
        if (m.getVersions() != null) {
            for (String sversion : m.getVersions()) {
                if (!NBlankable.isBlank(sversion)) {
                    Element version = document.createElement("version");
                    version.appendChild(document.createTextNode(sversion));
                    versions.appendChild(version);
                }
            }
        }
        if (m.getLastUpdated() != null) {
            Element lastUpdated = document.createElement("lastUpdated");
            lastUpdated.appendChild(document.createTextNode(new SimpleDateFormat("yyyyMMddHHmmss").format(m.getLastUpdated())));
            versioning.appendChild(lastUpdated);
        }
        XmlUtils.writeDocument(document, writer, false,true);
    }

    public MavenMetadata parseMavenMetaData(Path stream) {
        try (InputStream s = Files.newInputStream(stream)) {
            return parseMavenMetaData(s);
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public MavenMetadata parseMavenMetaData(InputStream stream) {
        MavenMetadata info = new MavenMetadata();
        StringBuilder ver = new StringBuilder();
        StringBuilder latest = new StringBuilder();
        StringBuilder release = new StringBuilder();
        StringBuilder lastUpdated = new StringBuilder();
        StringBuilder groupId = new StringBuilder();
        StringBuilder artifactId = new StringBuilder();
        List<String> versions = new ArrayList<>();
        try {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            factory.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false);
            XMLEventReader eventReader
                    = factory.createXMLEventReader(new InputStreamReader(stream));
            Stack<String> nodePath = new Stack<>();
            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();
                switch (event.getEventType()) {
                    case XMLStreamConstants.START_ELEMENT: {
                        StartElement startElement = event.asStartElement();
                        String qName = startElement.getName().getLocalPart();
                        nodePath.push(qName);
                        ver.delete(0, ver.length());
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        if (isStackPath(nodePath, "metadata", "groupId")) {
                            groupId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "artifactId")) {
                            artifactId.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "versions", "version")) {
                            ver.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "release")) {
                            release.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "latest")) {
                            latest.append(event.asCharacters().getData());
                        } else if (isStackPath(nodePath, "metadata", "versioning", "lastUpdated")) {
                            lastUpdated.append(event.asCharacters().getData());
                        }
                        break;
                    }
                    case XMLStreamConstants.END_ELEMENT: {
                        if (isStackPath(nodePath, "metadata", "versioning", "versions", "version")) {
                            nodePath.pop();
                            if (ver.length() > 0) {
                                versions.add(ver.toString());
                            }
                        } else {
                            nodePath.pop();
                        }
                        break;
                    }
                }
            }
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
        info.setGroupId(groupId.toString().trim());
        info.setArtifactId(artifactId.toString().trim());
        info.setLatest(latest.toString().trim());
        info.setRelease(release.toString().trim());
        try {
            //<lastUpdated>2018 12 22 22 59 57</lastUpdated>

            info.setLastUpdated(lastUpdated.toString().trim().isEmpty() ? null : new SimpleDateFormat("yyyyMMddHHmmss").parse(lastUpdated.toString().trim()));
        } catch (Exception ex) {
            LOG.with().level(Level.SEVERE).error(ex)
                    .log(NMsg.ofJ("failed to parse date {0} : {1}", lastUpdated, ex));
        }
        for (String version : versions) {
            info.getVersions().add(version.trim());
        }
        return info;
    }

    private boolean isStackPath(Stack<String> stack, String... path) {
        if (stack.size() != path.length) {
            return false;
        }
        for (int i = 0; i < path.length; i++) {
            if (!stack.get(stack.size() - path.length + i).equals(path[i])) {
                return false;
            }
        }
        return true;
    }
}
