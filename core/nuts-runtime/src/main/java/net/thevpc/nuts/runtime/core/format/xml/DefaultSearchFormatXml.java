/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import net.thevpc.nuts.*;
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

    public DefaultSearchFormatXml(NutsSession session, NutsPrintStream writer, NutsFetchDisplayOptions options) {
        super(session, writer, NutsContentType.XML, options);
        codeFormat = session.text().setSession(session).getCodeFormat("xml");
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        NutsTextBuilder builder = getSession().text().builder();
        NutsSession session = getSession();

        builder.append(codeFormat.tokenToText("<?", "separator", session));
        builder.append(codeFormat.tokenToText("xml", "name", session));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("version", "attribute", session));
        builder.append(codeFormat.tokenToText("=", "separator", session));
        builder.append(codeFormat.tokenToText("\"1.0\"", "string", session));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("encoding", "attribute", session));
        builder.append(codeFormat.tokenToText("=", "separator", session));
        builder.append(codeFormat.tokenToText("?>", "separator", session));
        builder.append("\n");

        builder.append(codeFormat.tokenToText("<", "separator", session));
        builder.append(codeFormat.tokenToText(rootName, "name", session));
        builder.append(codeFormat.tokenToText(">", "separator", session));

        getWriter().println(builder.toString());
    }

    @Override
    public void next(Object object, long index) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        NutsXmlUtils.print(String.valueOf(index), object, getWriter(), compact, false, getWorkspace());
        PrintWriter pw = new PrintWriter(bos);
        org.w3c.dom.Element xmlElement = getSession().elem()
                .convert(object, org.w3c.dom.Element.class);
        Document doc = NutsXmlUtils.createDocument(getSession());
        doc.adoptNode(xmlElement);
        doc.appendChild(xmlElement);
        NutsXmlUtils.writeDocument(doc, new javax.xml.transform.stream.StreamResult(pw), compact, false, getSession());
        pw.flush();
        getWriter().print(codeFormat.stringToText(bos.toString(), getSession()));
    }

    @Override
    public void complete(long count) {
        NutsTextBuilder builder = getSession().text().builder();

        NutsSession session = getSession();
        builder.append(codeFormat.tokenToText("</", "separator", session));
        builder.append(codeFormat.tokenToText(rootName, "name", session));
        builder.append(codeFormat.tokenToText(">", "separator", session));

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
        switch (a.getKey().getString()) {
            case "--compact": {
                boolean val = cmd.nextBoolean().getValue().getBoolean();
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
