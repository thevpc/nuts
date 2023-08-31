/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format.xml;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.NFetchDisplayOptions;
import net.thevpc.nuts.runtime.standalone.format.DefaultSearchFormatBase;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTexts;
import org.w3c.dom.Document;

/**
 *
 * @author thevpc
 */
public class DefaultSearchFormatXml extends DefaultSearchFormatBase {

    private boolean compact;
    private String rootName = "root";
    private NCodeHighlighter codeFormat;
    NTexts txt;

    public DefaultSearchFormatXml(NSession session, NPrintStream writer, NFetchDisplayOptions options) {
        super(session, writer, NContentType.XML, options);
        txt = NTexts.of(session);
        codeFormat = NTexts.of(session).getCodeHighlighter("xml");
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        NTextBuilder builder = NTexts.of(getSession()).ofBuilder();
        NSession session = getSession();

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
        org.w3c.dom.Element xmlElement = NElements.of(getSession())
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
        NTextBuilder builder = NTexts.of(getSession()).ofBuilder();

        NSession session = getSession();
        builder.append(codeFormat.tokenToText("</", "separator", txt, session));
        builder.append(codeFormat.tokenToText(rootName, "name", txt, session));
        builder.append(codeFormat.tokenToText(">", "separator", txt, session));

        getWriter().println(builder.toString());
        getWriter().flush();
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NSession session = getSession();
        NArg a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        switch(a.key()) {
            case "--compact": {
                cmdLine.withNextFlag((v, r, s)->compact=v);
                return true;
            }
            case "--root-name": {
                cmdLine.withNextEntry((v, r, s)->rootName=v);
                return true;
            }
        }
        return false;
    }
}
