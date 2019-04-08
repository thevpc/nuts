package net.vpc.app.nuts.core.util.bundledlibs.mvn;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class MavenMetadataParser {
    public static MavenMetadata parseMavenMetaData(InputStream stream) {
        MavenMetadata info = new MavenMetadata();
        StringBuilder ver = new StringBuilder();
        StringBuilder latest = new StringBuilder();
        StringBuilder release = new StringBuilder();
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
                        ver.delete(0,ver.length());
                        break;
                    }
                    case XMLStreamConstants.CHARACTERS: {
                        if (isStackPath(nodePath, "metadata", "versioning", "versions", "version")) {
                            ver.append(event.asCharacters().getData());
                        }
                        if (isStackPath(nodePath, "metadata", "versioning", "release")) {
                            release.append(event.asCharacters().getData());
                        }
                        if (isStackPath(nodePath, "metadata", "versioning", "latest")) {
                            latest.append(event.asCharacters().getData());
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
        info.setLatest(latest.toString());
        info.setRelease(release.toString());
        for (String version : versions) {
//            if (latest.length() > 0 && CoreVersionUtils.compareVersions(version, latest.toString()) > 0) {
//                //do not add
//            } else {
                info.getVersions().add(version);
//            }
        }
        return info;
    }

    private static boolean isStackPath(Stack<String> stack, String... path) {
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
