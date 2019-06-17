package net.vpc.app.nuts.core.bridges.maven.mvnutil;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Stack;

public class ArchetypeCatalogParser {

    public static Iterator<PomId> createArchetypeCatalogIterator(final InputStream stream, final PomIdFilter filter, final boolean autoCloseStream) {
        return new Iterator<PomId>() {
            PomId last;
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
                if (eventReader == null || stream2 == null) {
                    autoCloseStream();
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
                                if (isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype")) {
                                    clear(groupId);
                                    clear(artifactId);
                                    clear(version);
                                }
                                break;
                            }
                            case XMLStreamConstants.CHARACTERS: {
                                if (isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype", "groupId")) {
                                    groupId.append(event.asCharacters().getData());
                                } else if (isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype", "artifactId")) {
                                    artifactId.append(event.asCharacters().getData());
                                } else if (isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype", "version")) {
                                    version.append(event.asCharacters().getData());
                                }
                                break;
                            }
                            case XMLStreamConstants.END_ELEMENT: {
                                if (isStackPath(nodePath, "archetype-catalog", "archetypes", "archetype")) {
                                    last = new PomId(groupId.toString(), artifactId.toString(), version.toString()
                                    );
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
                    autoCloseStream();
                    return false;
                }
                autoCloseStream();
                return false;
            }

            @Override
            public PomId next() {
                return last;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("remove not supported");
            }

            @Override
            protected void finalize() throws Throwable {
                super.finalize();
                autoCloseStream();
            }

            private void autoCloseStream() {
                if (autoCloseStream) {
                    if (stream2 != null) {
                        try {
                            stream2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        stream2 = null;
                    }
                }
            }
        };
    }

    private static StringBuilder clear(StringBuilder c) {
        return c.delete(0, c.length());
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
