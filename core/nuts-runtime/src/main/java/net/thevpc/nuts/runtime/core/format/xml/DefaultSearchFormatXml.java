/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCodeFormat;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextNodeBuilder;
import net.thevpc.nuts.runtime.core.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.core.format.DefaultSearchFormatBase;
import org.w3c.dom.Document;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase {

    private boolean compact;
    private String rootName = "root";
    private NutsCodeFormat codeFormat;

    public DefaultSearchFormatXml(NutsSession session, PrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.XML, options);
        codeFormat = session.getWorkspace().formats().text().setSession(session).getCodeFormat("xml");
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        NutsTextNodeBuilder builder = getWorkspace().formats().text().builder();
        NutsSession session = getSession();

        builder.append(codeFormat.tokenToNode("<?", "separator", session));
        builder.append(codeFormat.tokenToNode("xml", "name", session));

        builder.append(" ");
        builder.append(codeFormat.tokenToNode("version", "attribute", session));
        builder.append(codeFormat.tokenToNode("=", "separator", session));
        builder.append(codeFormat.tokenToNode("\"1.0\"", "string", session));

        builder.append(" ");
        builder.append(codeFormat.tokenToNode("encoding", "attribute", session));
        builder.append(codeFormat.tokenToNode("=", "separator", session));
        builder.append(codeFormat.tokenToNode("?>", "separator", session));
        builder.append("\n");

        builder.append(codeFormat.tokenToNode("<", "separator", session));
        builder.append(codeFormat.tokenToNode(rootName, "name", session));
        builder.append(codeFormat.tokenToNode(">", "separator", session));

        getWriter().println(builder.toString());
    }

    @Override
    public void next(Object object, long index) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        NutsXmlUtils.print(String.valueOf(index), object, getWriter(), compact, false, getWorkspace());
        PrintWriter pw = new PrintWriter(bos);
        org.w3c.dom.Element xmlElement = getWorkspace().formats().element()
                .setSession(getSession())
                .convert(object, org.w3c.dom.Element.class);
        Document doc = NutsXmlUtils.createDocument(getSession());
        doc.adoptNode(xmlElement);
        doc.appendChild(xmlElement);
        NutsXmlUtils.writeDocument(doc, new javax.xml.transform.stream.StreamResult(pw), compact, false, getSession());
        pw.flush();
        getWriter().print(codeFormat.textToNode(bos.toString(), getSession()));
    }

    @Override
    public void complete(long count) {
        NutsTextNodeBuilder builder = getWorkspace().formats().text().builder();

        NutsSession session = getSession();
        builder.append(codeFormat.tokenToNode("</", "separator", session));
        builder.append(codeFormat.tokenToNode(rootName, "name", session));
        builder.append(codeFormat.tokenToNode(">", "separator", session));

        getWriter().println(builder.toString());
        getWriter().flush();
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmd)) {
            return true;
        }
        boolean enabled = a.isEnabled();
        switch (a.getStringKey()) {
            case "--compact": {
                boolean val = cmd.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.compact = val;
                }
                return true;
            }
            case "--root-name": {
                String val = cmd.nextString().getStringValue();
                if (enabled) {
                    this.rootName = val;
                }
                return true;
            }
        }
        return false;
    }
}
