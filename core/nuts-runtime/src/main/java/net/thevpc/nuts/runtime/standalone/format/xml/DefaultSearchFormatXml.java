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

    public DefaultSearchFormatXml(NWorkspace workspace, NPrintStream writer, NFetchDisplayOptions options) {
        super(workspace, writer, NContentType.XML, options);
        txt = NTexts.of();
        codeFormat = NTexts.of().getCodeHighlighter("xml");
    }

    public String getRootName() {
        return rootName;
    }

    @Override
    public void start() {
        NTextBuilder builder = NTexts.of().ofBuilder();

        builder.append(codeFormat.tokenToText("<?", "separator", txt));
        builder.append(codeFormat.tokenToText("xml", "name", txt));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("version", "attribute", txt));
        builder.append(codeFormat.tokenToText("=", "separator", txt));
        builder.append(codeFormat.tokenToText("\"1.0\"", "string", txt));

        builder.append(" ");
        builder.append(codeFormat.tokenToText("encoding", "attribute", txt));
        builder.append(codeFormat.tokenToText("=", "separator", txt));
        builder.append(codeFormat.tokenToText("?>", "separator", txt));
        builder.append("\n");

        builder.append(codeFormat.tokenToText("<", "separator", txt));
        builder.append(codeFormat.tokenToText(rootName, "name", txt));
        builder.append(codeFormat.tokenToText(">", "separator", txt));

        getWriter().println(builder.toString());
    }

    @Override
    public void next(Object object, long index) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
//        XmlUtils.print(String.valueOf(index), object, getWriter(), compact, false, getWorkspace());
        PrintWriter pw = new PrintWriter(bos);
        org.w3c.dom.Element xmlElement = NElements.of()
                .convert(object, org.w3c.dom.Element.class);
        Document doc = XmlUtils.createDocument();
        doc.adoptNode(xmlElement);
        doc.appendChild(xmlElement);
        XmlUtils.writeDocument(doc, new javax.xml.transform.stream.StreamResult(pw), compact, false);
        pw.flush();
        getWriter().print(codeFormat.stringToText(bos.toString(), txt));
    }

    @Override
    public void complete(long count) {
        NTextBuilder builder = NTexts.of().ofBuilder();

        builder.append(codeFormat.tokenToText("</", "separator", txt));
        builder.append(codeFormat.tokenToText(rootName, "name", txt));
        builder.append(codeFormat.tokenToText(">", "separator", txt));

        getWriter().println(builder.toString());
        getWriter().flush();
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a = cmdLine.peek().get();
        if (a == null) {
            return false;
        }
        if (getDisplayOptions().configureFirst(cmdLine)) {
            return true;
        }
        switch(a.key()) {
            case "--compact": {
                cmdLine.withNextFlag((v, r)->compact=v);
                return true;
            }
            case "--root-name": {
                cmdLine.withNextEntry((v, r)->rootName=v);
                return true;
            }
        }
        return false;
    }
}
