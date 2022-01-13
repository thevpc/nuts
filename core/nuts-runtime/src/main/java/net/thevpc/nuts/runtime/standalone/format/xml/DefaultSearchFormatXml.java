/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.format.NutsFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import org.w3c.dom.Document;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase {

    private boolean compact;
    private String rootName = "root";
    private NutsCodeHighlighter codeFormat;
    NutsTexts txt;

    public DefaultSearchFormatXml(NutsSession session, NutsPrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.XML, options);
        txt = NutsTexts.of(session);
        codeFormat = NutsTexts.of(session).setSession(session).getCodeHighlighter("xml");
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        NutsTextBuilder builder = NutsTexts.of(getSession()).builder();
        NutsSession session = getSession();

        builder.append(codeFormat.tokenToText("<?", "separator", txt, session));
        builder.append(codeFormat.tokenToText("xml", "name", txt, session));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("version", "attribute", txt, session));
        builder.append(codeFormat.tokenToText("=", "separator", txt, session));
        builder.append(codeFormat.tokenToText("\"1.0\"", "string", txt, session));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("encoding", "attribute", txt, session));
        builder.append(codeFormat.tokenToText("=", "separator", txt, session));
        builder.append(codeFormat.tokenToText("?>", "separator", txt, session));
        builder.append("\n");

        builder.append(codeFormat.tokenToText("<", "separator", txt, session));
        builder.append(codeFormat.tokenToText(rootName, "name", txt, session));
        builder.append(codeFormat.tokenToText(">", "separator", txt, session));

        getWriter().println(builder.toString());
    }

    @Override
    public void next(Object object, long index) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        XmlUtils.print(String.valueOf(index), object, getWriter(), compact, false, getWorkspace());
        PrintWriter pw = new PrintWriter(bos);
        org.w3c.dom.Element xmlElement = NutsElements.of(getSession())
                .convert(object, org.w3c.dom.Element.class);
        Document doc = XmlUtils.createDocument(getSession());
        doc.adoptNode(xmlElement);
        doc.appendChild(xmlElement);
        XmlUtils.writeDocument(doc, new javax.xml.transform.stream.StreamResult(pw), compact, false, getSession());
        pw.flush();
        getWriter().print(codeFormat.stringToText(bos.toString(), txt, getSession()));
    }

    @Override
    public void complete(long count) {
        NutsTextBuilder builder = NutsTexts.of(getSession()).builder();

        NutsSession session = getSession();
        builder.append(codeFormat.tokenToText("</", "separator", txt, session));
        builder.append(codeFormat.tokenToText(rootName, "name", txt, session));
        builder.append(codeFormat.tokenToText(">", "separator", txt, session));

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
        boolean enabled = a.isActive();
        switch (a.getKey().getString()) {
            case "--compact": {
                boolean val = cmd.nextBoolean().getBooleanValue();
                if (enabled) {
                    this.compact = val;
                }
                return true;
            }
            case "--root-name": {
                String val = cmd.nextString().getValue().getString();
                if (enabled) {
                    this.rootName = val;
                }
                return true;
            }
        }
        return false;
    }
}
