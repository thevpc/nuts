package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.PomDomVisitorContext;
import org.w3c.dom.Document;

public class DefaultPomDomVisitorContext implements PomDomVisitorContext {
    private Document document;

    public Document getDocument() {
        return document;
    }

    public DefaultPomDomVisitorContext setDocument(Document document) {
        this.document = document;
        return this;
    }
}
